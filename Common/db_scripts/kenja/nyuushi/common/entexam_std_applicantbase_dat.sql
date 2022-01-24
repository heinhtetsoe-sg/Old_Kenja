-- $Id: $

DROP TABLE ENTEXAM_STD_APPLICANTBASE_DAT
CREATE TABLE ENTEXAM_STD_APPLICANTBASE_DAT( \
    YEAR                VARCHAR(4)    NOT NULL, \
    EXAMNO              VARCHAR(8)    NOT NULL, \
    NAME                VARCHAR(120)  , \
    NAME_KANA           VARCHAR(240)  , \
    SEX                 VARCHAR(1)    , \
    BIRTHDAY            DATE          , \
    FINSCHOOLCD         VARCHAR(12)   , \
    ZIPCD               VARCHAR(8)    , \
    ADDR1               VARCHAR(150)  , \
    ADDR2               VARCHAR(150)  , \
    TELNO               VARCHAR(14)   , \
    EMAIL               VARCHAR(50)   , \
    FINISH_DATE         DATE          , \
    REMARK1             VARCHAR(150)  , \
    REMARK2             VARCHAR(150)  , \
    REMARK3             VARCHAR(150)  , \
    REMARK4             VARCHAR(150)  , \
    REMARK5             VARCHAR(150)  , \
    DEPOSIT             VARCHAR(1)    , \
    DEPOSIT_DATE        DATE          , \
    FEE                 VARCHAR(1)    , \
    FEE_DATE            DATE          , \
    DECLINE             VARCHAR(1)    , \
    DECLINE_DATE        DATE          , \
    ENTERING_FLG        VARCHAR(1)    , \
    REGISTERCD          VARCHAR(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE ENTEXAM_STD_APPLICANTBASE_DAT ADD CONSTRAINT PK_ENTEXAM_STD_APPLICANTBASE_DAT PRIMARY KEY (YEAR, EXAMNO)
