-- kanji=漢字
-- $Id: ea610e0aa42937102563eb611c0fc4a3666d605e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ACTION_DOCUMENT_DAT

create table ACTION_DOCUMENT_DAT \
(  \
        "SCHREGNO"              varchar(8) not null, \
        "ACTIONDATE"            date       not null, \
        "SEQ"                   smallint   not null, \
        "ACTIONTIME"            time , \
        "STAFFCD"               varchar(10), \
        "DIVIDECD"              varchar(2) , \
        "TITLE"                 varchar(120) , \
        "TEXT"                  varchar(700) , \
        "PRIVATE"               varchar(1) , \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table ACTION_DOCUMENT_DAT  \
add constraint PK_ACTION_DOC_DAT  \
primary key  \
(SCHREGNO, ACTIONDATE, SEQ)
