<?php

require_once('for_php7.php');

class knje370lQuery extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得
    function getListInfo($model)  {
        $query  = " SELECT DISTINCT ";
        $query .= "   T1.STAT_CD AS VALUE, ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "   T1.STAT_CD || ':' || VALUE(T2.COMPANY_NAME, '') AS LABEL ";
        } else {
            $query .= "   T1.STAT_CD || ':' || VALUE(T2.SCHOOL_NAME_SHOW1, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "   AFT_GRAD_COURSE_DAT T1 ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "   LEFT JOIN COMPANY_MST T2 ";
            $query .= "     ON T2.COMPANY_CD = T1.STAT_CD ";
        } else {
            $query .= "   LEFT JOIN COLLEGE_MST T2 ";
            $query .= "     ON T2.SCHOOL_CD = T1.STAT_CD ";
        }
        $query .= "   LEFT JOIN NAME_MST E005 ";
        $query .= "     ON NAMECD1 = 'E005' ";
        $query .= "    AND NAMECD2 = T1.DECISION ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND E005.NAMESPARE1 = '3' ";
        if ($model->field["FILTER"] == "2") {
            $query .= "   AND T1.PLANSTAT = '1' ";
        }
        if ($model->field["OUTPUT"] == "2") {
            $query .= "   AND T1.SENKOU_KIND = '1' ";
        } else {
            $query .= "   AND T1.SENKOU_KIND = '0' ";
        }
        $query .= " ORDER BY ";
        $query .= "   T1.STAT_CD ";
        return $query;
    }
}
?>
