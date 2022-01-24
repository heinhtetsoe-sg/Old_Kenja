-- kanji=����
-- $Id: 776c1c23e5afee5a8a5d4b39be22f4507493d0ef $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table BEHAVIOR_SEMES_DAT

create table BEHAVIOR_SEMES_DAT \
	(YEAR                 varchar(4) not null, \
	 SEMESTER             varchar(1) not null, \
	 SCHREGNO             varchar(8) not null, \
	 CODE                 varchar(2) not null, \
	 RECORD               varchar(1), \
	 REGISTERCD           varchar(8), \
	 UPDATED              timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table BEHAVIOR_SEMES_DAT \
add constraint PK_BEHAVIOR_SEMES \
primary key \
(YEAR, SEMESTER, SCHREGNO, CODE)
