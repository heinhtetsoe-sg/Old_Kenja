-- kanji=����
-- $Id: c90039515fcdc18a586fe183ef465744a9010e52 $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ASSESS_LEVEL_MST

create table ASSESS_LEVEL_MST ( \
    YEAR            varchar(4)  not null, \
    SEMESTER        varchar(1)  not null, \
    TESTKINDCD      varchar(2)  not null, \
    TESTITEMCD      varchar(2)  not null, \
    CLASSCD         varchar(2)  not null, \
    SCHOOL_KIND     varchar(2)  not null, \
    CURRICULUM_CD   varchar(2)  not null, \
    SUBCLASSCD      varchar(6)  not null, \
    DIV             varchar(1)  not null, \
    GRADE           varchar(2)  not null, \
    HR_CLASS        varchar(3)  not null, \
    COURSECD        varchar(1)  not null, \
    MAJORCD         varchar(3)  not null, \
    COURSECODE      varchar(4)  not null, \
    ASSESSLEVEL     smallint not null, \
    ASSESSMARK      varchar(6), \
    ASSESSLOW       decimal, \
    ASSESSHIGH      decimal, \
    PERCENT         DECIMAL(4,1), \
    PERCENT_ALLCNT  smallint, \
    PERCENT_CNT     smallint, \
    STANDARD_ASSESSLOW       decimal, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESS_LEVEL_MST add constraint pk_ass_lvl_mst \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE, ASSESSLEVEL)
