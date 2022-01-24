-- kanji=����
-- $Id: fd151467efe14f6593e89a7bbb8d0bf1737791ed $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback

drop table ENTEXAM_DISTINCTION_MST_OLD
create table ENTEXAM_DISTINCTION_MST_OLD like ENTEXAM_DISTINCTION_MST
insert into ENTEXAM_DISTINCTION_MST_OLD select * from ENTEXAM_DISTINCTION_MST

drop table ENTEXAM_DISTINCTION_MST

create table ENTEXAM_DISTINCTION_MST( \
    ENTEXAMYEAR     varchar(4)   not null, \
    APPLICANTDIV    varchar(1)   not null, \
    DISTINCT_ID     varchar(3)   not null, \
    DISTINCT_NAME   varchar(60)  not null, \
    TESTDIV         varchar(2)   not null, \
    EXAM_TYPE       varchar(2)   not null, \
    TEST_DATE       date         not null, \
    REGISTERCD      varchar(10),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_DISTINCTION_MST \
add constraint PK_ENT_DISTINCT_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, DISTINCT_ID)

insert into ENTEXAM_DISTINCTION_MST \
select \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        DISTINCT_ID, \
        DISTINCT_NAME, \
        '0' || TESTDIV, \
        EXAM_TYPE, \
        TEST_DATE, \
        REGISTERCD, \
        UPDATED \
from ENTEXAM_DISTINCTION_MST_OLD
