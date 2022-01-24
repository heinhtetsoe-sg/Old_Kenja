<?php

require_once('for_php7.php');

class knjm371Query extends Query {

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (T2.TR_CD1    = '".STAFFCD."' ";
            $query .= "  OR  T2.TR_CD2    = '".STAFFCD."' ";
            $query .= "  OR  T2.TR_CD3    = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClassAuth($model, $flg, $selectdata="") {
        $query  = " SELECT ";
        $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = T2.GRADE ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (T2.TR_CD1    = '".STAFFCD."' ";
            $query .= "  OR  T2.TR_CD2    = '".STAFFCD."' ";
            $query .= "  OR  T2.TR_CD3    = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        if($flg == 'list' && $selectdata){
            $query .= "     AND T2.GRADE || T2.HR_CLASS NOT IN('".implode("','",$selectdata)."') ";
        }
        if($flg == 'select' && $selectdata){
            $query .= "     AND T2.GRADE || T2.HR_CLASS IN('".implode("','",$selectdata)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得
    function getStudentList($model, $flg, $selectdata="") {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番' || '　' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        if($flg == 'list' && $selectdata){
            $query .= "     AND T1.SCHREGNO NOT IN('".implode("','",$selectdata)."') ";
        }
        if($flg == 'select' && $selectdata){
            $query .= "     AND T1.SCHREGNO IN('".implode("','",$selectdata)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

}
?>
