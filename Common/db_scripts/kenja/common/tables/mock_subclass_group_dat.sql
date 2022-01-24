-- kanji=漢字
-- $Id: 9af12556d074cb65030ce987e262d4c76d142ef1 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MOCK_SUBCLASS_GROUP_DAT

create table MOCK_SUBCLASS_GROUP_DAT ( \
    YEAR             varchar(4) not null, \
    MOCKCD           varchar(9) not null, \
    GROUP_DIV        varchar(2) not null, \
    GRADE            varchar(2) not null, \
    COURSECD         varchar(1) not null, \
    MAJORCD          varchar(3) not null, \
    COURSECODE       varchar(4) not null, \
    MOCK_SUBCLASS_CD varchar(6) not null, \
    REGISTERCD       varchar(8) , \
    UPDATED          timestamp default current timestamp \
)  in usr1dms index in idx1dms

alter table MOCK_SUBCLASS_GROUP_DAT add constraint PK_MOCK_SUB_GP_DAT \
primary key (YEAR, MOCKCD, GROUP_DIV, GRADE, COURSECD, MAJORCD, COURSECODE, MOCK_SUBCLASS_CD)

