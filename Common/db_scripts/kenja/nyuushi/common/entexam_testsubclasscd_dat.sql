-- kanji=����
-- $Id: 15f4045970f319a2f8c3497dc9eb73a5fd083e74 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ENTEXAM_TESTSUBCLASSCD_DAT
create table ENTEXAM_TESTSUBCLASSCD_DAT(  \
    ENTEXAMYEAR         varchar(4)   not null, \
    APPLICANTDIV        varchar(1)   not null, \
    TESTDIV             varchar(2)   not null, \
    EXAM_TYPE           varchar(2)   not null, \
    TESTSUBCLASSCD      varchar(2)   not null, \
    LINK_JUDGE_DIV      varchar(1)   ,  \
    TESTSUBCLASS_NAME   varchar(60)  ,  \
    TESTSUBCLASS_ABBV   varchar(30)  ,  \
    PERFECT             smallint     ,  \
    REMARK1             varchar(150) ,  \
    REMARK2             varchar(150) ,  \
    REMARK3             varchar(150) ,  \
    REGISTERCD          varchar(10)  ,  \
    UPDATED             timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ENTEXAM_TESTSUBCLASSCD_DAT add constraint PK_TESTSUBCLASSCD_D primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, TESTSUBCLASSCD)
