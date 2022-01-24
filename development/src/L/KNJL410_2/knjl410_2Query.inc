<?php

require_once('for_php7.php');

class knjl410_2Query extends Query {

    //イベント参加者データ取得
    function getRecruitDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO  = '".$model->recruit_no."' ";

        return $query;
    }

    //来校者要約データ取得
    function getRecruitConsultWrapupDat($model) {
        $query  = " SELECT ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     RECRUIT_CONSULT_WRAPUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO  = '".$model->recruit_no."' ";

        return $query;
    }

    //相談データ取得
    function getRecruitConsultDat($model, $div="") {

        $date = ($model->touroku_date == "") ? "" : str_replace("/", "-", $model->touroku_date);

        $query  = " SELECT ";
        $query .= "     T1.RECRUIT_NO, ";
        $query .= "     T1.TOUROKU_DATE, ";
        $query .= "     T1.CONSULT_CD, ";
        $query .= "     T1.METHOD_CD, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.CONTENTS, ";
        $query .= "     S1.STAFFNAME, ";
        $query .= "     N1.NAME1 AS CONSULT_NAME, ";
        $query .= "     N2.NAME1 AS METHOD_NAME ";
        $query .= " FROM ";
        $query .= "     RECRUIT_CONSULT_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST S1 ON S1.STAFFCD = T1.STAFFCD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR      = T1.YEAR ";
        $query .= "                            AND N1.NAMECD1   = 'L404' ";
        $query .= "                            AND N1.NAMECD2   = T1.CONSULT_CD ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON N2.YEAR      = T1.YEAR ";
        $query .= "                            AND N2.NAMECD1   = 'L405' ";
        $query .= "                            AND N2.NAMECD2   = T1.METHOD_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".(CTRL_YEAR + 1)."' ";
        $query .= " AND T1.RECRUIT_NO   = '".$model->recruit_no."' ";
        if ($div) {
            $query .= " AND T1.TOUROKU_DATE = '".$date."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.TOUROKU_DATE DESC ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //面談者取得
    function getStaffMst($model) {
        $query  = " SELECT ";
        $query .= "     S1.STAFFCD AS VALUE, ";
        $query .= "     S1.STAFFCD || ':' || S1.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST S1 ";
        $query .= "     INNER JOIN USERGROUP_DAT U1 ";
        $query .= "          ON U1.YEAR = S1.YEAR ";
        $query .= "         AND U1.STAFFCD = S1.STAFFCD ";
        $query .= "         AND U1.GROUPCD = '0010' ";  //0010:進路指導
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND U1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND U1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     S1.YEAR = '".(CTRL_YEAR)."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function getRow_check($model) {

        $db = Query::dbCheckOut();
        $date = str_replace("/", "-", $model->field["TOUROKU_DATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECRUIT_CONSULT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' AND ";
        $query .= "     TOUROKU_DATE    = '".$date."' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    // UPDATE2
    function &getUpdateQuery2($model, $fields) {

        $db = Query::dbCheckOut();

        //RECRUIT_CONSULT_WRAPUP_DAT
        $data = array();
        $data["YEAR"][TEXT]             = (CTRL_YEAR + 1);
        $data["RECRUIT_NO"][TEXT]       = $model->recruit_no;
        $data["REMARK"][TEXT]           = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        //データチェック
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECRUIT_CONSULT_WRAPUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
        $getDataCnt = $db->getOne($query);
        //UPDATE
        if ($getDataCnt == 0) {
            $db->query(Query::insertSQL($data, "RECRUIT_CONSULT_WRAPUP_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
            $where .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
            $db->query(Query::updateSQL($data, "RECRUIT_CONSULT_WRAPUP_DAT", $where));
        }

        Query::dbCheckIn($db);

        return;
    }

    // UPDATE
    function &getUpdateQuery($model, $fields) {

        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $fields["TOUROKU_DATE"]);

        $data = array();
        $data["YEAR"][TEXT]             = (CTRL_YEAR + 1);
        $data["RECRUIT_NO"][TEXT]       = $model->recruit_no;
        $data["TOUROKU_DATE"][TEXT]     = $date;
        $data["CONSULT_CD"][TEXT]       = $fields["CONSULT_CD"];
        $data["METHOD_CD"][TEXT]        = $fields["METHOD_CD"];
        $data["STAFFCD"][TEXT]          = $fields["STAFFCD"];
        $data["CONTENTS"][TEXT]         = $fields["CONTENTS"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        if ($model->cmd == "insert") {

            $db->query(Query::insertSQL($data, "RECRUIT_CONSULT_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
            $where .= "     RECRUIT_NO      = '".$model->recruit_no."' AND ";
            $where .= "     TOUROKU_DATE    = '".$date."' ";

            $db->query(Query::updateSQL($data, "RECRUIT_CONSULT_DAT", $where));
        }

        Query::dbCheckIn($db);

        return;
    }

    // DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $date = str_replace("/", "-", $model->field["TOUROKU_DATE"]);

        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_CONSULT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' AND ";
        $query .= "     TOUROKU_DATE    = '".$date."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }
}
?>
