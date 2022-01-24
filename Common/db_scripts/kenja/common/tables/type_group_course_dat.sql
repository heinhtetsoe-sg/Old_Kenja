-- kanji=����
-- $Id: 6b5d12ff9c4228535e0a5653cd4e6b59e581370c $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--

drop table TYPE_GROUP_COURSE_DAT

create table TYPE_GROUP_COURSE_DAT \
    (YEAR               varchar(4)    not null, \
     TYPE_GROUP_CD      varchar(6)    not null, \
     GRADE              varchar(2)    not null, \
     COURSECD           varchar(1)    not null, \
     MAJORCD            varchar(3)    not null, \
     COURSECODE         varchar(4)    not null, \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table TYPE_GROUP_COURSE_DAT add constraint PK_TYPE_GROUP_C_D primary key (YEAR, TYPE_GROUP_CD, GRADE, COURSECD, MAJORCD, COURSECODE)
