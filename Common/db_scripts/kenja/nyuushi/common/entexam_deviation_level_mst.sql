-- kanji=����
-- $Id: 3831c5c8aaaf7595bcc70418c654ee9c983b0ae2 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ENTEXAM_DEVIATION_LEVEL_MST
create table ENTEXAM_DEVIATION_LEVEL_MST \
(  \
    ENTEXAMYEAR         varchar(4)  not null, \
    DEV_CD              varchar(3)  not null, \
    DEV_MARK            varchar(6)          , \
    DEV_LOW             decimal(4, 1)       , \
    DEV_HIGH            decimal(4, 1)       , \
    REGISTERCD          varchar(10)         , \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_DEVIATION_LEVEL_MST add constraint PK_ENTEXAM_DEV_LEVEL_M \
primary key (ENTEXAMYEAR, DEV_CD)
