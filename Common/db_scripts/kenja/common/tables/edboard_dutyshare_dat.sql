-- $Id: 25c5dc14f68dbf9e19304821e2ee38978eb367ac $

DROP TABLE EDBOARD_DUTYSHARE_DAT
CREATE TABLE EDBOARD_DUTYSHARE_DAT( \
    EDBOARD_SCHOOLCD   VARCHAR(12)   NOT NULL, \
    DUTYSHARECD        VARCHAR(4)    NOT NULL, \
    EDBOARD_FLG        VARCHAR(1), \
    REGISTERCD         VARCHAR(8), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EDBOARD_DUTYSHARE_DAT ADD CONSTRAINT PK_ED_DUTYSH_DAT PRIMARY KEY (EDBOARD_SCHOOLCD, DUTYSHARECD)
