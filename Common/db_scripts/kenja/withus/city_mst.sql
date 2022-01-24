-- kanji=����
-- $Id: city_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table CITY_MST

create table CITY_MST \
	(PREF_CD        varchar(2)   not null, \
     CITY_CD        varchar(3)   not null, \
     CITY_NAME      varchar(120)  not null, \
     CITY_NAME_KANA varchar(120)  not null, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CITY_MST add constraint PK_CITY_MST primary key \
      (PREF_CD, CITY_CD)
