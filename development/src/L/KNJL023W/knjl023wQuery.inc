<?php

require_once('for_php7.php');

class knjl023wQuery extends Query 
{

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
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
        $query .= "     CASE WHEN T1.JUDGEMENT = '4' THEN T1.JUDGEMENT END AS KESSEKI, ";
        $query .= "     L1.NAMECD2 || ':' || L1.NAME1 AS JUDGEMENT_INFO, ";
        $query .= "     CASE WHEN T1.JUDGEMENT = '5' THEN T1.JUDGEMENT END AS GANSHO_HENKOU ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ";
        $query .= "                  ON  L1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                  AND L1.NAMECD1 = 'L013' ";
        $query .= "                  AND L1.NAMECD2 = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
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
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     EXAMNO = '".$model->examno."'";

        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        //変更前データを取得
        $baseRow = array();
        $baseRow = knjl023wQuery::get_edit_data($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT ---UPDATE
        $data = array();
        $judgement = "";
        if (strlen($model->field["KESSEKI"])) {
            $judgement = $model->field["KESSEKI"];
        } else if (strlen($model->field["GANSHO_HENKOU"])) {
            $judgement = $model->field["GANSHO_HENKOU"];
        }
        $data["JUDGEMENT"][TEXT]        = $judgement;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
        $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
        $where .= "   AND EXAMNO = '{$model->examno}' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>