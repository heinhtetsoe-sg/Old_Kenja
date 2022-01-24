<?php

require_once('for_php7.php');

class knjf170aQuery extends Query {
    //年度取得
   function getYear($model, $year="") {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     SCHOOLNAME2 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".$model->schoolkind."' ";
                if ($year) {
                    $query .= " AND YEAR        = '".$year."' ";
                }
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            if ($year) {
                $query .= " AND YEAR        = '".$year."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //キャンパス区分取得
   function getCampusDiv($model, $year) {
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        if ($model->Properties["useNurseoffRestrict"] == "1" && (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT)) {
            $query .= "     INNER JOIN STAFF_DETAIL_MST S1 ";
            $query .= "          ON T1.YEAR         = S1.YEAR ";
            $query .= "         AND S1.STAFFCD      = '".STAFFCD."' ";
            $query .= "         AND S1.STAFF_SEQ    = '008' ";
            $query .= "         AND T1.NAMECD2      = S1.FIELD1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$year."' AND ";
        $query .= "     T1.NAMECD1  = 'Z043' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.NAMESPARE2 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.NAMESPARE2 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種初期値
   function getCampusDiv2($model, $year) {
        $query .= " SELECT ";
        $query .= "     T1.NAMESPARE2, ";
        $query .= "     T1.NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        if ($model->Properties["useNurseoffRestrict"] == "1" && (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT)) {
            $query .= "     INNER JOIN STAFF_DETAIL_MST S1 ";
            $query .= "          ON T1.YEAR         = S1.YEAR ";
            $query .= "         AND S1.STAFFCD      = '".STAFFCD."' ";
            $query .= "         AND S1.STAFF_SEQ    = '008' ";
            $query .= "         AND T1.NAMECD2      = S1.FIELD1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$year."' AND ";
        $query .= "     T1.NAMECD1  = 'Z043' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.NAMESPARE2 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.NAMESPARE2 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";

        return $query;
    }

    //校種取得用（左画面）
   function getSchkind($year, $campus_div) {
        $query .= " SELECT ";
        $query .= "     T1.NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR    = '".$year."' ";
        $query .= "     AND T1.NAMECD1 = 'Z043' ";
        $query .= "     AND T1.NAMECD2 = '".$campus_div."' ";

        return $query;
    }

    //リスト取得
    function getList($model) {
        $sdate = $model->year.'-04-01';
        $edate = ($model->year+1).'-03-31';

        $query  = " WITH COURSE_MAJOR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.NAMESPARE1 AS SCHOOL_KIND, ";
        $query .= "         T1.NAMESPARE2 AS COURSECD, ";
        $query .= "         T1.NAMESPARE3 AS MAJORCD ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST T1 ";
        $query .= "         INNER JOIN V_NAME_MST S1 ";
        $query .= "              ON T1.YEAR     = S1.YEAR ";
        $query .= "             AND S1.NAMECD1  = 'Z043' ";
        $query .= "             AND S1.NAMECD2  = '".$model->campus_div."' ";
        $query .= "             AND T1.NAMECD1  = S1.NAMESPARE1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.NAMESPARE1 = '".$model->schoolkind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.NAMESPARE1   = '".SCHOOLKIND."' ";
        }
        $query .= " ), SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_REGD_GDAT T2, ";
        $query .= "         COURSE_MAJOR T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.YEAR         = '".$model->year."' AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T2.SCHOOL_KIND   = T3.SCHOOL_KIND AND ";
        $query .= "         T1.COURSECD     = T3.COURSECD AND ";
        $query .= "         T1.MAJORCD      = T3.MAJORCD ";
        $query .= " ), DATE_LIST AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.DATE ";
        $query .= "     FROM ";
        $query .= "         NURSEOFF_DIARY_CAMPUS_DAT T1, ";
        $query .= "         COURSE_MAJOR T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.DATE BETWEEN '".$sdate."' AND '".$edate."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
                $query .= "         T1.SCHOOL_KIND = '".$model->schoolkind."' AND ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "         T1.SCHOOL_KIND  = '".SCHOOLKIND."' AND ";
        }
        $query .= "         T1.CAMPUS_DIV   = '".$model->campus_div."' ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         T1.VISIT_DATE AS DATE ";
        $query .= "     FROM ";
        $query .= "         NURSEOFF_VISITREC_DAT T1, ";
        $query .= "         SCH_INFO T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.VISIT_DATE BETWEEN '".$sdate."' AND '".$edate."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     ORDER BY ";
        $query .= "         DATE ";
        $query .= " ), SCH_CNT AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.VISIT_DATE AS DATE, ";
        $query .= "         T1.TYPE, ";
        $query .= "         COUNT(T1.SCHREGNO) AS CNT ";
        $query .= "     FROM ";
        $query .= "         NURSEOFF_VISITREC_DAT T1, ";
        $query .= "         SCH_INFO T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.VISIT_DATE BETWEEN '".$sdate."' AND '".$edate."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         T1.VISIT_DATE, ";
        $query .= "         T1.TYPE ";
        $query .= " ) ";

        if ($model->sort["SRT_D"] == 1) {//初期値は降順
            $OrderBy1 = " DESC ";
        } elseif ($model->sort["SRT_D"] == -1) {
            $OrderBy1 = " ASC ";
        }
        $query .= " SELECT ";
        $query .= "     T1.DATE, ";
        $query .= "     S1.CNT AS TYPE1, ";
        $query .= "     S2.CNT AS TYPE2, ";
        $query .= "     S3.CNT AS TYPE3, ";
        $query .= "     S4.CNT AS TYPE4, ";
        $query .= "     S5.CNT AS TYPE5, ";
        $query .= "     T2.DATE AS DIARY, ";
        $query .= "     '".$model->schoolkind."' AS SCHKIND ";
        $query .= " FROM ";
        $query .= "     DATE_LIST T1 ";
        $query .= "     LEFT JOIN SCH_CNT S1 ON T1.DATE = S1.DATE AND S1.TYPE = '1' ";
        $query .= "     LEFT JOIN SCH_CNT S2 ON T1.DATE = S2.DATE AND S2.TYPE = '2' ";
        $query .= "     LEFT JOIN SCH_CNT S3 ON T1.DATE = S3.DATE AND S3.TYPE = '3' ";
        $query .= "     LEFT JOIN SCH_CNT S4 ON T1.DATE = S4.DATE AND S4.TYPE = '4' ";
        $query .= "     LEFT JOIN SCH_CNT S5 ON T1.DATE = S5.DATE AND S5.TYPE = '5' ";
        $query .= "     LEFT JOIN NURSEOFF_DIARY_CAMPUS_DAT T2 ";
        $query .= "              ON T1.DATE         = T2.DATE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "             AND T2.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "             AND T2.SCHOOL_KIND = '".$model->schoolkind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND T2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "             AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "             AND T2.CAMPUS_DIV   = '".$model->campus_div."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.DATE {$OrderBy1} ";

        return $query;

    }

    //１レコード取得
    function getRow($model, $date) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_DIARY_CAMPUS_DAT ";
        $query .= " WHERE ";
        $query .= "     DATE        = '".$date."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
                $query .= "     SCHOOL_KIND = '".$model->sendSchoolKind."' AND ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CAMPUS_DIV  = '".$model->campus_div."' ";

        return $query;
    }

    //出欠データ取得
    function getNurseoffAttendCampusDat($model, $date) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_ATTEND_CAMPUS_DAT ";
        $query .= " WHERE ";
        $query .= "     DATE        = '".$date."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
                $query .= "     SCHOOL_KIND = '".$model->sendSchoolKind."' AND ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CAMPUS_DIV  = '".$model->campus_div."' ";

        return $query;
    }

    //名称取得
    function getNameMst($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'A006' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model, $year, $campus_div, $schoolkind) {
        $query  = " WITH COURSE_MAJOR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.NAMESPARE1 AS SCHOOL_KIND, ";
        $query .= "         T1.NAMESPARE2 AS COURSECD, ";
        $query .= "         T1.NAMESPARE3 AS MAJORCD ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST T1 ";
        $query .= "         INNER JOIN V_NAME_MST S1 ";
        $query .= "              ON T1.YEAR     = S1.YEAR ";
        $query .= "             AND S1.NAMECD1  = 'Z043' ";
        $query .= "             AND S1.NAMECD2  = '".$campus_div."' ";
        $query .= "             AND T1.NAMECD1  = S1.NAMESPARE1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".$year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.NAMESPARE1 = '".$schoolkind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.NAMESPARE1   = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     COURSE_MAJOR T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = '".$year."' AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     T2.SCHOOL_KIND  = T3.SCHOOL_KIND AND ";
        }
        $query .= "     T1.COURSECD     = T3.COURSECD AND ";
        $query .= "     T1.MAJORCD      = T3.MAJORCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        if ($model->cmd == "update") {
            knjf170aQuery::getDeleteQuery($model);
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $date = str_replace("/", "-", $model->field["DATE"]);

        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $model->sendSchoolKind;
        }
        $data["CAMPUS_DIV"][TEXT]   = $model->campus_div;
        $data["DATE"][TEXT]         = $date;
        $data["WEATHER"][TEXT]      = $model->field["WEATHER"];
        $data["WEATHER_TEXT"][TEXT] = $model->field["WEATHER_TEXT"];
        $data["TEMPERATURE"][TEXT]  = $model->field["TEMPERATURE"];
        $data["EVENT"][TEXT]        = $model->field["EVENT"];
        $data["DIARY"][TEXT]        = $model->field["DIARY"];
        $data["YEAR"][TEXT]         = $model->year;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "NURSEOFF_DIARY_CAMPUS_DAT");
        $db->query($query);

        if ($model->Properties["useNurseoffAttend"] == "1") {
            //欠席状況更新
            foreach ($model->di_cd as $dkey => $dval) {
                foreach ($model->grade as $gkey => $gval) {
                    $data = array();
                    if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]  = $model->sendSchoolKind;
                    }
                    $data["CAMPUS_DIV"][TEXT]   = $model->campus_div;
                    $data["DATE"][TEXT]         = $date;
                    $data["GRADE"][TEXT]        = $gkey;
                    $data["DI_CD"][TEXT]        = $dkey;
                    $data["CNT"][NUMBER]        = $model->att_field[$dkey][$gkey];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "NURSEOFF_ATTEND_CAMPUS_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     NURSEOFF_DIARY_CAMPUS_DAT ";
        $query .= " WHERE ";
        $query .= "     DATE = '".str_replace("/", "-", $model->field["DATE"])."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->sendSchoolKind."' AND ";
        }
        $query .= "     CAMPUS_DIV  = '".$model->campus_div."' ";
        $db->query($query);

        if ($model->Properties["useNurseoffAttend"] == "1") {
            //欠席状況
            $query  = " DELETE FROM ";
            $query .= "     NURSEOFF_ATTEND_CAMPUS_DAT ";
            $query .= " WHERE ";
            $query .= "     DATE = '".str_replace("/", "-", $model->field["DATE"])."' AND ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
                $query .= "     SCHOOL_KIND = '".$model->sendSchoolKind."' AND ";
            }
            $query .= "     CAMPUS_DIV  = '".$model->campus_div."' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    , SCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= " ORDER BY ";
            $query .= "     SCHOOLCD ";
        }
        return $query;
    }
}
?>
