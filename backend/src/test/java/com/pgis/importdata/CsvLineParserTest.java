package com.pgis.importdata;

import com.pgis.deviceinfo.DeviceInfo;
import com.pgis.devicelocation.DeviceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvLineParserTest {

    @Test
    void parseDeviceInfo_mapsCsvHeaderOrder_jysfzAfterXxzjbh() {
        // 与 sb001.csv 表头一致：… xgrsfzh, xxzjbh, jysfz, pt
        String[] cols = new String[33];
        java.util.Arrays.fill(cols, "");
        cols[0] = "A-410000000000-0100";
        cols[1] = "uuid-batch";
        cols[2] = "20260901174009";
        cols[3] = "00";
        cols[4] = "jysb_sbxx#2";
        cols[5] = "1788255690191";
        cols[6] = "DEV001";
        cols[7] = "HUAWEI";
        cols[8] = "PLR-AL50";
        cols[9] = "07";
        cols[10] = "410183810000";
        cols[13] = "杨小凡";
        cols[14] = "呼号甲";
        cols[15] = "13800000000";
        cols[16] = "01";
        cols[30] = "pk-001";
        cols[31] = "410101199001011234";
        cols[32] = "20260901";

        DeviceInfo info = CsvLineParser.parseDeviceInfo(cols);

        assertEquals("DEV001", info.getSbbh());
        assertEquals("HUAWEI", info.getSbpp());
        assertEquals("PLR-AL50", info.getSbxh());
        assertEquals("07", info.getZblx());
        assertEquals("410183810000", info.getSsgajgjgdm());
        assertEquals("杨小凡", info.getJyxm());
        assertEquals("呼号甲", info.getHh());
        assertEquals("13800000000", info.getLxdh());
        assertEquals("01", info.getSbsyzt());
        assertEquals("pk-001", info.getXxzjbh());
        assertEquals("410101199001011234", info.getJysfz());
    }

    @Test
    void parseDeviceInfo_generatesUuidWhenPrimaryKeyBlank() {
        String[] cols = new String[32];
        java.util.Arrays.fill(cols, "");
        cols[6] = "DEV002";
        cols[30] = "  ";
        cols[31] = "410101199001011234";

        DeviceInfo info = CsvLineParser.parseDeviceInfo(cols);

        assertEquals(32, info.getXxzjbh().length());
        assertFalse(info.getXxzjbh().contains("-"));
        assertEquals("410101199001011234", info.getJysfz());
    }

    @Test
    void parseDeviceInfo_rejectsShortRow() {
        String[] cols = new String[10];
        java.util.Arrays.fill(cols, "x");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CsvLineParser.parseDeviceInfo(cols));
        assertTrue(ex.getMessage().contains("列数不足"));
    }

    @Test
    void parseDeviceLocation_mapsCoordinatesAfterEnvelope() {
        String[] cols = new String[30];
        java.util.Arrays.fill(cols, "");
        cols[6] = "LOC001";
        cols[7] = "117.1410444444";
        cols[8] = "39.1367611111";
        cols[11] = "999";
        cols[28] = "loc-pk-1";
        cols[29] = "20260906";

        DeviceLocation loc = CsvLineParser.parseDeviceLocation(cols);

        assertEquals("LOC001", loc.getSbbh());
        assertEquals("117.1410444444", loc.getJd());
        assertEquals("39.1367611111", loc.getWd());
        assertEquals("999", loc.getHjj());
        assertEquals("loc-pk-1", loc.getXxzjbh());
    }

    @Test
    void isHeaderRow_detectsSjhjAppid() {
        assertTrue(CsvLineParser.isHeaderRow(new String[]{"sjhj_appid", "sbbh"}));
        assertFalse(CsvLineParser.isHeaderRow(new String[]{"A-410000000000-0100", "x"}));
    }

    @Test
    void filePrefix_matchesIgnoreCaseAndSkipsNonCsv() {
        assertTrue(CsvLineParser.matchesTableFile("sb001_0.csv", "sb001"));
        assertTrue(CsvLineParser.matchesTableFile("SB001_1.CSV", "sb001"));
        assertFalse(CsvLineParser.matchesTableFile("sb002_0.csv", "sb001"));
        assertFalse(CsvLineParser.matchesTableFile("sb001_0.txt", "sb001"));
    }
}
