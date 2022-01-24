<?php

require_once('for_php7.php');
class knjd321cquery extends Query
{

    //データ出力用ＳＱＬ
    public function getYear($model)
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //データ出力用ＳＱＬ
    public function getMainSQL($model)
    {
        $query  = " WITH KIN_REC AS (";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     JUDGE_PATTERN, ";
        $query .= "     A_PATTERN_ASSESS, ";
        $query .= "     B_PATTERN_ASSESS, ";
        $query .= "     C_PATTERN_ASSESS ";
        $query .= " FROM ";
        $query .= "     KIN_RECORD_DAT ";
        $query .= " WHERE ";
        if ($model->field["YEAR"]) {
            $query .= "     YEAR = '{$model->field["YEAR"]}' AND ";
        }
        $query .= "     ((JUDGE_PATTERN = 'A' AND A_PATTERN_ASSESS = '1') OR ";
        $query .= "      (JUDGE_PATTERN = 'B' AND B_PATTERN_ASSESS = '1') OR ";
        $query .= "      (JUDGE_PATTERN = 'C' AND C_PATTERN_ASSESS = '1')) ";
        $query .= " ), REGD_MAX_SEME AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     MAX(T1.SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     KIN_REC T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR ";
        $query .= " ), REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     REGD_MAX_SEME T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        //欠課時数
        $query .= " ), ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         SUM(VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "                            - VALUE(T1.VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "                            - VALUE(T1.KOUDOME,0) ";
        }
        $query .= "                                - VALUE(T1.MOURNING,0)) AS LESSON_JISUU, ";
        $query .= "        (SUM(VALUE(T1.LATE,0) + VALUE(T1.EARLY,0)) / 3) + ";
        $query .= "         SUM(VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) + VALUE(T1.NURSEOFF,0)) AS KEKKA_JISUU ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT T1 ";
        $query .= "         INNER JOIN REGD_T T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = T2.YEAR ";
        $query .= "             AND NOT EXISTS ( ";
        $query .= "                             SELECT ";
        $query .= "                                 'x' ";
        $query .= "                             FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT E1 ";
        $query .= "                             WHERE ";
        $query .= "                                 T1.YEAR = E1.YEAR ";
        $query .= "                                 AND E1.REPLACECD = '1' ";
        $query .= "                                 AND T1.CLASSCD = E1.COMBINED_CLASSCD ";
        $query .= "                                 AND T1.SCHOOL_KIND = E1.COMBINED_SCHOOL_KIND ";
        $query .= "                                 AND T1.CURRICULUM_CD = E1.COMBINED_CURRICULUM_CD ";
        $query .= "                                 AND T1.SUBCLASSCD = E1.COMBINED_SUBCLASSCD ";
        $query .= "                           ) ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         E1.COMBINED_CLASSCD AS CLASSCD, ";
        $query .= "         E1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
        $query .= "         E1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
        $query .= "         E1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "         SUM(VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "                            - VALUE(T1.VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "                            - VALUE(T1.KOUDOME,0) ";
        }
        $query .= "                                - VALUE(T1.MOURNING,0)) AS LESSON_JISUU, ";
        $query .= "        (SUM(VALUE(T1.LATE,0) + VALUE(T1.EARLY,0)) / 3) + ";
        $query .= "         SUM(VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) + VALUE(T1.NURSEOFF,0)) AS KEKKA_JISUU ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT T1 ";
        $query .= "         INNER JOIN REGD_T T2 ON T2.YEAR = T1.YEAR ";
        $query .= "               AND T2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT E1 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = T2.YEAR ";
        $query .= "             AND E1.REPLACECD = '1' ";
        $query .= "             AND T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.CLASSCD = E1.ATTEND_CLASSCD ";
        $query .= "             AND T1.SCHOOL_KIND = E1.ATTEND_SCHOOL_KIND ";
        $query .= "             AND T1.CURRICULUM_CD = E1.ATTEND_CURRICULUM_CD ";
        $query .= "             AND T1.SUBCLASSCD = E1.ATTEND_SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         E1.COMBINED_CLASSCD, ";
        $query .= "         E1.COMBINED_SCHOOL_KIND, ";
        $query .= "         E1.COMBINED_CURRICULUM_CD, ";
        $query .= "         E1.COMBINED_SUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     KIN_REC.SCHREGNO, ";
        $query .= "     SCH_B.NAME, ";
        $query .= "     KIN_REC.YEAR, ";
        $query .= "     REGD_T.GRADE, ";
        $query .= "     KIN_REC.CLASSCD, ";
        $query .= "     KIN_REC.SCHOOL_KIND, ";
        $query .= "     KIN_REC.CURRICULUM_CD, ";
        $query .= "     KIN_REC.SUBCLASSCD, ";
        $query .= "     SUB_M.SUBCLASSNAME, ";
        $query .= "     KIN_REC.JUDGE_PATTERN, ";
        $query .= "     CASE KIN_REC.JUDGE_PATTERN ";
        $query .= "          WHEN 'A' THEN KIN_REC.A_PATTERN_ASSESS ";
        $query .= "          WHEN 'B' THEN KIN_REC.B_PATTERN_ASSESS ";
        $query .= "          WHEN 'C' THEN KIN_REC.C_PATTERN_ASSESS ";
        $query .= "          ELSE '' ";
        $query .= "     END AS PATTERN_ASSESS, ";
        $query .= "     CRE_M.CREDITS, ";
        $query .= "     ((CRE_M.CREDITS * (CASE WHEN REGD_T.GRADE = '03' THEN 8 ELSE 10 END)) + 1) AS JOUGEN, ";
        $query .= "     ATTE_S.KEKKA_JISUU, ";
        $query .= "     CASE WHEN ((CRE_M.CREDITS * (CASE WHEN REGD_T.GRADE = '03' THEN 8 ELSE 10 END)) + 1) < ATTE_S.KEKKA_JISUU ";
        $query .= "          THEN 'レ' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS KEKKA_JISUU_OVER, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     KIN_REC ";
        $query .= "     LEFT JOIN REGD_T ON KIN_REC.SCHREGNO = REGD_T.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST SCH_B ON KIN_REC.SCHREGNO = SCH_B.SCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON KIN_REC.CLASSCD = SUB_M.CLASSCD ";
        $query .= "          AND KIN_REC.SCHOOL_KIND = SUB_M.SCHOOL_KIND ";
        $query .= "          AND KIN_REC.CURRICULUM_CD = SUB_M.CURRICULUM_CD ";
        $query .= "          AND KIN_REC.SUBCLASSCD = SUB_M.SUBCLASSCD ";
        $query .= "     LEFT JOIN CREDIT_MST CRE_M ON REGD_T.YEAR = CRE_M.YEAR ";
        $query .= "          AND REGD_T.COURSECD = CRE_M.COURSECD ";
        $query .= "          AND REGD_T.MAJORCD = CRE_M.MAJORCD ";
        $query .= "          AND REGD_T.GRADE = CRE_M.GRADE ";
        $query .= "          AND REGD_T.COURSECODE = CRE_M.COURSECODE ";
        $query .= "          AND KIN_REC.CLASSCD = CRE_M.CLASSCD ";
        $query .= "          AND KIN_REC.SCHOOL_KIND = CRE_M.SCHOOL_KIND ";
        $query .= "          AND KIN_REC.CURRICULUM_CD = CRE_M.CURRICULUM_CD ";
        $query .= "          AND KIN_REC.SUBCLASSCD = CRE_M.SUBCLASSCD ";
        $query .= "     LEFT JOIN ATTEND_SUBCLASS ATTE_S ON KIN_REC.SCHREGNO = ATTE_S.SCHREGNO ";
        $query .= "          AND KIN_REC.CLASSCD = ATTE_S.CLASSCD ";
        $query .= "          AND KIN_REC.SCHOOL_KIND = ATTE_S.SCHOOL_KIND ";
        $query .= "          AND KIN_REC.CURRICULUM_CD = ATTE_S.CURRICULUM_CD ";
        $query .= "          AND KIN_REC.SUBCLASSCD = ATTE_S.SUBCLASSCD ";
        if ($model->field["CHK_OVER"] == "on") {
            $query .= " WHERE ";
            $query .= "     ((CRE_M.CREDITS * (CASE WHEN REGD_T.GRADE = '03' THEN 8 ELSE 10 END)) + 1) < ATTE_S.KEKKA_JISUU ";
        }
        $query .= " ORDER BY ";
        $query .= "     KIN_REC.SCHREGNO, ";
        $query .= "     REGD_T.GRADE, ";
        $query .= "     KIN_REC.YEAR, ";
        $query .= "     KIN_REC.CLASSCD, ";
        $query .= "     KIN_REC.SCHOOL_KIND, ";
        $query .= "     KIN_REC.CURRICULUM_CD, ";
        $query .= "     KIN_REC.SUBCLASSCD ";

        return $query;
    }
}
