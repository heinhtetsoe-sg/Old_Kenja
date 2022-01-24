-- kanji=����
-- $Id: 4b41d9594c7d85ee7479c44af34ef66aded3a5ea $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback

drop table CERTIF_KIND_YDAT

create table CERTIF_KIND_YDAT \
      (YEAR             varchar(4)      not null, \
       CERTIF_KINDCD    varchar(3)      not null, \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CERTIF_KIND_YDAT add constraint PK_CERTIFKIND_YDAT primary key \
      (YEAR, CERTIF_KINDCD)


