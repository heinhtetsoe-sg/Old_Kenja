-- $Id: bf69dfebf55a260b796c9b60d9d358d512885240 $

DROP TABLE DELBK_MEDEXAM_HDAT
CREATE TABLE DELBK_MEDEXAM_HDAT ( \
    DEL_SEQ             SMALLINT   NOT NULL, \
    YEAR                VARCHAR    (4) NOT NULL, \
    SCHREGNO            VARCHAR    (8) NOT NULL, \
    DATE                DATE, \
    TOOTH_DATE          DATE, \
    REGISTERCD          VARCHAR (8), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    DEL_REGISTERCD      VARCHAR(8), \
    DEL_UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE DELBK_MEDEXAM_HDAT ADD CONSTRAINT PK_DLBK_MEDEXAM_H PRIMARY KEY (DEL_SEQ, YEAR, SCHREGNO)