-- kanji=����
-- $Id: 75661b85d1066a79566859fc8ef27b85d1cc6202 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEXAM_MOCK_REMARK_DAT

create table HEXAM_MOCK_REMARK_DAT \
(  \
        YEAR        varchar(4) not null, \
        MOCKCD      varchar(9) not null, \
        SCHREGNO    varchar(8) not null, \
        REMARK_DIV  varchar(1) not null, \
        REMARK1     varchar(1050) , \
        REMARK2     varchar(1050) , \
        REMARK3     varchar(1050) , \
        REMARK4     varchar(1050) , \
        REGISTERCD  varchar(8), \
        UPDATED     timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table HEXAM_MOCK_REMARK_DAT  \
add constraint PK_HEXAM_MOCK_REM  \
primary key  \
(YEAR, MOCKCD, SCHREGNO, REMARK_DIV)
