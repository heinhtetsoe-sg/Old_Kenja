-- kanji=����
-- $Id: class_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--

drop table CLASS_MST

create table CLASS_MST \
      (CLASSCD         varchar(2)      not null, \
       CLASSNAME       varchar(30), \
       CLASSABBV       varchar(15), \
       CLASSNAME_ENG   varchar(40), \
       CLASSABBV_ENG   varchar(30), \
       CLASSORDERNAME1 varchar(60), \
       CLASSORDERNAME2 varchar(60), \
       CLASSORDERNAME3 varchar(60), \
       SHOWORDER       smallint, \
       SHOWORDER2      smallint, \
       SHOWORDER3      smallint, \
       INOUT_DIV       varchar(1), \
       REGISTERCD      varchar(8), \
       UPDATED         timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CLASS_MST add constraint PK_CLASS_MST primary key (CLASSCD)


