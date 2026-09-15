package com.pgis.alarm;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlarmMapper {
    AlarmRecord selectById(@Param("jjbh") String jjbh);

    List<AlarmRecord> selectPage(@Param("jjbh") String jjbh,
                                 @Param("xzqhdm") String xzqhdm,
                                 @Param("bjrxm") String bjrxm,
                                 @Param("jqlbdm") String jqlbdm,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    long countPage(@Param("jjbh") String jjbh,
                   @Param("xzqhdm") String xzqhdm,
                   @Param("bjrxm") String bjrxm,
                   @Param("jqlbdm") String jqlbdm);

    List<AlarmMapPoint> selectMapPoints(@Param("minLng") double minLng,
                                        @Param("minLat") double minLat,
                                        @Param("maxLng") double maxLng,
                                        @Param("maxLat") double maxLat,
                                        @Param("xzqhdm") String xzqhdm);
}
