-- kanji=����
-- $Id: dd8c8aa8a63bb5bcd7d1c05a9f2829886af5accb $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table JVIEWSTAT_SUB_DAT

create table JVIEWSTAT_SUB_DAT(  \
        YEAR          VARCHAR(4)  NOT NULL, \
        SEMESTER      VARCHAR(1)  NOT NULL, \
        SCHREGNO      VARCHAR(8)  NOT NULL, \
        CLASSCD       VARCHAR(2)  NOT NULL, \
        SCHOOL_KIND   VARCHAR(2)  NOT NULL, \
        CURRICULUM_CD VARCHAR(2)  NOT NULL, \
        SUBCLASSCD    VARCHAR(6)  NOT NULL, \
        VIEWCD        VARCHAR(4)  NOT NULL, \
        STATUS        VARCHAR(1),  \
        REGISTERCD    VARCHAR(8),  \
        UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) in usr1dms index in idx1dms


alter table JVIEWSTAT_SUB_DAT  \
add constraint PK_JVS_SUB_DAT  \
primary key  \
(YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)
