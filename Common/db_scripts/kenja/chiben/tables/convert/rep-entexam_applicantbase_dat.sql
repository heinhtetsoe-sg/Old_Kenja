-- $Id: rep-entexam_applicantbase_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_APPLICANTBASE_DAT_OLD

RENAME TABLE ENTEXAM_APPLICANTBASE_DAT TO ENTEXAM_APPLICANTBASE_DAT_OLD

CREATE TABLE ENTEXAM_APPLICANTBASE_DAT( \
    ENTEXAMYEAR          VARCHAR(4)    NOT NULL, \
    APPLICANTDIV         VARCHAR(1)    NOT NULL, \
    EXAMNO               VARCHAR(5)    NOT NULL, \
    TESTDIV              VARCHAR(1)    NOT NULL, \
    SHDIV                VARCHAR(1)    NOT NULL, \
    DESIREDIV            VARCHAR(1)    NOT NULL, \
    TESTDIV1             VARCHAR(1), \
    TESTDIV2             VARCHAR(1), \
    TESTDIV3             VARCHAR(1), \
    TESTDIV4             VARCHAR(1), \
    TESTDIV5             VARCHAR(1), \
    RECEPTDATE           DATE, \
    NAME                 VARCHAR(60), \
    NAME_KANA            VARCHAR(120), \
    SEX                  VARCHAR(1), \
    ERACD                VARCHAR(1), \
    BIRTH_Y              VARCHAR(2), \
    BIRTH_M              VARCHAR(2), \
    BIRTH_D              VARCHAR(2), \
    BIRTHDAY             DATE, \
    FS_CD                VARCHAR(7), \
    FS_NAME              VARCHAR(45), \
    FS_AREA_CD           VARCHAR(2), \
    FS_AREA_DIV          VARCHAR(2), \
    FS_NATPUBPRIDIV      VARCHAR(1), \
    FS_GRDYEAR           VARCHAR(4), \
    FS_ERACD             VARCHAR(1), \
    FS_Y                 VARCHAR(2), \
    FS_M                 VARCHAR(2), \
    FS_DAY               DATE, \
    FS_GRDDIV            VARCHAR(1), \
    PRISCHOOLCD          VARCHAR(7), \
    INTERVIEW_ATTEND_FLG VARCHAR(1), \
    SUC_COURSECD         VARCHAR(1), \
    SUC_MAJORCD          VARCHAR(3), \
    SUC_COURSECODE       VARCHAR(4), \
    JUDGEMENT            VARCHAR(1), \
    JUDGE_KIND           VARCHAR(1), \
    SUB_ORDER            VARCHAR(4), \
    SPECIAL_MEASURES     VARCHAR(1), \
    PROCEDUREDIV         VARCHAR(1), \
    PROCEDUREDATE        DATE, \
    PAY_MONEY            INTEGER, \
    ENTDIV               VARCHAR(1), \
    HONORDIV             VARCHAR(1), \
    SUCCESS_NOTICENO     VARCHAR(6), \
    FAILURE_NOTICENO     VARCHAR(6), \
    REMARK1              VARCHAR(246), \
    REMARK2              VARCHAR(246), \
    RECOM_EXAMNO         VARCHAR(5), \
    PICTURE_ERACD        VARCHAR(1), \
    PICTURE_Y            VARCHAR(2), \
    PICTURE_M            VARCHAR(2), \
    PICTURE_DAY          DATE, \
    SELECT_SUBCLASS_DIV  VARCHAR(1), \
    SHIFT_DESIRE_FLG     VARCHAR(1), \
    SH_SCHOOLCD          VARCHAR(7), \
    SLIDE_FLG            VARCHAR(1), \
    GENERAL_FLG          VARCHAR(1), \
    SPORTS_FLG           VARCHAR(1), \
    DORMITORY_FLG        VARCHAR(1), \
    RECOM_ITEM1          VARCHAR(1), \
    RECOM_ITEM2          VARCHAR(1), \
    RECOM_ITEM3          VARCHAR(1), \
    RECOM_ITEM4          VARCHAR(1), \
    BUS_USE              VARCHAR(1), \
    STATIONDIV           VARCHAR(1), \
    BUS_USER_COUNT       SMALLINT, \
    PRE_RECEPTNO         VARCHAR(5), \
    PERSONAL_FLG         VARCHAR(1), \
    REGISTERCD           VARCHAR(8), \
    UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ENTEXAM_APPLICANTBASE_DAT \
    SELECT \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        EXAMNO, \
        TESTDIV, \
        SHDIV, \
        DESIREDIV, \
        TESTDIV1, \
        TESTDIV2, \
        TESTDIV3, \
        TESTDIV4, \
        TESTDIV5, \
        RECEPTDATE, \
        NAME, \
        NAME_KANA, \
        SEX, \
        ERACD, \
        BIRTH_Y, \
        BIRTH_M, \
        BIRTH_D, \
        BIRTHDAY, \
        FS_CD, \
        FS_NAME, \
        FS_AREA_CD, \
        CAST(NULL AS VARCHAR(2)) AS FS_AREA_DIV, \
        FS_NATPUBPRIDIV, \
        FS_GRDYEAR, \
        FS_ERACD, \
        FS_Y, \
        FS_M, \
        FS_DAY, \
        CAST(NULL AS VARCHAR(1)) AS FS_GRDDIV, \
        PRISCHOOLCD, \
        INTERVIEW_ATTEND_FLG, \
        SUC_COURSECD, \
        SUC_MAJORCD, \
        SUC_COURSECODE, \
        JUDGEMENT, \
        CAST(NULL AS VARCHAR(1)) AS JUDGE_KIND, \
        CAST(NULL AS VARCHAR(4)) AS SUB_ORDER, \
        SPECIAL_MEASURES, \
        PROCEDUREDIV, \
        PROCEDUREDATE, \
        CAST(NULL AS INTEGER) AS PAY_MONEY, \
        ENTDIV, \
        HONORDIV, \
        SUCCESS_NOTICENO, \
        FAILURE_NOTICENO, \
        REMARK1, \
        REMARK2, \
        RECOM_EXAMNO, \
        PICTURE_ERACD, \
        PICTURE_Y, \
        PICTURE_M, \
        PICTURE_DAY, \
        SELECT_SUBCLASS_DIV, \
        SHIFT_DESIRE_FLG, \
        CAST(NULL AS VARCHAR(7)) AS SH_SCHOOLCD, \
        CAST(NULL AS VARCHAR(1)) AS SLIDE_FLG, \
        CAST(NULL AS VARCHAR(1)) AS GENERAL_FLG, \
        CAST(NULL AS VARCHAR(1)) AS SPORTS_FLG, \
        CAST(NULL AS VARCHAR(1)) AS DORMITORY_FLG, \
        CAST(NULL AS VARCHAR(1)) AS RECOM_ITEM1, \
        CAST(NULL AS VARCHAR(1)) AS RECOM_ITEM2, \
        CAST(NULL AS VARCHAR(1)) AS RECOM_ITEM3, \
        CAST(NULL AS VARCHAR(1)) AS RECOM_ITEM4, \
        CAST(NULL AS VARCHAR(1)) AS BUS_USE, \
        CAST(NULL AS VARCHAR(1)) AS STATIONDIV, \
        CAST(NULL AS SMALLINT) AS BUS_USER_COUNT, \
        CAST(NULL AS VARCHAR(5)) AS PRE_RECEPTNO, \
        CAST(NULL AS VARCHAR(1)) AS PERSONAL_FLG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ENTEXAM_APPLICANTBASE_DAT_OLD

ALTER TABLE ENTEXAM_APPLICANTBASE_DAT ADD CONSTRAINT PK_ENTEXAM_APP PRIMARY KEY (ENTEXAMYEAR,EXAMNO)