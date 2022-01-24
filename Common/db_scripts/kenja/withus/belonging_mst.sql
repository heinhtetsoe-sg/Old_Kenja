-- kanji=����
-- $Id: belonging_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--

drop   table BELONGING_MST
create table BELONGING_MST \
    (BELONGING_DIV       varchar(3) not null, \
     FOUNDEDYEAR         varchar(4), \
     PRESENT_EST         varchar(3), \
     CLASSIFICATION      varchar(1), \
     SCHOOLNAME1         varchar(90), \
     SCHOOLNAME2         varchar(90), \
     SCHOOLNAME3         varchar(90), \
     SCHOOLNAME_ENG      varchar(60), \
     SCHOOLZIPCD         varchar(8), \
     SCHOOLPREF_CD       varchar(2), \
     SCHOOLADDR1         varchar(75), \
     SCHOOLADDR2         varchar(75), \
     SCHOOLADDR3         varchar(75), \
     SCHOOLADDR1_ENG     varchar(50), \
     SCHOOLADDR2_ENG     varchar(50), \
     SCHOOLTELNO         varchar(14), \
     SCHOOLTELNO_SEARCH  varchar(14), \
     SCHOOLFAXNO         varchar(14), \
     SCHOOLMAIL          varchar(40), \
     FOUNDED_DATE        date, \
     ORDER               smallint, \
     OPEN_DATE           date not null, \
     CLOSE_DATE          date, \
     REGISTERCD          varchar(8), \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table BELONGING_MST add constraint PK_BELONGING_MST primary key (BELONGING_DIV)
