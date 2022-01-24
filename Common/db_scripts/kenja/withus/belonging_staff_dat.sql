-- kanji=����
-- $Id: belonging_staff_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--

drop table BELONGING_STAFF_DAT

create table BELONGING_STAFF_DAT \
      (YEAR             varchar(4)      not null, \
       BELONGING_DIV    varchar(3)      not null, \
       STAFFCD          varchar(8)      not null, \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table BELONGING_STAFF_DAT add constraint PK_BELONGING_STAFF primary key \
      (YEAR, BELONGING_DIV, STAFFCD)


