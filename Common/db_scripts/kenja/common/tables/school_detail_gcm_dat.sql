-- kanji=漢字
-- $Id: ddae92a783ff39e6dbe5b7de84fe480995dd93ad $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHOOL_DETAIL_GCM_DAT

create table SCHOOL_DETAIL_GCM_DAT \
(  \
    YEAR            varchar(4)  not null, \
    SCHOOLCD        varchar(12) not null, \
    SCHOOL_KIND     varchar(2)  not null, \
    GRADE           varchar(2)  not null, \
    COURSECD        varchar(1)  not null, \
    MAJORCD         varchar(3)  not null, \
    SEQ             varchar(3)  not null, \
    REMARK1         varchar(90), \
    REMARK2         varchar(90), \
    REMARK3         varchar(90), \
    REMARK4         varchar(90), \
    REMARK5         varchar(90), \
    REMARK6         varchar(90), \
    REMARK7         varchar(90), \
    REMARK8         varchar(90), \
    REMARK9         varchar(90), \
    REMARK10        varchar(90), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHOOL_DETAIL_GCM_DAT add constraint PK_SCHOOL_GCM \
primary key (YEAR, SCHOOLCD, SCHOOL_KIND, GRADE, COURSECD, MAJORCD, SEQ)
