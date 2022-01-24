-- $Id: 16db53c87913031665104d1137d80f37d1cea488 $

drop table STAFF_REQUESTFORM_DAT

CREATE TABLE STAFF_REQUESTFORM_DAT( \
    STAFFCD               VARCHAR(8)  NOT NULL, \
    SDATE                 DATE        NOT NULL, \
    EDATE                 DATE        NOT NULL, \
    WORK_DIV              VARCHAR(2)  NOT NULL, \
    REASON                VARCHAR(150), \
    REMARK                VARCHAR(120), \
    REGISTERCD            VARCHAR(8), \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE STAFF_REQUESTFORM_DAT ADD CONSTRAINT PK_STAFF_REQ_DAT PRIMARY KEY (STAFFCD, SDATE)