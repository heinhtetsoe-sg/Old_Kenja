-- kanji=����
-- $Id: 8b45f85eb56e184f6b942e24c4b8dcb9130271bc $
-- ��ʻ������ɾ��׻���ˡ����ǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table COMB_GCALC_DAT

create table COMB_GCALC_DAT(  \
    YEAR                        VARCHAR(4) NOT NULL, \
    COMBINED_CLASSCD            VARCHAR(2) NOT NULL, \
    COMBINED_SCHOOL_KIND        VARCHAR(2) NOT NULL, \
    COMBINED_CURRICULUM_CD      VARCHAR(2) NOT NULL, \
    COMBINED_SUBCLASSCD         VARCHAR(6) NOT NULL, \
    GVAL_CALC                   VARCHAR(1), \
    REGISTERCD                  VARCHAR(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table COMB_GCALC_DAT add constraint PK_COM_GC_DAT \
primary key (YEAR, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD)
