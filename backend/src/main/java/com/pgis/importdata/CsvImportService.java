package com.pgis.importdata;

import com.pgis.deviceinfo.DeviceInfo;
import com.pgis.deviceinfo.DeviceInfoMapper;
import com.pgis.deviceinfo.MapLatestLocationStore;
import com.pgis.deviceinfo.MapQueryCache;
import com.pgis.devicelocation.DeviceLocation;
import com.pgis.devicelocation.DeviceLocationMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Service
public class CsvImportService {

    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);
    private static final int BATCH_SIZE = 1000;
    private static final int LOG_EVERY_BATCHES = 50;
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

    private final DeviceInfoMapper deviceInfoMapper;
    private final DeviceLocationMapper deviceLocationMapper;
    private final MapQueryCache mapQueryCache;
    private final MapLatestLocationStore latestLocationStore;

    public CsvImportService(DeviceInfoMapper deviceInfoMapper, DeviceLocationMapper deviceLocationMapper,
                            MapQueryCache mapQueryCache, MapLatestLocationStore latestLocationStore) {
        this.deviceInfoMapper = deviceInfoMapper;
        this.deviceLocationMapper = deviceLocationMapper;
        this.mapQueryCache = mapQueryCache;
        this.latestLocationStore = latestLocationStore;
    }

    public ImportResult importDeviceInfo(List<MultipartFile> files) {
        return importFiles(files, "sb001", this::parseAndFlushInfo);
    }

    public ImportResult importDeviceLocation(List<MultipartFile> files) {
        return importFiles(files, "sb002", this::parseAndFlushLocation);
    }

    private ImportResult importFiles(List<MultipartFile> files, String prefix, FileImporter importer) {
        ImportResult result = new ImportResult();
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("请选择要导入的文件");
        }
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename() == null ? "unknown.csv" : file.getOriginalFilename();
            String simpleName = filename.replace('\\', '/');
            simpleName = simpleName.substring(simpleName.lastIndexOf('/') + 1);
            if (!CsvLineParser.matchesTableFile(simpleName, prefix)) {
                result.addSkipped(simpleName, "不是 csv 或不匹配当前表前缀 " + prefix);
                continue;
            }
            try (InputStream in = file.getInputStream()) {
                log.info("开始导入文件 {} size={}", simpleName, file.getSize());
                importer.importFile(simpleName, in, result);
                log.info("文件导入结束 {} inserted={} updated={} failed={}",
                        simpleName, result.getInserted(), result.getUpdated(), result.getFailed());
            } catch (Exception ex) {
                result.addFailure(simpleName, 0, "文件读取失败: " + ex.getMessage());
            }
        }
        mapQueryCache.evictAll();
        return result;
    }

    private void parseAndFlushInfo(String filename, InputStream in, ImportResult result) throws Exception {
        List<DeviceInfo> batch = new ArrayList<>(BATCH_SIZE);
        AtomicInteger flushed = new AtomicInteger();
        readRows(filename, in, result, cols -> {
            DeviceInfo entity = CsvLineParser.parseDeviceInfo(cols);
            if (CsvLineParser.isBlank(entity.getSbbh())) {
                throw new IllegalArgumentException("设备编号为空");
            }
            batch.add(entity);
            if (batch.size() >= BATCH_SIZE) {
                flushInfo(filename, batch, result);
                logFlushProgress(filename, result, flushed.incrementAndGet());
                batch.clear();
            }
            return null;
        });
        if (!batch.isEmpty()) {
            flushInfo(filename, batch, result);
        }
    }

    private void parseAndFlushLocation(String filename, InputStream in, ImportResult result) throws Exception {
        List<DeviceLocation> batch = new ArrayList<>(BATCH_SIZE);
        AtomicInteger flushed = new AtomicInteger();
        readRows(filename, in, result, cols -> {
            DeviceLocation entity = CsvLineParser.parseDeviceLocation(cols);
            if (CsvLineParser.isBlank(entity.getSbbh())) {
                throw new IllegalArgumentException("设备编号为空");
            }
            batch.add(entity);
            if (batch.size() >= BATCH_SIZE) {
                flushLocation(filename, batch, result);
                logFlushProgress(filename, result, flushed.incrementAndGet());
                batch.clear();
            }
            return null;
        });
        if (!batch.isEmpty()) {
            flushLocation(filename, batch, result);
        }
    }

    private void readRows(String filename, InputStream in, ImportResult result,
                          Function<String[], Void> onRow) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        reader.mark(1);
        int first = reader.read();
        if (first != 0xFEFF && first != -1) {
            reader.reset();
        }
        try (CSVParser parser = CSV_FORMAT.parse(reader)) {
            for (CSVRecord record : parser) {
                String[] cols = toArray(record);
                if (record.getRecordNumber() == 1 && CsvLineParser.isHeaderRow(cols)) {
                    continue;
                }
                try {
                    onRow.apply(cols);
                } catch (IllegalArgumentException ex) {
                    result.addFailure(filename, record.getRecordNumber(), ex.getMessage());
                } catch (Exception ex) {
                    result.addFailure(filename, record.getRecordNumber(), ex.getMessage());
                }
            }
        }
    }

    private void flushInfo(String filename, List<DeviceInfo> batch, ImportResult result) {
        try {
            List<String> ids = batch.stream().map(DeviceInfo::getXxzjbh).toList();
            Set<String> existing = new HashSet<>(deviceInfoMapper.selectExistingIds(ids));
            deviceInfoMapper.upsertBatch(batch);
            for (DeviceInfo item : batch) {
                if (existing.contains(item.getXxzjbh())) {
                    result.addUpdated(item.getXxzjbh());
                } else {
                    result.addInserted(1);
                }
            }
        } catch (Exception ex) {
            for (DeviceInfo ignored : batch) {
                result.addFailure(filename, 0, "批次写入失败: " + ex.getMessage());
            }
        }
    }

    private void flushLocation(String filename, List<DeviceLocation> batch, ImportResult result) {
        try {
            List<String> ids = batch.stream().map(DeviceLocation::getXxzjbh).toList();
            Set<String> existing = new HashSet<>(deviceLocationMapper.selectExistingIds(ids));
            deviceLocationMapper.upsertBatch(batch);
            latestLocationStore.refresh(batch.stream().map(DeviceLocation::getSbbh).toList());
            for (DeviceLocation item : batch) {
                if (existing.contains(item.getXxzjbh())) {
                    result.addUpdated(item.getXxzjbh());
                } else {
                    result.addInserted(1);
                }
            }
        } catch (Exception ex) {
            for (DeviceLocation ignored : batch) {
                result.addFailure(filename, 0, "批次写入失败: " + ex.getMessage());
            }
        }
    }

    private void logFlushProgress(String filename, ImportResult result, int flushedBatches) {
        if (flushedBatches % LOG_EVERY_BATCHES == 0) {
            log.info("导入进度 {} batches={} inserted={} updated={} failed={}",
                    filename, flushedBatches, result.getInserted(), result.getUpdated(), result.getFailed());
        }
    }

    private static String[] toArray(CSVRecord record) {
        String[] cols = new String[record.size()];
        for (int i = 0; i < record.size(); i++) {
            cols[i] = record.get(i);
        }
        return cols;
    }

    @FunctionalInterface
    private interface FileImporter {
        void importFile(String filename, InputStream in, ImportResult result) throws Exception;
    }
}
