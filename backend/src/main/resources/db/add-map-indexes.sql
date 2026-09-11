-- 已有 MySQL 库补齐地图聚合索引（新库已写在 schema.sql 里）
-- 定位表很大时会锁表一段时间，建议业务低峰执行

CREATE INDEX idx_001_sjly ON jysb_sbxx_001 (sjly);
CREATE INDEX idx_001_sjly_dev ON jysb_sbxx_001 (sjly, sbbh, zblx);
CREATE INDEX idx_002_sbbh_latest ON jysb_sbxx_002 (sbbh, sbsyjssj, lrsj, xxzjbh);
