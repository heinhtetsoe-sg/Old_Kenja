-- kanji=����
-- $Id: w_rateassesshist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
-- ɾ�껻������ǡ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table W_RATEASSESSHIST_DAT

create table W_RATEASSESSHIST_DAT ( \
    YEAR               varchar(4) not null, \
    SEMESTER           varchar(1) not null, \
    GRADE              varchar(2) not null, \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp default current timestamp, \
    primary key ( YEAR,SEMESTER,GRADE ) \
) in usr1dms index in idx1dms

