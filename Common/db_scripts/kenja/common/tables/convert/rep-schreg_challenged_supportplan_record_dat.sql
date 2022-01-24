-- kanji=漢字
-- $Id: ed2eafde7062bcf6198309b15559b0fc9e3853d3 $
-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。
drop table SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT_OLD
create table SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT_OLD like SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT
insert into SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT_OLD select * from SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT

drop   table SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT

create table SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT ( \
    YEAR        varchar(4) not null, \
    RECORD_DATE date not null, \
    SCHREGNO    varchar(8) not null, \
    DIV         varchar(2) not null, \
    HOPE1       varchar(700), \
    HOPE2       varchar(700), \
    GOALS       varchar(700), \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT ADD CONSTRAINT PK_SCHCHAL_SPLAN_REC_D PRIMARY KEY (YEAR, RECORD_DATE, SCHREGNO, DIV)

insert into SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT \
select \
        YEAR, \
        CASE SEMESTER WHEN '1' THEN '2020-04-01' WHEN '2' THEN '2020-10-01' ELSE '2021-01-01' END AS RECORD_DATE, \
        SCHREGNO, \
        DIV, \
        HOPE1, \
        HOPE2, \
        GOALS, \
        REGISTERCD, \
        UPDATED \
from SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT_OLD



