<?php

require_once('for_php7.php');


class knjm010Query extends Query {

    //年組取得（権限チェック）
    function getAuth($year, $semester, $grade)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
            $query .= "AND GRADE='".$grade ."' ";
            $query .= " ORDER BY HR_CLASS ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= "AND GRADE='".$grade ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " ORDER BY HR_CLASS ";
        }

        return $query;
    }

    //生徒取得
    function getGrade($model)
    {
        $query  = " SELECT DISTINCT GRADE ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR = '" .$model->control["年度"] ."' AND ";
        $query .= "       SEMESTER = '" .$model->control["学期"]. "' ";
        $query .= " ORDER BY GRADE ";

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
        if ($model->field["RISHUUCHECK"] === '1') {
            $query .= "    INNER JOIN SCHREG_REGD_DAT L1 ON L1.YEAR = T1.YEAR ";
            $query .= "                                 AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "                                 AND L1.GRADE = T1.GRADE ";
            $query .= "                                 AND L1.HR_CLASS = T1.HR_CLASS ";
            $query .= "    INNER JOIN SUBCLASS_STD_SELECT_DAT L2 ON L2.SCHREGNO = L1.SCHREGNO ";
            $query .= "                                         AND L2.YEAR = L1.YEAR ";
            $query .= "                                         AND L2.SEMESTER = L1.SEMESTER ";
        }
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".$model->control["年度"]."' ";
        $query .= "    AND T1.SEMESTER = '".$model->control["学期"] ."' ";
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
        if ($model->field["RISHUUCHECK"] === '1') {
            $query .= "    INNER JOIN SUBCLASS_STD_SELECT_DAT L2 ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                         AND L2.YEAR = T1.YEAR ";
            $query .= "                                         AND L2.SEMESTER = T1.SEMESTER ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->control["年度"]."' AND ";
        $query .= "    T1.SEMESTER = '".$model->control["学期"]."' AND ";
        $query .= "    T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"] ."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.ATTENDNO ";

        return $query;
    }
}
?>
