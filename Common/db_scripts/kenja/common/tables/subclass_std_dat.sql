-- kanji=漢字
-- $Id: ad5c0a7df84885cd06d070008247fbdbc9aa16c1 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

DROP TABLE SUBCLASS_STD_DAT

CREATE TABLE SUBCLASS_STD_DAT \
      (YEAR          VARCHAR(4)      NOT NULL, \
       SEMESTER      VARCHAR(1)      NOT NULL, \
       CLASSCD       VARCHAR(2)      NOT NULL, \
       CURRICULUM_CD VARCHAR(1)      NOT NULL, \
       SUBCLASSCD    VARCHAR(6)      NOT NULL, \
       SCHREGNO      VARCHAR(8)      NOT NULL, \
       REGISTERCD    VARCHAR(8), \
       UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SUBCLASS_STD_DAT ADD CONSTRAINT PK_SBCLS_STD_DAT PRIMARY KEY \
      (YEAR,SEMESTER,CLASSCD,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)

