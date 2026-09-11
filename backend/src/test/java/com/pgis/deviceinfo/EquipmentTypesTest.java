package com.pgis.deviceinfo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EquipmentTypesTest {

    @Test
    void mergeFillsAllCodesAndNormalizesNumericZblx() {
        List<EquipmentTypeStat> merged = EquipmentTypes.merge(List.of(
                new EquipmentTypeStat("1", null, 2L),
                new EquipmentTypeStat("08", null, 3L),
                new EquipmentTypeStat(null, null, 1L)
        ));
        assertEquals(12, merged.size());
        assertEquals("警用汽车", merged.get(0).getName());
        assertEquals(2L, merged.get(0).getCount());
        assertEquals(3L, merged.stream().filter(s -> "08".equals(s.getZblx())).findFirst().orElseThrow().getCount());
        assertEquals(1L, merged.stream().filter(s -> "99".equals(s.getZblx())).findFirst().orElseThrow().getCount());
        assertEquals(0L, merged.stream().filter(s -> "02".equals(s.getZblx())).findFirst().orElseThrow().getCount());
    }
}
