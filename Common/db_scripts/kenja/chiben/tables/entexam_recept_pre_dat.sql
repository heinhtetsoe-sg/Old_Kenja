-- $Id: entexam_recept_pre_dat.sql 62711 2018-10-09 07:39:53Z nakamoto $

DROP TABLE ENTEXAM_RECEPT_PRE_DAT
CREATE TABLE ENTEXAM_RECEPT_PRE_DAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    APPLICANTDIV              VARCHAR(1)    NOT NULL, \
    PRE_RECEPTNO              VARCHAR(5)    NOT NULL, \
    ATTEND_ALL_FLG            VARCHAR(1), \
    TOTAL2                    SMALLINT, \
    AVARAGE2                  DECIMAL(4,1), \
    TOTAL_RANK2               SMALLINT, \
    DIV_RANK2                 SMALLINT, \
    TOTAL4                    SMALLINT, \
    AVARAGE4                  DECIMAL(4,1), \
    TOTAL_RANK4               SMALLINT, \
    DIV_RANK4                 SMALLINT, \
    TOTAL3                    SMALLINT, \
    AVARAGE3                  DECIMAL(4,1), \
    TOTAL_RANK3               SMALLINT, \
    DIV_RANK3                 SMALLINT, \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_RECEPT_PRE_DAT ADD CONSTRAINT PK_ENTEXAM_REC_PRE PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,PRE_RECEPTNO)