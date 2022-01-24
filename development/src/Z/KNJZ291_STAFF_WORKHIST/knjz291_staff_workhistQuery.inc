<?php

require_once('for_php7.php');

class knjz291_staff_workhistQuery extends Query {

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
        $query .= "     T1.FROM_DATE, ";
        $query .= "     T1.FROM_SCHOOLCD, ";
        $query .= "     L3.EDBOARD_SCHOOLNAME AS FROM_SCHOOLNAME, ";
        $query .= "     T1.FROM_DIV, ";
        $query .= "     L2.NAME1 AS FROM_NAME, ";
        $query .= "     COURSE1.COURSENAME AS FROM_COURSENAME, ";
        $query .= "     T1.TO_DATE, ";
        $query .= "     T1.TO_SCHOOLCD, ";
        $query .= "     L5.EDBOARD_SCHOOLNAME AS TO_SCHOOLNAME, ";
        $query .= "     T1.TO_DIV, ";
        $query .= "     L4.NAME1 AS TO_NAME, ";
        $query .= "     COURSE2.COURSENAME AS TO_COURSENAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_STAFF_WORK_HIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z041' ";
        $query .= "          AND T1.FROM_DIV = L2.NAMECD2 ";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST L3 ON T1.FROM_SCHOOLCD = L3.EDBOARD_SCHOOLCD ";
        $query .= "     LEFT JOIN COURSE_MST COURSE1 ON T1.FROM_COURSECD = COURSE1.COURSECD ";
        $query .= "     LEFT JOIN NAME_MST L4 ON L4.NAMECD1 = 'Z041' ";
        $query .= "          AND T1.TO_DIV = L4.NAMECD2 ";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST L5 ON T1.TO_SCHOOLCD = L5.EDBOARD_SCHOOLCD ";
        $query .= "     LEFT JOIN COURSE_MST COURSE2 ON T1.TO_COURSECD = COURSE2.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$model->sendStaffcd}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.FROM_DATE DESC ";

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
        $query .= "     EDBOARD_STAFF_WORK_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$model->sendStaffcd}' ";
        $query .= "     AND T1.FROM_DATE = '".str_replace("/", "-", $model->clickFdate)."' ";

        return $query;
    }

    //Z010
    function getKyouikuIinkai() {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'Z010' ";
        $query .= "     AND T1.ABBV1 = '1' ";

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
        if ($div == "FROM") {
            $query .= "     AND T1.ABBV1 = '1' ";
        }
        if ($div == "TO") {
            $query .= "     AND T1.ABBV2 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校取得
    function getEdSchool($model, $kyouikuIinkai) {

        $query  = " SELECT ";
        $query .= "     T1.EDBOARD_SCHOOLCD AS VALUE, ";
        $query .= "     T1.EDBOARD_SCHOOLCD || ':' || T1.EDBOARD_SCHOOLNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST T1 ";
        if ($kyouikuIinkai == 0) {
            $query .= " WHERE ";
            $query .= "     T1.EDBOARD_SCHOOLCD = '{$model->edboard_schoolcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程取得
    function getCouser() {
        $query  = " SELECT ";
        $query .= "     COURSECD AS VALUE, ";
        $query .= "     COURSECD || ':' || COURSENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";

        return $query;
    }

    //職員取得
    function getStaff($staffCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$staffCd."' ";

        return $query;
    }

    //データチェック
    function getTyoufukuCnt($model) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EDBOARD_STAFF_WORK_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $query .= "     AND FROM_DATE = '".str_replace("/", "-", $model->field["FROM_DATE"])."' ";

        return $query;
    }

    /* データ追加処理 */
    function insertData($model)
    {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $data = array();
        $data["STAFFCD"][TEXT]          = $model->sendStaffcd;
        $data["FROM_DATE"][DATE]        = $model->field["FROM_DATE"];
        $data["WORK_DIV"][TEXT]         = "0";
        $data["FROM_DIV"][TEXT]         = $model->field["FROM_DIV"];
        $data["FROM_SCHOOLCD"][TEXT]    = $model->field["FROM_SCHOOLCD"];
        $data["FROM_COURSECD"][TEXT]    = $model->field["FROM_COURSECD"];
        $data["TO_DATE"][DATE]          = $model->field["TO_DATE"];
        $data["TO_DIV"][TEXT]           = $model->field["TO_DIV"];
        $data["TO_SCHOOLCD"][TEXT]      = $model->field["TO_SCHOOLCD"];
        $data["TO_COURSECD"][TEXT]      = $model->field["TO_COURSECD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "EDBOARD_STAFF_WORK_HIST_DAT");
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
        $data["FROM_DIV"][TEXT]         = $model->field["FROM_DIV"];
        $data["FROM_SCHOOLCD"][TEXT]    = $model->field["FROM_SCHOOLCD"];
        $data["FROM_COURSECD"][TEXT]    = $model->field["FROM_COURSECD"];
        $data["TO_DATE"][DATE]          = $model->field["TO_DATE"];
        $data["TO_DIV"][TEXT]           = $model->field["TO_DIV"];
        $data["TO_SCHOOLCD"][TEXT]      = $model->field["TO_SCHOOLCD"];
        $data["TO_COURSECD"][TEXT]      = $model->field["TO_COURSECD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $where .= "     AND FROM_DATE = '".str_replace("/", "-", $model->field["FROM_DATE"])."' ";

        $query = Query::updateSQL($data, "EDBOARD_STAFF_WORK_HIST_DAT", $where);
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);
    }

    /* データ削除処理 */
    function deleteData($model)
    {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $query  = " DELETE FROM EDBOARD_STAFF_WORK_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->sendStaffcd."' ";
        $query .= "     AND FROM_DATE = '".str_replace("/", "-", $model->field["FROM_DATE"])."' ";
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);
    }

    /* STAFF_YDAT削除 */
    function deleteStaffYdat($model)
    {
        if ($model->field["TO_DATE"] == "") {
            return;
        }
        if ($model->kyouikuIinkai == 1) {
            return;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($tYear, $tMonth, $tDay) = explode("/", $model->field["TO_DATE"]);
        if ($tMonth >= "04") {
            $tYear = $tYear++;
        }
        $query  = " DELETE FROM STAFF_YDAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$model->sendStaffcd}' ";
        $query .= "     AND YEAR = '{$tYear}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>