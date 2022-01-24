<?php

require_once('for_php7.php');

class knjl306gQuery extends Query {

    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ出力
    function getOutputQueryCsv($model) {
        $query .= " SELECT ";
        $query .= "     HALL.EXAMHALL_NAME, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "          AND RECEPT.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT HALL ON RECEPT.ENTEXAMYEAR = HALL.ENTEXAMYEAR ";
        $query .= "          AND RECEPT.APPLICANTDIV = HALL.APPLICANTDIV ";
        $query .= "          AND RECEPT.TESTDIV = HALL.TESTDIV ";
        if ($model->isKeiai == "1") {
            $query .= "          AND BASE.SHDIV = HALL.EXAM_TYPE ";
        } else {
            $query .= "          AND HALL.EXAM_TYPE = '1' ";
        }
        $query .= "          AND RECEPT.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND RECEPT.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND RECEPT.EXAM_TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "     HALL.EXAM_TYPE, ";
        $query .= "     HALL.EXAMHALLCD, ";
        $query .= "     RECEPT.RECEPTNO ";
        return $query;
    }

}
?>
