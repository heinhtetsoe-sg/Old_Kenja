<?php

require_once('for_php7.php');

class knjz068eQuery extends Query {

    //IBコース一覧取得
    function getIBPrgCourse($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A034' AND ";
        $query .= "     NAMECD2 = '0' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }    

    //学年一覧取得
    function getIBGrade($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                            AND L1.NAMECD1 = 'A034' ";
        $query .= "                            AND L1.NAME1 = '".$model->ibprg_course."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE BETWEEN L1.NAME2 AND L1.NAME3 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
