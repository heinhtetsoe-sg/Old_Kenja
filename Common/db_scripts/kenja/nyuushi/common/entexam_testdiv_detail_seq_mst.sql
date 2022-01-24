-- kanji=����
-- $Id: 5ee2544a9315ca5c535d449ba3f93ebb2a88a123 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ENTEXAM_TESTDIV_DETAIL_SEQ_MST

create table ENTEXAM_TESTDIV_DETAIL_SEQ_MST(  \
    ENTEXAMYEAR          varchar(4)   not null, \
    APPLICANTDIV         varchar(1)   not null, \
    TESTDIV              varchar(2)   not null, \
    SEQ                  varchar(3)   not null, \
    REMARK1              varchar(150) , \
    REMARK2              varchar(150) , \
    REMARK3              varchar(150) , \
    REMARK4              varchar(150) , \
    REMARK5              varchar(150) , \
    REMARK6              varchar(150) , \
    REMARK7              varchar(150) , \
    REMARK8              varchar(150) , \
    REMARK9              varchar(150) , \
    REMARK10             varchar(150) , \
    REGISTERCD           varchar(10)  , \
    UPDATED              timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ENTEXAM_TESTDIV_DETAIL_SEQ_MST add constraint PK_ENTEXAM_TESTDIV_DETAIL_SEQ_M primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ)
