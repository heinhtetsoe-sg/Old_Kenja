-- $Id: rep-entexam_score_detail_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_SCORE_DETAIL_DAT_OLD
RENAME TABLE ENTEXAM_SCORE_DETAIL_DAT TO ENTEXAM_SCORE_DETAIL_DAT_OLD
CREATE TABLE ENTEXAM_SCORE_DETAIL_DAT( \
    ENTEXAMYEAR    VARCHAR(4)    NOT NULL, \
    APPLICANTDIV   VARCHAR(1)    NOT NULL, \
    TESTDIV        VARCHAR(1)    NOT NULL, \
    EXAM_TYPE      VARCHAR(1)    NOT NULL, \
    RECEPTNO       VARCHAR(5)    NOT NULL, \
    TESTSUBCLASSCD VARCHAR(1)    NOT NULL, \
    TESTPAPERCD    VARCHAR(1)    NOT NULL, \
    SCORE          SMALLINT, \
    REGISTERCD     VARCHAR(8), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ENTEXAM_SCORE_DETAIL_DAT \
    SELECT \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        TESTDIV, \
        EXAM_TYPE, \
        RECEPTNO, \
        TESTSUBCLASSCD, \
        TESTPAPERCD, \
        SCORE, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ENTEXAM_SCORE_DETAIL_DAT_OLD

ALTER TABLE ENTEXAM_SCORE_DETAIL_DAT ADD CONSTRAINT PK_ENTEXAM_S_D_MST PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO,TESTSUBCLASSCD,TESTPAPERCD)