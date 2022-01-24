<?php

require_once('for_php7.php');

class knjz291_staff_addressQuery extends Query {

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
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T1.STAFFZIPCD, ";
        $query .= "     T1.STAFFADDR1, ";
        $query .= "     substr(T1.STAFFADDR2, 1, 51) AS STAFFADDR2 ";
        $query .= " FROM ";
        $query .= "     STAFF_ADDRESS_DAT T1 ";
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
    function getData($model, $sdate) {

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     STAFF_ADDRESS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$model->sendStaffcd}' ";
        $query .= "     AND T1.SDATE = '".str_replace("/", "-", $sdate)."' ";

        return $query;
    }

    //データチェック
    function getTyoufukuCnt($model) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STAFF_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $query .= "     AND SDATE = '".str_replace("/", "-", $model->field["SDATE"])."' ";

        return $query;
    }

    //直近の開始日を取得
    function getNearSdate($model, $div) {

        $query  = " SELECT ";
        if ($div == 'min') {
            $query .= "     MIN(SDATE) ";
        } else if ($div == 'max') {
            $query .= "     MAX(SDATE) ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     STAFF_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";
        if($div == 'min') {
            $query .= "     AND SDATE > '".str_replace("/", "-", $model->field["SDATE"])."' ";
        } else if ($div == 'max') {
            $query .= "     AND SDATE < '".str_replace("/", "-", $model->field["SDATE"])."' ";
        }

        return $query;
    }

    /* データ追加処理 */
    function insertData($model)
    {
        $db = ($model->sendStaffDiv == "2") ? Query::dbCheckOut() : Query::dbCheckOut2();
        $db->autoCommit(false);

        //直近のデータから終了日を取得
        $query = knjz291_staff_addressQuery::getNearSdate($model, 'min');
        $sdate_min = $db->getOne($query);
        $edate = ($sdate_min) ? date("Y-m-d", strtotime("-1 day" ,strtotime($sdate_min))) : "";

        $data = array();
        $data["STAFFCD"][TEXT]      = $model->sendStaffcd;
        $data["SDATE"][DATE]        = $model->field["SDATE"];
        $data["EDATE"][DATE]        = $edate;
        $data["STAFFZIPCD"][TEXT]   = $model->field["STAFFZIPCD"];
        $data["STAFFADDR1"][TEXT]   = $model->field["STAFFADDR1"];
        $data["STAFFADDR2"][TEXT]   = $model->field["STAFFADDR2"];
        $data["STAFFTELNO"][TEXT]   = $model->field["STAFFTELNO"];
        $data["STAFFFAXNO"][TEXT]   = $model->field["STAFFFAXNO"];
        $data["STAFFE_MAIL"][TEXT]  = $model->field["STAFFE_MAIL"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "STAFF_ADDRESS_DAT");
        $db->query($query);

        //直近のデータの終了日を更新
        $query = knjz291_staff_addressQuery::getNearSdate($model, 'max');
        $sdate_max = $db->getOne($query);
        $edate = ($sdate_max) ? date("Y-m-d", strtotime("-1 day" ,strtotime($model->field["SDATE"]))) : "";

        if ($sdate_max) {
            $data = array();
            $data["EDATE"][DATE]                = $edate;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     STAFFCD     = '".$model->sendStaffcd."' ";
            $where .= "     AND SDATE   = '".str_replace("/", "-", $sdate_max)."' ";

            $query = Query::updateSQL($data, "STAFF_ADDRESS_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    /* データ更新処理 */
    function updateData($model)
    {
        $db = ($model->sendStaffDiv == "2") ? Query::dbCheckOut() : Query::dbCheckOut2();
        $db->autoCommit(false);

        $data = array();
        $data["STAFFZIPCD"][TEXT]   = $model->field["STAFFZIPCD"];
        $data["STAFFADDR1"][TEXT]   = $model->field["STAFFADDR1"];
        $data["STAFFADDR2"][TEXT]   = $model->field["STAFFADDR2"];
        $data["STAFFTELNO"][TEXT]   = $model->field["STAFFTELNO"];
        $data["STAFFFAXNO"][TEXT]   = $model->field["STAFFFAXNO"];
        $data["STAFFE_MAIL"][TEXT]  = $model->field["STAFFE_MAIL"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ";
        $where .= "     STAFFCD     = '".$model->sendStaffcd."' ";
        $where .= "     AND SDATE   = '".str_replace("/", "-", $model->field["SDATE"])."' ";

        $query = Query::updateSQL($data, "STAFF_ADDRESS_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    /* データ削除処理 */
    function deleteData($model)
    {
        $db = ($model->sendStaffDiv == "2") ? Query::dbCheckOut() : Query::dbCheckOut2();
        $db->autoCommit(false);

        $query  = " DELETE FROM STAFF_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD     = '".$model->sendStaffcd."' ";
        $query .= "     AND SDATE   = '".str_replace("/", "-", $model->field["SDATE"])."' ";
        $db->query($query);

        //直近のデータから終了日を取得
        $query = knjz291_staff_addressQuery::getNearSdate($model, 'min');
        $sdate_min = $db->getOne($query);
        $edate = ($sdate_min) ? date("Y-m-d", strtotime("-1 day" ,strtotime($sdate_min))) : "";

        //直近のデータから開始日を取得
        $query = knjz291_staff_addressQuery::getNearSdate($model, 'max');
        $sdate_max = $db->getOne($query);

        if ($sdate_max) {
            $data = array();
            $data["EDATE"][DATE]                = $edate;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     STAFFCD     = '".$model->sendStaffcd."' ";
            $where .= "     AND SDATE   = '".str_replace("/", "-", $sdate_max)."' ";

            $query = Query::updateSQL($data, "STAFF_ADDRESS_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //MAX日付取得
    function getMaxSdate($model) {

        $query  = " SELECT ";
        $query .= "     MAX(SDATE) AS SDATE ";
        $query .= " FROM ";
        $query .= "     STAFF_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$model->sendStaffcd}' ";

        return $query;
    }

    /* STAFF_MST更新処理 */
    function updateStaffMst($model)
    {
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //セットデータ取得
        $query = knjz291_staff_addressQuery::getMaxSdate($model);
        $sdate = ($model->sendStaffDiv == "2") ? $db->getOne($query) : $db2->getOne($query);
        $query = knjz291_staff_addressQuery::getData($model, $sdate);
        $setData = ($model->sendStaffDiv == "2") ? $db->getRow($query, DB_FETCHMODE_ASSOC) : $db2->getRow($query, DB_FETCHMODE_ASSOC);

        //STAFF_MST更新
        $db->autoCommit(false);
        $data = array();
        $data["STAFFZIPCD"][TEXT]   = $setData["STAFFZIPCD"];
        $data["STAFFADDR1"][TEXT]   = $setData["STAFFADDR1"];
        $data["STAFFADDR2"][TEXT]   = $setData["STAFFADDR2"];
        $data["STAFFTELNO"][TEXT]   = $setData["STAFFTELNO"];
        $data["STAFFFAXNO"][TEXT]   = $setData["STAFFFAXNO"];
        $data["STAFFE_MAIL"][TEXT]  = $setData["STAFFE_MAIL"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ";
        $where .= "     STAFFCD = '".$model->sendStaffcd."' ";

        $query = Query::updateSQL($data, "STAFF_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
    }
}
?>