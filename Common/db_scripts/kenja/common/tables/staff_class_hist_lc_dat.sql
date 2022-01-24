-- $Id: b58714ab488b2cf782288281e5d8e1300351092d $

DROP TABLE STAFF_CLASS_HIST_LC_DAT
CREATE TABLE STAFF_CLASS_HIST_LC_DAT( \
    YEAR       VARCHAR(4)    NOT NULL, \
    SEMESTER   VARCHAR(1)    NOT NULL, \
    GRADE      VARCHAR(3)    NOT NULL, \
    LC_CLASS   VARCHAR(3)    NOT NULL, \
    TR_DIV     VARCHAR(1)    NOT NULL, \
    FROM_DATE  DATE          NOT NULL, \
    TO_DATE    DATE, \
    STAFFCD    VARCHAR(10), \
    REGISTERCD VARCHAR(10), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE STAFF_CLASS_HIST_LC_DAT ADD CONSTRAINT PK_STF_CLASS_HLC PRIMARY KEY (YEAR,SEMESTER,GRADE,LC_CLASS,TR_DIV,FROM_DATE)