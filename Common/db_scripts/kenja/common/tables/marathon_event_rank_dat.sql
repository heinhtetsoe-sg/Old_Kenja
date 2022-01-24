-- kanji=����
-- $Id: 2c4852a2d86bb92fed40ef2f161a835591d20386 $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table MARATHON_EVENT_RANK_DAT

create table MARATHON_EVENT_RANK_DAT ( \
    YEAR                varchar(4)  not null, \
    SEQ                 varchar(2)  not null, \
    SCHREGNO            varchar(8)  not null, \
    SEX                 varchar(1), \
    TIME_H              smallint, \
    TIME_M              smallint, \
    TIME_S              smallint, \
    ATTEND_CD           varchar(2), \
    GRADE_RANK          smallint, \
    GRADE_RANK_SEX      smallint, \
    SCHOOL_RANK         smallint, \
    SCHOOL_RANK_SEX     smallint, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MARATHON_EVENT_RANK_DAT add constraint PK_MARATHON_EVENT_RANK_DAT primary key (YEAR, SEQ, SCHREGNO)
