-- $Id: b1ba5cd9065d509396f5c2abaa59a6e397ffc833 $

DROP TABLE DELBK_HEXAM_EMPREMARK_DAT
CREATE TABLE DELBK_HEXAM_EMPREMARK_DAT ( \
    DEL_SEQ              SMALLINT      NOT NULL, \
    SCHREGNO             VARCHAR(8)    NOT NULL, \
    JOBHUNT_REC          VARCHAR(900), \
    JOBHUNT_RECOMMEND    VARCHAR(1506), \
    JOBHUNT_ABSENCE      VARCHAR(190), \
    JOBHUNT_HEALTHREMARK VARCHAR(130), \
    REGISTERCD           VARCHAR(8), \
    UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    DEL_REGISTERCD       VARCHAR(8), \
    DEL_UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE DELBK_HEXAM_EMPREMARK_DAT ADD CONSTRAINT PK_DLBK_HEXAM_EMP PRIMARY KEY (DEL_SEQ, SCHREGNO)