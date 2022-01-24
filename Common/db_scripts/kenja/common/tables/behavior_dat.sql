-- kanji=����
-- $Id: fd42a2a227da4e3e3f1743a6e05ea3e761c2f530 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table BEHAVIOR_DAT

create table BEHAVIOR_DAT \
	(YEAR                 varchar(4) not null, \
	 SCHREGNO             varchar(8) not null, \
	 DIV                  varchar(1) not null, \
	 CODE                 varchar(2) not null, \
	 ANNUAL               varchar(2) not null, \
	 RECORD               varchar(1), \
	 REGISTERCD           varchar(8), \
	 UPDATED              timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table BEHAVIOR_DAT \
add constraint PK_BEHAVIOR_DAT \
primary key \
(YEAR,SCHREGNO,DIV,CODE)
