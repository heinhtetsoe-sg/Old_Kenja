-- $Id: $

DROP TABLE ENTEXAM_STD_SCORE_DAT
CREATE TABLE ENTEXAM_STD_SCORE_DAT( \
    YEAR                VARCHAR(4)    NOT NULL, \
    RECEPTNO            VARCHAR(4)    NOT NULL, \
    EXAM_SUBCLASS       VARCHAR(2)    NOT NULL, \
    SCORE               SMALLINT      , \
    ABSENCE_FLG         VARCHAR(1)    , \
    REGISTERCD          VARCHAR(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE ENTEXAM_STD_SCORE_DAT ADD CONSTRAINT PK_ENTEXAM_STD_SCORE_DAT PRIMARY KEY (YEAR, RECEPTNO, EXAM_SUBCLASS)
