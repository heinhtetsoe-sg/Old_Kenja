-- kanji=����
-- $Id: 397347b45fbd91c538f8a75d780871ac03abea18 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table REP_SUBCLASS_COMBINED_DAT

create table REP_SUBCLASS_COMBINED_DAT ( \
    YEAR                      VARCHAR(4) NOT NULL, \
    COMBINED_CLASSCD          VARCHAR(2) NOT NULL, \
    COMBINED_SCHOOL_KIND      VARCHAR(2) NOT NULL, \
    COMBINED_CURRICULUM_CD    VARCHAR(2) NOT NULL, \
    COMBINED_SUBCLASSCD       VARCHAR(6) NOT NULL, \
    ATTEND_CLASSCD            VARCHAR(2) NOT NULL, \
    ATTEND_SCHOOL_KIND        VARCHAR(2) NOT NULL, \
    ATTEND_CURRICULUM_CD      VARCHAR(2) NOT NULL, \
    ATTEND_SUBCLASSCD         VARCHAR(6) NOT NULL, \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table REP_SUBCLASS_COMBINED_DAT add constraint PK_REP_COMBINED \
        primary key (YEAR, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD, ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD)
