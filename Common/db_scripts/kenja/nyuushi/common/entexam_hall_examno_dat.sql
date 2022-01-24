-- kanji=漢字
-- $Id: ac0697027bb9078a9a4a44cd012ae111944be4f8 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
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
