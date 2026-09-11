-- 警用设备基本信息表 JYSB_SBXX_001
-- 类型按规范：字符型/数值型/时间戳（时间戳存 YYYYMMDDHHMMSS 共14位）
CREATE TABLE IF NOT EXISTS jysb_sbxx_001 (
    sbbh       VARCHAR(20)  NOT NULL COMMENT '设备编号，设备唯一编号',
    sbpp       VARCHAR(20)  DEFAULT NULL COMMENT '设备品牌',
    sbxh       VARCHAR(20)  DEFAULT NULL COMMENT '设备型号',
    zblx       DECIMAL(2, 0) DEFAULT NULL COMMENT '装备类型，数值型，见装备类型代码',
    ssgajgjgdm VARCHAR(20)  DEFAULT NULL COMMENT '所属公安机关机构代码',
    cphm       VARCHAR(20)  DEFAULT NULL COMMENT '车牌号码，车辆时必填',
    jybh       VARCHAR(20)  DEFAULT NULL COMMENT '警员编号，设备使用人警号',
    jyxm       VARCHAR(20)  DEFAULT NULL COMMENT '警员姓名，设备使用人姓名',
    jysfz      VARCHAR(20)  DEFAULT NULL COMMENT '警员身份证号，设备使用人身份证号',
    hh         VARCHAR(50)  DEFAULT NULL COMMENT '呼号',
    lxdh       VARCHAR(11)  DEFAULT NULL COMMENT '联系电话，设备使用人联系电话',
    sbsyzt     DECIMAL(2, 0) DEFAULT NULL COMMENT '设备使用状态代码，数值型，01在线02不在线',
    bz         VARCHAR(100) DEFAULT NULL COMMENT '备注',
    sjczlxdm   VARCHAR(1)   DEFAULT NULL COMMENT '数据操作类型代码，1新增2修改3撤销',
    sjly       VARCHAR(6)   DEFAULT NULL COMMENT '数据来源，6位行政区划代码',
    lrsj       CHAR(14)     DEFAULT NULL COMMENT '录入时间，时间戳，YYYYMMDDHHMMSS',
    lrdwdm     VARCHAR(12)  DEFAULT NULL COMMENT '录入单位代码',
    lrdwmc     VARCHAR(100) DEFAULT NULL COMMENT '录入单位名称',
    lrrxm      VARCHAR(50)  DEFAULT NULL COMMENT '录入人姓名',
    lrrsfzh    VARCHAR(18)  DEFAULT NULL COMMENT '录入人身份证号',
    xgsj       CHAR(14)     DEFAULT NULL COMMENT '修改时间，时间戳，YYYYMMDDHHMMSS',
    xgdwdm     VARCHAR(12)  DEFAULT NULL COMMENT '修改单位代码',
    xgdwmc     VARCHAR(100) DEFAULT NULL COMMENT '修改单位名称',
    xgrxm      VARCHAR(50)  DEFAULT NULL COMMENT '修改人姓名',
    xgrsfzh    VARCHAR(18)  DEFAULT NULL COMMENT '修改人身份证号',
    xxzjbh     VARCHAR(32)  NOT NULL COMMENT '信息主键编号',
    PRIMARY KEY (xxzjbh),
    INDEX idx_001_sbbh (sbbh),
    INDEX idx_001_sjly (sjly),
    INDEX idx_001_sjly_dev (sjly, sbbh, zblx)
) COMMENT='警用设备基本信息（JYSB_SBXX_001）';

-- 警用设备定位信息表 JYSB_SBXX_002
-- 类型按规范：字符型/数值型/日期型/时间戳
CREATE TABLE IF NOT EXISTS jysb_sbxx_002 (
    sbbh      VARCHAR(20)    NOT NULL COMMENT '设备编号，设备唯一编号',
    jd        DECIMAL(20, 10) DEFAULT NULL COMMENT '经度，数值型，WGS-84',
    wd        DECIMAL(20, 10) DEFAULT NULL COMMENT '纬度，数值型，WGS-84',
    spsd      DECIMAL(8, 2)   DEFAULT NULL COMMENT '水平速度，数值型，米/秒，保留两位小数',
    czsd      DECIMAL(8, 2)   DEFAULT NULL COMMENT '垂直速度，数值型，米/秒，保留两位小数',
    hjj       DECIMAL(3, 0)   DEFAULT NULL COMMENT '航迹角，数值型，范围(0,360]，无能力时报999',
    gc        DECIMAL(8, 2)   DEFAULT NULL COMMENT '高程，数值型，米，保留两位小数',
    jd_01     DECIMAL(2, 0)   DEFAULT NULL COMMENT '定位精度，数值型',
    sbsykssj  CHAR(14)        DEFAULT NULL COMMENT '设备使用开始时间，日期型，YYYYMMDDHHMMSS',
    sbsyjssj  CHAR(14)        DEFAULT NULL COMMENT '设备使用结束时间，日期型，YYYYMMDDHHMMSS',
    sjczlxdm  VARCHAR(1)      DEFAULT NULL COMMENT '数据操作类型代码，1新增2修改3撤销',
    sjly      VARCHAR(6)      DEFAULT NULL COMMENT '数据来源，6位行政区划代码',
    lrsj      CHAR(14)        DEFAULT NULL COMMENT '录入时间，时间戳，YYYYMMDDHHMMSS',
    lrdwdm    VARCHAR(12)     DEFAULT NULL COMMENT '录入单位代码',
    lrdwmc    VARCHAR(100)    DEFAULT NULL COMMENT '录入单位名称',
    lrrxm     VARCHAR(50)     DEFAULT NULL COMMENT '录入人姓名',
    lrrsfzh   VARCHAR(18)     DEFAULT NULL COMMENT '录入人身份证号',
    xgsj      CHAR(14)        DEFAULT NULL COMMENT '修改时间，时间戳，YYYYMMDDHHMMSS',
    xgdwdm    VARCHAR(12)     DEFAULT NULL COMMENT '修改单位代码',
    xgdwmc    VARCHAR(100)    DEFAULT NULL COMMENT '修改单位名称',
    xgrxm     VARCHAR(50)     DEFAULT NULL COMMENT '修改人姓名',
    xgrsfzh   VARCHAR(18)     DEFAULT NULL COMMENT '修改人身份证号',
    xxzjbh    VARCHAR(32)     NOT NULL COMMENT '信息主键编号',
    PRIMARY KEY (xxzjbh),
    INDEX idx_002_sbbh (sbbh),
    INDEX idx_002_sbbh_latest (sbbh, sbsyjssj, lrsj, xxzjbh)
) COMMENT='警用设备定位信息（JYSB_SBXX_002）';

-- 每台设备一条最新有效定位，供地图聚合直接 JOIN
CREATE TABLE IF NOT EXISTS jysb_sbxx_002_latest (
    sbbh      VARCHAR(20)     NOT NULL,
    jd        DECIMAL(20, 10) NOT NULL,
    wd        DECIMAL(20, 10) NOT NULL,
    sbsyjssj  CHAR(14)        DEFAULT NULL,
    lrsj      CHAR(14)        DEFAULT NULL,
    xxzjbh    VARCHAR(32)     NOT NULL,
    PRIMARY KEY (sbbh),
    INDEX idx_002_latest_geo (jd, wd)
) COMMENT='设备最新定位快照';
