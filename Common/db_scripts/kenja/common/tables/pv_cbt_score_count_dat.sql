
create table PV_CBT_SCORE_COUNT_DAT( \
    RECORD_KUBUN   VARCHAR(2)   NOT NULL, \
    YEAR           VARCHAR(4)   NOT NULL, \
    SCHOOL_CD      VARCHAR(30) , \
    KNJID          VARCHAR(10)  NOT NULL, \
    TEST_ID        VARCHAR(10)  NOT NULL, \
    TAKE_CNT       SMALLINT     NOT NULL, \
    TAKE_GRADE     VARCHAR(2)   NOT NULL, \
    TEST_KIND_CD   VARCHAR(5)   NOT NULL, \
    CLASSCD        VARCHAR(2)   NOT NULL, \
    SUBCLASSCD     VARCHAR(2)   NOT NULL, \
    TEST_LEVEL_CD  VARCHAR(2)  , \
    TAKE_TURN      SMALLINT    , \
    START_DATE     TIMESTAMP   , \
    END_DATE       TIMESTAMP   , \
    POINT          SMALLINT    , \
    GET_POINT      SMALLINT    , \
    PART_CNT       SMALLINT    , \
    REGISTERCD     VARCHAR(10) , \
    UPDATED        TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table PV_CBT_SCORE_COUNT_DAT add constraint PK_PV_CBT_SCORE_COUNT_DAT primary key (YEAR,KNJID,TEST_ID,TAKE_CNT,TAKE_GRADE)

