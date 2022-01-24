<?php

require_once('for_php7.php');

class knjl030mQuery extends Query {

    //名称
    function GetName($year, $namecd1)
    {
        $query = "SELECT NAMECD2, NAME1, NAMESPARE2 FROM V_NAME_MST
                   WHERE YEAR    = '".$year."'
                     AND NAMECD1 = '".$namecd1."'";
        return $query;
    }
    
    //試験会場設定データ
    function selectQuery(&$model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "edit"){
            $query .= "     EXAMHALLCD = '" .$model->examhallcd ."' AND ";
        }
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     EXAM_TYPE    = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     S_RECEPTNO ";
        return $query;
    }

    //座席番号重複チェック
    function receptnoCheck(&$model)
    {
        $db = Query::dbCheckOut();

        $s_receptno = (int) $model->field["S_RECEPTNO"];
        $e_receptno = $s_receptno + (int) $model->field["CAPA_CNT"] - 1;

        $s_receptno = sprintf("%05d", $s_receptno);
        $e_receptno = sprintf("%05d", $e_receptno);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' AND ";
        if ($model->cmd == "update" ) {
            $query .= " EXAMHALLCD != '" .$model->examhallcd ."' AND ";
        }
        $query .= "    (S_RECEPTNO <= '{$s_receptno}' OR S_RECEPTNO <= '{$e_receptno}') AND ";
        $query .= "    (E_RECEPTNO >= '{$s_receptno}' OR E_RECEPTNO >= '{$e_receptno}') ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //更新
    function update(&$model)
    {
        $db = Query::dbCheckOut();

        $s_receptno = (int) $model->field["S_RECEPTNO"];
        $e_receptno = $s_receptno + (int) $model->field["CAPA_CNT"] - 1;

        $query = " UPDATE ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " SET ";
        $query .= "     EXAMHALL_NAME = '" .Query::addquote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= "     CAPA_CNT = " .$model->field["CAPA_CNT"] .", ";
        $query .= "     S_RECEPTNO = '" .sprintf("%05d", $s_receptno) ."', ";
        $query .= "     E_RECEPTNO = '" .sprintf("%05d", $e_receptno) ."', ";
        $query .= "     REGISTERCD = '".STAFFCD ."', ";
        $query .= "     UPDATED  = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     EXAMHALLCD = '" .$model->examhallcd ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //追加
    function insert(&$model)
    {
        $db = Query::dbCheckOut();

        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD DESC ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //会場コード、開始座席番号を設定。
        if (is_array($row)){
            $examhallcd = (int) $row["EXAMHALLCD"] + 1;
            $s_receptno = (int) $row["E_RECEPTNO"] + 1;
        }else{
            $examhallcd = 1;
            $s_receptno = 1;
        }
        //画面の開始座席番号があるときは、この番号を開始座席番号とする。
        if (is_numeric($model->field["S_RECEPTNO"])) {
            $s_receptno = (int) $model->field["S_RECEPTNO"];
        }
        $e_receptno = $s_receptno + (int) $model->field["CAPA_CNT"] - 1;

        $query  = " INSERT INTO ENTEXAM_HALL_DAT( ";
        $query .= " APPLICANTDIV, ";
        $query .= " TESTDIV, ";
        $query .= " EXAM_TYPE, ";
        $query .= " EXAMHALLCD, ";
        $query .= " EXAMHALL_NAME, ";
        $query .= " CAPA_CNT, ";
        $query .= " S_RECEPTNO, ";
        $query .= " E_RECEPTNO, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= " '" .$model->applicantdiv ."', ";
        $query .= " '1', ";
        $query .= " '" .$model->exam_type ."', ";
        $query .= " '".sprintf("%04d", $examhallcd). "', ";
        $query .= " '" .Query::addquote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= " " .$model->field["CAPA_CNT"] .", ";
        $query .= " '".sprintf("%05d", $s_receptno). "', ";
        $query .= " '".sprintf("%05d", $e_receptno). "', ";
        $query .= " '".STAFFCD ."' ";
        $query .= " ) ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    //削除
    function delete(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' AND ";
        $query .= "     EXAMHALLCD IN ('" .implode($model->field["CHECKED"],"','") ."') ";

        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }
}
?>