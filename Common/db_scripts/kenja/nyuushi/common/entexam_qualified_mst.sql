-- kanji=����
-- $Id: a0d8f4726ae6da1eb1acbdeccbc78fcb799d1807 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table ENTEXAM_QUALIFIED_MST

create table ENTEXAM_QUALIFIED_MST(  \
    ENTEXAMYEAR          varchar(4)   not null, \
    APPLICANTDIV         varchar(1)   not null, \
    QUALIFIED_CD         varchar(2)   not null, \
    QUALIFIED_JUDGE_CD   varchar(2)   not null, \
    QUALIFIED_NAME       varchar(300) , \
    QUALIFIED_ABBV       varchar(90)  , \
    PLUS_POINT           decimal(4, 1) , \
    REGISTERCD           varchar(10)  ,  \
    UPDATED              timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ENTEXAM_QUALIFIED_MST add constraint PK_ENTEXAM_QUALIFIED_M primary key (ENTEXAMYEAR, APPLICANTDIV, QUALIFIED_CD, QUALIFIED_JUDGE_CD)
