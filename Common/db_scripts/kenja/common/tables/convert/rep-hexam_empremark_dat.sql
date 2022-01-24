-- $Id: cae1d57c311d339f83f738bd6853fa05a125f1ae $

DROP TABLE HEXAM_EMPREMARK_DAT_OLD
RENAME TABLE HEXAM_EMPREMARK_DAT TO HEXAM_EMPREMARK_DAT_OLD
CREATE TABLE HEXAM_EMPREMARK_DAT( \
    SCHREGNO             VARCHAR(8)    NOT NULL, \
    JOBHUNT_REC          VARCHAR(618), \
    JOBHUNT_RECOMMEND    VARCHAR(2000), \
    JOBHUNT_ABSENCE      VARCHAR(190), \
    JOBHUNT_HEALTHREMARK VARCHAR(130), \
    REGISTERCD           VARCHAR(10), \
    UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO HEXAM_EMPREMARK_DAT \
    SELECT \
        SCHREGNO, \
        JOBHUNT_REC, \
        JOBHUNT_RECOMMEND, \
        JOBHUNT_ABSENCE, \
        JOBHUNT_HEALTHREMARK, \
        REGISTERCD, \
        UPDATED \
    FROM \
        HEXAM_EMPREMARK_DAT_OLD

ALTER TABLE HEXAM_EMPREMARK_DAT ADD CONSTRAINT PK_HEXAM_EMP_DAT PRIMARY KEY (SCHREGNO)