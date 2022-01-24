-- $Id: ded9badd69064d906a23ec755a86ef9468a2703c $
-- 時間割チェックリスト(KNJB0045)で使用するテーブル

-- スクリプトの使用方法: db2 +c -f sch_checklist_hdat.sql

drop   table SCH_CHECKLIST_HDAT

create table SCH_CHECKLIST_HDAT ( \
    STATUS      varchar(8) not null check(STATUS in ('RUNNING', 'OK', 'ERROR')), \
    RADIO       varchar(1), \
    YEAR        varchar(4), \
    SEMESTER    varchar(1), \
    BSCSEQ      smallint, \
    DATE_FROM   date, \
    DATE_TO     date, \
    OPERATION   smallint, \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp, \
    primary key ( STATUS ) \
) in usr1dms index in idx1dms

