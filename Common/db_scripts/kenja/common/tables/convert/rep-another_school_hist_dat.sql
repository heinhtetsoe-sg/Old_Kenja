-- kanji=漢字
-- $Id: d4a28f33d230df44f723f5c289ef4f13f54363f3 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ANOTHER_SCHOOL_HIST_DAT_OLD

RENAME TABLE ANOTHER_SCHOOL_HIST_DAT TO ANOTHER_SCHOOL_HIST_DAT_OLD

CREATE TABLE ANOTHER_SCHOOL_HIST_DAT ( \
    SCHREGNO                VARCHAR(8) NOT NULL, \
    SEQ                     SMALLINT NOT NULL, \
    STUDENT_DIV             VARCHAR(1), \
    FORMER_REG_SCHOOLCD     VARCHAR(12), \
    MAJOR_NAME              VARCHAR(120), \
    REGD_S_DATE             DATE, \
    REGD_E_DATE             DATE, \
    PERIOD_MONTH_CNT        VARCHAR(2), \
    ABSENCE_CNT             VARCHAR(2), \
    MONTH_CNT               VARCHAR(2), \
    ENT_FORM                VARCHAR(1), \
    REASON                  VARCHAR(150), \
    ANOTHER_SPORT           VARCHAR(1), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ANOTHER_SCHOOL_HIST_DAT ADD CONSTRAINT PK_ANOTHER_HIST PRIMARY KEY (SCHREGNO, SEQ)

INSERT INTO ANOTHER_SCHOOL_HIST_DAT \
    SELECT \
        SCHREGNO, \
        SEQ, \
        STUDENT_DIV, \
        FORMER_REG_SCHOOLCD, \
        MAJOR_NAME, \
        REGD_S_DATE, \
        REGD_E_DATE, \
        PERIOD_MONTH_CNT, \
        ABSENCE_CNT, \
        MONTH_CNT, \
        ENT_FORM, \
        REASON, \
        CAST(NULL AS VARCHAR(1)) AS ANOTHER_SPORT, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ANOTHER_SCHOOL_HIST_DAT_OLD
