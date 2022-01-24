-- kanji=����
-- $Id: another_school_getcredits_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ANOTHER_SCHOOL_GETCREDITS_DAT

create table ANOTHER_SCHOOL_GETCREDITS_DAT \
(  \
    YEAR                    varchar(4) not null, \
    GET_DIV                 varchar(1) not null, \
    APPLICANTNO             varchar(7) not null, \
    GET_METHOD              varchar(1) not null, \
    CLASSCD                 varchar(2) not null, \
    CURRICULUM_CD           varchar(1) not null, \
    SUBCLASSCD              varchar(6) not null, \
    CREDIT_CURRICULUM_CD    varchar(1) not null, \
    CREDIT_ADMITSCD         varchar(6) not null, \
    SUBCLASSNAME            varchar(60), \
    SUBCLASSABBV            varchar(15), \
    GET_CREDIT              smallint, \
    VALUATION               smallint, \
    FORMER_REG_SCHOOLCD     varchar(11), \
    GET_DATE                date, \
    REMARK                  varchar(90), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ANOTHER_SCHOOL_GETCREDITS_DAT  \
add constraint PK_ANOTHER_GETCRE \
primary key  \
(YEAR, GET_DIV, APPLICANTNO, GET_METHOD, CLASSCD, CURRICULUM_CD, SUBCLASSCD)
