<?php

require_once('for_php7.php');

class knjz040pQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験会場リスト（左画面）
    function hallList(&$model) {
        $namecd1 = ($model->fields["APPLICANTDIV"] == "1") ? "L024" : "L004";

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.TESTDIV || ':' || L1.NAME1 AS TESTDIVNAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ";
        $query .= "          ON L1.YEAR     = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.NAMECD1  = '{$namecd1}' ";
        $query .= "         AND L1.NAMECD2  = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->fields["APPLICANTDIV"]."' ";
        $query .= "     AND T1.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAMHALLCD ";

        return $query;
    }

    //１レコード取得（右画面）
    function getRecord(&$model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->fields["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "     AND EXAMHALLCD   = '".$model->examhallcd."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";

        return $query;
    }

    //INSERT
    function insert(&$model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->fields["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD DESC ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)){
            $examhallcd = $row["EXAMHALLCD"] + 1;
        } else {
            $examhallcd = 1;
        }
        $examhallcd = sprintf("%04d", $examhallcd);
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]     = $model->exam_type;
        $data["EXAMHALLCD"][TEXT]    = $examhallcd;
        $data["EXAMHALL_NAME"][TEXT] = $model->field["EXAMHALL_NAME"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";
        
        $query = Query::insertSQL($data, "ENTEXAM_HALL_YDAT");

        $db->query($query);
        Query::dbCheckIn($db);

        return $examhallcd;
    }

    //UPDATE
    function update(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["EXAMHALL_NAME"][TEXT] = $model->field["EXAMHALL_NAME"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $where .= "     AND APPLICANTDIV = '".$model->fields["APPLICANTDIV"]."' ";
        $where .= "     AND TESTDIV      = '".$model->testdiv."' ";  
        $where .= "     AND EXAMHALLCD   = '".$model->examhallcd."' ";
        $where .= "     AND EXAM_TYPE    = '".$model->exam_type."' ";

        $query = Query::updateSQL($data, "ENTEXAM_HALL_YDAT", $where);

        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //DELETE
    function delete(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->fields["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "     AND EXAMHALLCD   = '".$model->examhallcd."' ";

        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

}
?>
