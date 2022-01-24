-- kanji=����
-- $Id: b433b595e6f7741860b031ce585a3e634a89426e $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--Web�д��������

drop table COLLECT_SGL_WEB_DEPOSIT_DAT

create table COLLECT_SGL_WEB_DEPOSIT_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "SEQ"                   int         not null, \
        "ACCOUNTNAME"           varchar(120), \
        "ENT_MONEY"             varchar(10) , \
        "RESERVE_MONEY"         varchar(10) , \
        "REGISTERCD"            varchar(10) , \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_WEB_DEPOSIT_DAT \
add constraint PK_CO_WEB_DEP_DAT \
primary key \
(YEAR, SCHREGNO)
