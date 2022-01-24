<?php

require_once('for_php7.php');

class knjm505dQuery extends Query {

    //学期取得
    function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //クラス取得
    function getAuthClass($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "    T1.HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"] ."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "    AND (T1.TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "         T1.TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "         T1.TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= "ORDER BY";
        $query .= "    VALUE ";

        return $query;
    }

    //生徒取得
    function getAuthStudent($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.SCHREGNO AS VALUE, ";
        $query .= "    T1.SCHREGNO || '　' || T1.ATTENDNO ||  '番　' || L1.NAME AS LABEL, ";
        $query .= "    T1.ATTENDNO ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.SEMESTER = '".$model->field["SEMESTER"] ."' AND ";
        $query .= "    T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"] ."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.ATTENDNO ";

        return $query;
    }
}
?>
