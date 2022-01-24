-- $Id: cb2a1f7e7e1f97f5c695cabcefa627854bf3541b $

DROP TABLE HREPORTREMARK_P_DAT
CREATE TABLE HREPORTREMARK_P_DAT( \
    YEAR             VARCHAR(4)    NOT NULL, \
    SEMESTER         VARCHAR(1)    NOT NULL, \
    SCHREGNO         VARCHAR(8)    NOT NULL, \
    TOTALSTUDYTIME   VARCHAR(500), \
    SPECIALACTREMARK VARCHAR(323), \
    COMMUNICATION    VARCHAR(500), \
    REMARK1          VARCHAR(323), \
    REMARK2          VARCHAR(323), \
    REMARK3          VARCHAR(780), \
    FOREIGNLANGACT   VARCHAR(450), \
    ATTENDREC_REMARK VARCHAR(242), \
    REGISTERCD       VARCHAR(8), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HREPORTREMARK_P_DAT ADD CONSTRAINT PK_HREPORTREMARKP PRIMARY KEY (YEAR,SEMESTER,SCHREGNO)