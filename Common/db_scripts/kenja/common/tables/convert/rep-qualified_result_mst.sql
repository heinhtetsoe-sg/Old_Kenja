-- $Id: 6d75c2f51159083169030c37a2e43959c600df12 $

DROP TABLE QUALIFIED_RESULT_MST_OLD
RENAME TABLE QUALIFIED_RESULT_MST TO QUALIFIED_RESULT_MST_OLD
create table QUALIFIED_RESULT_MST \
(  \
    YEAR                VARCHAR(4)  not null, \
    QUALIFIED_CD        VARCHAR(4)  not null, \
    RESULT_CD           VARCHAR(4)  not null, \
    RESULT_NAME         VARCHAR(60), \
    RESULT_NAME_ABBV    VARCHAR(50), \
    CERT_FLG            VARCHAR(1), \
    LIMITED_PERIOD      INTEGER, \
    RESULT_LEVEL        INTEGER, \
    NOT_PRINT           VARCHAR(1), \
    SCORE               SMALLINT, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE QUALIFIED_RESULT_MST ADD CONSTRAINT PK_QUALIFIED_RE_M PRIMARY KEY (YEAR, QUALIFIED_CD, RESULT_CD)

INSERT INTO QUALIFIED_RESULT_MST \
    SELECT \
        YEAR                , \
        QUALIFIED_CD        , \
        RESULT_CD           , \
        RESULT_NAME         , \
        RESULT_NAME_ABBV    , \
        CERT_FLG            , \
        LIMITED_PERIOD      , \
        RESULT_LEVEL        , \
        CAST(NULL AS VARCHAR(1)) AS NOT_PRINT , \
        CAST(NULL AS SMALLINT) AS SCORE       , \
        REGISTERCD          , \
        UPDATED               \
    FROM \
        QUALIFIED_RESULT_MST_OLD
