-- kanji=漢字
-- $Id: 8c06dc862e717b742dfb2004e9621a7c10d5cea1 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table RECORD_MOCK_ORDER_DAT

create table RECORD_MOCK_ORDER_DAT \
    (YEAR           varchar(4) not null, \
     GRADE          varchar(2) not null, \
     SEQ            smallint not null, \
     TEST_DIV       varchar(1) not null, \
     SEMESTER       varchar(1), \
     TESTKINDCD     varchar(2), \
     TESTITEMCD     varchar(2), \
     MOCKCD         varchar(9), \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table RECORD_MOCK_ORDER_DAT add constraint PK_REC_MOCK_ORDER primary key (YEAR, GRADE, SEQ)


