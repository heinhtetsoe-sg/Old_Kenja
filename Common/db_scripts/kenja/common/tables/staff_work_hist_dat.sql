-- $Id: 4e4e3ba111853f304b4d3ee85baf61e950704ebe $

drop table STAFF_WORK_HIST_DAT

CREATE TABLE STAFF_WORK_HIST_DAT( \
    STAFFCD             VARCHAR(10) NOT NULL, \
    FROM_DATE           DATE        NOT NULL, \
    WORK_DIV            VARCHAR(1)  NOT NULL, \
    FROM_DIV            VARCHAR(2)  NOT NULL, \
    FROM_SCHOOLCD       VARCHAR(12), \
    FROM_COURSECD       VARCHAR(1), \
    TO_DATE             DATE, \
    TO_DIV              VARCHAR(2), \
    TO_SCHOOLCD         VARCHAR(12), \
    TO_COURSECD         VARCHAR(1), \
    REMARK              VARCHAR(150), \
    USE_KNJ             VARCHAR(2), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE STAFF_WORK_HIST_DAT ADD CONSTRAINT PK_WORK_HIST PRIMARY KEY (STAFFCD, FROM_DATE, WORK_DIV)