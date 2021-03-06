-- $Id: rep-entexam_majors_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_MAJORS_MST_OLD
RENAME TABLE ENTEXAM_MAJORS_MST TO ENTEXAM_MAJORS_MST_OLD
CREATE TABLE ENTEXAM_MAJORS_MST( \
    MAJORLCD      VARCHAR(2)    NOT NULL, \
    MAJORSCD      VARCHAR(1)    NOT NULL, \
    MAJORSNAME    VARCHAR(60), \
    MAJORSABBV    VARCHAR(6), \
    MAIN_COURSECD VARCHAR(1), \
    MAIN_MAJORCD  VARCHAR(3), \
    REGISTERCD    VARCHAR(8), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ENTEXAM_MAJORS_MST \
    SELECT \
        MAJORLCD, \
        MAJORSCD, \
        MAJORSNAME, \
        MAJORSABBV, \
        CAST(NULL AS VARCHAR(1)) AS MAIN_COURSECD, \
        MAIN_MAJORCD, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ENTEXAM_MAJORS_MST_OLD

ALTER TABLE ENTEXAM_MAJORS_MST ADD CONSTRAINT PK_MAJORS_MST PRIMARY KEY (MAJORLCD,MAJORSCD)