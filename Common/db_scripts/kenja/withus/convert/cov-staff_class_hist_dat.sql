-- kanji=漢字
-- $Id: cov-staff_class_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

INSERT INTO STAFF_CLASS_HIST_DAT ( \
    YEAR, \
    SEMESTER, \
    GRADE, \
    HR_CLASS, \
    TR_DIV, \
    FROM_DATE, \
    TO_DATE, \
    STAFFCD, \
    REGISTERCD, \
    UPDATED \
    ) \
WITH REGD AS ( \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        '1' AS TR_DIV, \
        TR_CD1 AS STAFFCD \
    FROM \
        SCHREG_REGD_HDAT \
    UNION ALL \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        '2' AS TR_DIV, \
        TR_CD2 AS STAFFCD \
    FROM \
        SCHREG_REGD_HDAT \
    UNION ALL \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        '3' AS TR_DIV, \
        TR_CD3 AS STAFFCD \
    FROM \
        SCHREG_REGD_HDAT \
    UNION ALL \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        '4' AS TR_DIV, \
        SUBTR_CD1 AS STAFFCD \
    FROM \
        SCHREG_REGD_HDAT \
    UNION ALL \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        '5' AS TR_DIV, \
        SUBTR_CD2 AS STAFFCD \
    FROM \
        SCHREG_REGD_HDAT \
    UNION ALL \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        '6' AS TR_DIV, \
        SUBTR_CD3 AS STAFFCD \
    FROM \
        SCHREG_REGD_HDAT \
    ) \
, HIST AS ( \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS \
    FROM \
        STAFF_CLASS_HIST_DAT \
    GROUP BY \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS \
    ) \
SELECT \
    REGD.YEAR, \
    REGD.SEMESTER, \
    REGD.GRADE, \
    REGD.HR_CLASS, \
    REGD.TR_DIV, \
    DATE(REGD.YEAR || '-04-01') AS FROM_DATE, \
    DATE(RTRIM(CHAR(SMALLINT(REGD.YEAR) + 1)) || '-03-31') AS TO_DATE, \
    REGD.STAFFCD, \
    'alp' AS REGISTERCD, \
    sysdate() AS UPDATED \
FROM \
    REGD \
WHERE \
    REGD.STAFFCD IS NOT NULL AND \
    NOT EXISTS( \
        SELECT \
            'X' \
        FROM \
            HIST \
        WHERE \
            HIST.YEAR = REGD.YEAR AND \
            HIST.SEMESTER = REGD.SEMESTER AND \
            HIST.GRADE = REGD.GRADE AND \
            HIST.HR_CLASS = REGD.HR_CLASS \
    )
