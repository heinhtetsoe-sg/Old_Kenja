
create table PV_CBT_TESTKIND_MST( \
    TEST_ID         VARCHAR(10)  NOT NULL, \
    TEST_KIND_CD    VARCHAR(5)   NOT NULL, \
    TEST_KIND_NAME  VARCHAR(60) , \
    GRADE           VARCHAR(2)  , \
    SUBCLASSCD      VARCHAR(2)  , \
    TEST_LEVEL_CD   VARCHAR(2)  , \
    TEST_COUNT      SMALLINT    , \
    REGISTERCD      VARCHAR(10) , \
    UPDATED         TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table PV_CBT_TESTKIND_MST add constraint PK_PV_CBT_TESTKIND_MST primary key (TEST_ID,TEST_KIND_CD)

