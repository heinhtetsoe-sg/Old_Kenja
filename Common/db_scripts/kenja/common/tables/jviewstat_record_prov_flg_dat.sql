-- kanji=漢字
-- $Id: 658ecf16e1b9ab12675a0c9cda28ae762734cbcc $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table JVIEWSTAT_RECORD_PROV_FLG_DAT

create table JVIEWSTAT_RECORD_PROV_FLG_DAT(  \
    YEAR          varchar(4)  not null, \
    SEMESTER      varchar(1)  not null, \
    TESTKINDCD    varchar(2)  not null, \
    TESTITEMCD    varchar(2)  not null, \
    SCORE_DIV     varchar(2)  not null, \
    CLASSCD       varchar(2)  not null, \
    SCHOOL_KIND   varchar(2)  not null, \
    CURRICULUM_CD varchar(2)  not null, \
    SUBCLASSCD    varchar(6)  not null, \
    SCHREGNO      varchar(8)  not null, \
    PROV_DIV      varchar(2)  not null, \
    PROV_FLG      varchar(1)  , \
    REMARK1       varchar(90) , \
    REMARK2       varchar(90) , \
    REMARK3       varchar(90) , \
    REGISTERCD    varchar(10) ,  \
    UPDATED       timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table JVIEWSTAT_RECORD_PROV_FLG_DAT  \
add constraint pk_jviewstat_rec  \
primary key  \
(YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, PROV_DIV)
