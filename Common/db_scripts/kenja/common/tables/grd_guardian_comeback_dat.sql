-- $Id: 7197bf13cd78f4c1430fb7e1ff1c4de2ec901710 $

DROP TABLE GRD_GUARDIAN_COMEBACK_DAT
CREATE TABLE GRD_GUARDIAN_COMEBACK_DAT( \
    SCHREGNO               VARCHAR(8)    NOT NULL, \
    COMEBACK_DATE          date          NOT NULL, \
    RELATIONSHIP           VARCHAR(2)    NOT NULL, \
    GUARD_NAME             VARCHAR(120), \
    GUARD_KANA             VARCHAR(240), \
    GUARD_REAL_NAME        VARCHAR(120), \
    GUARD_REAL_KANA        VARCHAR(240), \
    GUARD_SEX              VARCHAR(1), \
    GUARD_BIRTHDAY         DATE, \
    GUARD_ZIPCD            VARCHAR(8), \
    GUARD_ADDR1            VARCHAR(150), \
    GUARD_ADDR2            VARCHAR(150), \
    GUARD_TELNO            VARCHAR(14), \
    GUARD_TELNO2           VARCHAR(14), \
    GUARD_FAXNO            VARCHAR(14), \
    GUARD_E_MAIL           VARCHAR(50), \
    GUARD_JOBCD            VARCHAR(2), \
    GUARD_WORK_NAME        VARCHAR(120), \
    GUARD_WORK_TELNO       VARCHAR(14), \
    GUARANTOR_RELATIONSHIP VARCHAR(2), \
    GUARANTOR_NAME         VARCHAR(120), \
    GUARANTOR_KANA         VARCHAR(240), \
    GUARANTOR_REAL_NAME    VARCHAR(120), \
    GUARANTOR_REAL_KANA    VARCHAR(240), \
    GUARANTOR_SEX          VARCHAR(1), \
    GUARANTOR_ZIPCD        VARCHAR(8), \
    GUARANTOR_ADDR1        VARCHAR(150), \
    GUARANTOR_ADDR2        VARCHAR(150), \
    GUARANTOR_TELNO        VARCHAR(14), \
    GUARANTOR_JOBCD        VARCHAR(2), \
    PUBLIC_OFFICE          VARCHAR(30), \
    REGISTERCD             VARCHAR(10), \
    UPDATED                TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE GRD_GUARDIAN_COMEBACK_DAT ADD CONSTRAINT PK_GRD_GUARD_COME PRIMARY KEY (SCHREGNO, COMEBACK_DATE)