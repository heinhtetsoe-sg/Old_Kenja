-- kanji=����
-- $Id: htrainremark_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table HTRAINREMARK_DAT

create table HTRAINREMARK_DAT \
	(YEAR                 varchar(4) not null, \
	 SCHREGNO             varchar(8) not null, \
	 ANNUAL               varchar(2) not null, \
	 SPECIALACTREMARK     varchar(208), \
	 TOTALREMARK          varchar(802), \
	 ATTENDREC_REMARK     varchar(122), \
	 REGISTERCD           varchar(8), \
	 UPDATED              timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table HTRAINREMARK_DAT \
add constraint PK_HTRAINREMARK \
primary key \
(YEAR,SCHREGNO)
