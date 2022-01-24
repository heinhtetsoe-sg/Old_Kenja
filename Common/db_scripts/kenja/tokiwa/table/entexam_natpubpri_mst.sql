-- $Id: entexam_natpubpri_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_NATPUBPRI_MST
CREATE TABLE ENTEXAM_NATPUBPRI_MST( \
    NATPUBPRI_CD   VARCHAR(1)    NOT NULL, \
    NATPUBPRI_NAME VARCHAR(9)    NOT NULL, \
    NATPUBPRI_ABBV VARCHAR(9), \
    REGISTERCD     VARCHAR(8), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_NATPUBPRI_MST ADD CONSTRAINT PK_ENTEXAM_NAT_MST PRIMARY KEY (NATPUBPRI_CD)