<?php

require_once('for_php7.php');

class knjl216yQuery extends Query {

    //履歴一覧
    function getList($model, $div = "") {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.NAME1 AS RELATION_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_FAMILY_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H201' ";
        $query .= "          AND T1.RELATIONSHIP = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND EXAMNO = '{$model->examno}' ";
        if ($div == "select") {
            $query .= "     AND SEQ = {$model->seq} ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //名称マスタよりデータを取得
    function get_appl_name($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO = '{$model->examno}' ";

        return $query;
    }

    //名称マスタよりデータを取得
    function get_name_cd($model, $cd)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$cd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データカウント
    function getCountdata($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_FAMILY_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND EXAMNO = '{$model->examno}' ";
        $query .= "     AND SEQ = '{$model->seq}' ";

        return $query;
    }

    //SEQ取得
    function getSeq($model)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(SEQ), 0) + 1 AS MAX_SEQ ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_FAMILY_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND EXAMNO = '{$model->examno}' ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl216yQuery::getSeq($model);
        $maxSeq = $db->getOne($query);

        $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][NUMBER]        = $maxSeq;
        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["AGE"][NUMBER]        = $model->field["AGE"];
        $data["RELATIONSHIP"][TEXT] = $model->field["RELATIONSHIP"];
        $data["REMARK"][TEXT]       = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_FAMILY_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["AGE"][NUMBER]        = $model->field["AGE"];
        $data["RELATIONSHIP"][TEXT] = $model->field["RELATIONSHIP"];
        $data["REMARK"][TEXT]       = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $where .= "       AND EXAMNO = '{$model->examno}' ";
        $where .= "       AND SEQ = {$model->seq} ";
        $query = Query::updateSQL($data, "ENTEXAM_FAMILY_DAT", $where);

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query .= " DELETE FROM ENTEXAM_FAMILY_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "       AND EXAMNO = '{$model->examno}' ";
        $query .= "       AND SEQ = {$model->seq} ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
