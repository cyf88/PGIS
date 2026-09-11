package com.pgis.devicelocation;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceLocationMapper {
    DeviceLocation selectById(@Param("xxzjbh") String xxzjbh);

    List<DeviceLocation> selectPage(@Param("sbbh") String sbbh,
                                    @Param("sjly") String sjly,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    long countPage(@Param("sbbh") String sbbh, @Param("sjly") String sjly);

    int insert(DeviceLocation entity);

    int update(DeviceLocation entity);

    int deleteById(@Param("xxzjbh") String xxzjbh);

    List<String> selectExistingIds(@Param("ids") List<String> ids);

    int upsertBatch(@Param("list") List<DeviceLocation> list);

    int deleteLatestBySbbhs(@Param("sbbhs") List<String> sbbhs);

    int insertLatestBySbbhs(@Param("sbbhs") List<String> sbbhs);
}
