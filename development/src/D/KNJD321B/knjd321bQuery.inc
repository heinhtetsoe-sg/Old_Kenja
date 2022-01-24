<?php

require_once('for_php7.php');


class knjd321bQuery extends Query {

    //学年取得
    function getGrade()
    {
        $query .= " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME2 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //欠課時数
    function getPatern0Taisyou($model) {

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T3.HR_NAME, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN ";
        $query .= "         SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR ";
        $query .= "                            AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "                            AND T1.GRADE = T3.GRADE ";
        $query .= "                            AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";

        //2:欠課時数
        $query .= " ), ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
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
        $query .= "         INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND NOT EXISTS ( ";
        $query .= "                             SELECT ";
        $query .= "                                 'x' ";
        $query .= "                             FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT E1 ";
        $query .= "                             WHERE ";
        $query .= "                                 E1.REPLACECD = '1' ";
        $query .= "                                 AND T1.YEAR = E1.YEAR ";
        $query .= "                                 AND T1.CLASSCD = E1.COMBINED_CLASSCD ";
        $query .= "                                 AND T1.SCHOOL_KIND = E1.COMBINED_SCHOOL_KIND ";
        $query .= "                                 AND T1.CURRICULUM_CD = E1.COMBINED_CURRICULUM_CD ";
        $query .= "                                 AND T1.SUBCLASSCD = E1.COMBINED_SUBCLASSCD ";
        $query .= "                           ) ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
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
        $query .= "         INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT E1 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.REPLACECD = '1' ";
        $query .= "             AND T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.CLASSCD = E1.ATTEND_CLASSCD ";
        $query .= "             AND T1.SCHOOL_KIND = E1.ATTEND_SCHOOL_KIND ";
        $query .= "             AND T1.CURRICULUM_CD = E1.ATTEND_CURRICULUM_CD ";
        $query .= "             AND T1.SUBCLASSCD = E1.ATTEND_SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         E1.COMBINED_CLASSCD, ";
        $query .= "         E1.COMBINED_SCHOOL_KIND, ";
        $query .= "         E1.COMBINED_CURRICULUM_CD, ";
        $query .= "         E1.COMBINED_SUBCLASSCD ";

        /************/
        /* 欠課時数 */
        /************/
        $query .= " ) ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         REC.JUDGE_PATTERN ";
        $query .= "     FROM ";
        $query .= "         (SELECT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SUM(KEKKA_JISUU) AS KEKKA_JISUU ";
        $query .= "          FROM ATTEND_SUBCLASS T1 ";
        $query .= "          GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) T1 ";
        $query .= "         INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '".CTRL_YEAR."' ";
        $query .= "              AND T4.COURSECD        = T2.COURSECD ";
        $query .= "              AND T4.MAJORCD         = T2.MAJORCD ";
        $query .= "              AND T4.GRADE           = T2.GRADE ";
        $query .= "              AND T4.COURSECODE      = T2.COURSECODE ";
        $query .= "              AND T4.CLASSCD         = T1.CLASSCD ";
        $query .= "              AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
        $query .= "              AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        $query .= "              AND T4.SUBCLASSCD      = T1.SUBCLASSCD ";
        $query .= "         INNER JOIN KIN_RECORD_DAT REC ON REC.YEAR = '".CTRL_YEAR."' ";
        $query .= "               AND REC.CLASSCD       = T1.CLASSCD ";
        $query .= "               AND REC.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "               AND REC.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "               AND REC.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "               AND REC.SCHREGNO      = T1.SCHREGNO ";
        $query .= "               AND REC.JUDGE_PATTERN IN ('A', 'B', 'C') ";
        $query .= "     WHERE ";
        $query .= "         T4.CREDITS IS NOT NULL AND ";
        $query .= "         ((T4.CREDITS * (CASE WHEN T2.GRADE = '03' THEN 8 ELSE 10 END)) + 1) <= T1.KEKKA_JISUU ";

        return $query;
    }

    //A～Cパターンを０で更新する。
    function getPatern0Update($model, $row) {

        $query  = " UPDATE KIN_RECORD_DAT ";
        $query .= " SET (".$row["JUDGE_PATTERN"]."_PATTERN_ASSESS) ";
        $query .= " = '0' ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND CLASSCD = '".$row["CLASSCD"]."' ";
        $query .= "   AND SCHOOL_KIND = '".$row["SCHOOL_KIND"]."' ";
        $query .= "   AND CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
        $query .= "   AND SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";
        $query .= "   AND SCHREGNO = '".$row["SCHREGNO"]."' ";
        return $query;
    }

    //ATTEND_SUBCLASS_OVER_DAT削除
    function getDeleteSubclassOverDat($model) {

        $query  = " DELETE FROM ATTEND_SUBCLASS_OVER_DAT ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND GRADE = '".$model->field["GRADE"]."' ";
        return $query;
    }

    //ATTEND_SUBCLASS_OVER_DAT作成
    function getInsertSubclassOverDat($model, $row) {

        $data = array();
        $data["YEAR"][TEXT]           = CTRL_YEAR ;
        $data["GRADE"][TEXT]          = $model->field["GRADE"];
        $data["SCHREGNO"][TEXT]       = $row["SCHREGNO"];
        $data["CLASSCD"][TEXT]        = $row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]    = $row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]  = $row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]     = $row["SUBCLASSCD"];

        $query = Query::insertSQL($data, "ATTEND_SUBCLASS_OVER_DAT");

        return $query;
    }

}
?>
