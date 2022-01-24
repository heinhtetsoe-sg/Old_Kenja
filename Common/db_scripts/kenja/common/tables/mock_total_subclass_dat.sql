-- kanji=漢字
-- $Id: fdcf22ebedbebd0ae87691828ae357a684d126cd $

-- 支部マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table MOCK_TOTAL_SUBCLASS_DAT

create table MOCK_TOTAL_SUBCLASS_DAT ( \
    YEAR             varchar(4) not null, \
    MOCKCD           varchar(9) not null, \
    COURSECD         varchar(1) not null, \
    MAJORCD          varchar(3) not null, \
    COURSECODE       varchar(4) not null, \
    MOCK_SUBCLASS_CD varchar(6) , \
    REGISTERCD       varchar(10), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_TOTAL_SUBCLASS_DAT add constraint PK_MOCK_TOTAL_SUBCLASS_D primary key (YEAR, MOCKCD, COURSECD, MAJORCD, COURSECODE)
