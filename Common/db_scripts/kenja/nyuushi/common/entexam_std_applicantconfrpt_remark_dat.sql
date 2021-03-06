-- $Id: $

DROP TABLE ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT
CREATE TABLE ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT( \
    YEAR                VARCHAR(4)    NOT NULL, \
    EXAMNO              VARCHAR(8)    NOT NULL, \
    SPECIAL_ACT1        VARCHAR(1)    , \
    SPECIAL_ACT2        VARCHAR(1)    , \
    SPECIAL_ACT3        VARCHAR(1)    , \
    SPECIAL_ACT4        VARCHAR(1)    , \
    SPECIAL_ACT5        VARCHAR(1)    , \
    SPECIAL_ACT6        VARCHAR(1)    , \
    SPECIAL_ACT7        VARCHAR(1)    , \
    SPECIAL_ACT8        VARCHAR(1)    , \
    SPECIAL_ACT9        VARCHAR(1)    , \
    SPECIAL_ACT10       VARCHAR(1)    , \
    ACT_TOTAL           VARCHAR(2)    , \
    SPECIAL_REMARK      VARCHAR(250)  , \
    REMARK1             VARCHAR(150)  , \
    REMARK2             VARCHAR(150)  , \
    REMARK3             VARCHAR(150)  , \
    REGISTERCD          VARCHAR(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT ADD CONSTRAINT PK_ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT PRIMARY KEY (YEAR, EXAMNO)
