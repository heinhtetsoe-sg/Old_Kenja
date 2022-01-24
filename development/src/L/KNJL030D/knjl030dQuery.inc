<?php

require_once('for_php7.php');

class knjl030dQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験会場設定データ
    function selectQuery(&$model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '" .$model->examyear ."' AND ";
        $query .= "     APPLICANTDIV    = '" .$model->applicantdiv ."' AND ";
        $query .= "     TESTDIV         = '" .$model->testdiv ."' AND ";
        if ($model->cmd == "edit"){
            $query .= "     EXAMHALLCD      = '" .$model->examhallcd ."' AND ";
        }
        $query .= "     EXAM_TYPE       = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";

        return $query;
    }

    //MAX会場コード取得
    function getMaxExamhallcd(&$model) {
        $query  = " SELECT ";
        $query .= "     MAX(EXAMHALLCD) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->examyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->exam_type."' ";

        return $query;
    }

    //更新
    function update(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $data = array();

        if ($model->cmd == "insert") {
            //MAX会場コード
            $maxExamhallcd = $db->getOne(knjl030dQuery::getMaxExamhallcd($model));

            //セット用会場コード
            $examhallcd = (($maxExamhallcd > 0) ? $maxExamhallcd : 0) + 1;

            $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
            $data["EXAMHALLCD"][TEXT]       = sprintf("%04d", $examhallcd);
        }
        $data["EXAMHALL_NAME"][TEXT]    = $model->field["EXAMHALL_NAME"];
        $data["CAPA_CNT"][NUMBER]       = $model->field["CAPA_CNT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($model->cmd == "insert") {
            //追加
            $db->query(Query::insertSQL($data, "ENTEXAM_HALL_YDAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '".$model->examyear."' AND ";
            $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $where .= "     TESTDIV         = '".$model->testdiv."' AND ";
            $where .= "     EXAMHALLCD      = '".$model->examhallcd."' AND ";
            $where .= "     EXAM_TYPE       = '".$model->exam_type."' ";
            //更新
            $db->query(Query::updateSQL($data, "ENTEXAM_HALL_YDAT", $where));
        }

        //割り振り実行
        knjl030dQuery::exec($db, $model);
        $db->commit(); 

        Query::dbCheckIn($db);
    }

    //削除
    function delete(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->examyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->exam_type."' AND ";
        $query .= "     EXAMHALLCD IN ('".implode($model->field["CHECKED"],"','")."') ";

        $db->query($query);

        //割り振り実行
        knjl030dQuery::exec($db, $model);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //割り振り実行
    function exec(&$db, &$model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->examyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->exam_type."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";

        $hallArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hallArray[] = $row;
        }
        $result->free();

        $sReceptNo = "0";
        foreach ($hallArray as $key => $val) {
            $query  = " WITH BASE_T AS ( ";
            $query .= "     SELECT ";
            $query .= "         EXAMNO ";
            $query .= "     FROM ";
            $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "     WHERE ";
            $query .= "         ENTEXAMYEAR     = '".$model->examyear."' AND ";
            $query .= "         APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $query .= "         TESTDIV         = '".$model->testdiv."' AND ";
            $query .= "         EXAMNO          > '".$sReceptNo."' ";
            $query .= "     ORDER BY ";
            $query .= "         EXAMNO ";
            $query .= "     FETCH FIRST {$val["CAPA_CNT"]} ROWS ONLY ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     MIN(EXAMNO) AS MIN_EXAMNO, ";
            $query .= "     MAX(EXAMNO) AS MAX_EXAMNO ";
            $query .= " FROM ";
            $query .= "     BASE_T ";

            $setExamno = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //開始・終了受験番号セット
            $data = array();
            $data["S_RECEPTNO"][TEXT]       = $setExamno["MIN_EXAMNO"];
            $data["E_RECEPTNO"][TEXT]       = $setExamno["MAX_EXAMNO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '".$model->examyear."' AND ";
            $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
            $where .= "     TESTDIV         = '".$model->testdiv."' AND ";
            $where .= "     EXAM_TYPE       = '".$model->exam_type."' AND ";
            $where .= "     EXAMHALLCD      = '".$val["EXAMHALLCD"]."' ";

            $db->query(Query::updateSQL($data, "ENTEXAM_HALL_YDAT", $where));

            $sReceptNo = $setExamno["MAX_EXAMNO"] ? $setExamno["MAX_EXAMNO"] : "ZZZZZ";
        }
    }
}
?>