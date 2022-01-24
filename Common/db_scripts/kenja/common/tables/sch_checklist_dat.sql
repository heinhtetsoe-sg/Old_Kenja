-- $Id: b5cd711d6e7543f584140d78f3d8665ed5263022 $
-- 時間割チェックリスト(KNJB0045)で使用するテーブル

-- スクリプトの使用方法: db2 +c -f sch_checklist_dat.sql

drop   table SCH_CHECKLIST_DAT

create table SCH_CHECKLIST_DAT ( \
    KEY1        smallint, \
    KEY2        smallint, \
    KEY3        smallint, \
    KEY4        smallint, \
    MSGTYPE     smallint check(MSGTYPE in (1, 2, 3, 4)), \
    ITEM        varchar(200), \
    MSG         varchar(200) \
) in usr1dms index in idx1dms

