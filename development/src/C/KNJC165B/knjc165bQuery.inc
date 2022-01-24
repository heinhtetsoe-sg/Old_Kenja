<?php

require_once('for_php7.php');


class knjc165bQuery extends Query
{

    //年度取得
    public function getYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        // $query .= "     YEAR || '年度' AS LABEL ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
        //どこの学校でも1学期は登録する前提。それが無ければコンボに出て来ない。
        $query  = " SELECT ";
        $query .= "     '1' AS VALUE, ";
        $query .= "     '1:4/1～8/31' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER = '1' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '2' AS VALUE, ";
        $query .= "     '2:9/1～12/31' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER = '1' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '3' AS VALUE, ";
        $query .= "     '3:1/1～3/31' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER = '1' ";

        return $query;
    }

    //学校種別の取得
    public function getNameMstA023($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 IN ('".SCHOOLKIND."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //課程取得
    public function getCourse($model)
    {
        $query  = " WITH REGD_DAT AS ( ";
        $query .= " SELECT DISTINCT T1.COURSECD ";
        $query .= "   FROM SCHREG_REGD_DAT T1 ";
        $query .= "        INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "                ON T2.YEAR  = T1.YEAR ";
        $query .= "               AND T2.GRADE = T1.GRADE ";
        $query .= "               AND T2.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " WHERE T1.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.COURSECD AS VALUE, ";
        $query .= "   T1.COURSENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   COURSE_MST T1 ";
        $query .= "   INNER JOIN REGD_DAT T2 ";
        $query .= "           ON T2.COURSECD = T1.COURSECD ";

        return $query;
    }
}
