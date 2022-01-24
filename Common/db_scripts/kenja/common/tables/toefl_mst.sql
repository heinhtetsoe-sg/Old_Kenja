-- kanji=����
-- $Id: 0503937677a9942ef4d523bf1193aec45bb6f55f $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table TOEFL_MST

create table TOEFL_MST ( \
    YEAR                           varchar(4)  not null, \
    BASE_SCORE                     integer     , \
    RANGE_F                        integer     , \
    RANGE_T                        integer     , \
    REGISTERCD                     varchar(10) , \
    UPDATED                        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table TOEFL_MST add constraint PK_TOEFL_M primary key (YEAR)
