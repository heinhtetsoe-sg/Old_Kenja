-- kanji=漢字
-- $Id: 53389cfa5288d7c5e9c0c162e5f4cbd1aee85088 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE COURSE_COUNSELING_DAT

CREATE TABLE COURSE_COUNSELING_DAT \
(  \
    ENTRYDATE         DATE        NOT NULL, \
    SEQ               INTEGER     NOT NULL, \
    SCHREGNO          VARCHAR(8)  NOT NULL, \
    TITLE             VARCHAR(150), \
    STAFFCD           VARCHAR(8), \
    CONTENTS          VARCHAR(1050), \
    YEAR              VARCHAR(4)  NOT NULL, \
    REGISTERCD        VARCHAR(8), \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COURSE_COUNSELING_DAT ADD CONSTRAINT PK_COURSE_CNSL_DAT \
PRIMARY KEY (ENTRYDATE,SEQ,SCHREGNO)
