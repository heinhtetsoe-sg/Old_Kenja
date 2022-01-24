<?php

require_once('for_php7.php');


class knjc171Query extends Query {

    //Z010
    function getZ010() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";
        return $query;
    }

    //名称マスタの取得
    function getNameMst($namecd1) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR    = '".CTRL_YEAR."' ";
        $query .= "   AND NAMECD1 = '".$namecd1."' ";
        return $query;
    }

    //年度の日付範囲
    function getSemester9() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SDATE, EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '9' ";
        return $query;
    }

    //講座データの取得
    function selectChairQuery($model) {
        $date = str_replace("/", "-", $model->field["DATE"]);
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CHAIRCD || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     T1.CHAIRCD || '-' || T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     T1.CHAIRNAME ||':'|| T5.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "             ON T2.YEAR       = T1.YEAR ";
        $query .= "            AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "            AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     INNER JOIN CHAIR_STF_DAT T3 ";
        $query .= "             ON T3.YEAR       = T1.YEAR ";
        $query .= "            AND T3.SEMESTER   = T1.SEMESTER ";
        $query .= "            AND T3.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     INNER JOIN SCH_CHR_DAT T4 ";
        $query .= "             ON T4.EXECUTEDATE = '".$date."' ";
        $query .= "            AND T4.PERIODCD    = '".$model->field["PERIOD"]."' ";
        $query .= "            AND T4.CHAIRCD     = T1.CHAIRCD ";
        $query .= "     INNER JOIN SUBCLASS_MST T5 ";
        $query .= "             ON T5.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            AND T5.CLASSCD       = T1.CLASSCD ";
            $query .= "            AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "            AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "       T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
