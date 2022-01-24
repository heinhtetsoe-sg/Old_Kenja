<?php

require_once('for_php7.php');

class knjz025Query extends Query {

    //教育委員会チェック
    function checkEdboard() {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //データ存在チェック
    function checkControlMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CONTROL_MST ";

        return $query;
    }

    //年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //学校データ取得
    function selectQuery($year) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //都道府県一覧取得
    function getPrefCd() {
        $query  = " SELECT ";
        $query .= "     PREF_CD || ':' || PREF_NAME AS LABEL, ";
        $query .= "     PREF_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校マスタ更新(全レコード）
    function updateSchoolMst($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". $model->year ."' ";
        $db->query($query);

        //INSERT
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SCHOOLNAME1"][TEXT]          = $model->field["SCHOOLNAME1"];
        $data["SCHOOLNAME2"][TEXT]          = $model->field["SCHOOLNAME2"];
        $data["SCHOOLNAME3"][TEXT]          = $model->field["SCHOOLNAME3"];
        $data["SCHOOLNAME_ENG"][TEXT]       = $model->field["SCHOOLNAME_ENG"];
        $data["SCHOOLZIPCD"][TEXT]          = $model->field["SCHOOLZIPCD"];
        $data["SCHOOLADDR1"][TEXT]          = $model->field["SCHOOLADDR1"];
        $data["SCHOOLADDR2"][TEXT]          = $model->field["SCHOOLADDR2"];
        $data["SCHOOLADDR1_ENG"][TEXT]      = $model->field["SCHOOLADDR1_ENG"];
        $data["SCHOOLADDR2_ENG"][TEXT]      = $model->field["SCHOOLADDR2_ENG"];
        $data["SCHOOLTELNO"][TEXT]          = $model->field["SCHOOLTELNO"];
        $data["SCHOOLFAXNO"][TEXT]          = $model->field["SCHOOLFAXNO"];
        $data["SCHOOLMAIL"][TEXT]           = $model->field["SCHOOLMAIL"];
        $data["SCHOOLURL"][TEXT]            = $model->field["SCHOOLURL"];
        $data["SEMESTERDIV"][TEXT]          = 1;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_MST");
        $db->query($query);

        //都道府県データ更新
        knjz025Query::updatePrefCd($model, $db);
        $db->commit();

        Query::dbCheckIn($db);
        return true;
    }

    //SCHOOL_DETAIL_DAT更新（都道府県データ）
    function updatePrefCd($model, $db) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     SCHOOL_SEQ = '002' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            //更新
            $data = array();
            $data["SCHOOL_REMARK1"][TEXT]   = $model->field["PREF_CD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' AND ";
            $where .= "     SCHOOL_SEQ = '002' ";

            $query = Query::updateSQL($data, "SCHOOL_DETAIL_DAT", $where);
            $db->query($query);
        } else {
            //追加
            $data = array();
            $data["YEAR"][TEXT]            = $model->year;
            $data["SCHOOL_SEQ"][TEXT]      = "002";
            $data["SCHOOL_REMARK1"][TEXT]  = $model->field["PREF_CD"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
            $db->query($query);
        }
    }

    //学期マスタ更新
    function updateSemesterMst($year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     SEMESTER_MST";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $db->query($query);

        //INSERT
        $semester = array('1', '9');
        foreach ($semester as $sem) {
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SEMESTER"][TEXT]         = $sem;
            $data["SEMESTERNAME"][TEXT]     = "　";
            $data["SDATE"][TEXT]            = $year."-04-01";
            $data["EDATE"][TEXT]            = ($year+1)."-03-31";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SEMESTER_MST");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
