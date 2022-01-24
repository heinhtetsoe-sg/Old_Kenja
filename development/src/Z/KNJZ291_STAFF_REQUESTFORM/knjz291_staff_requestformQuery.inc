<?php

require_once('for_php7.php');

class knjz291_staff_requestformQuery extends Query {

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
        $query .= "     T1.WORK_DIV, ";
        $query .= "     L2.NAME1 AS WORK_NAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     substr(T1.REASON, 1, 78) AS REASON ";
        $query .= " FROM ";
        $query .= "     STAFF_REQUESTFORM_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z028' ";
        $query .= "          AND T1.WORK_DIV = L2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$model->sendStaffcd}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE DESC ";

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
        $query .= "     STAFF_REQUESTFORM_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$model->sendStaffcd}' ";
        $query .= "     AND T1.SDATE = '".str_replace("/", "-", $model->clickSdate)."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $div = "") {

        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = '{$namecd1}' ";
        if ($div) {
            $query .= "     AND T1.NAMESPARE1 = '3' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データチェック
    function getTyoufukuCnt($model) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STAFF_REQUESTFORM_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $query .= "     AND SDATE = '".str_replace("/", "-", $model->field["SDATE"])."' ";

        return $query;
    }

    /* データ追加処理 */
    function insertData($model)
    {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $data = array();
        $data["STAFFCD"][TEXT]          = $model->sendStaffcd;
        $data["SDATE"][DATE]            = $model->field["SDATE"];
        $data["EDATE"][DATE]            = $model->field["EDATE"];
        $data["WORK_DIV"][TEXT]         = $model->field["WORK_DIV"];
        $data["REASON"][TEXT]           = $model->field["REASON"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_REQUESTFORM_DAT");
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);
    }

    /* データ更新処理 */
    function updateData($model)
    {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $data = array();
        $data["EDATE"][DATE]            = $model->field["EDATE"];
        $data["WORK_DIV"][TEXT]         = $model->field["WORK_DIV"];
        $data["REASON"][TEXT]           = $model->field["REASON"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $where .= "     AND SDATE = '".str_replace("/", "-", $model->field["SDATE"])."' ";

        $query = Query::updateSQL($data, "STAFF_REQUESTFORM_DAT", $where);
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);
    }

    /* データ削除処理 */
    function deleteData($model)
    {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $query  = " DELETE FROM STAFF_REQUESTFORM_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $query .= "     AND SDATE = '".str_replace("/", "-", $model->field["SDATE"])."' ";
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);
    }
}
?>