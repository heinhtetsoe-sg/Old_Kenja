<?php

require_once('for_php7.php');

class knjl223gQuery extends Query {

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "") {
        $query  = " SELECT ";
        if ($cd == 'L013') {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        } else {
            $query .= "     NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$cd}' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '{$sitei}' ";
        }
        if ($cd == 'L013') {
            $query .= "     AND NAMECD2 = '4' "; //4:欠席
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }


    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     value(L013.NAMECD2, '') || ':' || value(L013.NAME1, '') AS JUDGEMENT_INFO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ON L013.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L013.NAMECD1 = 'L013' ";
        $query .= "                              AND L013.NAMECD2 = BASE.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.EXAMNO       = '{$model->examno}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データカウント(ENTEXAM_APPLICANTBASE_DAT)
    function getCountdata($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO       = '{$model->examno}' ";

        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        //変更前データを取得
        $baseRow = array();
        $baseRow = knjl223gQuery::get_edit_data($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT ---UPDATE
        $data = array();
        $data["JUDGEMENT"][TEXT]        = $model->field["JUDGEMENT"];   //画面データ保存
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ENTEXAMYEAR  = '{$model->year}' ";
        $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $where .= "   AND EXAMNO       = '{$model->examno}' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //受付データ
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $model->field["JUDGEMENT"];   //画面データ保存
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ENTEXAMYEAR  = '{$model->year}' ";
        $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV      = '{$baseRow["TESTDIV"]}' ";
        $where .= "   AND EXAMNO       = '{$model->examno}' ";
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>