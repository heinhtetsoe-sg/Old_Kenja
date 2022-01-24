-- kanji=����
-- $Id: 660268ea40edc4e5518cb3d1935719133430a16c $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table MARATHON_EVENT_MST

create table MARATHON_EVENT_MST ( \
    YEAR                varchar(4)  not null, \
    SEQ                 varchar(2)  not null, \
    NUMBER_OF_TIMES     varchar(15), \
    EVENT_NAME          varchar(30), \
    EVENT_DATE          date, \
    MAN_METERS          DECIMAL(6, 3), \
    WOMEN_METERS        DECIMAL(6, 3), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MARATHON_EVENT_MST add constraint PK_MARATHON_EVENT_MST primary key (YEAR, SEQ)
