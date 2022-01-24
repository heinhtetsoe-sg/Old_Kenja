-- kanji=漢字
-- $Id: 58066b36ecafbbbcfe09e5e589e4203a1c07598c $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

DROP TABLE SPECIALACT_ATTEND_DAT_OLD
RENAME TABLE SPECIALACT_ATTEND_DAT TO SPECIALACT_ATTEND_DAT_OLD

CREATE TABLE SPECIALACT_ATTEND_DAT \
    ( YEAR            VARCHAR(4) NOT NULL, \
      SEMESTER        VARCHAR(1) NOT NULL, \
      SCHREGNO        VARCHAR(8) NOT NULL, \
      CLASSCD         VARCHAR(2) NOT NULL, \
      SCHOOL_KIND     VARCHAR(2) NOT NULL, \
      CURRICULUM_CD   VARCHAR(2) NOT NULL, \
      SUBCLASSCD      VARCHAR(6) NOT NULL, \
      ATTENDDATE      DATE       NOT NULL, \
      PERIODF         VARCHAR(1) NOT NULL, \
      PERIODT         VARCHAR(1) NOT NULL, \
      CHAIRCD         VARCHAR(7), \
      CREDIT_TIME     DECIMAL(3,1), \
      REMARK          VARCHAR(90), \
      REGISTERCD      VARCHAR(8), \
      UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

alter table SPECIALACT_ATTEND_DAT add constraint PK_SPECIALACT_ATTE \
primary key (YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ATTENDDATE, PERIODF)

INSERT INTO SPECIALACT_ATTEND_DAT \
     SELECT \
         YEAR, \
         SEMESTER, \
         SCHREGNO, \
         LEFT(SUBCLASSCD, 2) AS CLASSCD, \
         'H' AS SCHOOL_KIND, \
         '2' AS CURRICULUM_CD, \
         SUBCLASSCD, \
         ATTENDDATE, \
         PERIODF, \
         PERIODT, \
         CHAIRCD, \
         CREDIT_TIME, \
         REMARK, \
         REGISTERCD, \
         UPDATED \
     FROM \
        SPECIALACT_ATTEND_DAT_OLD
