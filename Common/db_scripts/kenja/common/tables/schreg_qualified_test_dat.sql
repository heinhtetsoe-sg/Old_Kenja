-- kanji=漢字
-- $Id: 7487852c44e6a88d8d532c16445e851df626bc1e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHREG_QUALIFIED_TEST_DAT

create table SCHREG_QUALIFIED_TEST_DAT \
(  \
    YEAR                VARCHAR(4)  not null, \
    SCHREGNO            VARCHAR(8)  not null, \
    TEST_DATE           DATE        not null, \
    QUALIFIED_CD        VARCHAR(4)  not null, \
    TEST_CD             VARCHAR(4)  not null, \
    RESULT_CD           VARCHAR(4), \
    LIMITED_DATE        DATE, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_QUALIFIED_TEST_DAT add constraint PK_SCH_QUAL_TES_D \
primary key (YEAR, SCHREGNO, TEST_DATE, QUALIFIED_CD, TEST_CD)
