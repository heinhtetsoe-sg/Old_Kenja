<?php

require_once('for_php7.php');

class knjz024Query extends Query {
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

    function getCourseMajor($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE, ";
        $query .= "     A043.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST VCM ";
        $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
        $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
        $query .= " WHERE ";
        $query .= "     VCM.YEAR = '{$model->year}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE ";
            $query .= "     NAME1 = '".$model->sendSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     NAME1 = '".SCHOOLKIND."' ";
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
        $query .= "     V_SCHOOL_GCM_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
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
            $setNameCd = "Z".$model->sendSchkind."05";
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
        $query .= "     COURSECD || '-' ||  MAJORCD  = '{$model->field["COURSE_MAJOR"]}' ";

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

    /**********************/
    /* 削除してインサート */
    /**********************/
    function updateSchoolDetailDat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSE_MAJOR"]);

        $query = "";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->sendSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query("DELETE FROM SCHOOL_DETAIL_GCM_DAT WHERE YEAR = '{$model->year}' AND SEQ = '001' AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_GCM_DAT WHERE YEAR = '{$model->year}' AND SEQ = '002' AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_GCM_DAT WHERE YEAR = '{$model->year}' AND SEQ = '003' AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_GCM_DAT WHERE YEAR = '{$model->year}' AND SEQ = '004' AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_GCM_DAT WHERE YEAR = '{$model->year}' AND SEQ = '006' AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}'".$query);
        $db->query("DELETE FROM SCHOOL_DETAIL_GCM_DAT WHERE YEAR = '{$model->year}' AND SEQ = '007' AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}'".$query);
        if ($model->Properties["use_Month_Syusu"] == "1") {
            $db->query("DELETE FROM ATTEND_SYUSU_MST  WHERE YEAR = '{$model->year}' AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}'");
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
        $data["GRADE"][TEXT]        = "00";
        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK1"][TEXT]      = $model->field["JUGYOU_JISU_FLG"];
        $data["REMARK2"][TEXT]      = $model->field["RISYU_BUNSI"];
        $data["REMARK3"][TEXT]      = $model->field["RISYU_BUNBO"];
        $data["REMARK4"][TEXT]      = $model->field["SYUTOKU_BUNSI"];
        $data["REMARK5"][TEXT]      = $model->field["SYUTOKU_BUNBO"];
        $data["REMARK6"][TEXT]      = $model->field["JITU_JIFUN"];
        $data["REMARK7"][TEXT]      = $model->field["JITU_SYUSU"];
        $data["REMARK8"][TEXT]      = $model->field["JOUGENTI_SANSYUTU_HOU"];
        $data["REMARK9"][TEXT]      = $model->field["AMARI_KURIAGE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_GCM_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]         = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["GRADE"][TEXT]        = "00";
        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["SEQ"][TEXT]          = "002";
        $data["REMARK1"][TEXT]  = $model->field["PREF_CD"];
        if ($model->dataBaseinfo === '2') {
            $data["REMARK3"][TEXT]  = $model->field["KYOUIKU_IINKAI_SCHOOLCD"];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_GCM_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]         = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["GRADE"][TEXT]        = "00";
        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["SEQ"][TEXT]          = "003";
        $data["REMARK2"][TEXT]      = $model->field["RISYU_BUNSI_SPECIAL"];
        $data["REMARK3"][TEXT]      = $model->field["RISYU_BUNBO_SPECIAL"];
        $data["REMARK4"][TEXT]      = $model->field["RISYU_BUNSI_SPECIAL"];
        $data["REMARK5"][TEXT]      = $model->field["RISYU_BUNBO_SPECIAL"];
        $data["REMARK6"][TEXT]      = $model->field["TOKUBETU_KATUDO_KANSAN"];
        $data["REMARK7"][TEXT]      = $model->field["JITU_JIFUN_SPECIAL"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_GCM_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]         = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["GRADE"][TEXT]        = "00";
        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["SEQ"][TEXT]          = "004";
        $data["REMARK2"][TEXT]      = $model->field["KESSEKI_WARN_BUNSI"];
        $data["REMARK3"][TEXT]      = $model->field["KESSEKI_WARN_BUNBO"];
        $data["REMARK4"][TEXT]      = $model->field["KESSEKI_OUT_BUNSI"];
        $data["REMARK5"][TEXT]      = $model->field["KESSEKI_OUT_BUNBO"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_GCM_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]         = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["GRADE"][TEXT]        = "00";
        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["SEQ"][TEXT]          = "006";
        for ($sem = 1; $sem <= $model->field["MAX_SEMESTER"]; $sem++) {
            $data["REMARK".$sem][TEXT] = $model->field["HOUTEI_SYUSU_SEMESTER".$sem];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_GCM_DAT");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]         = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["GRADE"][TEXT]        = "00";
        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["SEQ"][TEXT]          = "007";
        $data["REMARK1"][TEXT]      = $model->field["PARTS_HYOUKA_HYOUTEI_KEISAN"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_GCM_DAT");
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
                    $data["COURSECD"][TEXT]     = $coursecd;
                    $data["MAJORCD"][TEXT]      = $majorcd;
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
