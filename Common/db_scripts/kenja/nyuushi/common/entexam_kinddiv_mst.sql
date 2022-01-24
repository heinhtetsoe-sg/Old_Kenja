-- kanji=漢字
-- $Id: d7729fd383e26e8d2478615c6f86b436f920b7af $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
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
