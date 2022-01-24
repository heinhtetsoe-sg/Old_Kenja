-- kanji=����
-- $Id: cfebd6dafc7bc9c7655254fa474ccfcb7c7d7dea $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--��ڒ����ރ}�X�^

drop table COLLECT_M_MST_OLD

create table COLLECT_M_MST_OLD like COLLECT_M_MST

insert into COLLECT_M_MST_OLD select * from COLLECT_M_MST

drop table COLLECT_M_MST \

create table COLLECT_M_MST \
( \
        "SCHOOLCD"            varchar(12) not null, \
        "SCHOOL_KIND"         varchar(2)  not null, \
        "YEAR"                varchar(4)  not null, \
        "COLLECT_L_CD"        varchar(2)  not null, \
        "COLLECT_M_CD"        varchar(2)  not null, \
        "COLLECT_M_NAME"      varchar(90), \
        "COLLECT_S_EXIST_FLG" varchar(1), \
        "COLLECT_M_MONEY"     integer, \
        "KOUHI_SHIHI"         varchar(1), \
        "GAKUNOKIN_DIV"       varchar(1), \
        "REDUCTION_DIV"       varchar(1), \
        "IS_REDUCTION_SCHOOL" varchar(1), \
        "IS_CREDITCNT"        varchar(1), \
        "IS_REFUND"           varchar(1), \
        "IS_REPAY"            varchar(1), \
        "CLASSCD"             varchar(2), \
        "TEXTBOOKDIV"         varchar(1), \
        "SHOW_ORDER"          varchar(2), \
        "LMS_GRP_CD"          varchar(6), \
        "REMARK"              varchar(60), \
        "DIVIDE_PROCESS"      varchar(1) , \
        "ROUND_DIGIT"         varchar(1) , \
        "REGISTERCD"          varchar(10), \
        "UPDATED"             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_M_MST \
add constraint PK_COL_M_MST \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_L_CD, COLLECT_M_CD)

insert into COLLECT_M_MST select * from COLLECT_M_MST_OLD

