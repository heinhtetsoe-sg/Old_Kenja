-- $Id: rep-hexamremark_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE HEXAMREMARK_DAT_OLD
RENAME TABLE HEXAMREMARK_DAT TO HEXAMREMARK_DAT_OLD
CREATE TABLE HEXAMREMARK_DAT( \
    SCHREGNO               VARCHAR(6)    NOT NULL, \
    COMMENTEX_A_CD         VARCHAR(1), \
    ATTENDREC_REMARKGRADE1 VARCHAR(28), \
    ATTENDREC_REMARKGRADE2 VARCHAR(28), \
    ATTENDREC_REMARKGRADE3 VARCHAR(28), \
    DISEASE                VARCHAR(178), \
    DOC_REMARK             VARCHAR(62), \
    TR_REMARK              VARCHAR(110), \
    SPECIALACTREC_GRADE1   VARCHAR(222), \
    SPECIALACTREC_GRADE2   VARCHAR(222), \
    SPECIALACTREC_GRADE3   VARCHAR(222), \
    TRAIN_REF_GRADE1       VARCHAR(418), \
    TRAIN_REF_GRADE1_1     VARCHAR(300), \
    TRAIN_REF_GRADE1_2     VARCHAR(300), \
    TRAIN_REF_GRADE1_3     VARCHAR(150), \
    TRAIN_REF_GRADE2       VARCHAR(418), \
    TRAIN_REF_GRADE2_1     VARCHAR(300), \
    TRAIN_REF_GRADE2_2     VARCHAR(300), \
    TRAIN_REF_GRADE2_3     VARCHAR(150), \
    TRAIN_REF_GRADE3       VARCHAR(418), \
    TRAIN_REF_GRADE3_1     VARCHAR(300), \
    TRAIN_REF_GRADE3_2     VARCHAR(300), \
    TRAIN_REF_GRADE3_3     VARCHAR(150), \
    ALLACTSTUDYREMARK      VARCHAR(158), \
    ALLACTSTUDYVALUE       VARCHAR(238), \
    REMARK                 VARCHAR(544), \
    JOBHUNT_REC            VARCHAR(334), \
    JOBHUNT_RECOMMEND      VARCHAR(858), \
    JOBHUNT_ABSENCE        VARCHAR(86), \
    JOBHUNT_HEALTHREMARK   VARCHAR(88), \
    UPDATED                TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO HEXAMREMARK_DAT \
    SELECT \
        SCHREGNO, \
        COMMENTEX_A_CD, \
        ATTENDREC_REMARKGRADE1, \
        ATTENDREC_REMARKGRADE2, \
        ATTENDREC_REMARKGRADE3, \
        DISEASE, \
        DOC_REMARK, \
        TR_REMARK, \
        SPECIALACTREC_GRADE1, \
        SPECIALACTREC_GRADE2, \
        SPECIALACTREC_GRADE3, \
        TRAIN_REF_GRADE1, \
        CAST(NULL AS VARCHAR(300)) AS TRAIN_REF_GRADE1_1, \
        CAST(NULL AS VARCHAR(300)) AS TRAIN_REF_GRADE1_2, \
        CAST(NULL AS VARCHAR(150)) AS TRAIN_REF_GRADE1_3, \
        TRAIN_REF_GRADE2, \
        CAST(NULL AS VARCHAR(300)) AS TRAIN_REF_GRADE2_1, \
        CAST(NULL AS VARCHAR(300)) AS TRAIN_REF_GRADE2_2, \
        CAST(NULL AS VARCHAR(150)) AS TRAIN_REF_GRADE2_3, \
        TRAIN_REF_GRADE3, \
        CAST(NULL AS VARCHAR(300)) AS TRAIN_REF_GRADE3_1, \
        CAST(NULL AS VARCHAR(300)) AS TRAIN_REF_GRADE3_2, \
        CAST(NULL AS VARCHAR(150)) AS TRAIN_REF_GRADE3_3, \
        ALLACTSTUDYREMARK, \
        ALLACTSTUDYVALUE, \
        REMARK, \
        JOBHUNT_REC, \
        JOBHUNT_RECOMMEND, \
        JOBHUNT_ABSENCE, \
        JOBHUNT_HEALTHREMARK, \
        UPDATED \
    FROM \
        HEXAMREMARK_DAT_OLD

ALTER TABLE HEXAMREMARK_DAT ADD CONSTRAINT PK_HEXAMREMARK_DAT PRIMARY KEY (SCHREGNO)