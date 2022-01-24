-- kanji=漢字
-- $Id: ed2eafde7062bcf6198309b15559b0fc9e3853d3 $
-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

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
