-- kanji=漢字
-- $Id: abb4488810ea9c4dad8e9d459bb0ec293d1a3fb8 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEXAM_RECORD_REMARK_DAT

create table HEXAM_RECORD_REMARK_DAT \
(  \
        "YEAR"              varchar(4) not null, \
        "SEMESTER"          varchar(1) not null, \
        "TESTKINDCD"        varchar(2) not null, \
        "TESTITEMCD"        varchar(2) not null, \
        "SCHREGNO"          varchar(8) not null, \
        "REMARK_DIV"        varchar(1) not null, \
        "REMARK1"           varchar(1050) , \
        "REMARK2"           varchar(1050) , \
        "REMARK3"           varchar(1050) , \
        "REMARK4"           varchar(1050) , \
        "REGISTERCD"        varchar(8), \
        "UPDATED"           timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table HEXAM_RECORD_REMARK_DAT  \
add constraint PK_HEXAM_REC_REMAR  \
primary key  \
(YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCHREGNO, REMARK_DIV)
