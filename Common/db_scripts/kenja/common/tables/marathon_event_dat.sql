-- kanji=����
-- $Id: e5da81dd961bf9a519265a4adfd30bb43d81e06c $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table MARATHON_EVENT_DAT

create table MARATHON_EVENT_DAT ( \
    YEAR                varchar(4)  not null, \
    SEQ                 varchar(2)  not null, \
    SCHREGNO            varchar(8)  not null, \
    TIME_H              smallint, \
    TIME_M              smallint, \
    TIME_S              smallint, \
    ATTEND_CD           varchar(2), \
    REMARK              varchar(60), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MARATHON_EVENT_DAT add constraint PK_MARATHON_EVENT_DAT primary key (YEAR, SEQ, SCHREGNO)
