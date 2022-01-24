-- $Id: 3ab7147ac7c1be761943763c9d30398292b68941 $

DROP TABLE ATTEND_SEMES_DEL_HDAT
CREATE TABLE ATTEND_SEMES_DEL_HDAT( \
    EXECUTEDATE     DATE            NOT NULL, \
    SEQ             VARCHAR(3)      NOT NULL, \
    YEAR            VARCHAR(4)      NOT NULL, \
    SEMESTER        VARCHAR(1)      NOT NULL, \
    GRADE           VARCHAR(2)      NOT NULL, \
    MONTH           VARCHAR(2)      NOT NULL, \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEND_SEMES_DEL_HDAT ADD CONSTRAINT PK_ATTSEM_DEL_HDAT \
      PRIMARY KEY (EXECUTEDATE, SEQ)