<?php

require_once('for_php7.php');

class knjb236Query extends Query {
    //学期取得
    function getSemester($semester="") {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE, ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        if ($semester) {
            $query .= " AND SEMESTER = '".$semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //基本時間割タイトル取得
    function getSchPtrnHdat($model) {
        $query  = " SELECT ";
        $query .= "     T1.BSCSEQ AS VALUE, ";
        $query .= "     T2.SEMESTERNAME || ' Seq' || RTRIM(CHAR(T1.BSCSEQ)) || ':' || VALUE(T1.TITLE,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
