-- kanji=����
-- $Id: 0811a12b9d57abe2bfaa3bc2fd29ead0562ecf06 $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback


drop table SUBCLASS_RATE_SEMES_DAT

create table SUBCLASS_RATE_SEMES_DAT \
( \
    YEAR            varchar(4)    not null, \
    CLASSCD         varchar(2)    not null, \
    SCHOOL_KIND     varchar(2)    not null, \
    CURRICULUM_CD   varchar(2)    not null, \
    SUBCLASSCD      varchar(6)    not null, \
    SEMESTER        varchar(1)    not null, \
    RATE            smallint, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_RATE_SEMES_DAT \
add constraint PK_SUBCLASS_RATE_SEMES_D \
primary key \
(YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEMESTER)
