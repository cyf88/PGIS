package com.pgis.importdata;

import com.pgis.deviceinfo.DeviceInfo;
import com.pgis.deviceinfo.DeviceInfoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CsvImportServiceTest {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Test
    void importDeviceInfo_insertsThenUpdatesOnPrimaryKeyConflict() {
        // 列序与 sb001.csv 一致：6信封 + 25业务(至xgrsfzh) + xxzjbh + jysfz + pt
        String row = envelope()
                + "DEV-IMP,HUAWEI,M1,07,410000000000,,1001,测试员,,13800000001,01,,1,410000,"
                + "20260101010101,,,,,20260101010101,,,,,pk-imp-1,410101199001011234,20260901";
        MockMultipartFile file = csv("sb001_sample.csv", row);

        ImportResult first = csvImportService.importDeviceInfo(List.of(file));
        assertEquals(1, first.getInserted());
        assertEquals(0, first.getUpdated());
        assertEquals(0, first.getFailed());

        String updated = envelope()
                + "DEV-IMP,HUAWEI,M2,07,410000000000,,1001,测试员,,13800000001,01,,1,410000,"
                + "20260101010101,,,,,20260101010101,,,,,pk-imp-1,410101199001011234,20260901";
        ImportResult second = csvImportService.importDeviceInfo(List.of(csv("sb001_sample.csv", updated)));
        assertEquals(0, second.getInserted());
        assertEquals(1, second.getUpdated());
        assertTrue(second.getUpdatedKeys().contains("pk-imp-1"));

        DeviceInfo stored = deviceInfoMapper.selectById("pk-imp-1");
        assertEquals("M2", stored.getSbxh());
        assertEquals("410101199001011234", stored.getJysfz());
    }

    @Test
    void importDeviceInfo_skipsWrongPrefixAndBlankDeviceId() {
        String blankSbbh = envelope()
                + ",HUAWEI,M1,07,410000000000,,1001,测试员,,13800000001,01,,1,410000,"
                + "20260101010101,,,,,20260101010101,,,,,pk-imp-2,,20260901";
        MockMultipartFile wrong = csv("sb002_0.csv", envelope()
                + "DEV-X,HUAWEI,M1,07,410000000000,,1001,测试员,,13800000001,01,,1,410000,"
                + "20260101010101,,,,,20260101010101,,,,,pk-x,,20260901");
        MockMultipartFile bad = csv("sb001_bad.csv", blankSbbh);

        ImportResult result = csvImportService.importDeviceInfo(List.of(wrong, bad));
        assertEquals(1, result.getSkippedFiles().size());
        assertEquals("sb002_0.csv", result.getSkippedFiles().get(0).getFile());
        assertEquals(1, result.getFailed());
        assertEquals("设备编号为空", result.getFailures().get(0).getReason());
    }

    private static MockMultipartFile csv(String name, String content) {
        return new MockMultipartFile("files", name, "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    private static String envelope() {
        return "A-410000000000-0100,batch-id,20260901174009,00,jysb_sbxx#2,1,";
    }
}
