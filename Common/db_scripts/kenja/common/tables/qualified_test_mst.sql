-- kanji=漢字
-- $Id: a239f523290d2b385687086c2e36498736191763 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table QUALIFIED_TEST_MST

create table QUALIFIED_TEST_MST \
(  \
    YEAR                VARCHAR(4)  not null, \
    QUALIFIED_CD        VARCHAR(4)  not null, \
    TEST_CD             VARCHAR(4)  not null, \
    TEST_NAME           VARCHAR(60), \
    TEST_NAME_ABBV      VARCHAR(50), \
    PREREQ_RESALT_CD    VARCHAR(4), \
    TEST_FEE            INTEGER, \
    TEST_LEVEL          INTEGER, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table QUALIFIED_TEST_MST add constraint PK_QUALIFIED_TE_M \
primary key (YEAR, QUALIFIED_CD, TEST_CD)
