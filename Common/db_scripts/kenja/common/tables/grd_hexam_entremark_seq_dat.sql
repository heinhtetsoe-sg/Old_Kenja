-- $Id: grd_hexam_entremark_seq_dat.sql 77570 2020-11-24 00:53:45Z ishii $

DROP TABLE GRD_HEXAM_ENTREMARK_SEQ_DAT
CREATE TABLE GRD_HEXAM_ENTREMARK_SEQ_DAT( \
    YEAR             VARCHAR(4)    NOT NULL, \
    SCHREGNO         VARCHAR(8)    NOT NULL, \
    PATTERN_SEQ      VARCHAR(1)    NOT NULL, \
    ANNUAL           VARCHAR(2)    NOT NULL, \
    ATTENDREC_REMARK VARCHAR(238), \
    SPECIALACTREC    VARCHAR(700), \
    TRAIN_REF        VARCHAR(1248), \
    TRAIN_REF1       VARCHAR(453), \
    TRAIN_REF2       VARCHAR(520), \
    TRAIN_REF3       VARCHAR(800), \
    TOTALSTUDYACT    VARCHAR(746), \
    TOTALSTUDYVAL    VARCHAR(746), \
    CALSSACT         VARCHAR(300), \
    STUDENTACT       VARCHAR(218), \
    CLUBACT          VARCHAR(225), \
    SCHOOLEVENT      VARCHAR(218), \
    TOTALSTUDYACT_SLASH_FLG     VARCHAR(1), \
    TOTALSTUDYVAL_SLASH_FLG     VARCHAR(1), \
    ATTENDREC_REMARK_SLASH_FLG  VARCHAR(1), \
    REGISTERCD       VARCHAR(10), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE GRD_HEXAM_ENTREMARK_SEQ_DAT ADD CONSTRAINT SQL090925155104150 PRIMARY KEY (YEAR,SCHREGNO,PATTERN_SEQ)
