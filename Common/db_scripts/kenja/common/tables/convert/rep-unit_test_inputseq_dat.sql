-- $Id: b095d1459b3b42c869b1417e9904c6714360dce8 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table UNIT_TEST_INPUTSEQ_DAT_OLD
create table UNIT_TEST_INPUTSEQ_DAT_OLD like UNIT_TEST_INPUTSEQ_DAT
insert into UNIT_TEST_INPUTSEQ_DAT_OLD select * from UNIT_TEST_INPUTSEQ_DAT

drop table UNIT_TEST_INPUTSEQ_DAT
create table UNIT_TEST_INPUTSEQ_DAT( \
    YEAR                varchar(4)      not null, \
    GRADE               varchar(2)      not null, \
    HR_CLASS            varchar(3)      not null, \
    CLASSCD             varchar(2)      not null, \
    SCHOOL_KIND         varchar(2)      not null, \
    CURRICULUM_CD       varchar(2)      not null, \
    SUBCLASSCD          varchar(6)      not null, \
    SEQ                 smallint        not null, \
    VIEWCD              varchar(4)      not null, \
    VIEWFLG             varchar(1), \
    UNIT_ASSESSHIGH     smallint, \
    WEIGHTING           smallint, \
    WEIGHTING_CALC      smallint, \
    WEIGHTING_EXE       smallint, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table UNIT_TEST_INPUTSEQ_DAT add constraint PK_UNIT_TEST_IPSQ \
    primary key (YEAR, GRADE, HR_CLASS, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEQ, VIEWCD)

insert into UNIT_TEST_INPUTSEQ_DAT \
select \
    YEAR, \
    GRADE, \
    HR_CLASS, \
    CLASSCD, \
    SCHOOL_KIND, \
    CURRICULUM_CD, \
    SUBCLASSCD, \
    SEQ, \
    VIEWCD, \
    VIEWFLG, \
    UNIT_ASSESSHIGH, \
    cast(null as smallint) as WEIGHTING, \
    cast(null as smallint) as WEIGHTING_CALC, \
    cast(null as smallint) as WEIGHTING_EXE, \
    REGISTERCD, \
    UPDATED \
from UNIT_TEST_INPUTSEQ_DAT_OLD
