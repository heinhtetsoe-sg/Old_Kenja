-- $Id: rep-entexam_area_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_AREA_MST_OLD
RENAME TABLE ENTEXAM_AREA_MST TO ENTEXAM_AREA_MST_OLD
CREATE TABLE ENTEXAM_AREA_MST( \
    NATPUBPRI_CD VARCHAR(1)    NOT NULL, \
    AREA_DIV_CD  VARCHAR(2)    NOT NULL, \
    AREA_CD      VARCHAR(2)    NOT NULL, \
    AREA_NAME    VARCHAR(30)   NOT NULL, \
    AREA_ABBV    VARCHAR(30), \
    REGISTERCD   VARCHAR(8), \
    UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ENTEXAM_AREA_MST \
    SELECT \
        substr(NATPUBPRI_CD,2,1) AS NATPUBPRI_CD, \
        AREA_DIV_CD, \
        AREA_CD, \
        AREA_NAME, \
        AREA_ABBV, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ENTEXAM_AREA_MST_OLD

ALTER TABLE ENTEXAM_AREA_MST ADD CONSTRAINT PK_ENTEXAM_ARE_MST PRIMARY KEY (NATPUBPRI_CD,AREA_DIV_CD,AREA_CD)