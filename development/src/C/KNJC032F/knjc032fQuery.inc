<?php

require_once('for_php7.php');
class knjc032fQuery extends Query {
    //MIN権限グループ取得
    function getMinGroupcd($model, $schoolKind) {
        $query  = " SELECT ";
        $query .= "     SUBSTR(MIN((CASE GROUPCD WHEN '9999' THEN '0' ELSE '1' END) || GROUPCD),2) ";
        $query .= " FROM ";
        $query .= "     USERGROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     STAFFCD     = '".STAFFCD."' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 ADMIN_CONTROL_ATTEND_DAT E1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR         = E1.YEAR AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "                 E1.SCHOOL_KIND = '".$schoolKind."' AND ";
        }
        $query .= "                 E1.PROGRAMID    = '".PROGRAMID."' AND ";
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

    //校種取得
    function getSchoolKind1($model, $grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$grade."'  ";

        return $query;
    }

    //校種取得
    function getSchoolKind2($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."'  ";

        return $query;
    }

    //出欠項目名一覧取得
    function getAdminControlAttendItemnameDat($model, $cm, $school_kind) {
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
    function getAdminControlAttendDat($model, $cm, $school_kind) {
        $query  = " SELECT ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     SHOWORDER, ";
        $query .= "     INPUT_FLG ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        $query .= "     CONTROL_DIV = '1' AND ";
        $query .= "     ATTEND_DIV  = '1' AND ";
        $query .= "     GRADE       = '00'  AND ";
        $query .= "     COURSECD || MAJORCD = '".$cm."' AND ";
        $query .= "     PROGRAMID   = '".PROGRAMID."' AND ";
        $query .= "     GROUPCD     = '".$model->groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(SHOWORDER) ";

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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
                $query .= "          ON T1.YEAR         = S2.YEAR ";
                $query .= "         AND T1.GRADE        = S2.GRADE ";
                $query .= "         AND S2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学級取得
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T2.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD3  = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     S1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT S1 ";
                $query .= "                 WHERE ";
                $query .= "                     S1.YEAR         = T1.YEAR AND ";
                $query .= "                     S1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     S1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR AND ";
            $query .= "                     S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //複式学級取得
    function getGroupHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.GHR_CD AS VALUE, ";
        $query .= "     T3.GHR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GHR_DAT T2, ";
        $query .= "     SCHREG_REGD_GHR_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GHR_CD   = T3.GHR_CD ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T3.TR_CD1      = '".STAFFCD."' OR ";
            $query .= "          T3.TR_CD2      = '".STAFFCD."' OR ";
            $query .= "          T3.TR_CD3      = '".STAFFCD."' OR ";
            $query .= "          T3.SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "          T3.SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "          T3.SUBTR_CD3   = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     S1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT S1 ";
                $query .= "                 WHERE ";
                $query .= "                     S1.YEAR         = T1.YEAR AND ";
                $query .= "                     S1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     S1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR AND ";
            $query .= "                     S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            //複式学級
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     T1.GHR_ATTENDNO || ' ' || T2.NAME_SHOW AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.GHR_CD   = '".$model->field["GROUP_HR_CLASS"]."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND EXISTS (SELECT ";
                    $query .= "                 'X' ";
                    $query .= "             FROM ";
                    $query .= "                 SCHREG_REGD_DAT S1, ";
                    $query .= "                 SCHREG_REGD_GDAT S2 ";
                    $query .= "             WHERE ";
                    $query .= "                 S1.YEAR         = S2.YEAR AND ";
                    $query .= "                 S1.YEAR         = T1.YEAR AND ";
                    $query .= "                 S1.SEMESTER     = T1.SEMESTER AND ";
                    $query .= "                 S1.SCHREGNO     = T1.SCHREGNO AND ";
                    $query .= "                 S1.GRADE        = S2.GRADE AND ";
                    $query .= "                 S2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    $query .= "             ) ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND EXISTS (SELECT ";
                $query .= "                 'X' ";
                $query .= "             FROM ";
                $query .= "                 SCHREG_REGD_DAT S1, ";
                $query .= "                 SCHREG_REGD_GDAT S2 ";
                $query .= "             WHERE ";
                $query .= "                 S1.YEAR         = S2.YEAR AND ";
                $query .= "                 S1.YEAR         = T1.YEAR AND ";
                $query .= "                 S1.SEMESTER     = T1.SEMESTER AND ";
                $query .= "                 S1.SCHREGNO     = T1.SCHREGNO AND ";
                $query .= "                 S1.GRADE        = S2.GRADE AND ";
                $query .= "                 S2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     GHR_ATTENDNO ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     T1.ATTENDNO || ' ' || T2.NAME_SHOW AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.GRADE    = '".$model->field["grade"]."' AND ";
            $query .= "     T1.HR_CLASS = '".$model->field["class"]."' ";
            $query .= " ORDER BY ";
            $query .= "     ATTENDNO ";
        }

        return $query;
    }

    //学校マスタ取得
    function getSchoolMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
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

    //出欠月別累積データ・管理者コントロールデータ
    function selectAttendQuery($model, $schoolMst) {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }

        $query  = " WITH SEM_MONTH AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T2.MONTH, ";
        $query .= "         T2.MONTHNAME, ";
        $query .= "         VALUE(T2.NAMESPARE1, '') AS NAMESPARE1, ";
        $query .= "         T2.MONTHCD ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             SEMESTER, ";
        $query .= "             SEMESTERNAME, ";
        $query .= "             CASE WHEN MONTH(SDATE) <= 3 THEN MONTH(SDATE)+12 ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "             CASE WHEN MONTH(EDATE) <= 3 THEN MONTH(EDATE)+12 ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= "         FROM ";
        $query .= "             SEMESTER_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' AND ";
        $query .= "             SEMESTER <> '9' ";
        $query .= "         ) T1, ";
        $query .= "         (SELECT ";
        $query .= "             NAMECD2 AS MONTH, ";
        $query .= "             NAME1 AS MONTHNAME, ";
        $query .= "             CASE WHEN INT(NAMECD2) <= 3 THEN INT(NAMECD2)+12 ELSE INT(NAMECD2) END AS MONTHCD, ";
        $query .= "             NAMESPARE1 ";
        $query .= "         FROM ";
        $query .= "             NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             NAMECD1 = '".$setNameCd."' ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T2.MONTHCD BETWEEN T1.S_MONTH AND T1.E_MONTH ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         'ZZ' AS MONTH, ";
        $query .= "         CASE WHEN SEMESTER = '9' THEN '累計' ELSE SEMESTERNAME || '計' END AS MONTHNAME, ";
        $query .= "         '' AS NAMESPARE1, ";
        $query .= "         CAST(NULL AS SMALLINT) AS MONTHCD ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= " ), ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.MONTH, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.LESSON, ";
        $query .= "         T1.OFFDAYS, ";
        $query .= "         T1.ABROAD, ";
        $query .= "         T1.ABSENT, ";
        $query .= "         T1.SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         T1.VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         T1.KOUDOME, ";
        }
        $query .= "         T1.MOURNING, ";
        $query .= "         VALUE(T1.LESSON, 0) - VALUE(T1.SUSPEND, 0) - VALUE(T1.MOURNING, 0) - VALUE(T1.ABROAD, 0) - VALUE(T1.OFFDAYS, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         - VALUE(T1.KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         - VALUE(T1.VIRUS, 0) ";
        }
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "         + VALUE(T1.OFFDAYS, 0) ";
        }
        $query .= "         AS ATTEND, ";
        $query .= "         T1.SICK, ";
        $query .= "         T1.NOTICE, ";
        $query .= "         T1.NONOTICE, ";
        $query .= "         VALUE(T1.LESSON, 0) - VALUE(T1.SUSPEND, 0) - VALUE(T1.MOURNING, 0) - VALUE(T1.ABROAD, 0) - VALUE(T1.OFFDAYS, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         - VALUE(T1.KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         - VALUE(T1.VIRUS, 0) ";
        }
        $query .= "         - VALUE(T1.SICK, 0) - VALUE(T1.NOTICE, 0) - VALUE(T1.NONOTICE, 0) ";
        $query .= "         AS PRESENT, ";
        $query .= "         T1.LATE, ";
        $query .= "         T1.EARLY, ";
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $query .= "         L1.CNT AS DETAIL_001, ";
            $query .= "         L2.CNT AS DETAIL_002, ";
            $query .= "         L3.CNT AS DETAIL_003, ";
            $query .= "         L4.CNT AS DETAIL_004, ";
            $query .= "         L101.CNT AS DETAIL_101, ";
            $query .= "         L102.CNT AS DETAIL_102 ";
        } else {
            $query .= "         T1.REIHAI_KEKKA AS DETAIL_001, ";
            $query .= "         T1.M_KEKKA_JISU AS DETAIL_002, ";
            $query .= "         T1.REIHAI_TIKOKU AS DETAIL_003, ";
            $query .= "         T1.JYUGYOU_TIKOKU AS DETAIL_004, ";
            $query .= "         T1.JYUGYOU_JISU_DECIMAL AS DETAIL_101, ";
            $query .= "         T1.TOCHU_KEKKA AS DETAIL_102 ";
        }
        $query .= "     FROM ";
        $query .= "         V_ATTEND_SEMES_DAT T1 ";
        $query .= "         LEFT JOIN ATTEND_SEMES_DETAIL_DAT L1 ";
        $query .= "              ON T1.COPYCD   = L1.COPYCD ";
        $query .= "             AND T1.YEAR     = L1.YEAR ";
        $query .= "             AND T1.MONTH    = L1.MONTH ";
        $query .= "             AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "             AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "             AND L1.SEQ      = '001' ";
        $query .= "         LEFT JOIN ATTEND_SEMES_DETAIL_DAT L2 ";
        $query .= "              ON T1.COPYCD   = L2.COPYCD ";
        $query .= "             AND T1.YEAR     = L2.YEAR ";
        $query .= "             AND T1.MONTH    = L2.MONTH ";
        $query .= "             AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "             AND T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "             AND L2.SEQ      = '002' ";
        $query .= "         LEFT JOIN ATTEND_SEMES_DETAIL_DAT L3 ";
        $query .= "              ON T1.COPYCD   = L3.COPYCD ";
        $query .= "             AND T1.YEAR     = L3.YEAR ";
        $query .= "             AND T1.MONTH    = L3.MONTH ";
        $query .= "             AND T1.SEMESTER = L3.SEMESTER ";
        $query .= "             AND T1.SCHREGNO = L3.SCHREGNO ";
        $query .= "             AND L3.SEQ      = '003' ";
        $query .= "         LEFT JOIN ATTEND_SEMES_DETAIL_DAT L4 ";
        $query .= "              ON T1.COPYCD   = L4.COPYCD ";
        $query .= "             AND T1.YEAR     = L4.YEAR ";
        $query .= "             AND T1.MONTH    = L4.MONTH ";
        $query .= "             AND T1.SEMESTER = L4.SEMESTER ";
        $query .= "             AND T1.SCHREGNO = L4.SCHREGNO ";
        $query .= "             AND L4.SEQ      = '004' ";
        $query .= "         LEFT JOIN ATTEND_SEMES_DETAIL_DAT L101 ";
        $query .= "              ON T1.COPYCD   = L101.COPYCD ";
        $query .= "             AND T1.YEAR     = L101.YEAR ";
        $query .= "             AND T1.MONTH    = L101.MONTH ";
        $query .= "             AND T1.SEMESTER = L101.SEMESTER ";
        $query .= "             AND T1.SCHREGNO = L101.SCHREGNO ";
        $query .= "             AND L101.SEQ    = '101' ";
        $query .= "         LEFT JOIN ATTEND_SEMES_DETAIL_DAT L102 ";
        $query .= "              ON T1.COPYCD   = L102.COPYCD ";
        $query .= "             AND T1.YEAR     = L102.YEAR ";
        $query .= "             AND T1.MONTH    = L102.MONTH ";
        $query .= "             AND T1.SEMESTER = L102.SEMESTER ";
        $query .= "             AND T1.SCHREGNO = L102.SCHREGNO ";
        $query .= "             AND L102.SEQ    = '102' ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "             T1.SCHREGNO = '".$model->field["schregno"]."' ";
        $query .= " ), SUM_ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         'ZZ' AS MONTH, ";
        $query .= "         '' AS SCHREGNO, ";
        $query .= "         SUM(LESSON) AS LESSON, ";
        $query .= "         SUM(OFFDAYS) AS OFFDAYS, ";
        $query .= "         SUM(ABROAD) AS ABROAD, ";
        $query .= "         SUM(ABSENT) AS ABSENT, ";
        $query .= "         SUM(SUSPEND) AS SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VIRUS) AS VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(KOUDOME) AS KOUDOME, ";
        }
        $query .= "         SUM(MOURNING) AS MOURNING, ";
        $query .= "         SUM(ATTEND) AS ATTEND, ";
        $query .= "         SUM(SICK) AS SICK, ";
        $query .= "         SUM(NOTICE) AS NOTICE, ";
        $query .= "         SUM(NONOTICE) AS NONOTICE, ";
        $query .= "         SUM(PRESENT) AS PRESENT, ";
        $query .= "         SUM(LATE) AS LATE, ";
        $query .= "         SUM(EARLY) AS EARLY, ";
        $query .= "         SUM(DETAIL_001) AS DETAIL_001, ";
        $query .= "         SUM(DETAIL_002) AS DETAIL_002, ";
        $query .= "         SUM(DETAIL_003) AS DETAIL_003, ";
        $query .= "         SUM(DETAIL_004) AS DETAIL_004, ";
        $query .= "         SUM(DETAIL_101) AS DETAIL_101, ";
        $query .= "         SUM(DETAIL_102) AS DETAIL_102 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES T1 ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER ";
        $query .= " ), SUM_ATTEND_SEMES2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         '9' AS SEMESTER, ";
        $query .= "         'ZZ' AS MONTH, ";
        $query .= "         '' AS SCHREGNO, ";
        $query .= "         SUM(LESSON) AS LESSON, ";
        $query .= "         SUM(OFFDAYS) AS OFFDAYS, ";
        $query .= "         SUM(ABROAD) AS ABROAD, ";
        $query .= "         SUM(ABSENT) AS ABSENT, ";
        $query .= "         SUM(SUSPEND) AS SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VIRUS) AS VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(KOUDOME) AS KOUDOME, ";
        }
        $query .= "         SUM(MOURNING) AS MOURNING, ";
        $query .= "         SUM(ATTEND) AS ATTEND, ";
        $query .= "         SUM(SICK) AS SICK, ";
        $query .= "         SUM(NOTICE) AS NOTICE, ";
        $query .= "         SUM(NONOTICE) AS NONOTICE, ";
        $query .= "         SUM(PRESENT) AS PRESENT, ";
        $query .= "         SUM(LATE) AS LATE, ";
        $query .= "         SUM(EARLY) AS EARLY, ";
        $query .= "         SUM(DETAIL_001) AS DETAIL_001, ";
        $query .= "         SUM(DETAIL_002) AS DETAIL_002, ";
        $query .= "         SUM(DETAIL_003) AS DETAIL_003, ";
        $query .= "         SUM(DETAIL_004) AS DETAIL_004, ";
        $query .= "         SUM(DETAIL_101) AS DETAIL_101, ";
        $query .= "         SUM(DETAIL_102) AS DETAIL_102 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES T1 ";
        $query .= " ), ATTEND AS ( ";
        $query .= "     SELECT * FROM ATTEND_SEMES ";
        $query .= "     UNION ";
        $query .= "     SELECT * FROM SUM_ATTEND_SEMES ";
        $query .= "     UNION ";
        $query .= "     SELECT * FROM SUM_ATTEND_SEMES2 ";
        $query .= " ), ADMIN_CONTROL AS ( ";
        $query .= "     SELECT ";
        $query .= "         CONTROL_CODE ";
        $query .= "     FROM ";
        $query .= "         ADMIN_CONTROL_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SCHOOL_KIND IN (SELECT DISTINCT ";
        $query .= "                             T2.SCHOOL_KIND ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_REGD_DAT T1, ";
        $query .= "                             SCHREG_REGD_GDAT T2 ";
        $query .= "                         WHERE ";
        $query .= "                             T1.YEAR     = T2.YEAR AND ";
        $query .= "                             T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                             T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "                             T1.GRADE    = T2.GRADE AND ";
        $query .= "                             T1.SCHREGNO = '".$model->field["schregno"]."') AND ";
        $query .= "         CONTROL_FLG = '2' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.MONTH, ";
        $query .= "     T1.MONTHNAME, ";
        $query .= "     T1.NAMESPARE1, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T4.APPOINTED_DAY, ";
        $query .= "     T2.LESSON, ";
        $query .= "     T2.OFFDAYS, ";
        $query .= "     T2.ABROAD, ";
        $query .= "     T2.ABSENT, ";
        $query .= "     T2.SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     T2.KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     T2.VIRUS, ";
        }
        $query .= "     T2.MOURNING, ";
        $query .= "     T2.ATTEND, ";
        $query .= "     T2.SICK, ";
        $query .= "     T2.NOTICE, ";
        $query .= "     T2.NONOTICE, ";
        $query .= "     T2.PRESENT, ";
        $query .= "     T2.LATE, ";
        $query .= "     T2.EARLY, ";
        $query .= "     T2.DETAIL_001, ";
        $query .= "     T2.DETAIL_002, ";
        $query .= "     T2.DETAIL_003, ";
        $query .= "     T2.DETAIL_004, ";
        $query .= "     T2.DETAIL_101, ";
        $query .= "     T2.DETAIL_102, ";
        $query .= "     T5.REMARK1 AS REMARK, ";
        $query .= "     T3.CONTROL_CODE, ";
        $query .= "     T2.SCHREGNO AS SEM_SCHREGNO ";
        $query .= " FROM  ";
        $query .= "     SEM_MONTH T1 ";
        $query .= "     LEFT JOIN ATTEND T2 ON T1.MONTH = T2.MONTH AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     LEFT JOIN ADMIN_CONTROL T3 ON T1.MONTH = T3.CONTROL_CODE ";
        $query .= "     LEFT JOIN APPOINTED_DAY_MST T4 ";
        $query .= "          ON T4.YEAR         = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T4.SCHOOL_KIND  = '".$model->school_kind."' ";
        }
        $query .= "         AND T1.MONTH        = T4.MONTH ";
        $query .= "         AND T1.SEMESTER     = T4.SEMESTER ";
        $query .= "     LEFT JOIN ATTEND_SEMES_REMARK_DAT T5 ";
        $query .= "          ON T5.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = T5.SEMESTER ";
        $query .= "         AND T1.MONTH    = T5.MONTH ";
        $query .= "         AND T5.SCHREGNO = '".$model->field["schregno"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.NAMESPARE1, ";
        $query .= "     T1.MONTHCD ";

        return $query;
    }

    //締め日の取得
    function getAppointedDay($month, $semester, $model) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        }
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$semester."' ";

        return $query;
    }

    //異動データ（退学・転学・卒業）取得
    function getIdouData($schregno, $date1, $date2) {
        $query  = " SELECT ";
        $query .= "     CASE WHEN GRD_DATE < '".$date1."' THEN 1 ELSE 0 END AS IDOU_COLOR, ";
        $query .= "     CASE WHEN GRD_DATE < '".$date2."' THEN 1 ELSE 0 END AS IDOU_TEXT ";
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
        $query .= "     SUM(CASE WHEN '".$date1."' BETWEEN TRANSFER_SDATE AND VALUE(TRANSFER_EDATE, '".(CTRL_YEAR+1)."' || '-03-31') THEN 1 ";
        $query .= "              WHEN TRANSFER_SDATE BETWEEN '".$date2."' AND '".$date1."' THEN 1 ";
        $query .= "              WHEN TRANSFER_EDATE BETWEEN '".$date2."' AND '".$date1."' THEN 1 ";
        $query .= "              ELSE 0 END) AS IDOU_COLOR, ";
        $query .= "     SUM(CASE WHEN TRANSFER_SDATE <= '".$date2."' AND '".$date1."' <= VALUE(TRANSFER_EDATE, '".(CTRL_YEAR+1)."' || '-03-31') THEN 1 ELSE 0 END) AS IDOU_TEXT ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') ";

        return $query;
    }

    //学期・月範囲取得
    function selectSemesAll() {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     MONTH(SDATE) AS S_MONTH, ";
        $query .= "     DAY(SDATE) AS S_DAY, ";
        $query .= "     MONTH(EDATE) AS E_MONTH, ";
        $query .= "     DAY(EDATE) AS E_DAY ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

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

    //データ取得 -- ATTEND_SEMES_DAT / ATTEND_SEMES_REMARK_DAT / ATTEND_SEMES_DETAIL_DAT
    function getAttendData($model, $table) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->field["schregno"]."' ";

        return $query;
    }

    //出欠月別累積データの更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //フィールド取得
        $query = knjc032fQuery::getFieldList();
        $table_field = array();
        $table_field = $db->getCol($query);

        $tableList = array("ATTEND_SEMES_DAT"           => "semes",
                           "ATTEND_SEMES_REMARK_DAT"    => "remark",
                           "ATTEND_SEMES_DETAIL_DAT"    => "detail"
                          );

        $semesData = $remarkData = $detailData = array();
        foreach ($tableList as $table => $var) {
            //データ取得
            $getData = array();
            $query = knjc032fQuery::getAttendData($model, $table);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $monthsem = $row["MONTH"]."-".$row["SEMESTER"];
                if ($table == "ATTEND_SEMES_DETAIL_DAT") {
                    $getData[$monthsem][$row["SEQ"]] = $row;
                } else {
                    $getData[$monthsem] = $row;
                }
            }
            $varname = $var."Data";
            $$varname = $getData;
        }

        foreach ($model->field["MONTH"] as $key => $month) {
            $monthAr = preg_split("/-/", $month);

            /**********************/
            /*  ATTEND_SEMES_DAT  */
            /**********************/
            //更新
            $data = $upItemArray = array();
            $data["APPOINTED_DAY"][TEXT] = $model->field["APPOINTED_DAY"][$key];
            foreach ($model->item_array as $order => $val) {
                //入力可の項目が更新対象
                if (in_array($val["item"], $table_field) && $val["input"]) {
                    if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                        $data[$val["item"]][NUMBER] = $model->field[$val["item"]][$key];
                    } else {
                        $data[$val["item"]][NUMBER] = ($model->field[$val["item"]][$key]) ? $model->field[$val["item"]][$key] : 0;
                    }
                    $upItemArray[] = $val["item"];
                }
            }
            if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                foreach ($table_field as $field) {
                    //入力可以外の項目（値なし）を更新
                    if (!(in_array($field, $upItemArray) || $semesData[$month][$field] > 0)) {
                        $data[$field][NUMBER] = 0;
                    }
                }
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";

            if (strlen($semesData[$month]["SCHREGNO"])) {
                //UPDATE
                $where  = " WHERE ";
                $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER = '".$monthAr[1]."' AND ";
                $where .= "     MONTH    = '".$monthAr[0]."' AND ";
                $where .= "     SCHREGNO = '".$model->field["schregno"]."' ";

                $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
            }  else {
                //INSERT
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["MONTH"][TEXT]    = $monthAr[0];
                $data["SEMESTER"][TEXT] = $monthAr[1];
                $data["SCHREGNO"][TEXT] = $model->field["schregno"];

                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
            }
            $db->query($query);

            /*****************************/
            /*  ATTEND_SEMES_REMARK_DAT  */
            /*****************************/
            //更新
            $data = array();
            $data["REMARK1"][TEXT]      = $model->field["REMARK"][$key];
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if (strlen($remarkData[$month]["SCHREGNO"])) {
                //UPDATE
                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     MONTH       = '".$monthAr[0]."' AND ";
                $where .= "     SEMESTER    = '".$monthAr[1]."' AND ";
                $where .= "     SCHREGNO    = '".$model->field["schregno"]."' ";

                $query = Query::updateSQL($data, "ATTEND_SEMES_REMARK_DAT", $where);
            } else {
                //INSERT
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["MONTH"][TEXT]    = $monthAr[0];
                $data["SEMESTER"][TEXT] = $monthAr[1];
                $data["SCHREGNO"][TEXT] = $model->field["schregno"];

                $query = Query::insertSQL($data, "ATTEND_SEMES_REMARK_DAT");
            }
            $db->query($query);

            /*****************************/
            /*  ATTEND_SEMES_DETAIL_DAT  */
            /*****************************/
            foreach ($model->item_array as $order => $val) {
                list ($item, $seq) = explode('_', $val["item"]);

                if ($item == "DETAIL") {
                    //更新
                    $data = array();
                    $zero = ($seq == "101") ? 0.0 : 0;
                    $data_field = ($seq == "101") ? "CNT_DECIMAL" : "CNT";
                    if ($val["input"]) {
                        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                            $data[$data_field][NUMBER] = $model->field[$val["item"]][$key];
                        } else {
                            $data[$data_field][NUMBER] = ($model->field[$val["item"]][$key])  ?  $model->field[$val["item"]][$key] : $zero;
                        }
                    } else {
                        if (!($model->Properties["use_Attend_zero_hyoji"] == "1" || $detailData[$month][$seq][$data_field] > 0)) {
                            $data[$data_field][NUMBER] = $zero;
                        }
                    }
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    if (strlen($detailData[$month][$seq]["SCHREGNO"])) {
                        //UPDATE
                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                        $where .= "     MONTH       = '".$monthAr[0]."' AND ";
                        $where .= "     SEMESTER    = '".$monthAr[1]."' AND ";
                        $where .= "     SCHREGNO    = '".$model->field["schregno"]."' AND ";
                        $where .= "     SEQ         = '".$seq."' ";

                        $query = Query::updateSQL($data, "ATTEND_SEMES_DETAIL_DAT", $where);
                    } else {
                        //INSERT
                        $data["COPYCD"][TEXT]   = "0";
                        $data["YEAR"][TEXT]     = CTRL_YEAR;
                        $data["MONTH"][TEXT]    = $monthAr[0];
                        $data["SEMESTER"][TEXT] = $monthAr[1];
                        $data["SCHREGNO"][TEXT] = $model->field["schregno"];
                        $data["SEQ"][TEXT]      = $seq;

                        $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                    }
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
