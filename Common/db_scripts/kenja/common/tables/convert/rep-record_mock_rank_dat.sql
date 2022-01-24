-- kanji=漢字
-- $Id: 7bdd0c618c0a3a96034d954a7d5e9111673b8e4f $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
      
drop table RECORD_MOCK_RANK_DAT_OLD
create table RECORD_MOCK_RANK_DAT_OLD like RECORD_MOCK_RANK_DAT
insert into  RECORD_MOCK_RANK_DAT_OLD select * from RECORD_MOCK_RANK_DAT

drop   table RECORD_MOCK_RANK_DAT
create table RECORD_MOCK_RANK_DAT ( \
       YEAR           VARCHAR(4) NOT NULL, \
       SCHREGNO       VARCHAR(8) NOT NULL, \
       DATA_DIV       VARCHAR(1) NOT NULL, \
       COURSE_DIV     VARCHAR(1) NOT NULL, \
       CLASSCD        VARCHAR(2) NOT NULL, \
       SCHOOL_KIND    VARCHAR(2) NOT NULL, \
       CURRICULUM_CD  VARCHAR(2) NOT NULL, \
       SUBCLASSCD     VARCHAR(6) NOT NULL, \
       GRADE          VARCHAR(2) NOT NULL, \
       SCORE1         SMALLINT, \
       SCORE2         SMALLINT, \
       SCORE3         SMALLINT, \
       RANK1          SMALLINT, \
       RANK2          SMALLINT, \
       RANK           SMALLINT, \
       PERFECT1       SMALLINT, \
       PERFECT2       SMALLINT, \
       PERFECT3       SMALLINT, \
       REGISTERCD     VARCHAR(8), \
       UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table RECORD_MOCK_RANK_DAT add constraint pk_rec_mock_rank \
      primary key (YEAR, SCHREGNO, DATA_DIV, COURSE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)

insert into RECORD_MOCK_RANK_DAT \
    SELECT \
        YEAR, \
        SCHREGNO, \
        DATA_DIV, \
        COURSE_DIV, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        GRADE, \
        SCORE1, \
        SCORE2, \
        SCORE3, \
        RANK1, \
        RANK2, \
        RANK, \
        PERFECT1, \
        PERFECT2, \
        PERFECT3, \
        REGISTERCD, \
        UPDATED \
    FROM \
        RECORD_MOCK_RANK_DAT_OLD
