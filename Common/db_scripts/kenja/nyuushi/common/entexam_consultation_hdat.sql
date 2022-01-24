-- KANJI=´Á»ú
-- $ID: $

DROP TABLE ENTEXAM_CONSULTATION_HDAT

CREATE TABLE ENTEXAM_CONSULTATION_HDAT ( \
    ENTEXAMYEAR             VARCHAR(4)   NOT NULL, \
    APPLICANTDIV            VARCHAR(1)   NOT NULL, \
    TESTDIV                 VARCHAR(2)   NOT NULL, \
    ACCEPTNO                VARCHAR(4)   NOT NULL, \
    CREATE_DATE             DATE         NOT NULL, \
    EXAMNO                  VARCHAR(10)  , \
    NAME                    VARCHAR(63)  , \
    NAME_KANA               VARCHAR(243) , \
    SEX                     VARCHAR(1)   , \
    PS_UPDATED              DATE         , \
    PS_ACCEPTNO             VARCHAR(4)   , \
    PS_CD                   VARCHAR(7)   , \
    PS_ITEM1                SMALLINT     , \
    PS_ITEM2                DECIMAL(4,1) , \
    PS_ITEM3                DECIMAL(4,1) , \
    PS_ITEM4                DECIMAL(4,1) , \
    PS_ITEM5                DECIMAL(4,1) , \
    FS_UPDATED              DATE         , \
    FS_ACCEPTNO             VARCHAR(4)   , \
    FS_CD                   VARCHAR(7)   , \
    FS_ITEM1                SMALLINT     , \
    FS_ITEM2                DECIMAL(4,1) , \
    FS_ITEM3                DECIMAL(4,1) , \
    FS_ITEM4                DECIMAL(4,1) , \
    FS_ITEM5                DECIMAL(4,1) , \
    REGISTERCD              VARCHAR(10)  , \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_CONSULTATION_HDAT ADD CONSTRAINT PK_ENTEXAM_CONSH \
      PRIMARY KEY (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, ACCEPTNO, CREATE_DATE)