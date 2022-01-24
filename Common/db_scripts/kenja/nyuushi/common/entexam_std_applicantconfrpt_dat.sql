-- $Id: $

DROP TABLE ENTEXAM_STD_APPLICANTCONFRPT_DAT
CREATE TABLE ENTEXAM_STD_APPLICANTCONFRPT_DAT( \
    YEAR                VARCHAR(4)    NOT NULL, \
    EXAMNO              VARCHAR(8)    NOT NULL, \
    GRADE               VARCHAR(2)    NOT NULL, \
    JAPANESE            SMALLINT      , \
    MATH                SMALLINT      , \
    SOCIETY             SMALLINT      , \
    SCIENCE             SMALLINT      , \
    ENGLISH             SMALLINT      , \
    HEALTH_PHYSICAL     SMALLINT      , \
    TECH_HOME           SMALLINT      , \
    MUSIC               SMALLINT      , \
    ART                 SMALLINT      , \
    TOTAL3              SMALLINT      , \
    TOTAL5              SMALLINT      , \
    TOTAL9              SMALLINT      , \
    ATTENDANCE          SMALLINT      , \
    REGISTERCD          VARCHAR(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE ENTEXAM_STD_APPLICANTCONFRPT_DAT ADD CONSTRAINT PK_ENTEXAM_STD_APPLICANTCONFRPT_DAT PRIMARY KEY (YEAR, EXAMNO, GRADE)

