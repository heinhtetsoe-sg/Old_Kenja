-- kanji=漢字
-- $Id: a0d8f4726ae6da1eb1acbdeccbc78fcb799d1807 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
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
