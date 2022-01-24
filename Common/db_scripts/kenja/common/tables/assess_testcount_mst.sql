-- kanji=漢字
-- $Id: b2e3ef47c4e00a1b9bc764c513416f54e395667e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ASSESS_TESTCOUNT_MST

create table ASSESS_TESTCOUNT_MST( \
    YEAR           VARCHAR(4)    NOT NULL, \
    ASSESSCD       VARCHAR(1)    NOT NULL, \
    TESTCOUNT      SMALLINT      NOT NULL, \
    CLASSCD        VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND    VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD  VARCHAR(2)    NOT NULL, \
    SUBCLASSCD     VARCHAR(6)    NOT NULL, \
    ASSESSLEVEL    SMALLINT      NOT NULL, \
    ASSESSMARK     VARCHAR(6)   , \
    ASSESSLOW      DECIMAL(4,1) , \
    ASSESSHIGH     DECIMAL(4,1) , \
    REGISTERCD     VARCHAR(10)  , \
    UPDATED        TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table ASSESS_TESTCOUNT_MST add constraint PK_ASSESS_TESTCOUNT_MST \
primary key (YEAR,ASSESSCD,TESTCOUNT,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,ASSESSLEVEL)

