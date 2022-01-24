-- kanji=����
-- $Id: staff_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--

drop table STAFF_MST

create table STAFF_MST \
      (STAFFCD           varchar(8)      not null, \
       STAFFNAME         varchar(60), \
       STAFFNAME_SHOW    varchar(15), \
       STAFFNAME_KANA    varchar(120), \
       STAFFNAME_ENG     varchar(60), \
       BELONGING_DIV     varchar(3), \
       JOBCD             varchar(4), \
       SECTIONCD         varchar(4), \
       DUTYSHARECD       varchar(4), \
       CHARGECLASSCD     varchar(1), \
       STAFFSEX          varchar(1), \
       STAFFBIRTHDAY     date, \
       STAFFZIPCD        varchar(8), \
       STAFFPREF_CD      varchar(2), \
       STAFFADDR1        varchar(75), \
       STAFFADDR2        varchar(75), \
       STAFFADDR3        varchar(75), \
       STAFFTELNO        varchar(14), \
       STAFFTELNO_SEARCH varchar(14), \
       STAFFFAXNO        varchar(14), \
       STAFFE_MAIL       varchar(25), \
       REGISTERCD        varchar(8), \
       UPDATED           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table STAFF_MST add constraint PK_STAFF_MST primary key (STAFFCD)

