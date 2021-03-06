-- $Id: rep-entexam_applicantbase_pre_dat.sql 70053 2019-10-07 01:31:47Z yamashiro $

DROP TABLE ENTEXAM_APPLICANTBASE_PRE_DAT_OLD

RENAME TABLE ENTEXAM_APPLICANTBASE_PRE_DAT TO ENTEXAM_APPLICANTBASE_PRE_DAT_OLD

CREATE TABLE ENTEXAM_APPLICANTBASE_PRE_DAT( \
    ENTEXAMYEAR    VARCHAR(4)    NOT NULL, \
    APPLICANTDIV   VARCHAR(1)    NOT NULL, \
    PRE_RECEPTNO   VARCHAR(5)    NOT NULL, \
    PRE_TESTDIV    VARCHAR(1)    NOT NULL, \
    PRE_EXAM_TYPE  VARCHAR(1), \
    PRE_RECEPTDIV  VARCHAR(1), \
    PRE_RECEPTDATE DATE, \
    NAME           VARCHAR(60), \
    NAME_KANA      VARCHAR(120), \
    SEX            VARCHAR(1), \
    GNAME          VARCHAR(60), \
    GKANA          VARCHAR(120), \
    ZIPCD          VARCHAR(8), \
    ADDRESS1       VARCHAR(75), \
    ADDRESS2       VARCHAR(75), \
    TELNO          VARCHAR(14), \
    FS_CD          VARCHAR(7), \
    PS_CD          VARCHAR(7), \
    PS_CONTACT     VARCHAR(1), \
    BUS_USE        VARCHAR(1), \
    STATIONDIV     VARCHAR(1), \
    BUS_USER_COUNT SMALLINT, \
    REMARK         VARCHAR(45), \
    RECOM_EXAMNO   VARCHAR(5), \
    REGISTERCD     VARCHAR(8), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ENTEXAM_APPLICANTBASE_PRE_DAT \
    SELECT \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        PRE_RECEPTNO, \
        '1' AS PRE_TESTDIV, \
        PRE_EXAM_TYPE, \
        PRE_RECEPTDIV, \
        PRE_RECEPTDATE, \
        NAME, \
        NAME_KANA, \
        SEX, \
        GNAME, \
        GKANA, \
        ZIPCD, \
        ADDRESS1, \
        ADDRESS2, \
        TELNO, \
        FS_CD, \
        PS_CD, \
        PS_CONTACT, \
        BUS_USE, \
        STATIONDIV, \
        BUS_USER_COUNT, \
        REMARK, \
        CAST(NULL AS VARCHAR(5)) AS RECOM_EXAMNO, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ENTEXAM_APPLICANTBASE_PRE_DAT_OLD

ALTER TABLE ENTEXAM_APPLICANTBASE_PRE_DAT ADD CONSTRAINT PK_ENTEXAM_APP_PRE PRIMARY KEY (ENTEXAMYEAR,PRE_RECEPTNO)