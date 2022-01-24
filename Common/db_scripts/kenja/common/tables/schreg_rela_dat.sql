-- $Id: 20bcf7261aa3982433c54e9c8bc7fd1a1fdc22e6 $

DROP   TABLE SCHREG_RELA_DAT_OLD
RENAME TABLE SCHREG_RELA_DAT TO SCHREG_RELA_DAT_OLD

CREATE TABLE SCHREG_RELA_DAT( \
    SCHREGNO        VARCHAR(8)  NOT NULL, \
    RELANO          VARCHAR(2)  NOT NULL, \
    RELANAME        VARCHAR(60), \
    RELAKANA        VARCHAR(120), \
    RELASEX         VARCHAR(1), \
    RELABIRTHDAY    DATE, \
    OCCUPATION      VARCHAR(60), \
    REGIDENTIALCD   VARCHAR(2), \
    RELATIONSHIP    VARCHAR(2), \
    RELA_SCHREGNO   VARCHAR(8), \
    REGD_GRD_FLG    VARCHAR(2), \
    RELA_GRADE      VARCHAR(2), \
    REMARK          VARCHAR(60), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_RELA_DAT \
ADD CONSTRAINT PK_SCHREG_RELA_DAT \
PRIMARY KEY (SCHREGNO, RELANO)
