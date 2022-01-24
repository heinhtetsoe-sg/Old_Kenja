-- kanji=漢字
-- $Id: 6e9558fbe6b1722b67927704cc6b1a5abfee5418 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

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
