<?php

require_once('for_php7.php');

class knjl023gQuery extends Query 
{

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
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
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
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.JUDGEMENT, ";
        $query .= "     L1.NAMECD2 || ':' || L1.NAME1 AS JUDGEMENT_INFO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "                  ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                  AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                  AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "                  AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ";
        $query .= "                  ON  L1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                  AND L1.NAMECD1 = 'L013' ";
        $query .= "                  AND L1.NAMECD2 = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.EXAMNO = '".$model->examno."'";

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
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "     EXAMNO = '".$model->examno."'";

        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        //変更前データを取得
        $baseRow = array();
        $baseRow = knjl023gQuery::get_edit_data($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT ---UPDATE
        $data = array();
        $data["JUDGEMENT"][TEXT]        = $model->field["JUDGEMENT"];   //画面データ保存
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
        $where .= "   AND EXAMNO = '{$model->examno}' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //受付データ
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $model->field["JUDGEMENT"];   //画面データ保存
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
        $where .= "   AND TESTDIV = '".$baseRow["TESTDIV"]."' ";
        $where .= "   AND EXAMNO = '{$model->examno}' ";
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>