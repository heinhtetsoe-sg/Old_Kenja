<?php

require_once('for_php7.php');

class knjz021Query extends Query {
    //学校名取得
    function getSchoolName2($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->sendSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    function getVNameMstZ042($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'Z042' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function checkControlMst($db) {
        if ($db->getOne("SELECT COUNT(*) FROM CONTROL_MST") > 0) {
            return true;
        } else {
            return false;
        }
    }

    function getSchoolDetailDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->sendSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }
    
    function getSemester($model, $flg="") {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     MAX(SEMESTER) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND SEMESTER <> '9' ";
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     SEMESTER ";
        }

        return $query;
    }

    //都道府県
    function getPrefCd() {
        $query  = " SELECT ";
        $query .= "     PREF_CD || ':' || PREF_NAME AS LABEL, ";
        $query .= "     PREF_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "     PREF_CD ";

        return $query;
    }

    //学期情報取得
    function selectSemesAll($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //月一覧取得
    function getMonthList($model) {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd  = "Z".$model->sendSchkind."05";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' ";

        return $query;
    }

    //月毎の授業週数データ取得
    function getAttendSyusuMst($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     MONTH, ";
        $query .= "     SEMESTER, ";
        $query .= "     SYUSU ";
        $query .= " FROM ";
        $query .= "     ATTEND_SYUSU_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     GRADE       = '00' AND ";
        $query .= "     COURSECD    = '0' AND ";
        $query .= "     MAJORCD     = '000' ";

        return $query;
    }

    //教育委員会、学校サーバ情報
    function getDataBaseInfo() {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //教育委員側DB　教育委員会設定学校マスタを取得
    function getEdboardSchoolcd() {
        $query .= " SELECT ";
        $query .= "     EDBOARD_SCHOOLCD AS VALUE, ";
        $query .= "     EDBOARD_SCHOOLCD || ':' || EDBOARD_SCHOOLNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    /**********************/
    /* 削除してインサート */
    /**********************/
    function updateSchoolDetailDat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->sendSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query("DELETE FROM SCHOOL_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHOOL_SEQ = '001'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHOOL_SEQ = '002'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHOOL_SEQ = '003'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHOOL_SEQ = '004'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHOOL_SEQ = '006'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHOOL_SEQ = '007'".$query);
        if ($model->Properties["use_Month_Syusu"] == "1") {
            $db->query("DELETE FROM ATTEND_SYUSU_MST  WHERE YEAR = '{$model->year}'");
        }

        $data = array();
        $data["YEAR"][TEXT]            = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["SCHOOL_SEQ"][TEXT]      = "001";
        $data["SCHOOL_REMARK1"][TEXT]  = $model->field["JUGYOU_JISU_FLG"];
        $data["SCHOOL_REMARK2"][TEXT]  = $model->field["RISYU_BUNSI"];
        $data["SCHOOL_REMARK3"][TEXT]  = $model->field["RISYU_BUNBO"];
        $data["SCHOOL_REMARK4"][TEXT]  = $model->field["SYUTOKU_BUNSI"];
        $data["SCHOOL_REMARK5"][TEXT]  = $model->field["SYUTOKU_BUNBO"];
        $data["SCHOOL_REMARK6"][TEXT]  = $model->field["JITU_JIFUN"];
        $data["SCHOOL_REMARK7"][TEXT]  = $model->field["JITU_SYUSU"];
        $data["SCHOOL_REMARK8"][TEXT]  = $model->field["JOUGENTI_SANSYUTU_HOU"];
        $data["SCHOOL_REMARK9"][TEXT]  = $model->field["AMARI_KURIAGE"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]            = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["SCHOOL_SEQ"][TEXT]      = "002";
        $data["SCHOOL_REMARK1"][TEXT]  = $model->field["PREF_CD"];
        if ($model->dataBaseinfo === '2') {
            $data["SCHOOL_REMARK3"][TEXT]  = $model->field["KYOUIKU_IINKAI_SCHOOLCD"];
        }
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]            = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["SCHOOL_SEQ"][TEXT]      = "003";
        $data["SCHOOL_REMARK2"][TEXT]  = $model->field["RISYU_BUNSI_SPECIAL"];
        $data["SCHOOL_REMARK3"][TEXT]  = $model->field["RISYU_BUNBO_SPECIAL"];
        $data["SCHOOL_REMARK4"][TEXT]  = $model->field["RISYU_BUNSI_SPECIAL"];
        $data["SCHOOL_REMARK5"][TEXT]  = $model->field["RISYU_BUNBO_SPECIAL"];
        $data["SCHOOL_REMARK6"][TEXT]  = $model->field["TOKUBETU_KATUDO_KANSAN"];
        $data["SCHOOL_REMARK7"][TEXT]  = $model->field["JITU_JIFUN_SPECIAL"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]            = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["SCHOOL_SEQ"][TEXT]      = "004";
        $data["SCHOOL_REMARK2"][TEXT]  = $model->field["KESSEKI_WARN_BUNSI"];
        $data["SCHOOL_REMARK3"][TEXT]  = $model->field["KESSEKI_WARN_BUNBO"];
        $data["SCHOOL_REMARK4"][TEXT]  = $model->field["KESSEKI_OUT_BUNSI"];
        $data["SCHOOL_REMARK5"][TEXT]  = $model->field["KESSEKI_OUT_BUNBO"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]            = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["SCHOOL_SEQ"][TEXT]      = "006";
        for ($sem = 1; $sem <= $model->field["MAX_SEMESTER"]; $sem++) {
            $data["SCHOOL_REMARK".$sem][TEXT] = $model->field["HOUTEI_SYUSU_SEMESTER".$sem];
        }
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]            = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["SCHOOL_SEQ"][TEXT]      = "007";
        $data["SCHOOL_REMARK1"][TEXT]  = $model->field["PARTS_HYOUKA_HYOUTEI_KEISAN"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
        $db->query($query);

        if ($model->Properties["use_Month_Syusu"] == "1") {
            for ($dcnt = 0; $dcnt < get_count($model->seminfo); $dcnt++) {
                for ($i = $model->seminfo[$dcnt]["S_MONTH"]; $i <= $model->seminfo[$dcnt]["E_MONTH"]; $i++) {
                    $month = $i;
                    if ($i > 12) $month = $i - 12;
                    $month = sprintf('%02d', $month);

                    $data = array();
                    $data["YEAR"][TEXT]         = $model->year;
                    $data["MONTH"][TEXT]        = $month;
                    $data["SEMESTER"][TEXT]     = $model->seminfo[$dcnt]["SEMESTER"];
                    $data["GRADE"][TEXT]        = "00";
                    $data["COURSECD"][TEXT]     = "0";
                    $data["MAJORCD"][TEXT]      = "000";
                    $data["SYUSU"][NUMBER]      = $model->field["HOUTEI_SYUSU_MONTH_".$model->seminfo[$dcnt]["SEMESTER"]."-".$month];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    $query = Query::insertSQL($data, "ATTEND_SYUSU_MST");
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
