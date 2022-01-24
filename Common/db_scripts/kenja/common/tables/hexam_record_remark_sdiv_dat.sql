-- kanji=����
-- $Id: 7ff7707b03c681483a7aa9102e1aac9f4b506dd8 $

-- ����:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEXAM_RECORD_REMARK_SDIV_DAT

create table HEXAM_RECORD_REMARK_SDIV_DAT \
(  \
        "YEAR"              varchar(4) not null, \
        "SEMESTER"          varchar(1) not null, \
        "TESTKINDCD"        varchar(2) not null, \
        "TESTITEMCD"        varchar(2) not null, \
        "SCORE_DIV"         varchar(2) not null, \
        "SCHREGNO"          varchar(8) not null, \
        "REMARK_DIV"        varchar(1) not null, \
        "REMARK1"           varchar(1050) , \
        "REMARK2"           varchar(1050) , \
        "REMARK3"           varchar(1050) , \
        "REMARK4"           varchar(1050) , \
        "REGISTERCD"        varchar(8), \
        "UPDATED"           timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table HEXAM_RECORD_REMARK_SDIV_DAT  \
add constraint PK_HEXAM_REC_REMAR_S  \
primary key  \
(YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, SCHREGNO, REMARK_DIV)