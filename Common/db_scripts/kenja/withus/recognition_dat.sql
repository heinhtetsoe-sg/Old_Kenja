-- kanji=����
-- $Id: recognition_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
--drop table RECOGNITION_DAT

--create table RECOGNITION_DAT \
--    (APPLICANTNO              varchar(4) not null, \
--    RECOGNITION_CLASSCD       varchar(2) not null, \
--    RECOGNITION_SUBCLASSCD    varchar(6) not null, \
--    RECOGNITION_SUBCLASSNAME  varchar(60), \
--    RECOGNITION_SUBCLASSABBV  varchar(15), \
--    SUBCLASSCD                varchar(6), \
--    RECOGNITION_CREDIT        varchar(2), \
--    REGISTERCD                varchar(8), \
--    UPDATED                   timestamp default current timestamp \
--      ) in usr1dms index in idx1dms

--alter table RECOGNITION_DAT add constraint PK_RECOGNITION_DAT primary key \
--      (APPLICANTNO, RECOGNITION_CLASSCD)
