-- kanji=����
-- $Id: rec_graduate_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table REC_GRADUATE_DAT

create table REC_GRADUATE_DAT \
(  \
    SCHREGNO        varchar(8) not null, \
    GRD_FLG         varchar(1), \
    PAY_FLG         varchar(1), \
    REQUIRE_FLG     varchar(1), \
    GET_CREDITS     smallint, \
    SPECIAL_COUNT   smallint, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_GRADUATE_DAT  \
add constraint PK_REC_GRADUATE \
primary key  \
(SCHREGNO)
