-- kanji=����
-- $Id: shamexamination_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback

drop table SHAMEXAMINATION_DAT

create table SHAMEXAMINATION_DAT \
	(YEAR                varchar(4)	not null, \
	 SHAMEXAMCD          varchar(2) not null, \
	 SCHREGNO            varchar(8) not null, \
	 SUBCLASSCD          varchar(6)	not null, \
	 SUBCLASSNAME        varchar(30), \
	 SUBCLASSCD_CNT      varchar(4), \
     TRADE               varchar(90), \
	 SCORE               decimal (4,1), \
	 DEVIATION           decimal (4,1), \
	 SCHOOL_DEVIATION    decimal (4,1), \
	 PRECEDENCE          integer, \
	 SCHOOL_PRECEDENCE   integer, \
     REGISTERCD          varchar(8), \
	 UPDATED             timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table SHAMEXAMINATION_DAT add constraint pk_shamexam_dat primary key (YEAR, SHAMEXAMCD, SCHREGNO, SUBCLASSCD)


