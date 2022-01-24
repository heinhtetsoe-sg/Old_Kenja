-- $Id: 009243ed9d59fb271da53e4970a684050e6aa740 $

DROP VIEW V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT

CREATE VIEW V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT \
   (YEAR, \
    SEMESTER, \
    SCHREGNO, \
    GAKUBU_SCHOOL_KIND, \
    GHR_CD, \
    GRADE, \
    HR_CLASS, \
    CONDITION, \
    GROUPCD, \
    CLASSCD, \
    SCHOOL_KIND, \
    CURRICULUM_CD, \
    SUBCLASSCD, \
    UNITCD, \
    REGISTERCD, \
    UPDATED) \
AS \
SELECT \
    T1.YEAR, \
    T1.SEMESTER, \
    T1.SCHREGNO, \
    T1.GAKUBU_SCHOOL_KIND, \
    T1.GHR_CD, \
    T1.GRADE, \
    T1.HR_CLASS, \
    T1.CONDITION, \
    T1.GROUPCD, \
    T2.CLASSCD, \
    T2.SCHOOL_KIND, \
    T2.CURRICULUM_CD, \
    T2.SUBCLASSCD, \
    T3.UNITCD, \
    T1.REGISTERCD, \
    T1.UPDATED \
FROM \
    GRADE_KIND_SCHREG_GROUP_DAT T1 \
    LEFT JOIN GRADE_KIND_COMP_GROUP_DAT T2  ON T1.YEAR = T2.YEAR \
                                           AND T1.SEMESTER = T2.SEMESTER \
                                           AND T1.GAKUBU_SCHOOL_KIND = T2.GAKUBU_SCHOOL_KIND \
                                           AND T1.GHR_CD = T2.GHR_CD \
                                           AND T1.GRADE = T2.GRADE \
                                           AND T1.HR_CLASS = T2.HR_CLASS \
                                           AND T1.CONDITION = T2.CONDITION \
                                           AND T1.GROUPCD = T2.GROUPCD \
    LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST T3 ON T1.YEAR = T3.YEAR \
                                           AND T1.SEMESTER = T3.SEMESTER \
                                           AND T1.GAKUBU_SCHOOL_KIND = T3.GAKUBU_SCHOOL_KIND \
                                           AND T1.GHR_CD = T3.GHR_CD \
                                           AND T1.GRADE = T3.GRADE \
                                           AND T1.HR_CLASS = T3.HR_CLASS \
                                           AND T1.CONDITION = T3.CONDITION \
                                           AND T1.GROUPCD = T3.GROUPCD \
                                           AND T2.CLASSCD = T3.CLASSCD \
                                           AND T2.SCHOOL_KIND = T3.SCHOOL_KIND \
                                           AND T2.CURRICULUM_CD = T3.CURRICULUM_CD \
                                           AND T2.SUBCLASSCD = T3.SUBCLASSCD