-- kanji=漢字
-- $Id: rep-another_school_getcredits_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ANOTHER_SCHOOL_GETCREDITS_DAT_OLD

create table ANOTHER_SCHOOL_GETCREDITS_DAT_OLD like ANOTHER_SCHOOL_GETCREDITS_DAT

insert into ANOTHER_SCHOOL_GETCREDITS_DAT_OLD select * from ANOTHER_SCHOOL_GETCREDITS_DAT

drop table ANOTHER_SCHOOL_GETCREDITS_DAT

create table ANOTHER_SCHOOL_GETCREDITS_DAT \
(  \
    YEAR                    varchar(4) not null, \
    APPLICANTNO             varchar(7) not null, \
    GET_METHOD              varchar(1) not null, \
    CLASSCD                 varchar(2) not null, \
    CURRICULUM_CD           varchar(1) not null, \
    SUBCLASSCD              varchar(6) not null, \
    CREDIT_CURRICULUM_CD    varchar(1) not null, \
    CREDIT_ADMITSCD         varchar(6) not null, \
    SUBCLASSNAME            varchar(60), \
    SUBCLASSABBV            varchar(16), \
    GET_CREDIT              smallint, \
    VALUATION               smallint, \
    FORMER_REG_SCHOOLCD     varchar(11), \
    REMARK                  varchar(90), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ANOTHER_SCHOOL_GETCREDITS_DAT  \
add constraint PK_ANOTHER_GETCRE \
primary key  \
(YEAR, APPLICANTNO, GET_METHOD, CLASSCD, CURRICULUM_CD, SUBCLASSCD)


insert into ANOTHER_SCHOOL_GETCREDITS_DAT \
select \
    YEAR, \
    APPLICANTNO, \
    GET_METHOD, \
    CLASSCD, \
    CURRICULUM_CD, \
    SUBCLASSCD, \
    CREDIT_CURRICULUM_CD, \
    CREDIT_ADMITSCD, \
    '', \
    '', \
    GET_CREDIT, \
    VALUATION, \
    FORMER_REG_SCHOOLCD, \
    REMARK, \
    REGISTERCD, \
    UPDATED \
    
FROM \
    ANOTHER_SCHOOL_GETCREDITS_DAT_OLD
