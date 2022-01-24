-- kanji=����
-- $Id: comp_credit_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table COMP_CREDIT_DAT

create table COMP_CREDIT_DAT \
(  \
    YEAR            varchar(4) not null, \
    APPLICANTNO     varchar(7) not null, \
    SLIP_NO         varchar(8) not null, \
    CREDIT_DIV      varchar(1) not null, \
    COMP_ENT_FLG    varchar(1), \
    COMP_CREDIT     smallint, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMP_CREDIT_DAT  \
add constraint PK_COMP_CREDIT_DAT \
primary key  \
(YEAR, APPLICANTNO, SLIP_NO, CREDIT_DIV)
