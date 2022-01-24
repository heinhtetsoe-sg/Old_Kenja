<?php

require_once('for_php7.php');

class knjl022bQuery extends Query 
{

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        if ($cd !== 'L008') {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        } else {
            $query .= "     ABBV1, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
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

    //確約区分のコンボ表示するためのデータ取得
    function getKakuyakuData($model, $testDiv)
    {
        $query  = " SELECT ";
        $query .= "     JUDGMENT_DIV || ':' || PROMISE_COURSE_NAME AS LABEL, ";
        $query .= "     JUDGMENT_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        if ($testDiv == "1") {
            $query .= " AND PROMISE_RECOMMEND_TEST_FLG = '1' ";
        } else {
            $query .= " AND PROMISE_GENERAL_TEST_FLG = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     JUDGMENT_DIV ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.NAME, ";
        $query .= "     L1.*, ";
        //$query .= "     L2.REMARK1 AS DETAIL_REMARK1, ";
        $query .= "     L2.REMARK2 AS DETAIL_REMARK2, ";
        $query .= "     L2.REMARK3 AS DETAIL_REMARK3, ";
        $query .= "     L2.REMARK4 AS DETAIL_REMARK4, ";
        $query .= "     L2.REMARK8 AS KAKUYAKU_SET ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = L1.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = L1.EXAMNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L2 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = L2.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = L2.EXAMNO AND ";
        $query .= "     L2.SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.EXAMNO = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データカウント(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
    function getCountDetaildata($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "     EXAMNO = '".$model->examno."' AND ";
        $query .= "     SEQ = '004' ";
        return $query;
    }
    
    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データチェック
        $getCount = $db->getOne(knjl022bQuery::getCountDetaildata($model));
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---UPDATE
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "004";
        }
        $data["REMARK2"][TEXT]      = $model->field["DETAIL_REMARK2"];
        $data["REMARK3"][TEXT]      = $model->field["DETAIL_REMARK3"];
        $data["REMARK4"][TEXT]      = $model->field["DETAIL_REMARK4"];
        $data["REMARK8"][TEXT]      = $model->field["KAKUYAKU_SET"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
            $where .= "       EXAMNO = '".$model->examno."' AND ";
            $where .= "       SEQ = '004' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>