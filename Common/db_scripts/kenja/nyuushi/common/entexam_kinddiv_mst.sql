-- kanji=����
-- $Id: d7729fd383e26e8d2478615c6f86b436f920b7af $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ENTEXAM_KINDDIV_MST

create table ENTEXAM_KINDDIV_MST(  \
    ENTEXAMYEAR         varchar(4)   not null, \
    APPLICANTDIV        varchar(1)   not null, \
    KINDDIV             varchar(2)   not null, \
    KINDDIV_NAME        varchar(60)  , \
    KINDDIV_ABBV        varchar(30)  , \
    REGISTERCD          varchar(10)  ,  \
    UPDATED             timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ENTEXAM_KINDDIV_MST add constraint PK_ENTEXAM_KINDDIV_M primary key (ENTEXAMYEAR, APPLICANTDIV, KINDDIV)
