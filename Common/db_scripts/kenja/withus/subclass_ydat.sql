-- kanji=����
-- $Id: subclass_ydat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--

drop table SUBCLASS_YDAT

create table SUBCLASS_YDAT \
      (YEAR             varchar(4)      not null, \
       CLASSCD          varchar(2)      not null, \
       CURRICULUM_CD    varchar(1)      not null, \
       SUBCLASSCD       varchar(6)      not null, \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SUBCLASS_YDAT add constraint PK_SUBCLASS_YDAT primary key \
    (YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD)
