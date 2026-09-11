package com.pgis.deviceinfo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceInfoMapper {
    DeviceInfo selectById(@Param("xxzjbh") String xxzjbh);

    List<DeviceInfo> selectPage(@Param("sbbh") String sbbh,
                                @Param("jyxm") String jyxm,
                                @Param("ssgajgjgdm") String ssgajgjgdm,
                                @Param("sjly") String sjly,
                                @Param("offset") int offset,
                                @Param("size") int size);

    long countPage(@Param("sbbh") String sbbh,
                   @Param("jyxm") String jyxm,
                   @Param("ssgajgjgdm") String ssgajgjgdm,
                   @Param("sjly") String sjly);

    int insert(DeviceInfo entity);

    int update(DeviceInfo entity);

    int deleteById(@Param("xxzjbh") String xxzjbh);

    List<String> selectExistingIds(@Param("ids") List<String> ids);

    int upsertBatch(@Param("list") List<DeviceInfo> list);

    List<DeviceInfoWithLocation> selectBySjlyWithLatestLocation(@Param("sjly") String sjly,
                                                              @Param("offset") int offset,
                                                              @Param("size") int size);

    long countBySjly(@Param("sjly") String sjly);

    long countLatestInBounds(@Param("minLng") double minLng,
                             @Param("minLat") double minLat,
                             @Param("maxLng") double maxLng,
                             @Param("maxLat") double maxLat,
                             @Param("sjly") String sjly);

    List<MapClusterPoint> selectRegionClustersInBounds(@Param("minLng") double minLng,
                                                       @Param("minLat") double minLat,
                                                       @Param("maxLng") double maxLng,
                                                       @Param("maxLat") double maxLat,
                                                       @Param("sjly") String sjly,
                                                       @Param("prefixLen") int prefixLen);

    List<MapClusterPoint> selectPointsInBounds(@Param("minLng") double minLng,
                                               @Param("minLat") double minLat,
                                               @Param("maxLng") double maxLng,
                                               @Param("maxLat") double maxLat,
                                               @Param("sjly") String sjly,
                                               @Param("limit") int limit);

    List<EquipmentTypeStat> selectTypeCountsInBounds(@Param("minLng") double minLng,
                                                     @Param("minLat") double minLat,
                                                     @Param("maxLng") double maxLng,
                                                     @Param("maxLat") double maxLat,
                                                     @Param("sjly") String sjly);

    RegionBounds selectRegionBounds(@Param("sjly") String sjly);

    List<LatestMapPoint> selectLatestDevicesInBounds(@Param("minLng") double minLng,
                                                     @Param("minLat") double minLat,
                                                     @Param("maxLng") double maxLng,
                                                     @Param("maxLat") double maxLat,
                                                     @Param("sjly") String sjly,
                                                     @Param("useLatestTable") boolean useLatestTable);
}
