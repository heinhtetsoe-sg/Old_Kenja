-- $Id: grd_hexam_entremark_seq_hdat.sql 77561 2020-11-16 00:44:49Z ishii $

DROP TABLE GRD_HEXAM_ENTREMARK_SEQ_HDAT
CREATE TABLE GRD_HEXAM_ENTREMARK_SEQ_HDAT( \
    SCHREGNO                   VARCHAR(8)  NOT NULL, \
    PATTERN_SEQ                VARCHAR(1)  NOT NULL, \
    COMMENTEX_A_CD             VARCHAR(1)   , \
    DISEASE                    VARCHAR(259) , \
    DOC_REMARK                 VARCHAR(90)  , \
    TR_REMARK                  VARCHAR(159) , \
    TOTALSTUDYACT              VARCHAR(746) , \
    TOTALSTUDYVAL              VARCHAR(845) , \
    BEHAVEREC_REMARK           VARCHAR(845) , \
    HEALTHREC                  VARCHAR(845) , \
    SPECIALACTREC              VARCHAR(845) , \
    TRIN_REF                   VARCHAR(1248), \
    REMARK                     VARCHAR(1500), \
    REMARK2                    VARCHAR(500) , \
    TOTALSTUDYACT_SLASH_FLG    VARCHAR(1)   , \
    TOTALSTUDYVAL_SLASH_FLG    VARCHAR(1)   , \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE GRD_HEXAM_ENTREMARK_SEQ_HDAT ADD CONSTRAINT PK_GRD_HEXAMENTH_SEQ PRIMARY KEY (SCHREGNO,PATTERN_SEQ)