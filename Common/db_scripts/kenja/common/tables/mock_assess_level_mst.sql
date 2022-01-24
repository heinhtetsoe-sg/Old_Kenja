-- kanji=����
-- $Id: f9e3e9b6f37bf00499e4c57627c65d84b233ff3b $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table MOCK_ASSESS_LEVEL_MST

create table MOCK_ASSESS_LEVEL_MST ( \
    YEAR                varchar(4) not null, \
    MOCKCD              varchar(9) not null, \
    MOCK_SUBCLASS_CD    varchar(6) not null, \
    DIV                 varchar(1) not null, \
    GRADE               varchar(2) not null, \
    HR_CLASS            varchar(3) not null, \
    COURSECD            varchar(1) not null, \
    MAJORCD             varchar(3) not null, \
    COURSECODE          varchar(4) not null, \
    ASSESSLEVEL         smallint not null, \
    ASSESSMARK          varchar(6), \
    ASSESSLOW           decimal, \
    ASSESSHIGH          decimal, \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_ASSESS_LEVEL_MST add constraint pk_mock_ass_lvl_m \
      primary key (YEAR, MOCKCD, MOCK_SUBCLASS_CD, DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE, ASSESSLEVEL)
