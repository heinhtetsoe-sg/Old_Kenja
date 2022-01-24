-- $Id: d2e87fe6f6b95297d4dd818a9df3c2d9fecce3e9 $

drop table GRD_GUARDIAN_COMEBACK_DAT_OLD
create table GRD_GUARDIAN_COMEBACK_DAT_OLD like GRD_GUARDIAN_COMEBACK_DAT
insert into GRD_GUARDIAN_COMEBACK_DAT_OLD select * from GRD_GUARDIAN_COMEBACK_DAT

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

INSERT INTO GRD_GUARDIAN_COMEBACK_DAT \
    SELECT \
        SCHREGNO                                , \
        COMEBACK_DATE                           , \
        RELATIONSHIP                            , \
        GUARD_NAME                              , \
        GUARD_KANA                              , \
        GUARD_REAL_NAME                         , \
        GUARD_REAL_KANA                         , \
        GUARD_SEX                               , \
        GUARD_BIRTHDAY                          , \
        GUARD_ZIPCD                             , \
        GUARD_ADDR1                             , \
        GUARD_ADDR2                             , \
        GUARD_TELNO                             , \
        CAST(NULL AS VARCHAR(14)) GUARD_TELNO2  , \
        GUARD_FAXNO                             , \
        GUARD_E_MAIL                            , \
        GUARD_JOBCD                             , \
        GUARD_WORK_NAME                         , \
        GUARD_WORK_TELNO                        , \
        GUARANTOR_RELATIONSHIP                  , \
        GUARANTOR_NAME                          , \
        GUARANTOR_KANA                          , \
        GUARANTOR_REAL_NAME                     , \
        GUARANTOR_REAL_KANA                     , \
        GUARANTOR_SEX                           , \
        GUARANTOR_ZIPCD                         , \
        GUARANTOR_ADDR1                         , \
        GUARANTOR_ADDR2                         , \
        GUARANTOR_TELNO                         , \
        GUARANTOR_JOBCD                         , \
        PUBLIC_OFFICE                           , \
        REGISTERCD                              , \
        UPDATED                                   \
    FROM \
        GRD_GUARDIAN_COMEBACK_DAT_OLD
