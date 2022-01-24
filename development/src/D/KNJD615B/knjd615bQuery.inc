<?php

require_once('for_php7.php');

class knjd615bQuery extends Query {
    //中学なのか高校なのか
    function getGdat($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     GRADE = '$grade' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR."'";
            if ($model->field["SEMESTER"]=="9") {
                $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            } else {
                $query .= "AND SEMESTER='".$model->field["SEMESTER"] ."' ";
            }
            $query .= "AND GRADE='".$model->field["GRADE"] ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR."'";
            if ($model->field["SEMESTER"]=="9") {
                $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            } else {
                $query .= "AND SEMESTER='".$model->field["SEMESTER"] ."' ";
            }
            $query .= "AND GRADE='".$model->field["GRADE"] ."'";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    function getJob($model) {
        $query  = "SELECT JOBCD AS VALUE, JOBNAME AS LABEL ";
        $query .= "FROM V_JOB_MST ";
        $query .= "WHERE YEAR='" .CTRL_YEAR."'";
        $query .= "ORDER BY JOBCD ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE || '学年' AS LABEL, ";
        $query .= "    GRADE AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE < '11' ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
        } else {
            $query .= "AND SEMESTER='".$model->field["SEMESTER"] ."' ";
        }
        $query .= "ORDER BY VALUE ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //テスト種別取得
    function getTestItem($semester)
    {
        $query = "WITH MAIN_T(LABEL, VALUE) AS ( ";
        $query.= "SELECT ";
        $query.= "    TESTKINDCD || TESTITEMCD || '　' || TESTITEMNAME, ";
        $query.= "    TESTKINDCD || TESTITEMCD || '_1' ";
        $query.= "FROM ";
        $query.= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query.= "WHERE ";
        $query.= "    YEAR = '".CTRL_YEAR."' AND ";
        $query.= "    SEMESTER = '".$semester."' ";
        if ($semester == "9") {
            $query.= "UNION ALL ";
            $query.= "    VALUES('9900　学年評定', '9900_2') ";
        }
        $query.= ") ";
        $query.= "SELECT ";
        $query.= "    LABEL, ";
        $query.= "    VALUE ";
        $query.= "FROM ";
        $query.= "    MAIN_T ";
        $query.= "ORDER BY ";
        $query.= "    VALUE ";

        return $query;
    }

}
?>
