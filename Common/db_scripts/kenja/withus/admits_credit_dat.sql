-- kanji=����
-- $Id: admits_credit_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ADMITS_CREDIT_DAT

create table ADMITS_CREDIT_DAT \
(  \
    GET_METHOD          varchar(1) not null, \
    CURRICULUM_CD       varchar(1) not null, \
    ADMITSCD            varchar(6) not null, \
    ADMITSNAME          varchar(60), \
    ADMITSABBV          varchar(15), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ADMITS_CREDIT_DAT  \
add constraint PK_ADMITS_CREDIT_D \
primary key  \
(GET_METHOD, CURRICULUM_CD, ADMITSCD)
