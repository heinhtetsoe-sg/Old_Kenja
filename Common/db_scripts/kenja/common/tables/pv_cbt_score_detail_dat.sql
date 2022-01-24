
create table PV_CBT_SCORE_DETAIL_DAT( \
    RECORD_KUBUN      VARCHAR(2)   NOT NULL, \
    YEAR              VARCHAR(4)   NOT NULL, \
    SCHOOL_CD         VARCHAR(30) , \
    KNJID             VARCHAR(10)  NOT NULL, \
    TEST_ID           VARCHAR(10)  NOT NULL, \
    TAKE_CNT          SMALLINT     NOT NULL, \
    TAKE_GRADE        VARCHAR(2)   NOT NULL, \
    TEST_KIND_CD      VARCHAR(5)   NOT NULL, \
    PART_ID           VARCHAR(10)  NOT NULL, \
    PART_LEVEL        VARCHAR(2)  , \
    QUESTION_NO       VARCHAR(15)  NOT NULL, \
    QUESTION_LEVEL    VARCHAR(2)  , \
    QUESTION_POINT    SMALLINT    , \
    QUESTION_SCORE    VARCHAR(5)  , \
    QUESTION_FIELDNO  VARCHAR(10) , \
    RESISTERCD        VARCHAR(10) , \
    UPDATED           TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table PV_CBT_SCORE_DETAIL_DAT add constraint PK_PV_CBT_SCORE_DETAIL_DAT primary key (YEAR,KNJID,TEST_ID,TAKE_CNT,TAKE_GRADE,PART_ID,QUESTION_NO)

