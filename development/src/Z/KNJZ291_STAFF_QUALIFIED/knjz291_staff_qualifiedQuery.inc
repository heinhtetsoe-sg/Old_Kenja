<?php

require_once('for_php7.php');

class knjz291_staff_qualifiedQuery extends Query {

    //教育委員会学校コード
    function getEdboardSchool() {
        $query  = " SELECT ";
        $query .= "     KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //リスト表示
    function getList($model) {

        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.QUALIFIED_CD, ";
        $query .= "     L2.NAME1 AS QUALIFIED_CD_NAME, ";
        $query .= "     T1.QUALIFIED_NAME, ";
        $query .= "     T1.GET_DATE ";
        $query .= " FROM ";
        $query .= "     STAFF_QUALIFIED_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z031' ";
        $query .= "          AND T1.QUALIFIED_CD = L2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$model->sendStaffcd}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEQ DESC ";

        return $query;
    }

    //職員名
    function getStaffName($model) {

        $query  = " SELECT ";
        $query .= "     T1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$model->sendStaffcd}' ";

        return $query;
    }

    //登録画面
    function getData($model) {

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     STAFF_QUALIFIED_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$model->sendStaffcd}' ";
        $query .= "     AND T1.SEQ = '{$model->clickSeq}' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1) {

        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データチェック
    function getTyoufukuCnt($model) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STAFF_QUALIFIED_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $query .= "     AND SEQ = '".$model->field["SEQ"]."' ";

        return $query;
    }

    //MAXSEQ取得
    function getMaxSeq($model) {

        $query  = " SELECT ";
        $query .= "     RTRIM(CHAR(MAX(INT(SEQ))+1)) ";
        $query .= " FROM ";
        $query .= "     STAFF_QUALIFIED_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";

        return $query;
    }

    /* データ追加処理 */
    function insertData($model)
    {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $query = knjz291_staff_qualifiedQuery::getMaxSeq($model);
        $maxSEQ = ($db2->getOne($query) > 0) ? sprintf("%03d", $db2->getOne($query)) : '001';

        $data = array();
        $data["STAFFCD"][TEXT]          = $model->sendStaffcd;
        $data["SEQ"][TEXT]              = $maxSEQ;
        $data["QUALIFIED_CD"][TEXT]     = $model->field["QUALIFIED_CD"];
        $data["QUALIFIED_NAME"][TEXT]   = $model->field["QUALIFIED_NAME"];
        $data["GET_DATE"][DATE]         = $model->field["GET_DATE"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_QUALIFIED_DAT");
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);

        return $maxSEQ;
    }

    /* データ更新処理 */
    function updateData($model)
    {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $data = array();
        $data["QUALIFIED_CD"][TEXT]     = $model->field["QUALIFIED_CD"];
        $data["QUALIFIED_NAME"][TEXT]   = $model->field["QUALIFIED_NAME"];
        $data["GET_DATE"][DATE]         = $model->field["GET_DATE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $where .= "     AND SEQ = '".$model->field["SEQ"]."' ";

        $query = Query::updateSQL($data, "STAFF_QUALIFIED_DAT", $where);
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);
    }

    /* データ削除処理 */
    function deleteData($model)
    {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $query  = " DELETE FROM STAFF_QUALIFIED_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $query .= "     AND SEQ = '".$model->field["SEQ"]."' ";
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);
    }
}
?>