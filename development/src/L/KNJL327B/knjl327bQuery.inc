<?php

require_once('for_php7.php');

class knjl327bQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    function getTankiriCourse($year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     EARLY_PASSEXAMCOURSECD || ':' || JUDGMENT_COURSE_ABBV AS LABEL, ";
        $query .= "     EARLY_PASSEXAMCOURSECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= "     AND CHANGE_SINGLE_TEST_FLG = '1' "; //一般入試(単切)
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
