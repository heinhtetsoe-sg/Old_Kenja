-- kanji=????
-- $Id: 09afb3e8a1298f48ffd901d5cd1ace8a7bffa9cf $

DROP TABLE HTRAINREMARK_DETAIL2_DAT
CREATE TABLE HTRAINREMARK_DETAIL2_DAT( \
    YEAR                VARCHAR(4)    NOT NULL, \
    SCHREGNO            VARCHAR(8)    NOT NULL, \
    HTRAIN_SEQ          VARCHAR(3)    NOT NULL, \
    REMARK1             VARCHAR(1500), \
    REMARK2             VARCHAR(780), \
    REMARK3             VARCHAR(780), \
    REMARK4             VARCHAR(780), \
    REMARK5             VARCHAR(780), \
    REGISTERCD          VARCHAR(8), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HTRAINREMARK_DETAIL2_DAT ADD CONSTRAINT PK_HTRAINRMK_D2_D PRIMARY KEY (YEAR,SCHREGNO,HTRAIN_SEQ)