-- $Id: 5449f5b63ecf04977a3d86ac96bd8d320c3272ae $

DROP VIEW V_STAFF_HR_DAT

CREATE VIEW V_STAFF_HR_DAT ( \
    YEAR, \
    SEMESTER, \
    SCHOOL_KIND, \
    GRADE_CD, \
    GRADE, \
    HR_CLASS, \
    STAFFCD, \
    HR_NAME, \
    HR_NAMEABBV, \
    GRADE_NAME1, \
    GRADE_NAME2, \
    GRADE_NAME3, \
    GRADE_NAME, \
    HR_CLASS_NAME1, \
    HR_CLASS_NAME2, \
    HR_FACCD, \
    CLASSWEEKS, \
    CLASSDAYS \
) AS \
SELECT \
    STAFF_T.YEAR, \
    STAFF_T.SEMESTER, \
    RG_DAT.SCHOOL_KIND, \
    RG_DAT.GRADE_CD, \
    STAFF_T.GRADE, \
    STAFF_T.HR_CLASS, \
    STAFF_T.STAFFCD, \
    STAFF_T.HR_NAME, \
    STAFF_T.HR_NAMEABBV, \
    RG_DAT.GRADE_NAME1, \
    RG_DAT.GRADE_NAME2, \
    RG_DAT.GRADE_NAME3, \
    STAFF_T.GRADE_NAME, \
    STAFF_T.HR_CLASS_NAME1, \
    STAFF_T.HR_CLASS_NAME2, \
    STAFF_T.HR_FACCD, \
    STAFF_T.CLASSWEEKS, \
    STAFF_T.CLASSDAYS \
FROM \
    ( \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        TR_CD1 AS STAFFCD, \
        HR_NAME, \
        HR_NAMEABBV, \
        GRADE_NAME, \
        HR_CLASS_NAME1, \
        HR_CLASS_NAME2, \
        HR_FACCD, \
        CLASSWEEKS, \
        CLASSDAYS \
    FROM \
        SCHREG_REGD_HDAT \
    WHERE \
        TR_CD1 IS NOT NULL \
    UNION \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        TR_CD2 AS STAFFCD, \
        HR_NAME, \
        HR_NAMEABBV, \
        GRADE_NAME, \
        HR_CLASS_NAME1, \
        HR_CLASS_NAME2, \
        HR_FACCD, \
        CLASSWEEKS, \
        CLASSDAYS \
    FROM \
        SCHREG_REGD_HDAT \
    WHERE \
        TR_CD2 IS NOT NULL \
    UNION \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        TR_CD3 AS STAFFCD, \
        HR_NAME, \
        HR_NAMEABBV, \
        GRADE_NAME, \
        HR_CLASS_NAME1, \
        HR_CLASS_NAME2, \
        HR_FACCD, \
        CLASSWEEKS, \
        CLASSDAYS \
    FROM \
        SCHREG_REGD_HDAT \
    WHERE \
        TR_CD3 IS NOT NULL \
    UNION \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        SUBTR_CD1 AS STAFFCD, \
        HR_NAME, \
        HR_NAMEABBV, \
        GRADE_NAME, \
        HR_CLASS_NAME1, \
        HR_CLASS_NAME2, \
        HR_FACCD, \
        CLASSWEEKS, \
        CLASSDAYS \
    FROM \
        SCHREG_REGD_HDAT \
    WHERE \
        SUBTR_CD1 IS NOT NULL \
    UNION \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        SUBTR_CD2 AS STAFFCD, \
        HR_NAME, \
        HR_NAMEABBV, \
        GRADE_NAME, \
        HR_CLASS_NAME1, \
        HR_CLASS_NAME2, \
        HR_FACCD, \
        CLASSWEEKS, \
        CLASSDAYS \
    FROM \
        SCHREG_REGD_HDAT \
    WHERE \
        SUBTR_CD2 IS NOT NULL \
    UNION \
    SELECT \
        YEAR, \
        SEMESTER, \
        GRADE, \
        HR_CLASS, \
        SUBTR_CD3 AS STAFFCD, \
        HR_NAME, \
        HR_NAMEABBV, \
        GRADE_NAME, \
        HR_CLASS_NAME1, \
        HR_CLASS_NAME2, \
        HR_FACCD, \
        CLASSWEEKS, \
        CLASSDAYS \
    FROM \
        SCHREG_REGD_HDAT \
    WHERE \
        SUBTR_CD3 IS NOT NULL \
    ) STAFF_T \
    LEFT JOIN SCHREG_REGD_GDAT RG_DAT ON STAFF_T.YEAR = RG_DAT.YEAR \
         AND STAFF_T.GRADE = RG_DAT.GRADE \
GROUP BY \
    STAFF_T.YEAR, \
    STAFF_T.SEMESTER, \
    RG_DAT.SCHOOL_KIND, \
    RG_DAT.GRADE_CD, \
    STAFF_T.GRADE, \
    STAFF_T.HR_CLASS, \
    STAFF_T.STAFFCD, \
    STAFF_T.HR_NAME, \
    STAFF_T.HR_NAMEABBV, \
    RG_DAT.GRADE_NAME1, \
    RG_DAT.GRADE_NAME2, \
    RG_DAT.GRADE_NAME3, \
    STAFF_T.GRADE_NAME, \
    STAFF_T.HR_CLASS_NAME1, \
    STAFF_T.HR_CLASS_NAME2, \
    STAFF_T.HR_FACCD, \
    STAFF_T.CLASSWEEKS, \
    STAFF_T.CLASSDAYS
