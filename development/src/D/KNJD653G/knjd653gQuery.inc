<?php

require_once('for_php7.php');

class knjd653gQuery extends Query {
    function getSchoolname()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";
        return $query;
    }
    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$model->control["年度"] ."'";
            if ($model->field["SEMESTER"]=="9") {
                $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            } else {
                $query .= "AND SEMESTER='".$model->field["SEMESTER"] ."' ";
            }
            $query .= "AND GRADE='".$model->field["GRADE"] ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            $query .= "WHERE T1.YEAR='" .$model->control["年度"] ."'";
            if ($model->field["SEMESTER"]=="9") {
                $query .= "AND T1.SEMESTER='".CTRL_SEMESTER ."' ";
            } else {
                $query .= "AND T1.SEMESTER='".$model->field["SEMESTER"] ."' ";
            }
            $query .= "AND T1.GRADE='".$model->field["GRADE"] ."'";
            $query .= "     AND ((T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "           T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "           T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "           T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "           T1.SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "           T1.SUBTR_CD3  = '".STAFFCD."') ";
            $query .= "       OR (T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR ";
        $query .= "                                   AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= " AND T1.SEMESTER='".CTRL_SEMESTER ."' ";
        } else {
            $query .= " AND T1.SEMESTER='".$model->field["SEMESTER"] ."' ";
        }
        $query .= "     AND T1.GRADE < '11' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "           T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "           T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "           T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "           T1.SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "           T1.SUBTR_CD3  = '".STAFFCD."') ";
            $query .= "       OR (T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query = "WITH MAIN_T(LABEL, VALUE, KINDCD, ITEMCD) AS ( ";
        $query.= "SELECT ";
        $query.= "    TESTKINDCD || TESTITEMCD || '　' || TESTITEMNAME, ";
        $query.= "    TESTKINDCD || TESTITEMCD, ";
        $query.= "    TESTKINDCD, ";
        $query.= "    TESTITEMCD ";
        $query.= "FROM ";
        $query.= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query.= "WHERE ";
        $query.= "    YEAR = '".CTRL_YEAR."' AND ";
        $query.= "    SEMESTER = '".$semester."' ";
        $query.= ") ";
        $query.= "SELECT ";
        $query.= "    LABEL, ";
        $query.= "    VALUE ";
        $query.= "FROM ";
        $query.= "    MAIN_T ";
        $query.= "ORDER BY ";
        $query.= "    KINDCD, ";
        $query.= "    ITEMCD ";

        return $query;
    }

}
?>
