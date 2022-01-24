<?php

require_once('for_php7.php');

class knjz291_staff_name_histQuery extends Query {

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
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T1.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     STAFF_NAME_HIST_DAT T1 ";
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
        $query .= "     STAFF_NAME_HIST_DAT T1 ";
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
        $query .= "     STAFF_NAME_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD     = '".$model->sendStaffcd."' ";
        $query .= "     AND SDATE   = '".str_replace("/", "-", $model->field["SDATE"])."' ";

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
        $query .= "     STAFF_NAME_HIST_DAT ";
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
        $query = knjz291_staff_name_histQuery::getNearSdate($model, 'min');
        $sdate_min = $db->getOne($query);
        $edate = ($sdate_min) ? date("Y-m-d", strtotime("-1 day" ,strtotime($sdate_min))) : "";

        $data = array();
        $data["STAFFCD"][TEXT]              = $model->sendStaffcd;
        $data["SDATE"][DATE]                = $model->field["SDATE"];
        if (!$edate) {
            $data["EDATE"][DATE]                = "9999-03-31";
        } else {
            $data["EDATE"][DATE]                = $edate;
        }
        $data["STAFFNAME"][TEXT]            = $model->field["STAFFNAME"];
        $data["STAFFNAME_SHOW"][TEXT]       = $model->field["STAFFNAME_SHOW"];
        $data["STAFFNAME_KANA"][TEXT]       = $model->field["STAFFNAME_KANA"];
        $data["STAFFNAME_ENG"][TEXT]        = $model->field["STAFFNAME_ENG"];
        $data["STAFFNAME_REAL"][TEXT]       = $model->field["STAFFNAME_REAL"];
        $data["STAFFNAME_KANA_REAL"][TEXT]  = $model->field["STAFFNAME_KANA_REAL"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "STAFF_NAME_HIST_DAT");
        $db->query($query);

        //直近のデータの終了日を更新
        $query = knjz291_staff_name_histQuery::getNearSdate($model, 'max');
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

            $query = Query::updateSQL($data, "STAFF_NAME_HIST_DAT", $where);
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
        $data["STAFFNAME"][TEXT]            = $model->field["STAFFNAME"];
        $data["STAFFNAME_SHOW"][TEXT]       = $model->field["STAFFNAME_SHOW"];
        $data["STAFFNAME_KANA"][TEXT]       = $model->field["STAFFNAME_KANA"];
        $data["STAFFNAME_ENG"][TEXT]        = $model->field["STAFFNAME_ENG"];
        $data["STAFFNAME_REAL"][TEXT]       = $model->field["STAFFNAME_REAL"];
        $data["STAFFNAME_KANA_REAL"][TEXT]  = $model->field["STAFFNAME_KANA_REAL"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $where .= "     AND SDATE = '".str_replace("/", "-", $model->field["SDATE"])."' ";

        $query = Query::updateSQL($data, "STAFF_NAME_HIST_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    /* データ削除処理 */
    function deleteData($model)
    {
        $db = ($model->sendStaffDiv == "2") ? Query::dbCheckOut() : Query::dbCheckOut2();
        $db->autoCommit(false);

        $query  = " DELETE FROM STAFF_NAME_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $query .= "     AND SDATE = '".str_replace("/", "-", $model->field["SDATE"])."' ";
        $db->query($query);

        //直近のデータから終了日を取得
        $query = knjz291_staff_name_histQuery::getNearSdate($model, 'min');
        $sdate_min = $db->getOne($query);
        $edate = ($sdate_min) ? date("Y-m-d", strtotime("-1 day" ,strtotime($sdate_min))) : "";

        //直近のデータから開始日を取得
        $query = knjz291_staff_name_histQuery::getNearSdate($model, 'max');
        $sdate_max = $db->getOne($query);

        if ($sdate_max) {
            $data = array();
            if (!$edate) {
                $data["EDATE"][DATE]                = "9999-03-31";
            } else {
                $data["EDATE"][DATE]                = $edate;
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     STAFFCD     = '".$model->sendStaffcd."' ";
            $where .= "     AND SDATE   = '".str_replace("/", "-", $sdate_max)."' ";

            $query = Query::updateSQL($data, "STAFF_NAME_HIST_DAT", $where);
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
        $query .= "     STAFF_NAME_HIST_DAT ";
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
        $query = knjz291_staff_name_histQuery::getMaxSdate($model);
        $sdate = ($model->sendStaffDiv == "2") ? $db->getOne($query) : $db2->getOne($query);
        $query = knjz291_staff_name_histQuery::getData($model, $sdate);
        $setData = ($model->sendStaffDiv == "2") ? $db->getRow($query, DB_FETCHMODE_ASSOC) : $db2->getRow($query, DB_FETCHMODE_ASSOC);

        //STAFF_MST更新
        $db->autoCommit(false);
        $data = array();
        $data["STAFFNAME"][TEXT]            = $setData["STAFFNAME"];
        $data["STAFFNAME_SHOW"][TEXT]       = $setData["STAFFNAME_SHOW"];
        $data["STAFFNAME_KANA"][TEXT]       = $setData["STAFFNAME_KANA"];
        $data["STAFFNAME_ENG"][TEXT]        = $setData["STAFFNAME_ENG"];
        $data["STAFFNAME_REAL"][TEXT]       = $setData["STAFFNAME_REAL"];
        $data["STAFFNAME_KANA_REAL"][TEXT]  = $setData["STAFFNAME_KANA_REAL"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

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