-- KANJI=����
-- $ID: $

DROP TABLE ENTEXAM_NEWSFLASH_DAT

CREATE TABLE ENTEXAM_NEWSFLASH_DAT ( \
    ENTEXAMYEAR             VARCHAR(4)  NOT NULL, \
    APPLICANTDIV            VARCHAR(1)  NOT NULL, \
    TESTDIV                 VARCHAR(2)  NOT NULL, \
    EXAMNO                  VARCHAR(10) NOT NULL, \
    RECEPT_DATE             DATE        , \
    SHDIV                   VARCHAR(1)  , \
    DESIREDIV               VARCHAR(2)  , \
    SEX                     VARCHAR(1)  , \
    NATPUBPRIDIV            VARCHAR(1)  , \
    REGISTERCD              VARCHAR(10) , \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_NEWSFLASH_DAT ADD CONSTRAINT PK_ENTEXAM_NEWSF \
      PRIMARY KEY (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAMNO)