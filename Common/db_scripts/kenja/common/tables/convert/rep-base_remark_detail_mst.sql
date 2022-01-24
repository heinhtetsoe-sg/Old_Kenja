-- $Id: 4a8a505217505e1e70dcd8b63ba04f3158ebf125 $
DROP TABLE BASE_REMARK_DETAIL_MST_OLD
RENAME TABLE BASE_REMARK_DETAIL_MST TO BASE_REMARK_DETAIL_MST_OLD
CREATE TABLE BASE_REMARK_DETAIL_MST( \
    CODE                     VARCHAR(2)   NOT NULL, \
    SEQ                      VARCHAR(2)   NOT NULL, \
    NAME                     VARCHAR(45), \
    QUESTION_CONTENTS        VARCHAR(1000), \
    ANSWER_PATTERN           VARCHAR(1), \
    ANSWER_SELECT_COUNT      VARCHAR(2), \
    REGISTERCD               VARCHAR(10), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE BASE_REMARK_DETAIL_MST ADD CONSTRAINT PK_BASE_RMRK_DTL_M PRIMARY KEY (CODE, SEQ)

INSERT INTO BASE_REMARK_DETAIL_MST \
    SELECT \
    CODE       , \
    SEQ        , \
    NAME       , \
    CAST(NULL AS VARCHAR(1000)) AS QUESTION_CONTENTS  , \
    CAST(NULL AS VARCHAR(1))    AS ANSWER_PATTERN     , \
    CAST(NULL AS VARCHAR(2))    AS ANSWER_SELECT_COUN ,  \
    REGISTERCD , \
    UPDATED     \
    FROM \
        BASE_REMARK_DETAIL_MST_OLD