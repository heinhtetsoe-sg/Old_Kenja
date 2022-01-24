<?php

require_once('for_php7.php');

class knjl030bQuery extends Query {

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

    //試験会場設定データ
    function selectQuery(&$model)
    {
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR   = '" .$model->year ."' AND ";
        $query .= "     APPLICANTDIV   = '" .$model->applicantdiv ."' AND ";
        $query .= "     TESTDIV   = '" .$model->testdiv ."' AND ";
        if ($model->cmd == "edit"){
            $query .= "     EXAMHALLCD = '" .$model->examhallcd ."' AND ";
        }
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";
        return $query;
    }
    function update(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query .= " UPDATE ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " SET ";
        $query .= "     EXAMHALL_NAME = '" .Query::addQuote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= "     CAPA_CNT = " .$model->field["CAPA_CNT"] .", ";
        $query .= "     REGISTERCD = '".STAFFCD ."', ";
        $query .= "     UPDATED  = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR   = '" .$model->year ."' ";
        $query .= "     AND APPLICANTDIV   = '" .$model->applicantdiv ."' ";
        $query .= "     AND TESTDIV = '" .$model->testdiv ."' ";
        $query .= "     AND EXAMHALLCD = '" .$model->examhallcd ."' ";
        $query .= "     AND EXAM_TYPE = '" .$model->exam_type ."' ";

        $db->query($query);
        //割り振り実行
        knjl030bQuery::exec($db, $model);
        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //割り振り実行
    function exec(&$db, &$model)
    {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR   = '" .$model->year ."' ";
        $query .= "     AND APPLICANTDIV   = '" .$model->applicantdiv ."' ";
        $query .= "     AND TESTDIV   = '" .$model->testdiv ."' ";
        $query .= "     AND EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";

        $result = $db->query($query);
        $hallArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hallArray[] = $row;
        }
        $result->free();

        $sReceptNo = "0";
        foreach ($hallArray as $key => $val) {
            $query  = " WITH RECEPT_T AS ( ";
            $query .= " SELECT ";
            $query .= "     RECEPTNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR   = '".$model->year."' ";
            $query .= "     AND APPLICANTDIV   = '".$model->applicantdiv."' ";
            $query .= "     AND TESTDIV   = '".$model->testdiv."' ";
            $query .= "     AND EXAM_TYPE = '".$model->exam_type."' ";
            $query .= "     AND RECEPTNO > '".$sReceptNo."' ";
            $query .= " ORDER BY ";
            $query .= "     RECEPTNO ";
            $query .= " FETCH FIRST {$val["CAPA_CNT"]} ROWS ONLY ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     MIN(RECEPTNO) AS MIN_R, ";
            $query .= "     MAX(RECEPTNO) AS MAX_R ";
            $query .= " FROM ";
            $query .= "     RECEPT_T ";

            $setRecept = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $query  = " UPDATE ";
            $query .= "     ENTEXAM_HALL_YDAT T1 ";
            $query .= " SET ";
            $query .= "     (S_RECEPTNO, E_RECEPTNO) = ('{$setRecept["MIN_R"]}', '{$setRecept["MAX_R"]}') ";
            $query .= " WHERE ";
            $query .= "         T1.ENTEXAMYEAR      = '".$model->year."' ";
            $query .= "         AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "         AND T1.TESTDIV      = '".$model->testdiv."' ";
            $query .= "         AND T1.EXAM_TYPE    = '".$model->exam_type."' ";
            $query .= "         AND T1.EXAMHALLCD   = '".$val["EXAMHALLCD"]."' ";

            $sReceptNo = $setRecept["MAX_R"] ? $setRecept["MAX_R"] : "ZZZZZ";
            $db->query($query);
        }

    }
    function insert(&$model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR   = '" .$model->year ."' ";
        $query .= "     AND APPLICANTDIV   = '" .$model->applicantdiv ."' ";
        $query .= "     AND TESTDIV   = '" .$model->testdiv ."' ";
        $query .= "     AND EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD DESC ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)){
            $examhallcd = $row["EXAMHALLCD"] + 1;
            $s_receptno = $row["E_RECEPTNO"] + 1;
        } else {
            $examhallcd = 1;
        }

        $query  = " INSERT INTO ENTEXAM_HALL_YDAT( ";
        $query .= " ENTEXAMYEAR, ";
        $query .= " APPLICANTDIV, ";
        $query .= " TESTDIV, ";
        $query .= " EXAM_TYPE, ";
        $query .= " EXAMHALLCD, ";
        $query .= " EXAMHALL_NAME, ";
        $query .= " CAPA_CNT, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= " '" .$model->year ."', ";
        $query .= " '" .$model->applicantdiv ."', ";
        $query .= " '" .$model->testdiv ."', ";
        $query .= " '" .$model->exam_type ."', ";
        $query .= " '".sprintf("%04d", $examhallcd). "', ";
        $query .= " '" .Query::addQuote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= " " .$model->field["CAPA_CNT"] .", ";
        $query .= " '".STAFFCD ."' ";
        $query .= " ) ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    function delete(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR   = '" .$model->year ."' ";
        $query .= "     AND APPLICANTDIV   = '" .$model->applicantdiv ."' ";
        $query .= "     AND TESTDIV   = '" .$model->testdiv ."' ";
        $query .= "     AND EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= "     AND EXAMHALLCD IN ('" .implode($model->field["CHECKED"],"','") ."') ";

        $db->query($query);

        //割り振り実行
        knjl030bQuery::exec($db, $model);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

}
?>