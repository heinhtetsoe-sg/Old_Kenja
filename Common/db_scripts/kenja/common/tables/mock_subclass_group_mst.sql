-- kanji=漢字
-- $Id: 33e44ccf679dca7869cf652fce539305f3c88758 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MOCK_SUBCLASS_GROUP_MST

create table MOCK_SUBCLASS_GROUP_MST ( \
    YEAR        varchar(4) not null, \
    MOCKCD      varchar(9) not null, \
    GROUP_DIV   varchar(2) not null, \
    GRADE       varchar(2) not null, \
    COURSECD    varchar(1) not null, \
    MAJORCD     varchar(3) not null, \
    COURSECODE  varchar(4) not null, \
    GROUP_NAME  varchar(30), \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp \
)  in usr1dms index in idx1dms

alter table MOCK_SUBCLASS_GROUP_MST add constraint PK_MOCK_SUB_GP_MST \
primary key (YEAR, MOCKCD, GROUP_DIV, GRADE, COURSECD, MAJORCD, COURSECODE)

