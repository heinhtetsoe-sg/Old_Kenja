-- kanji=����
-- $Id: 6e9558fbe6b1722b67927704cc6b1a5abfee5418 $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback

drop table ENTEXAM_TESTDIV_MST_OLD
create table ENTEXAM_TESTDIV_MST_OLD like ENTEXAM_TESTDIV_MST
insert into ENTEXAM_TESTDIV_MST_OLD select * from ENTEXAM_TESTDIV_MST

drop table ENTEXAM_TESTDIV_MST

create table ENTEXAM_TESTDIV_MST( \
    ENTEXAMYEAR     varchar(4)   not null, \
    APPLICANTDIV    varchar(1)   not null, \
    TESTDIV         varchar(2)   not null, \
    TESTDIV_NAME    varchar(30)  not null, \
    REGISTERCD      varchar(10),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_TESTDIV_MST \
add constraint PK_ENT_TESTDIV_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV)

insert into ENTEXAM_TESTDIV_MST \
select \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        '0' || TESTDIV, \
        TESTDIV_NAME, \
        REGISTERCD, \
        UPDATED \
from ENTEXAM_TESTDIV_MST_OLD
