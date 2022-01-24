<?php

class knjl380iquery extends Query {

    //名称マスタ取得
    function getNameMst($nameCd1, $nameCd2) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".(CTRL_YEAR + 1)."' ";
        if ($nameCd1 != "") {
            $query .= "   AND NAMECD1 = '{$nameCd1}' ";
        }
        if ($nameCd2 != "") {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    function getEntexamTestDivMst($applicantDiv) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "   AND TESTDIV IN ('1', '2') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //会場データ取得
    function getEntexamHall($applicantDiv, $testDiv, $formDiv) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     EXAMHALLCD VALUE ";
        $query .= "   , EXAMHALL_NAME LABEL ";
        $query .= " FROM ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        if ($applicantDiv != "") {
            $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        }
        if ($testDiv != "") {
            $query .= "   AND TESTDIV = '{$testDiv}' ";
        }
        if ($formDiv == "1"){
            $query .= "   AND EXAMHALLCD LIKE '2%' ";
        } else {
            $query .= "   AND EXAMHALLCD LIKE '1%' ";
        }
        
        $query .= "   AND EXAM_TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
