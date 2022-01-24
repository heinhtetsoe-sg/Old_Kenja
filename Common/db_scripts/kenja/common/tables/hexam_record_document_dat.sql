-- kanji=����
-- $Id: 9565e23c622b5777af7b73fc07b77ffda97e1635 $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--

drop table HEXAM_RECORD_DOCUMENT_DAT

create table HEXAM_RECORD_DOCUMENT_DAT \
    (YEAR           varchar(4)    not null, \
     SEMESTER       varchar(1)    not null, \
     GRADE          varchar(2)    not null, \
     TYPE_GROUP_CD  varchar(6)    not null, \
     REMARK_DIV     varchar(1)    not null, \
     REMARK1        varchar(1050), \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HEXAM_RECORD_DOCUMENT_DAT add constraint PK_HEX_REC_DOC primary key (YEAR, SEMESTER, GRADE, TYPE_GROUP_CD, REMARK_DIV)
