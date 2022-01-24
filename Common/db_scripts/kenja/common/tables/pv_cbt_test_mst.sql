
create table PV_CBT_TEST_MST( \
    PART_ID           VARCHAR(10)  NOT NULL, \
    PART_POINT        SMALLINT    , \
    KUBUN             VARCHAR(2)  , \
    TYPE              VARCHAR(2)  , \
    PART_LEVEL        VARCHAR(2)  , \
    PART_FIELDNO      VARCHAR(10) , \
    USE_FLG           VARCHAR(2)  , \
    QUESTION_NO       VARCHAR(15)  NOT NULL, \
    QUESTION_ORDER    VARCHAR(2)  , \
    QUESTION_LEVEL    VARCHAR(2)  , \
    QUESTION_FIELDNO  VARCHAR(10) , \
    QUESTION_POINT    SMALLINT    , \
    REGISTERCD        VARCHAR(10) , \
    UPDATED           TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table PV_CBT_TEST_MST add constraint PK_PV_CBT_TEST_MST primary key (PART_ID,QUESTION_NO)

