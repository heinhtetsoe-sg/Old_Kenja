-- kanji=����
-- $Id: pref_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table PREF_MST

create table PREF_MST \
	(PREF_CD    varchar(2)  not null, \
     PREF_NAME  varchar(12) not null, \
     REGISTERCD varchar(8), \
     UPDATED    timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table PREF_MST add constraint PK_PREF_MST primary key \
      (PREF_CD)
