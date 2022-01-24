-- kanji=����
-- $Id: 680c5052cbda57e62ad7ce517ad8de07f743050b $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback

drop table CERTIF_KIND_MST

create table CERTIF_KIND_MST \
      (CERTIF_KINDCD      varchar(3)      not null, \
       KINDNAME           varchar(24), \
       ISSUECD            varchar(1), \
       STUDENTCD          varchar(1), \
       GRADUATECD         varchar(1), \
       DROPOUTCD          varchar(1), \
       ELAPSED_YEARS      SMALLINT, \
       CERTIF_DIV         varchar(2), \
       CERTIF_GRPCD       varchar(3), \
       CURRENT_PRICE      varchar(4), \
       GRADUATED_PRICE    varchar(4), \
       ISSUENO_AUTOFLG    varchar(1), \
       CERTIF_SCHOOL_KIND varchar(2), \
       REGISTERCD         varchar(10), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CERTIF_KIND_MST add constraint PK_CERTKIND_MST primary key \
      (CERTIF_KINDCD)


