-- $Id: 3e25de06001d5ebd12377929d3ba6a840c668178 $

CREATE VIEW V_CREDIT_SPECIAL_MST \
    ( \
        YEAR, \
        COURSECD, \
        MAJORCD, \
        GRADE, \
        COURSECODE, \
        SPECIAL_GROUP_CD, \
        CREDITS, \
        ABSENCE_HIGH, \
        GET_ABSENCE_HIGH, \
        ABSENCE_WARN_SHUTOKU_SEM1, \
        ABSENCE_WARN_RISHU_SEM1, \
        ABSENCE_WARN_SHUTOKU_SEM2, \
        ABSENCE_WARN_RISHU_SEM2, \
        ABSENCE_WARN_SHUTOKU_SEM3, \
        ABSENCE_WARN_RISHU_SEM3, \
        ABSENCE_WARN, \
        ABSENCE_WARN2, \
        ABSENCE_WARN3, \
        REQUIRE_FLG, \
        AUTHORIZE_FLG, \
        COMP_UNCONDITION_FLG, \
        UPDATED \
    ) AS \
SELECT \
    YEAR, \
    COURSECD, \
    MAJORCD, \
    GRADE, \
    COURSECODE, \
    SPECIAL_GROUP_CD, \
    CREDITS, \
    ABSENCE_HIGH, \
    GET_ABSENCE_HIGH, \
    (ABSENCE_WARN) AS ABSENCE_WARN_SHUTOKU_SEM1, \
    (ABSENCE_WARN) AS ABSENCE_WARN_RISHU_SEM1, \
    (ABSENCE_WARN2) AS ABSENCE_WARN_SHUTOKU_SEM2, \
    (ABSENCE_WARN2) AS ABSENCE_WARN_RISHU_SEM2, \
    (ABSENCE_WARN3) AS ABSENCE_WARN_SHUTOKU_SEM3, \
    (ABSENCE_WARN3) AS ABSENCE_WARN_RISHU_SEM3, \
    ABSENCE_WARN, \
    ABSENCE_WARN2, \
    ABSENCE_WARN3, \
    REQUIRE_FLG, \
    AUTHORIZE_FLG, \
    COMP_UNCONDITION_FLG, \
    UPDATED \
FROM \
    CREDIT_SPECIAL_MST