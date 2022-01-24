-- kanji=����
-- $Id: 0f7cf887216f61ed7ae5412f2fae065ce5f94146 $

DROP VIEW V_APPOINTED_DAY_GRADE_MST

CREATE VIEW V_APPOINTED_DAY_GRADE_MST \
    (YEAR, \
     MONTH, \
     SEMESTER, \
     GRADE, \
     APPOINTED_DAY, \
     REGISTERCD, \
     UPDATED \
    ) \
AS SELECT \
     GDAT.YEAR, \
     APP.MONTH, \
     APP.SEMESTER, \
     GDAT.GRADE, \
     CASE WHEN APPG.APPOINTED_DAY IS NOT NULL THEN APPG.APPOINTED_DAY ELSE APP.APPOINTED_DAY END AS APPOINTED_DAY, \
     GDAT.REGISTERCD, \
     GDAT.UPDATED \
FROM \
    SCHREG_REGD_GDAT GDAT \
    INNER JOIN APPOINTED_DAY_MST APP ON GDAT.YEAR = APP.YEAR \
         AND GDAT.SCHOOL_KIND = APP.SCHOOL_KIND \
    LEFT JOIN APPOINTED_DAY_GRADE_MST APPG ON APP.YEAR = APPG.YEAR \
         AND APP.MONTH = APPG.MONTH \
         AND APP.SEMESTER = APPG.SEMESTER \
         AND GDAT.GRADE = APPG.GRADE