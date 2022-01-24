-- $Id: ede32d9fd7a789a55fde23f1474c0ff848a2012b $

DROP TABLE JOB_OFFER_DAT_OLD
RENAME TABLE JOB_OFFER_DAT TO JOB_OFFER_DAT_OLD
CREATE TABLE JOB_OFFER_DAT( \
    YEAR                VARCHAR(4)    NOT NULL, \
    SENKOU_NO           INT           NOT NULL, \
    ACCEPTANCE_DATE     DATE, \
    COMPANY_CD          CHAR(8), \
    COMPANY_NAME        VARCHAR(150), \
    COMPANY_NAMEKANA    VARCHAR(300), \
    COMPANY_CONTENTS    VARCHAR(300), \
    SHIHONKIN           VARCHAR(17), \
    MAKECOMPANY_YEAR    VARCHAR(4), \
    COMPANY_ZIPCD       VARCHAR(8), \
    COMPANY_ADDR1       VARCHAR(150), \
    COMPANY_ADDR2       VARCHAR(150), \
    COMPANY_TELNO1      VARCHAR(14), \
    COMPANY_TELNO2      VARCHAR(14), \
    COMPANY_FAXNO       VARCHAR(14), \
    INDUSTRY_SCD        VARCHAR(3), \
    SONINZU             INT, \
    SHUSHOKU_NAME       VARCHAR(150), \
    SHUSHOKU_ZIPCD      VARCHAR(8), \
    SHUSHOKU_ADDR1      VARCHAR(150), \
    SHUSHOKU_ADDR2      VARCHAR(150), \
    SHUSHOKU_TELNO1     VARCHAR(14), \
    SHUSHOKU_TELNO2     VARCHAR(14), \
    SHUSHOKU_FAXNO      VARCHAR(14), \
    TONINZU             INT, \
    COMPANY_SORT        CHAR(2), \
    COMPANY_SORT_REMARK VARCHAR(60), \
    TARGET_NINZU        SMALLINT, \
    TARGET_SEX          CHAR(1), \
    JUDGING_MEANS1      VARCHAR(2), \
    JUDGING_MEANS2      VARCHAR(2), \
    JUDGING_MEANS3      VARCHAR(2), \
    JUDGING_MEANS4      VARCHAR(2), \
    HOLIDAY1            VARCHAR(1), \
    HOLIDAY2            VARCHAR(1), \
    HOLIDAY3            VARCHAR(1), \
    HOLIDAY4            VARCHAR(1), \
    HOLIDAY_REMARK      VARCHAR(50), \
    SHIFT               VARCHAR(1), \
    COMPANY_LODGING     VARCHAR(1), \
    BASIC_SALARY        INT, \
    TAKE_SALARY         INT, \
    KANKATSU            VARCHAR(4), \
    JOBTYPE_LCD         VARCHAR(2), \
    JOBTYPE_MCD         VARCHAR(2), \
    JOBTYPE_SCD         VARCHAR(3), \
    TSUKIN_NINZU        SMALLINT, \
    SUMIKOMI_NINZU      SMALLINT, \
    FUMON_NINZU         SMALLINT, \
    SUISEN_NINZU        SMALLINT, \
    NIJI_BOSYU          VARCHAR(1), \
    TSUKIN_SALARY       INT, \
    SUMIKOMI_SALARY     INT, \
    SELECT_RECEPT_DATE  DATE, \
    SELECT_DATE         DATE, \
    KENGAKU_KAI         VARCHAR(1), \
    OTHER_HOLIDAY       VARCHAR(150), \
    REMARK              VARCHAR(150), \
    EMPLOYMENT_STATUS   VARCHAR(3), \
    APPLICATION_TARGET  VARCHAR(60), \
    PERSONNEL_MANAGER   VARCHAR(30), \
    DEPARTMENT_POSITION VARCHAR(40), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO JOB_OFFER_DAT \
    SELECT \
        YEAR             , \
        SENKOU_NO        , \
        ACCEPTANCE_DATE  , \
        COMPANY_CD       , \
        COMPANY_NAME     , \
        COMPANY_NAMEKANA , \
        COMPANY_CONTENTS , \
        SHIHONKIN        , \
        MAKECOMPANY_YEAR , \
        COMPANY_ZIPCD   , \
        COMPANY_ADDR1   , \
        COMPANY_ADDR2   , \
        COMPANY_TELNO1  , \
        COMPANY_TELNO2  , \
        COMPANY_FAXNO   , \
        INDUSTRY_SCD    , \
        SONINZU         , \
        SHUSHOKU_NAME   , \
        SHUSHOKU_ZIPCD  , \
        SHUSHOKU_ADDR1  , \
        SHUSHOKU_ADDR2  , \
        SHUSHOKU_TELNO1 , \
        SHUSHOKU_TELNO2 , \
        SHUSHOKU_FAXNO  , \
        TONINZU         , \
        COMPANY_SORT    , \
        CAST(NULL AS VARCHAR(60)) AS COMPANY_SORT_REMARK, \
        TARGET_NINZU    , \
        TARGET_SEX      , \
        JUDGING_MEANS1  , \
        JUDGING_MEANS2  , \
        JUDGING_MEANS3  , \
        JUDGING_MEANS4  , \
        HOLIDAY1        , \
        HOLIDAY2        , \
        HOLIDAY3        , \
        HOLIDAY4        , \
        HOLIDAY_REMARK  , \
        SHIFT           , \
        COMPANY_LODGING , \
        BASIC_SALARY    , \
        TAKE_SALARY     , \
        KANKATSU        , \
        JOBTYPE_LCD     , \
        JOBTYPE_MCD     , \
        JOBTYPE_SCD     , \
        TSUKIN_NINZU    , \
        SUMIKOMI_NINZU  , \
        FUMON_NINZU     , \
        SUISEN_NINZU    , \
        NIJI_BOSYU      , \
        TSUKIN_SALARY   , \
        SUMIKOMI_SALARY , \
        SELECT_RECEPT_DATE, \
        SELECT_DATE     , \
        KENGAKU_KAI     , \
        OTHER_HOLIDAY   , \
        REMARK          , \
        EMPLOYMENT_STATUS, \
        CAST(NULL AS VARCHAR(60)) AS APPLICATION_TARGET, \
        PERSONNEL_MANAGER, \
        DEPARTMENT_POSITION, \
        REGISTERCD, \
        UPDATED \
    FROM \
        JOB_OFFER_DAT_OLD

ALTER TABLE JOB_OFFER_DAT ADD CONSTRAINT PK_JOB_OFFER_DAT PRIMARY KEY (YEAR, SENKOU_NO)