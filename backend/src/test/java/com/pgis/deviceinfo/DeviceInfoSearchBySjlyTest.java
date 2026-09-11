package com.pgis.deviceinfo;

import com.pgis.devicelocation.DeviceLocation;
import com.pgis.devicelocation.DeviceLocationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DeviceInfoSearchBySjlyTest {

    @Autowired
    private DeviceInfoService deviceInfoService;

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private DeviceLocationMapper deviceLocationMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void searchBySjly_picksLatestLocationBySbsyjssj() {
        jdbcTemplate.update("DELETE FROM jysb_sbxx_002 WHERE sbbh IN ('DEV-SJLY-A','DEV-SJLY-B')");
        jdbcTemplate.update("DELETE FROM jysb_sbxx_001 WHERE xxzjbh IN ('info-a','info-b','info-other')");

        deviceInfoMapper.insert(info("info-a", "DEV-SJLY-A", "410000", "20260102000000"));
        deviceInfoMapper.insert(info("info-b", "DEV-SJLY-B", "410000", "20260101000000"));
        deviceInfoMapper.insert(info("info-other", "DEV-SJLY-C", "411000", "20260103000000"));

        deviceLocationMapper.insert(loc("loc-old", "DEV-SJLY-A", "113.1", "34.1", "20260101080000"));
        deviceLocationMapper.insert(loc("loc-new", "DEV-SJLY-A", "113.9", "34.9", "20260103180000"));

        assertEquals(2, deviceInfoService.countBySjly("410000"));

        List<DeviceInfoWithLocation> records = deviceInfoService.searchBySjly("410000", 1, 20).getRecords();
        assertEquals(2, records.size());
        DeviceInfoWithLocation first = records.get(0);
        assertEquals("DEV-SJLY-A", first.getSbbh());
        assertEquals(113.9, Double.parseDouble(first.getJd()), 0.0001);
        assertEquals(34.9, Double.parseDouble(first.getWd()), 0.0001);
        assertEquals("20260103180000", first.getSbsyjssj());

        DeviceInfoWithLocation second = records.get(1);
        assertEquals("DEV-SJLY-B", second.getSbbh());
        assertNull(second.getJd());
        assertNull(second.getWd());
    }

    @Test
    void searchBySjly_requiresSjly() {
        assertThrows(IllegalArgumentException.class, () -> deviceInfoService.searchBySjly("  ", 1, 20));
        assertThrows(IllegalArgumentException.class, () -> deviceInfoService.countBySjly(null));
    }

    private static DeviceInfo info(String pk, String sbbh, String sjly, String lrsj) {
        DeviceInfo e = new DeviceInfo();
        e.setXxzjbh(pk);
        e.setSbbh(sbbh);
        e.setSjly(sjly);
        e.setLrsj(lrsj);
        return e;
    }

    private static DeviceLocation loc(String pk, String sbbh, String jd, String wd, String sbsyjssj) {
        DeviceLocation e = new DeviceLocation();
        e.setXxzjbh(pk);
        e.setSbbh(sbbh);
        e.setJd(jd);
        e.setWd(wd);
        e.setSbsyjssj(sbsyjssj);
        e.setLrsj(sbsyjssj);
        return e;
    }
}
