-- kanji=漢字
-- $Id: db7f061816994ec31f10eef2d194fe6281d96088 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table ENTEXAM_RECEPT_DETAIL_DAT_OLD
create table ENTEXAM_RECEPT_DETAIL_DAT_OLD like ENTEXAM_RECEPT_DETAIL_DAT
insert into ENTEXAM_RECEPT_DETAIL_DAT_OLD select * from ENTEXAM_RECEPT_DETAIL_DAT

drop table ENTEXAM_RECEPT_DETAIL_DAT
create table ENTEXAM_RECEPT_DETAIL_DAT( \
    ENTEXAMYEAR               varchar(4)    not null, \
    APPLICANTDIV              varchar(1)    not null, \
    TESTDIV                   varchar(2)    not null, \
    EXAM_TYPE                 varchar(2)    not null, \
    RECEPTNO                  varchar(20)   not null, \
    SEQ                       varchar(3)    not null, \
    REMARK1                   varchar(150), \
    REMARK2                   varchar(150), \
    REMARK3                   varchar(150), \
    REMARK4                   varchar(150), \
    REMARK5                   varchar(150), \
    REMARK6                   varchar(150), \
    REMARK7                   varchar(150), \
    REMARK8                   varchar(150), \
    REMARK9                   varchar(150), \
    REMARK10                  varchar(150), \
    REGISTERCD                varchar(10), \
    UPDATED                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_RECEPT_DETAIL_DAT add constraint PK_ENTEXAM_RCPTD primary key (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO, SEQ)

insert into ENTEXAM_RECEPT_DETAIL_DAT \
select \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        '0' || TESTDIV, \
        EXAM_TYPE, \
        RECEPTNO, \
        SEQ, \
        REMARK1, \
        REMARK2, \
        REMARK3, \
        REMARK4, \
        REMARK5, \
        REMARK6, \
        REMARK7, \
        REMARK8, \
        REMARK9, \
        REMARK10, \
        REGISTERCD, \
        UPDATED \
from ENTEXAM_RECEPT_DETAIL_DAT_OLD
