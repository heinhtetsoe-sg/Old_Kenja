-- kanji=����
-- $Id: ba0a2c2f1c23566f5ac83bfdd40c71c33332f907 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table JVIEWSTAT_REPORTREMARK_DAT

create table JVIEWSTAT_REPORTREMARK_DAT(  \
    YEAR          VARCHAR(4)  NOT NULL, \
    SEMESTER      VARCHAR(1)  NOT NULL, \
    SCHREGNO      VARCHAR(8)  NOT NULL, \
    CLASSCD       VARCHAR(2)  NOT NULL, \
    SCHOOL_KIND   VARCHAR(2)  NOT NULL, \
    CURRICULUM_CD VARCHAR(2)  NOT NULL, \
    SUBCLASSCD    VARCHAR(6)  NOT NULL, \
    REMARK1       VARCHAR(780),  \
    REMARK2       VARCHAR(780),  \
    REMARK3       VARCHAR(780),  \
    REGISTERCD    VARCHAR(8),  \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) in usr1dms index in idx1dms

alter table JVIEWSTAT_REPORTREMARK_DAT  \
add constraint pk_jviewstat_rep  \
primary key  \
(YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
