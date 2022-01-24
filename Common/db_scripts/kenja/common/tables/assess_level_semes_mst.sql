-- kanji=����
-- $Id: ab1e71a4ffcc7c33030e914aff022190ca677270 $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ASSESS_LEVEL_SEMES_MST

create table ASSESS_LEVEL_SEMES_MST ( \
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
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESS_LEVEL_SEMES_MST add constraint pk_ass_l_s_mst \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE, ASSESSLEVEL)
