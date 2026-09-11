package com.pgis.devicelocation;

import com.pgis.common.PageResult;
import com.pgis.deviceinfo.MapLatestLocationStore;
import com.pgis.deviceinfo.MapQueryCache;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceLocationService {

    private final DeviceLocationMapper mapper;
    private final MapQueryCache mapQueryCache;
    private final MapLatestLocationStore latestLocationStore;

    public DeviceLocationService(DeviceLocationMapper mapper, MapQueryCache mapQueryCache,
                                 MapLatestLocationStore latestLocationStore) {
        this.mapper = mapper;
        this.mapQueryCache = mapQueryCache;
        this.latestLocationStore = latestLocationStore;
    }

    public PageResult<DeviceLocation> page(String sbbh, String sjly, int page, int size) {
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : size;
        long total = mapper.countPage(sbbh, sjly);
        List<DeviceLocation> records = mapper.selectPage(sbbh, sjly, (p - 1) * s, s);
        return new PageResult<>(total, p, s, records);
    }

    public DeviceLocation get(String xxzjbh) {
        return mapper.selectById(xxzjbh);
    }

    public DeviceLocation create(DeviceLocation entity) {
        if (!StringUtils.hasText(entity.getXxzjbh())) {
            entity.setXxzjbh(UUID.randomUUID().toString().replace("-", ""));
        }
        mapper.insert(entity);
        latestLocationStore.refresh(List.of(entity.getSbbh()));
        mapQueryCache.evictAll();
        return entity;
    }

    public void update(String xxzjbh, DeviceLocation entity) {
        entity.setXxzjbh(xxzjbh);
        mapper.update(entity);
        latestLocationStore.refresh(List.of(entity.getSbbh()));
        mapQueryCache.evictAll();
    }

    public void delete(String xxzjbh) {
        DeviceLocation old = mapper.selectById(xxzjbh);
        mapper.deleteById(xxzjbh);
        if (old != null && StringUtils.hasText(old.getSbbh())) {
            latestLocationStore.refresh(List.of(old.getSbbh()));
        }
        mapQueryCache.evictAll();
    }
}
