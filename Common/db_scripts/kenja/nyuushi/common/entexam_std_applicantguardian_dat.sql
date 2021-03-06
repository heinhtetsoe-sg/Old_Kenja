-- $Id: $

DROP TABLE ENTEXAM_STD_APPLICANTGUARDIAN_DAT
CREATE TABLE ENTEXAM_STD_APPLICANTGUARDIAN_DAT( \
    YEAR                VARCHAR(4)    NOT NULL, \
    EXAMNO              VARCHAR(8)    NOT NULL, \
    GUARD_NAME          VARCHAR(120)  , \
    GUARD_NAME_KANA     VARCHAR(240)  , \
    RELATION            VARCHAR(2)    , \
    GUARD_ZIP           VARCHAR(8)    , \
    GUARD_ADDR1         VARCHAR(150)  , \
    GUARD_ADDR2         VARCHAR(150)  , \
    GUARD_TELNO         VARCHAR(14)   , \
    REGISTERCD          VARCHAR(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE ENTEXAM_STD_APPLICANTGUARDIAN_DAT ADD CONSTRAINT PK_ENTEXAM_STD_APPLICANTGUARDIAN_DAT PRIMARY KEY (YEAR, EXAMNO)
