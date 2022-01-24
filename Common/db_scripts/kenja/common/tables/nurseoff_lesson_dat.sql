-- $Id: 5444c5d96142e6e74621e99e9795ca1300a8963b $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
-- ���ی������p�󋵕\�p���Ɠ����o�^�f�[�^

drop table NURSEOFF_LESSON_DAT \

create table NURSEOFF_LESSON_DAT \
( \
    YEAR                  varchar(4) not null , \
    MONTH                 varchar(2) not null , \
    LESSON                varchar(2) , \
    REGISTERCD            varchar(10) , \
    UPDATED               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table NURSEOFF_LESSON_DAT add constraint PK_NURSEOFF_LES_D primary key (YEAR, MONTH)
