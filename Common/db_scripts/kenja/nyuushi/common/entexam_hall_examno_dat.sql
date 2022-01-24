-- kanji=����
-- $Id: ac0697027bb9078a9a4a44cd012ae111944be4f8 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ENTEXAM_HALL_EXAMNO_DAT

create table ENTEXAM_HALL_EXAMNO_DAT(  \
    ENTEXAMYEAR          varchar(4)   not null, \
    APPLICANTDIV         varchar(1)   not null, \
    TESTDIV              varchar(2)   not null, \
    EXAMHALLCD           varchar(4)   not null, \
    EXAMHALL_DIV         varchar(1)   not null, \
    EXAMNO               varchar(10)  not null, \
    REGISTERCD           varchar(10)  ,  \
    UPDATED              timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ENTEXAM_HALL_EXAMNO_DAT add constraint PK_ENTEXAM_HALL_EXAMNO_D primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAMHALLCD, EXAMHALL_DIV, EXAMNO)
