-- kanji=����
-- $Id: subclass_replace_combined_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--

drop table SUBCLASS_REPLACE_COMBINED_DAT

create table SUBCLASS_REPLACE_COMBINED_DAT \
    (REPLACECD                varchar(1) not null, \
     YEAR                     varchar(4) not null, \
     COMBINED_CLASSCD         varchar(2) not null, \
     COMBINED_CURRICULUM_CD   varchar(1) not null, \
     COMBINED_SUBCLASSCD      varchar(6) not null, \
     ATTEND_CLASSCD           varchar(2) not null, \
     ATTEND_CURRICULUM_CD     varchar(1) not null, \
     ATTEND_SUBCLASSCD        varchar(6) not null, \
     CALCULATE_CREDIT_FLG     varchar(1) , \
     STUDYREC_CREATE_FLG      varchar(1) , \
     PRINT_FLG1               varchar(1) , \
     PRINT_FLG2               varchar(1) , \
     PRINT_FLG3               varchar(1) , \
     REGISTERCD               varchar(8), \
     UPDATED                  timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SUBCLASS_REPLACE_COMBINED_DAT add constraint PK_SUBREPCOMB primary key (YEAR,COMBINED_CLASSCD,COMBINED_CURRICULUM_CD,COMBINED_SUBCLASSCD,ATTEND_CLASSCD,ATTEND_CURRICULUM_CD,ATTEND_SUBCLASSCD)
