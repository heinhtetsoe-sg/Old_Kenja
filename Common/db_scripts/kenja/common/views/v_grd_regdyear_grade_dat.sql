-- $Id: 46cd403e41d8b5f431f8cb399f25e347dcca5caa $

DROP VIEW V_GRD_REGDYEAR_GRADE_DAT

CREATE VIEW V_GRD_REGDYEAR_GRADE_DAT \
    (SCHREGNO, \
     YEAR, \
     SEMESTER, \
     GRADE, \
     ANNUAL, \
     HR_CLASS, \
     ATTENDNO, \
     COURSECD, \
     MAJORCD, \
     COURSECODE \
    ) \
AS \
SELECT \
       SCHREGNO, \
       YEAR, \
       SEMESTER, \
       GRADE, \
       ANNUAL, \
       HR_CLASS, \
       ATTENDNO, \
       COURSECD, \
       MAJORCD, \
       COURSECODE \
  FROM GRD_REGD_DAT TBL \
 WHERE (SCHREGNO, YEAR, GRADE, SEMESTER) IN \
       ( \
        SELECT SCHREGNO, YEAR, GRADE, MAX(SEMESTER) \
          FROM GRD_REGD_DAT CHK \
         WHERE CHK.SCHREGNO = TBL.SCHREGNO \
           AND CHK.GRADE    = TBL.GRADE \
           AND (SCHREGNO, GRADE, YEAR) IN \
               ( \
                SELECT SCHREGNO, GRADE, MAX(YEAR) \
                  FROM GRD_REGD_DAT WK \
                 WHERE WK.SCHREGNO = CHK.SCHREGNO \
                   AND (WK.SCHREGNO,WK.YEAR,WK.SEMESTER) IN \
                       (SELECT WK2.SCHREGNO,WK2.YEAR,MAX(WK2.SEMESTER) \
                          FROM GRD_REGD_DAT WK2 \
                        GROUP BY WK2.SCHREGNO,WK2.YEAR \
                       ) \
                 GROUP BY WK.SCHREGNO, WK.GRADE \
               ) \      
        GROUP BY CHK.SCHREGNO, CHK.YEAR, CHK.GRADE \
       )

