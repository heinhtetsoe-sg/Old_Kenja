-- kanji=����
-- $Id: reduction_max_money_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ��ǯ�٥ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table REDUCTION_MAX_MONEY_DAT

create table REDUCTION_MAX_MONEY_DAT ( \
    YEAR               varchar(4) not null, \
    PREFECTURESCD      varchar(2) not null, \
    GRADE              varchar(2) not null, \
    RANK_DIV           varchar(2) not null, \
    REDUCTIONMONEY_1   integer, \
    REDUCTIONMONEY_2   integer, \
    MAX_MONEY          integer, \
    MIN_MONEY_1        integer, \
    MIN_MONEY_2        integer, \
    MIN_MONEY          integer, \
    PARENTS_MONEY_1    integer, \
    PARENTS_MONEY_2    integer, \
    MIN_MONEY_2        integer, \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_MAX_MONEY_DAT add constraint PK_REDUC_MAX_DAT primary key (YEAR, PREFECTURESCD, GRADE, RANK_DIV)
