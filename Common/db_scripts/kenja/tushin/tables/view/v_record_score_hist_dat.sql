-- $Id: v_record_score_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP VIEW V_RECORD_SCORE_HIST_DAT

CREATE VIEW V_RECORD_SCORE_HIST_DAT \
    (YEAR, \
     SEMESTER, \
     TESTKINDCD, \
     TESTITEMCD, \
     SCORE_DIV, \
     CLASSCD, \
     SCHOOL_KIND, \
     CURRICULUM_CD, \
     SUBCLASSCD, \
     SCHREGNO, \
     SEQ, \
     TEST_DATE, \
     CHAIRCD, \
     SCORE, \
     VALUE, \
     VALUE_DI, \
     GET_CREDIT, \
     ADD_CREDIT, \
     COMP_TAKESEMES, \
     COMP_CREDIT, \
     COMP_CONTINUE, \
     REGISTERCD, \
     UPDATED \
    ) \
AS \
SELECT \
    T1.* \
FROM \
    RECORD_SCORE_HIST_DAT T1, \
    (SELECT \
         YEAR, \
         SEMESTER, \
         TESTKINDCD, \
         TESTITEMCD, \
         SCORE_DIV, \
         CLASSCD, \
         SCHOOL_KIND, \
         CURRICULUM_CD, \
         SUBCLASSCD, \
         SCHREGNO, \
         MAX(SEQ) AS SEQ \
     FROM \
         RECORD_SCORE_HIST_DAT \
     GROUP BY \
         YEAR, \
         SEMESTER, \
         TESTKINDCD, \
         TESTITEMCD, \
         SCORE_DIV, \
         CLASSCD, \
         SCHOOL_KIND, \
         CURRICULUM_CD, \
         SUBCLASSCD, \
         SCHREGNO \
    ) T2 \
WHERE \
    T1.YEAR = T2.YEAR \
    AND T1.SEMESTER = T2.SEMESTER \
    AND T1.TESTKINDCD = T2.TESTKINDCD \
    AND T1.TESTITEMCD = T2.TESTITEMCD \
    AND T1.SCORE_DIV = T2.SCORE_DIV \
    AND T1.CLASSCD = T2.CLASSCD \
    AND T1.SCHOOL_KIND = T2.SCHOOL_KIND \
    AND T1.CURRICULUM_CD = T2.CURRICULUM_CD \
    AND T1.SUBCLASSCD = T2.SUBCLASSCD \
    AND T1.SCHREGNO = T2.SCHREGNO \
    AND T1.SEQ = T2.SEQ
