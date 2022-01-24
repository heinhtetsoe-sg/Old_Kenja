<?php

require_once('for_php7.php');

class knjx_c031fQuery extends Query {

    //学校マスタ取得
    function getSchoolMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
                $query .= " AND SCHOOL_KIND = '". SCHOOLKIND ."' ";
            } else {
                $query .= " AND SCHOOL_KIND IN (SELECT ";
                $query .= "                         SCHOOL_KIND ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT ";
                $query .= "                     WHERE ";
                $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
                $query .= "                         GRADE   = '".$model->field["grade"]."' ";
                $query .= "                     ) ";
            }
        }

        return $query;
    }

    function getSecurityHigh($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$model->getPrgId}' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //MIN権限グループ取得
    function getMinGroupcd($model) {
        $query  = " SELECT ";
        $query .= "     SUBSTR(MIN((CASE GROUPCD WHEN '9999' THEN '0' ELSE '1' END) || GROUPCD),2) ";
        $query .= " FROM ";
        $query .= "     USERGROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        }
        $query .= "     STAFFCD     = '".STAFFCD."' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 ADMIN_CONTROL_ATTEND_DAT E1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR         = E1.YEAR AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                 E1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                 T1.SCHOOL_KIND  = E1.SCHOOL_KIND AND ";
        }
        $query .= "                 E1.PROGRAMID    = '".$model->getPrgId."' AND ";
        $query .= "                 T1.GROUPCD      = E1.GROUPCD ";
        $query .= "             ) ";

        return $query;
    }

    //出欠項目名一覧取得
    function getAttendNameList($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A004' ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C002' ";

        return $query;
    }

    //出欠項目名一覧取得
    function getAdminControlAttendItemnameDat($model, $cm) {
        //学校種別
        $school_kind = '00';
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $school_kind = $model->field["SCHOOL_KIND"];
        }

        $query  = " SELECT ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     ATTEND_ITEMNAME ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_ITEMNAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        $query .= "     ATTEND_DIV  = '1' AND ";
        $query .= "     GRADE       = '00'  AND ";
        $query .= "     COURSECD || MAJORCD = '".$cm."' ";

        return $query;
    }

    //出欠管理者コントロール取得
    function getAdminControlAttendDat($model, $cm, $div) {
        $school_kind = '00';
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $school_kind = $model->field["SCHOOL_KIND"];
        }

        $query  = " SELECT ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     SHOWORDER, ";
        $query .= "     INPUT_FLG ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        $query .= "     CONTROL_DIV = '".$div."' AND ";
        $query .= "     ATTEND_DIV  = '1' AND ";
        $query .= "     GRADE       = '00'  AND ";
        $query .= "     COURSECD || MAJORCD = '".$cm."' AND ";
        $query .= "     PROGRAMID   = '".$model->getPrgId."' AND ";
        $query .= "     GROUPCD     = '".$model->groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(SHOWORDER) ";

        return $query;
    }

    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->selectSchoolKind) {
            $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コンボ取得
    function getCourseMajor($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || S1.COURSENAME || S1.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST S1 ";
        $query .= "          ON T1.YEAR         = S1.YEAR ";
        $query .= "         AND T1.COURSECD     = S1.COURSECD ";
        $query .= "         AND T1.MAJORCD      = S1.MAJORCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度学期一覧
    function getSelectFieldSQL($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SEMESTER_MST T2, ";
        $query .= "     SCHREG_REGD_DAT T3 ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T4 ";
            $query .= "              ON T3.YEAR         = T4.YEAR ";
            $query .= "             AND T3.GRADE        = T4.GRADE ";
            $query .= "             AND T4.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.GRADE    = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T3.COURSECD || T3.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " AND (";
            $query .= "     T1.TR_CD1       = '".STAFFCD."' OR ";
            $query .= "     T1.TR_CD2       = '".STAFFCD."' OR ";
            $query .= "     T1.TR_CD3       = '".STAFFCD."' OR ";
            $query .= "     T1.SUBTR_CD1    = '".STAFFCD."' OR ";
            $query .= "     T1.SUBTR_CD2    = '".STAFFCD."' OR ";
            $query .= "     T1.SUBTR_CD3    = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    function getSelectFieldSQL2($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T2.COURSECD || T2.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   T1.TR_CD1       = '".STAFFCD."' OR ";
            $query .= "         T1.TR_CD2       = '".STAFFCD."' OR ";
            $query .= "         T1.TR_CD3       = '".STAFFCD."' OR ";
            $query .= "         T1.SUBTR_CD1    = '".STAFFCD."' OR ";
            $query .= "         T1.SUBTR_CD2    = '".STAFFCD."' OR ";
            $query .= "         T1.SUBTR_CD3    = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".substr($model->field["YEAR"],0,4)."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //複式学級取得
    function getGroupHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.GHR_CD AS VALUE, ";
        $query .= "     T3.GHR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GHR_DAT T2, ";
        $query .= "     SCHREG_REGD_GHR_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GHR_CD   = T3.GHR_CD ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."') ";
        }
        if ($model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " AND (T3.TR_CD1      = '".STAFFCD."' OR ";
            $query .= "      T3.TR_CD2      = '".STAFFCD."' OR ";
            $query .= "      T3.TR_CD3      = '".STAFFCD."' OR ";
            $query .= "      T3.SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "      T3.SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "      T3.SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期情報取得
    function selectSemesAll($year_sem, $flg="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        if (!$flg) {
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        } else {
            $query .= "     MONTH(SDATE) AS S_MONTH, ";
            $query .= "     DAY(SDATE) AS S_DAY, ";
            $query .= "     MONTH(EDATE) AS E_MONTH, ";
            $query .= "     DAY(EDATE) AS E_DAY ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$year_sem."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月データの取得
    function selectMonthQuery($year, $month, $model) {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, NAME1, NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$year."' ";
        $query .= "     AND NAMECD1 = '{$setNameCd}' ";
        $query .= "     AND NAMECD2 IN (SELECT CONTROL_CODE FROM ADMIN_CONTROL_DAT WHERE YEAR = '".$year."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        }
        $query .= "     CONTROL_FLG = '2') ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //フィールド一覧取得
    function getFieldList() {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         COLNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SYSIBM.SYSCOLUMNS ";
        $query .= "     WHERE ";
        $query .= "         TBNAME  = 'ATTEND_SEMES_DAT' AND ";
        $query .= "         NULLS   = 'Y' AND ";
        $query .= "         NAME NOT IN ('APPOINTED_DAY', 'REGISTERCD', 'UPDATED') ";
        $query .= "     ORDER BY ";
        $query .= "         COLNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //フィールド取得
        $query = knjx_c031fQuery::getFieldList();
        $table_field = array();
        $table_field = $db->getCol($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            /**********************/
            /*  ATTEND_SEMES_DAT  */
            /**********************/

            $appointed_day = $db->getOne(knjx_c031fQuery::getAppointedDay($data_arr[$i]["YEAR"], $model->field["SCHOOL_KIND"], $data_arr[$i]["MONTH"], $data_arr[$i]["SEMESTER"]));

            $data = array();
            $data["COPYCD"][TEXT]           = '0';
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["APPOINTED_DAY"][TEXT]    = $appointed_day;
            foreach ($model->item_array as $order => $val) {
                //入力可の項目が更新対象
                if (in_array($val["item"], $table_field) && $val["input"]) {
                    $data[$val["item"]][NUMBER]     = $data_arr[$i][$val["item"]];
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $check  = " SELECT COUNT(*) FROM ATTEND_SEMES_DAT ";
            $check .= " WHERE ";
            $check .= "     COPYCD      = '0' AND ";
            $check .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $check .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
            $check .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $check .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";

            if (1 > $db->getOne($check)) {
                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                $where .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
                $where .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "     COPYCD      = '0' ";

                $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
            }
            $db->query($query);

            /*****************************/
            /*  ATTEND_SEMES_DETAIL_DAT  */
            /*****************************/

            foreach ($model->item_array as $order => $val) {
                list ($item, $seq) = explode('_', $val["item"]);
                if ($item == "DETAIL") {
                    $query  = " DELETE FROM ";
                    $query .= "     ATTEND_SEMES_DETAIL_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                    $query .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
                    $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                    $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                    $query .= "     SEQ         = '".$seq."' AND ";
                    $query .= "     COPYCD      = '0' ";
                    $db->query($query);
                
                    $data = array();
                    $data["COPYCD"][TEXT]           = '0';
                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
                    $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    $data["SEQ"][TEXT]              = $seq;
                    if ($seq == "101") {
                        $data["CNT_DECIMAL"][NUMBER]    = $data_arr[$i][$val["item"]];
                    } else {
                        $data["CNT"][NUMBER]            = $data_arr[$i][$val["item"]];
                    }
                    $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                    $db->query($query);
                }
            }

            /*****************************/
            /*  ATTEND_SEMES_REMARK_DAT  */
            /*****************************/

            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
            $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     COPYCD      = '0' ";
            $db->query($query);

            $data = array();
            $data["COPYCD"][TEXT]       = '0';
            $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
            $data["MONTH"][TEXT]        = $data_arr[$i]["MONTH"];
            $data["SEMESTER"][TEXT]     = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
            $data["REMARK1"][TEXT]      = $data_arr[$i]["REMARK"];
            $query = Query::insertSQL($data, "ATTEND_SEMES_REMARK_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //ATTEND_SEMES_DAT
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
            $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     COPYCD      = '0' ";

            $db->query($query);

            //ATTEND_SEMES_DETAIL_DAT
            foreach ($model->item_array as $order => $val) {
                list ($item, $seq) = explode('_', $val["item"]);
                if ($item == "DETAIL") {
                    $query  = " DELETE FROM ";
                    $query .= "     ATTEND_SEMES_DETAIL_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                    $query .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
                    $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                    $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                    $query .= "     SEQ         = '".$seq."' AND ";
                    $query .= "     COPYCD      = '0' ";

                    $db->query($query);
                }
            }

            //ATTEND_SEMES_REMARK_DAT
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
            $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     COPYCD      = '0' ";

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //存在チェック
    function checkRegdDat($year, $semester, $schregno, $model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if ($model->Properties["useSchool_KindField"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".$year."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "              ) ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND COURSECD || MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }

        return $query;
    }

    //異動データ（退学・転学・卒業）取得
    function getIdouData($schregno, $date1, $date2) {
        $query  = " SELECT ";
        $query .= "     CASE WHEN GRD_DATE < '".$date2."' THEN 1 ELSE 0 END ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRD_DIV IN ('1', '2', '3', '6', '7') ";

        return $query;
    }

    //異動データ（留学・休学）取得
    function getTransferData($schregno, $date1, $date2) {
        $query  = " SELECT ";
        $query .= "     SUM(CASE WHEN TRANSFER_SDATE <= '".$date2."' AND '".$date1."' <= CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END THEN 1 ELSE 0 END) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') ";

        return $query;
    }

    //制限チェック
    function checkRegdHdat($model, $year, $semester, $schregno) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT T2 ON T1.YEAR = T2.YEAR AND ";
        $query .= "             T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "             T1.GRADE    = T2.GRADE AND ";
        $query .= "             T1.HR_CLASS = T2.HR_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON T1.YEAR = G1.YEAR ";
            $query .= "         AND T1.GRADE        = G1.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$year."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        $query .= "         T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "         '".STAFFCD."' IN (T2.TR_CD1, T2.TR_CD2, T2.TR_CD3, T2.SUBTR_CD1, T2.SUBTR_CD2, T2.SUBTR_CD3) ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1) {
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         SCHREGNO ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_GHR_DAT T1, ";
            $query .= "         SCHREG_REGD_GHR_HDAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = T2.YEAR AND ";
            $query .= "         T1.YEAR     = '".$year."' AND ";
            $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "         T1.SEMESTER = '".$semester."' AND ";
            $query .= "         T1.GHR_CD   = T2.GHR_CD AND ";
            $query .= "         T1.SCHREGNO = '".$schregno."' AND ";
            $query .= "         '".STAFFCD."' IN (T2.TR_CD1, T2.TR_CD2, T2.TR_CD3, T2.SUBTR_CD1, T2.SUBTR_CD2, T2.SUBTR_CD3) ";
            if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
                $query .= " AND EXISTS( SELECT ";
                $query .= "                 'X' ";
                $query .= "             FROM ";
                $query .= "                 SCHREG_REGD_DAT E1 ";
                $query .= "             WHERE ";
                $query .= "                 E1.YEAR     = T1.YEAR AND ";
                $query .= "                 E1.SEMESTER = T1.SEMESTER AND ";
                $query .= "                 E1.SCHREGNO = T1.SCHREGNO AND ";
                $query .= "                 E1.COURSECD || E1.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
                $query .= "             ) ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND EXISTS( SELECT ";
                $query .= "                 'X' ";
                $query .= "             FROM ";
                $query .= "                 SCHREG_REGD_DAT E1, ";
                $query .= "                 SCHREG_REGD_GDAT E2 ";
                $query .= "             WHERE ";
                $query .= "                 E1.YEAR         = T1.YEAR AND ";
                $query .= "                 E2.YEAR         = T1.YEAR AND ";
                $query .= "                 E1.SEMESTER     = T1.SEMESTER AND ";
                $query .= "                 E1.SCHREGNO     = T1.SCHREGNO AND ";
                $query .= "                 E1.GRADE        = E2.GRADE AND ";
                $query .= "                 E2.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
                $query .= "             ) ";
            }
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MAIN ";

        return $query;
    }

    //締め日の取得
    function getAppointedDay($year, $school_kind, $month, $semester) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$semester."' ";


        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    function selectMainQuery($model, $schoolMst) {
    	$monthsem = preg_split("/-/", $model->field["MONTH"]);
        //累積期間月を配列にする。
        $range_month = array("04" => "'04'",
                             "05" => "'04','05'",
                             "06" => "'04','05','06'",
                             "07" => "'04','05','06','07'",
                             "08" => "'04','05','06','07','08'",
                             "09" => "'04','05','06','07','08','09'",
                             "10" => "'04','05','06','07','08','09','10'",
                             "11" => "'04','05','06','07','08','09','10','11'",
                             "12" => "'04','05','06','07','08','09','10','11','12'",
                             "01" => "'04','05','06','07','08','09','10','11','12','01'",
                             "02" => "'04','05','06','07','08','09','10','11','12','01','02'",
                             "03" => "'04','05','06','07','08','09','10','11','12','01','02','03'"
                             );

        $query  = " WITH SUM_ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(LESSON) AS SUM_LESSON, ";
        $query .= "         SUM(OFFDAYS) AS SUM_OFFDAYS, ";
        $query .= "         SUM(ABROAD) AS SUM_ABROAD, ";
        $query .= "         SUM(ABSENT) AS SUM_ABSENT, ";
        $query .= "         SUM(SUSPEND) AS SUM_SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     SUM(KOUDOME) AS SUM_KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     SUM(VIRUS) AS SUM_VIRUS, ";
        }
        $query .= "         SUM(MOURNING) AS SUM_MOURNING, ";
        $query .= "         SUM(VALUE(SICK, 0)) AS SUM_SICK, ";
        $query .= "         SUM(NOTICE) AS SUM_NOTICE, ";
        $query .= "         SUM(NONOTICE) AS SUM_NONOTICE, ";
        $query .= "         SUM(LATE) AS SUM_LATE, ";
        $query .= "         SUM(EARLY) AS SUM_EARLY, ";
        $query .= "         SUM(REIHAI_KEKKA) AS SUM_DETAIL_001, ";
        $query .= "         SUM(M_KEKKA_JISU) AS SUM_DETAIL_002, ";
        $query .= "         SUM(REIHAI_TIKOKU) AS SUM_DETAIL_003, ";
        $query .= "         SUM(JYUGYOU_TIKOKU) AS SUM_DETAIL_004, ";
        $query .= "         SUM(JYUGYOU_JISU_DECIMAL) AS SUM_DETAIL_101, ";
        $query .= "         SUM(TOCHU_KEKKA) AS SUM_DETAIL_102 ";
        $query .= "     FROM ";
        $query .= "         V_ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        if ($monthsem[0] != "" && $monthsem[0] != NULL) {
            $query .= "     AND MONTH IN( ".$range_month[$monthsem[0]].")";
            $query .= "     AND SEMESTER <= '".$monthsem[1]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";
        
        $query  .= " SELECT ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     G2.GHR_NAME AS HR_NAME, ";
            $query .= "     G1.GHR_ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "     S2.HR_NAME, ";
            $query .= "     T1.ATTENDNO, ";
        }
        $query .= "     S1.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     S3.MONTH, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S4.APPOINTED_DAY, ";
        $query .= "     S3.LESSON, ";
        $query .= "     S3.OFFDAYS, ";
        $query .= "     S3.ABROAD, ";
        $query .= "     S3.ABSENT, ";
        $query .= "     S3.SUSPEND, ";
        $query .= "     S3.KOUDOME, ";
        $query .= "     S3.VIRUS, ";
        $query .= "     S3.MOURNING, ";
        $query .= "     VALUE(S3.LESSON, 0) - VALUE(S3.SUSPEND, 0) - VALUE(S3.MOURNING, 0) - VALUE(S3.ABROAD, 0) - VALUE(S3.OFFDAYS, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(S3.KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(S3.VIRUS, 0) ";
        }
        $query .= "     + (CASE WHEN S6.SEM_OFFDAYS = '1' THEN VALUE(S3.OFFDAYS, 0) ELSE 0 END) ";
        $query .= "     AS ATTEND, ";
        $query .= "     S3.SICK, ";
        $query .= "     S3.NOTICE, ";
        $query .= "     S3.NONOTICE, ";
        $query .= "     VALUE(S3.SICK, 0) + VALUE(S3.NOTICE, 0) + VALUE(S3.NONOTICE, 0) ";
        $query .= "     + (CASE WHEN S6.SEM_OFFDAYS = '1' THEN VALUE(S3.OFFDAYS, 0) ELSE 0 END) ";
        $query .= "     AS KESSEKI, ";
        $query .= "     VALUE(S3.LESSON, 0) - VALUE(S3.SUSPEND, 0) - VALUE(S3.MOURNING, 0) - VALUE(S3.ABROAD, 0) - VALUE(S3.OFFDAYS, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(S3.KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(S3.VIRUS, 0) ";
        }
        $query .= "     - VALUE(S3.SICK, 0) - VALUE(S3.NOTICE, 0) - VALUE(S3.NONOTICE, 0) ";
        $query .= "     AS PRESENT, ";
        $query .= "     S3.LATE, ";
        $query .= "     S3.EARLY, ";
        $query .= "     L1.CNT AS DETAIL_001, ";
        $query .= "     L2.CNT AS DETAIL_002, ";
        $query .= "     L3.CNT AS DETAIL_003, ";
        $query .= "     L4.CNT AS DETAIL_004, ";
        $query .= "     L101.CNT_DECIMAL AS DETAIL_101, ";
        $query .= "     L102.CNT AS DETAIL_102, ";
        $query .= "     RMK.REMARK1 AS REMARK, ";
        
        
        $query .= "     SUMAD.SUM_LESSON, ";
        $query .= "     SUMAD.SUM_OFFDAYS, ";
        $query .= "     SUMAD.SUM_ABROAD, ";
        $query .= "     SUMAD.SUM_ABSENT, ";
        $query .= "     SUMAD.SUM_SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     SUMAD.SUM_KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     SUMAD.SUM_VIRUS, ";
        }
        $query .= "     SUMAD.SUM_MOURNING, ";
        $query .= "     VALUE(SUMAD.SUM_LESSON, 0) - VALUE(SUMAD.SUM_SUSPEND, 0) - VALUE(SUMAD.SUM_MOURNING, 0) - VALUE(SUMAD.SUM_OFFDAYS, 0) - VALUE(SUMAD.SUM_ABROAD, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(SUMAD.SUM_KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(SUMAD.SUM_VIRUS, 0) ";
        }
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(SUMAD.SUM_OFFDAYS, 0) ";
        }
        $query .= "     AS SUM_ATTEND, ";
        $query .= "     SUMAD.SUM_SICK, ";
        $query .= "     SUMAD.SUM_NOTICE, ";
        $query .= "     SUMAD.SUM_NONOTICE, ";
        $query .= "     (VALUE(SUMAD.SUM_SICK, 0) + VALUE(SUMAD.SUM_NOTICE, 0) + VALUE(SUMAD.SUM_NONOTICE, 0) ";
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(SUMAD.SUM_OFFDAYS, 0) ";
        }
        $query .= "     ) AS SUM_KESSEKI, ";
        $query .= "     ((VALUE(SUMAD.SUM_LESSON, 0) - (VALUE(SUMAD.SUM_SUSPEND, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     + VALUE(SUMAD.SUM_KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     + VALUE(SUMAD.SUM_VIRUS, 0) ";
        }
        $query .= "      + VALUE(SUMAD.SUM_MOURNING, 0) + VALUE(SUMAD.SUM_OFFDAYS, 0) + VALUE(SUMAD.SUM_ABROAD, 0))) - (VALUE(SUMAD.SUM_SICK, 0) + VALUE(SUMAD.SUM_NOTICE, 0) + VALUE(SUMAD.SUM_NONOTICE, 0))) AS SUM_PRESENT, ";
        $query .= "     SUMAD.SUM_LATE, ";
        $query .= "     SUMAD.SUM_EARLY, ";
        $query .= "     SUMAD.SUM_DETAIL_001, ";
        $query .= "     SUMAD.SUM_DETAIL_002, ";
        $query .= "     SUMAD.SUM_DETAIL_003, ";
        $query .= "     SUMAD.SUM_DETAIL_004, ";
        $query .= "     SUMAD.SUM_DETAIL_101, ";
        $query .= "     SUMAD.SUM_DETAIL_102, ";
        
        
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST S1 ";
        $query .= "          ON T1.SCHREGNO     = S1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT S2 ";
        $query .= "          ON T1.YEAR         = S2.YEAR ";
        $query .= "         AND T1.SEMESTER     = S2.SEMESTER ";
        $query .= "         AND T1.GRADE        = S2.GRADE ";
        $query .= "         AND T1.HR_CLASS     = S2.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT S5 ";
        $query .= "          ON T1.YEAR         = S5.YEAR ";
        $query .= "         AND T1.GRADE        = S5.GRADE ";
        $query .= "     INNER JOIN SCHOOL_MST S6 ";
        $query .= "          ON T1.YEAR         = S6.YEAR ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND S6.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "         AND S5.SCHOOL_KIND  = S6.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN V_ATTEND_SEMES_DAT S3 ";
        $query .= "          ON T1.YEAR         = S3.YEAR ";
        $query .= "         AND T1.SCHREGNO     = S3.SCHREGNO ";
        $query .= "         AND S3.MONTH || '-' || S3.SEMESTER  = '".$model->field["MONTH"]."' ";
        $query .= "     LEFT JOIN APPOINTED_DAY_MST S4 ";
        $query .= "          ON T1.YEAR = S4.YEAR ";
        $query .= "         AND S5.SCHOOL_KIND = S4.SCHOOL_KIND ";
        $query .= "         AND S3.MONTH || '-' || S3.SEMESTER = S4.MONTH || '-' || S4.SEMESTER ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT G1 ";
            $query .= "          ON T1.YEAR         = G1.YEAR ";
            $query .= "         AND T1.SEMESTER     = G1.SEMESTER ";
            $query .= "         AND T1.SCHREGNO     = G1.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_GHR_HDAT G2 ";
            $query .= "          ON T1.YEAR         = G2.YEAR ";
            $query .= "         AND T1.SEMESTER     = G2.SEMESTER ";
            $query .= "         AND G1.GHR_CD       = G2.GHR_CD ";
        }
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L1 ";
        $query .= "          ON S3.COPYCD       = L1.COPYCD ";
        $query .= "         AND S3.YEAR         = L1.YEAR ";
        $query .= "         AND S3.MONTH        = L1.MONTH ";
        $query .= "         AND S3.SEMESTER     = L1.SEMESTER ";
        $query .= "         AND S3.SCHREGNO     = L1.SCHREGNO ";
        $query .= "         AND L1.SEQ          = '001' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L2 ";
        $query .= "          ON S3.COPYCD       = L2.COPYCD ";
        $query .= "         AND S3.YEAR         = L2.YEAR ";
        $query .= "         AND S3.MONTH        = L2.MONTH ";
        $query .= "         AND S3.SEMESTER     = L2.SEMESTER ";
        $query .= "         AND S3.SCHREGNO     = L2.SCHREGNO ";
        $query .= "         AND L2.SEQ          = '002' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L3 ";
        $query .= "          ON S3.COPYCD       = L3.COPYCD ";
        $query .= "         AND S3.YEAR         = L3.YEAR ";
        $query .= "         AND S3.MONTH        = L3.MONTH ";
        $query .= "         AND S3.SEMESTER     = L3.SEMESTER ";
        $query .= "         AND S3.SCHREGNO     = L3.SCHREGNO ";
        $query .= "         AND L3.SEQ          = '003' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L4 ";
        $query .= "          ON S3.COPYCD       = L4.COPYCD ";
        $query .= "         AND S3.YEAR         = L4.YEAR ";
        $query .= "         AND S3.MONTH        = L4.MONTH ";
        $query .= "         AND S3.SEMESTER     = L4.SEMESTER ";
        $query .= "         AND S3.SCHREGNO     = L4.SCHREGNO ";
        $query .= "         AND L4.SEQ          = '004' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L101 ";
        $query .= "          ON S3.COPYCD       = L101.COPYCD ";
        $query .= "         AND S3.YEAR         = L101.YEAR ";
        $query .= "         AND S3.MONTH        = L101.MONTH ";
        $query .= "         AND S3.SEMESTER     = L101.SEMESTER ";
        $query .= "         AND S3.SCHREGNO     = L101.SCHREGNO ";
        $query .= "         AND L101.SEQ        = '101' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L102 ";
        $query .= "          ON S3.COPYCD       = L102.COPYCD ";
        $query .= "         AND S3.YEAR         = L102.YEAR ";
        $query .= "         AND S3.MONTH        = L102.MONTH ";
        $query .= "         AND S3.SEMESTER     = L102.SEMESTER ";
        $query .= "         AND S3.SCHREGNO     = L102.SCHREGNO ";
        $query .= "         AND L102.SEQ        = '102' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_REMARK_DAT RMK ";
        $query .= "          ON S3.YEAR         = RMK.YEAR ";
        $query .= "         AND S3.SEMESTER     = RMK.SEMESTER ";
        $query .= "         AND S3.MONTH        = RMK.MONTH ";
        $query .= "         AND S3.SCHREGNO     = RMK.SCHREGNO ";
        $query .= "     LEFT JOIN SUM_ATTEND_SEMES SUMAD ";
        $query .= "          ON SUMAD.SCHREGNO  = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     G1.GHR_CD = '".$model->field["GROUP_HR_CLASS"]."' ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T1.COURSECD || T1.MAJORCD  = '".$model->field["COURSE_MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     G1.GHR_CD, ";
            $query .= "     G1.GHR_ATTENDNO, ";
        } else {
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
        }
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
?>
