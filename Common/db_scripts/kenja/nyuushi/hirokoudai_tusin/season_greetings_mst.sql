-- kanji=����
-- $Id: ccf89d24654664a6a19309d8d99ceac62ba7b94d $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
-- 2018/11/01���ߡ�ALP��΢����ǡ����򥻥åȤ��롣

drop table SEASON_GREETINGS_MST

create table SEASON_GREETINGS_MST \
(  \
    MONTH           varchar(2)  not null, \
    SEQ             varchar(3)  not null, \
    GREETING        varchar(90), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SEASON_GREETINGS_MST add constraint PK_SEASON_GREET_M \
primary key (MONTH, SEQ)
