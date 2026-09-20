package com.pgis.caseinfo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CaseMapper {
    CaseRecord selectById(@Param("xxzjbh") String xxzjbh);

    List<CaseRecord> selectPage(@Param("asjbh") String asjbh,
                                @Param("ajmc") String ajmc,
                                @Param("ajfsdxzqhdm") String ajfsdxzqhdm,
                                @Param("sjly") String sjly,
                                @Param("offset") int offset,
                                @Param("size") int size);

    long countPage(@Param("asjbh") String asjbh,
                   @Param("ajmc") String ajmc,
                   @Param("ajfsdxzqhdm") String ajfsdxzqhdm,
                   @Param("sjly") String sjly);

    List<CaseMapPoint> selectMapPoints(@Param("minLng") double minLng,
                                       @Param("minLat") double minLat,
                                       @Param("maxLng") double maxLng,
                                       @Param("maxLat") double maxLat,
                                       @Param("xzqhdm") String xzqhdm);

    List<CaseRecord> selectNoLocation(@Param("limit") int limit);

    int updateLocation(@Param("xxzjbh") String xxzjbh,
                       @Param("ajfsdzjd") String ajfsdzjd,
                       @Param("ajfsdzwd") String ajfsdzwd);
}
