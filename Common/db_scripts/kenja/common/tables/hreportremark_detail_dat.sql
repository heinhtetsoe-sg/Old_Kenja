-- kanji=����
-- $Id: 7dd1545172e009ac95530be916fb065294f281e9 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table HREPORTREMARK_DETAIL_DAT

create table HREPORTREMARK_DETAIL_DAT \
    (YEAR                 varchar(4) not null, \
     SEMESTER             varchar(1) not null, \
     SCHREGNO             varchar(8) not null, \
     DIV                  varchar(2) not null, \
     CODE                 varchar(2) not null, \
     REMARK1              varchar(1500), \
     REMARK2              varchar(1500), \
     REMARK3              varchar(1500), \
     REMARK4              varchar(3000), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HREPORTREMARK_DETAIL_DAT \
add constraint PK_HREP_DETAIL_DAT \
primary key \
(YEAR, SEMESTER, SCHREGNO, DIV, CODE)
