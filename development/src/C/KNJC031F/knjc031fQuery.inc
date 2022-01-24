<?php

require_once('for_php7.php');

class knjc031fQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJC031F' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getAttendLessonInfo($model) {
        $query   = "  WITH SDETAIL AS ( ";
        $query  .= "    SELECT ";
        $query  .= "      'X' AS KEY, ";
        $query  .= "      SCHOOL_REMARK2, ";
        $query  .= "      SCHOOL_REMARK3 ";
        $query  .= "    FROM ";
        $query  .= "      SCHOOL_DETAIL_DAT ";
        $query  .= "    WHERE ";
        $query  .= "      YEAR = '".CTRL_YEAR."' ";
        $query  .= "      AND SCHOOLCD = '".SCHOOLCD."' ";
        $query  .= "      AND SCHOOL_KIND = '".$model->school_kind."' ";
        $query  .= "      AND SCHOOL_SEQ = '004' ";
        $query  .= "  ), LESSON AS ( ";
        $query  .= "    SELECT ";
        $query  .= "      COURSECD, MAJORCD, ";
        $query  .= "      SUM(LESSON) AS SUM_LESSON, ";
        $query  .= "      'X' AS KEY ";
        $query  .= "    FROM ";
        $query  .= "      ATTEND_LESSON_MST ";
        $query  .= "    WHERE ";
        $query  .= "      YEAR = '".CTRL_YEAR."' ";
        $query  .= "      AND GRADE = '".$model->field["grade"]."' ";
        $query  .= "    GROUP BY  ";
        $query  .= "      COURSECD, MAJORCD ";
        $query  .= "  ) ";
        $query  .= "  SELECT ";
        $query  .= "      COURSECD || '-' || MAJORCD AS CMCD, ";
        $query  .= "    (SUM_LESSON * INT (SCHOOL_REMARK2)) / INT (value(SCHOOL_REMARK3,1)) AS TYUUI_NISSU ";
        $query  .= "  FROM ";
        $query  .= "    LESSON T1 ";
        $query  .= "    LEFT JOIN SDETAIL T2 ";
        $query  .= "      ON T2.KEY = T1.KEY ";

        return $query;
    }

    //MIN権限グループ取得
    function getMinGroupcd($model, $schoolKind) {
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

    //名称マスタより校種取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023'  ";
        $query .= " ORDER BY ";
        $query .= "     ABBV3, ";
        $query .= "     NAME1 ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model, $grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$grade."'  ";

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
        $query .= "     CONTROL_DIV, ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     SHOWORDER, ";
        $query .= "     INPUT_FLG ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        $query .= "     ATTEND_DIV  = '1' AND ";
        $query .= "     GRADE       = '00'  AND ";
        $query .= "     COURSECD || MAJORCD = '".$cm."' AND ";
        $query .= "     PROGRAMID   = '".PROGRAMID."' AND ";
        $query .= "     GROUPCD     = '".$model->groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     CONTROL_DIV, ";
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

    //学級名称取得
    function selectHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   T2.TR_CD1       = '".STAFFCD."' OR ";
            $query .= "         T2.TR_CD2       = '".STAFFCD."' OR ";
            $query .= "         T2.TR_CD3       = '".STAFFCD."' OR ";
            $query .= "         T2.SUBTR_CD1    = '".STAFFCD."' OR ";
            $query .= "         T2.SUBTR_CD2    = '".STAFFCD."' OR ";
            $query .= "         T2.SUBTR_CD3    = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
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
        $query .= "     T3.GHR_NAME AS LABEL";
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
            $query .= " AND (   T3.TR_CD1       = '".STAFFCD."' OR ";
            $query .= "         T3.TR_CD2       = '".STAFFCD."' OR ";
            $query .= "         T3.TR_CD3       = '".STAFFCD."' OR ";
            $query .= "         T3.SUBTR_CD1    = '".STAFFCD."' OR ";
            $query .= "         T3.SUBTR_CD2    = '".STAFFCD."' OR ";
            $query .= "         T3.SUBTR_CD3    = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期・月取得
    function selectSemesAll($seme="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        if (!$seme) {
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        } else {
            $query .= "     SEMESTERNAME, ";
            $query .= "     MONTH(SDATE) AS S_MONTH, ";
            $query .= "     DAY(SDATE) AS S_DAY, ";
            $query .= "     MONTH(EDATE) AS E_MONTH, ";
            $query .= "     DAY(EDATE) AS E_DAY ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (!$seme) {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月データの取得
    function selectMonthQuery($month, $model) {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $kind = ($model->school_kind) ? $model->school_kind : SCHOOLKIND;
            $setNameCd = "Z".$kind."05";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$setNameCd}' ";
        $query .= "     AND NAMECD2 IN (SELECT CONTROL_CODE FROM ADMIN_CONTROL_DAT WHERE YEAR='".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     SCHOOL_KIND = '".$model->school_kind."' AND ";
         } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG ='2') ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //ATTEND_SEMES_DATのMAX(LESSON)取得
    function getMaxLesson1($model) {
        $query  = " SELECT ";
        $query .= "     MAX(T2.LESSON) AS LESSON ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT S1 ON T1.YEAR     = S1.YEAR ";
            $query .= "                                      AND T1.SEMESTER = S1.SEMESTER ";
            $query .= "                                      AND T1.SCHREGNO = S1.SCHREGNO ";
        }
        $query .= "    ,ATTEND_SEMES_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T2.MONTH || '-' || T2.SEMESTER = '".$model->field["month"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     S1.GHR_CD   = '".$model->field["GROUP_HR_CLASS"]."' AND ";
        } else {
            $query .= "     T1.GRADE    = '".$model->field["grade"]."' AND ";
            $query .= "     T1.HR_CLASS = '".$model->field["class"]."' AND ";
        }
        $query .= "     T2.COPYCD   = '0' ";

        return $query;
    }

    //ATTEND_LESSON_MSTのMAX(LESSON)取得
    function getMaxLesson2($model) {
        $query  = " WITH LESSON_LIST AS ( ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= " SELECT ";
            $query .= "     T2.LESSON ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT S1 ON T1.YEAR     = S1.YEAR ";
            $query .= "                                      AND T1.SEMESTER = S1.SEMESTER ";
            $query .= "                                      AND T1.SCHREGNO = S1.SCHREGNO, ";
            $query .= "     ATTEND_LESSON_MST T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = T2.YEAR AND ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "     T2.MONTH || '-' || T2.SEMESTER = '".$model->field["month"]."' AND ";
            $query .= "     T1.GRADE    = T2.GRADE AND ";
            $query .= "     S1.GHR_CD   = '".$model->field["GROUP_HR_CLASS"]."' AND ";
            $query .= "     T2.COURSECD = '0' AND ";
            $query .= "     T2.MAJORCD  = '000' ";
        } else {
            $query .= "     SELECT ";
            $query .= "         LESSON ";
            $query .= "     FROM ";
            $query .= "         ATTEND_LESSON_MST ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "         GRADE       = '".$model->field["grade"]."' AND ";
            $query .= "         COURSECD    = '0' AND ";
            $query .= "         MAJORCD     = '000' AND ";
            $query .= "         MONTH || '-' || SEMESTER = '".$model->field["month"]."' ";
        }
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T2.LESSON ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT S1 ON T1.YEAR     = S1.YEAR ";
            $query .= "                                      AND T1.SEMESTER = S1.SEMESTER ";
            $query .= "                                      AND T1.SCHREGNO = S1.SCHREGNO ";
        }
        $query .= "        ,ATTEND_LESSON_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T2.MONTH || '-' || T2.SEMESTER = '".$model->field["month"]."' AND ";
        $query .= "         T1.GRADE    = T2.GRADE AND ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "         S1.GHR_CD   = '".$model->field["GROUP_HR_CLASS"]."' AND ";
        } else {
            $query .= "         T1.GRADE    = '".$model->field["grade"]."' AND ";
            $query .= "         T1.HR_CLASS = '".$model->field["class"]."' AND ";
        }
        $query .= "         T1.COURSECD = T2.COURSECD AND ";
        $query .= "         T1.MAJORCD  = T2.MAJORCD ";
        $query .= " ) ";

        $query .= " SELECT MAX(LESSON) FROM LESSON_LIST ";

        return $query;
    }

    //締め日の取得
    function getAppointedDay($model) {

        $query .= " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND = '". $model->school_kind ."' AND ";
        }
        $query .= "     MONTH || '-' || SEMESTER = '".$model->field["month"]."' ";

        return $query;
    }

    //出欠完了フラグ取得
    function getExecuted($model) {
        $monthsem = preg_split("/-/", $model->field["month"]);

        $query  = " SELECT ";
        $query .= "     EXECUTED ";
        $query .= " FROM ";
        $query .= "     ATTEND_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$monthsem[0]."' AND ";
        $query .= "     SEMESTER    = '".$monthsem[1]."' AND ";
        $query .= "     ATTEND_DIV  = '1' AND ";
        $query .= "     GRADE       = '".$model->field["grade"]."' AND ";
        $query .= "     HR_CLASS    = '".$model->field["class"]."' AND ";
        $query .= "     CHAIRCD     = '0000000' ";

        return $query;
    }

    //クラス備考
    function getHrRemark($model)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     ATTEND_SEMES_REMARK_GHR_DAT ";
        } else {
            $query .= "     ATTEND_SEMES_REMARK_HR_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND MONTH || '-' || SEMESTER = '{$model->field["month"]}' ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     AND GHR_CD = '{$model->field["GROUP_HR_CLASS"]}' ";
        } else {
            $query .= "     AND GRADE || '-' || HR_CLASS = '{$model->field["hr_class"]}' ";
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

    //異動データ取得
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
        $query .= "     SUM(CASE WHEN '".$date1."' BETWEEN TRANSFER_SDATE AND CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END THEN 1 ELSE 0 END) AS IDOU_COLOR, ";
        $query .= "     SUM(CASE WHEN TRANSFER_SDATE <= '".$date2."' AND '".$date1."' <= CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END THEN 1 ELSE 0 END) AS IDOU_TEXT ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') ";

        return $query;
    }

    //出欠月別累積データ・出欠累積データ・出欠科目別累積データ・学籍異動データ
    function selectAttendQuery($model, $schoolMst) {
        $monthsem = array();
        $monthsem = preg_split("/-/", $model->field["month"]);
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

        $query  = " WITH ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         V_ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         MONTH    = '".$monthsem[0]."' AND ";
        $query .= "         SEMESTER = '".$monthsem[1]."'  ";
        $query .= " ), SUM_ATTEND_SEMES AS ( ";
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

        $query .= " SELECT ";
        $query .= "     SD.SCHREGNO, ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     S1.GHR_ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "     SD.ATTENDNO, ";
        }
        $query .= "     SD.GRADE, ";
        $query .= "     SD.COURSECD, ";
        $query .= "     SD.MAJORCD, ";
        $query .= "     SG.SCHOOL_KIND, ";
        $query .= "     SM.NAME_SHOW, ";
        $query .= "     AM.APPOINTED_DAY, ";
        $query .= "     AD.LESSON, ";
        $query .= "     AD.OFFDAYS, ";
        $query .= "     AD.ABROAD, ";
        $query .= "     AD.ABSENT, ";
        $query .= "     AD.SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     AD.KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     AD.VIRUS, ";
        }
        $query .= "     AD.MOURNING, ";
        $query .= "     VALUE(AD.LESSON, 0) - VALUE(AD.SUSPEND, 0) - VALUE(AD.MOURNING, 0) - VALUE(AD.ABROAD, 0) - VALUE(AD.OFFDAYS, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(AD.KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(AD.VIRUS, 0) ";
        }
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(AD.OFFDAYS, 0) ";
        }
        $query .= "     AS ATTEND, ";
        $query .= "     AD.SICK, ";
        $query .= "     AD.NOTICE, ";
        $query .= "     AD.NONOTICE, ";
        $query .= "     VALUE(AD.SICK, 0) + VALUE(AD.NOTICE, 0) + VALUE(AD.NONOTICE, 0) ";
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(AD.OFFDAYS, 0) ";
        }
        $query .= "     AS KESSEKI, ";
        $query .= "     VALUE(AD.LESSON, 0) - VALUE(AD.SUSPEND, 0) - VALUE(AD.MOURNING, 0) - VALUE(AD.ABROAD, 0) - VALUE(AD.OFFDAYS, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(AD.KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(AD.VIRUS, 0) ";
        }
        $query .= "               - VALUE(AD.SICK, 0) - VALUE(AD.NOTICE, 0) - VALUE(AD.NONOTICE, 0) ";
        $query .= "     AS PRESENT, ";
        $query .= "     AD.LATE, ";
        $query .= "     AD.EARLY, ";
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $query .= "     L1.CNT AS DETAIL_001, ";
            $query .= "     L2.CNT AS DETAIL_002, ";
            $query .= "     L3.CNT AS DETAIL_003, ";
            $query .= "     L4.CNT AS DETAIL_004, ";
            $query .= "     L101.CNT_DECIMAL AS DETAIL_101, ";
            $query .= "     L102.CNT AS DETAIL_102, ";
        } else {
            $query .= "     AD.REIHAI_KEKKA AS DETAIL_001, ";
            $query .= "     AD.M_KEKKA_JISU AS DETAIL_002, ";
            $query .= "     AD.REIHAI_TIKOKU AS DETAIL_003, ";
            $query .= "     AD.JYUGYOU_TIKOKU AS DETAIL_004, ";
            $query .= "     AD.JYUGYOU_JISU_DECIMAL AS DETAIL_101, ";
            $query .= "     AD.TOCHU_KEKKA AS DETAIL_102, ";
        }
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
        $query .= "     RMK.REMARK1 AS REMARK, ";
        $query .= "     AD.SCHREGNO AS SEM_SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT SD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT SG ";
        $query .= "          ON SD.YEAR         = SG.YEAR ";
        $query .= "         AND SD.GRADE        = SG.GRADE ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST SM ";
        $query .= "          ON SD.SCHREGNO     = SM.SCHREGNO ";
        $query .= "     LEFT JOIN ATTEND_SEMES AD ";
        $query .= "          ON AD.SCHREGNO     = SD.SCHREGNO ";
        $query .= "     LEFT JOIN APPOINTED_DAY_MST AM ";
        $query .= "          ON AD.YEAR         = AM.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND SG.SCHOOL_KIND  = AM.SCHOOL_KIND ";
        }
        $query .= "         AND AD.MONTH        = AM.MONTH ";
        $query .= "         AND AD.SEMESTER     = AM.SEMESTER ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L1 ";
        $query .= "          ON AD.COPYCD       = L1.COPYCD ";
        $query .= "         AND AD.YEAR         = L1.YEAR ";
        $query .= "         AND AD.MONTH        = L1.MONTH ";
        $query .= "         AND AD.SEMESTER     = L1.SEMESTER ";
        $query .= "         AND AD.SCHREGNO     = L1.SCHREGNO ";
        $query .= "         AND L1.SEQ          = '001' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L2 ";
        $query .= "          ON AD.COPYCD       = L2.COPYCD ";
        $query .= "         AND AD.YEAR         = L2.YEAR ";
        $query .= "         AND AD.MONTH        = L2.MONTH ";
        $query .= "         AND AD.SEMESTER     = L2.SEMESTER ";
        $query .= "         AND AD.SCHREGNO     = L2.SCHREGNO ";
        $query .= "         AND L2.SEQ          = '002' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L3 ";
        $query .= "          ON AD.COPYCD       = L3.COPYCD ";
        $query .= "         AND AD.YEAR         = L3.YEAR ";
        $query .= "         AND AD.MONTH        = L3.MONTH ";
        $query .= "         AND AD.SEMESTER     = L3.SEMESTER ";
        $query .= "         AND AD.SCHREGNO     = L3.SCHREGNO ";
        $query .= "         AND L3.SEQ          = '003' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L4 ";
        $query .= "          ON AD.COPYCD       = L4.COPYCD ";
        $query .= "         AND AD.YEAR         = L4.YEAR ";
        $query .= "         AND AD.MONTH        = L4.MONTH ";
        $query .= "         AND AD.SEMESTER     = L4.SEMESTER ";
        $query .= "         AND AD.SCHREGNO     = L4.SCHREGNO ";
        $query .= "         AND L4.SEQ          = '004' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L101 ";
        $query .= "          ON AD.COPYCD       = L101.COPYCD ";
        $query .= "         AND AD.YEAR         = L101.YEAR ";
        $query .= "         AND AD.MONTH        = L101.MONTH ";
        $query .= "         AND AD.SEMESTER     = L101.SEMESTER ";
        $query .= "         AND AD.SCHREGNO     = L101.SCHREGNO ";
        $query .= "         AND L101.SEQ        = '101' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L102 ";
        $query .= "          ON AD.COPYCD       = L102.COPYCD ";
        $query .= "         AND AD.YEAR         = L102.YEAR ";
        $query .= "         AND AD.MONTH        = L102.MONTH ";
        $query .= "         AND AD.SEMESTER     = L102.SEMESTER ";
        $query .= "         AND AD.SCHREGNO     = L102.SCHREGNO ";
        $query .= "         AND L102.SEQ        = '102' ";
        $query .= "     LEFT JOIN SUM_ATTEND_SEMES SUMAD ";
        $query .= "          ON SUMAD.SCHREGNO  = SD.SCHREGNO ";
        $query .= "     LEFT JOIN ATTEND_SEMES_REMARK_DAT RMK ";
        $query .= "          ON SD.YEAR         = RMK.YEAR ";
        $query .= "         AND SD.SEMESTER     = RMK.SEMESTER ";
        $query .= "         AND RMK.MONTH       = '".$monthsem[0]."' ";
        $query .= "         AND SD.SCHREGNO     = RMK.SCHREGNO ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT S1 ";
            $query .= "          ON SD.YEAR         = S1.YEAR ";
            $query .= "         AND SD.SEMESTER     = S1.SEMESTER ";
            $query .= "         AND SD.SCHREGNO     = S1.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "     SD.YEAR = '".CTRL_YEAR."' AND ";
        if ($monthsem[0] != "" && $monthsem[0] != NULL) {
            $query .= "     SD.SEMESTER = '".$monthsem[1]."' AND ";
        } else {
            $query .= "     SD.SEMESTER IS NULL AND ";
        }
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     S1.GHR_CD   = '".$model->field["GROUP_HR_CLASS"]."' ";
        } else {
            $query .= "     SD.GRADE    = '".$model->field["grade"]."' AND ";
            $query .= "     SD.HR_CLASS = '".$model->field["class"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND SD.COURSECD || SD.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

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

    function getColumnLength($db, $tabname, $colname) {

        $query  = " SELECT LENGTH ";
        $query .= " FROM (SELECT ";
        $query .= "     LENGTH ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '{$tabname}' ";
        $query .= "     AND COLNAME = '{$colname}' ";
        $query .= " ) T1 ";

        $len = $db->getOne($query);
        return $len;
    }

    //データ取得 -- ATTEND_SEMES_DAT / ATTEND_SEMES_REMARK_DAT / ATTEND_SEMES_DETAIL_DAT
    function getAttendData($model, $table, $month, $semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SCHREGNO IN ('".implode("','", $model->field["SCHREGNO"])."') ";

        return $query;
    }

    //出欠月別累積データの更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $monthsem = array();
        $monthsem = preg_split("/-/", $model->field["month"]);

        if ($model->Properties["useAttendSemesHrRemark"] == "1") {

            $tableName = "ATTEND_SEMES_REMARK_HR_DAT";
            if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
                $tableName = "ATTEND_SEMES_REMARK_GHR_DAT";
            }

            $query  = " DELETE FROM ";
            $query .= "     {$tableName} ";
            $query .= " WHERE ";
            $query .= "     YEAR = '". CTRL_YEAR ."' ";
            $query .= "     AND MONTH || '-' || SEMESTER = '{$model->field["month"]}' ";
            if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
                $query .= "     AND GHR_CD = '{$model->field["GROUP_HR_CLASS"]}' ";
            } else {
                $query .= "     AND GRADE || '-' || HR_CLASS = '{$model->field["hr_class"]}' ";
            }
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]     = CTRL_YEAR;
            $data["MONTH"][TEXT]    = $monthsem[0];
            $data["SEMESTER"][TEXT] = $monthsem[1];
            if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
                $data["GHR_CD"][TEXT]   = $model->field["GROUP_HR_CLASS"];
            } else {
                $data["GRADE"][TEXT]    = $model->field["grade"];
                $data["HR_CLASS"][TEXT] = $model->field["class"];
            }
            $data["REMARK1"][TEXT]      = $model->field["HR_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, $tableName);
            $db->query($query);
        }

        //フィールド取得
        $query = knjc031fQuery::getFieldList();
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
            $query = knjc031fQuery::getAttendData($model, $table, $monthsem[0], $monthsem[1]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($table == "ATTEND_SEMES_DETAIL_DAT") {
                    $getData[$row["SCHREGNO"]][$row["SEQ"]] = $row;
                } else {
                    $getData[$row["SCHREGNO"]] = $row;
                }
            }
            $varname = $var."Data";
            $$varname = $getData;
        }

        //更新対象行
        $useLine = explode(',', $model->field["useLine"]);

        $line = 0;
        foreach($model->field["SCHREGNO"] as $key => $schregno) {
            /**********************/
            /*  ATTEND_SEMES_DAT  */
            /**********************/
            //更新
            $data = $upItemArray = array();
            $data["APPOINTED_DAY"][TEXT] = $model->appointed_day;       //締め日
            foreach ($model->item_array as $order => $val) {
                //入力可の項目が更新対象
                if (in_array($val["item"], $table_field) && $val["input"] && in_array($key, $useLine)) {
                    if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                        $data[$val["item"]][NUMBER] = $model->field[$val["item"]][$line];
                    } else {
                        $data[$val["item"]][NUMBER] = ($model->field[$val["item"]][$line]) ? $model->field[$val["item"]][$line] : 0;
                    }
                    $upItemArray[] = $val["item"];
                }
            }
            if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                foreach ($table_field as $field) {
                    //入力可以外の項目（値なし）を更新
                    if (!(in_array($field, $upItemArray) || $semesData[$schregno][$field] > 0)) {
                        $data[$field][NUMBER] = 0;
                    }
                }
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD ;                    //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()";                 //更新日付

            if (strlen($semesData[$schregno]["SCHREGNO"])) {
                //UPDATE
                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                $where .= "     SCHREGNO    = '".$schregno."' ";

                $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
            } else {
                //INSERT
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["MONTH"][TEXT]    = $monthsem[0];
                $data["SEMESTER"][TEXT] = $monthsem[1];
                $data["SCHREGNO"][TEXT] = $schregno;

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

            if (strlen($remarkData[$schregno]["SCHREGNO"])) {
                //UPDATE
                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                $where .= "     SCHREGNO    = '".$schregno."' ";

                $query = Query::updateSQL($data, "ATTEND_SEMES_REMARK_DAT", $where);
            } else {
                //INSERT
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["MONTH"][TEXT]    = $monthsem[0];
                $data["SEMESTER"][TEXT] = $monthsem[1];
                $data["SCHREGNO"][TEXT] = $schregno;

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
                    if ($val["input"] && in_array($key, $useLine)) {
                        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                            $data[$data_field][NUMBER] = $model->field[$val["item"]][$line];
                        } else {
                            $data[$data_field][NUMBER] = ($model->field[$val["item"]][$line])  ?  $model->field[$val["item"]][$line] : $zero;
                        }
                    } else {
                        if (!($model->Properties["use_Attend_zero_hyoji"] == "1" || $detailData[$schregno][$seq][$data_field] > 0)) {
                            $data[$data_field][NUMBER] = $zero;
                        }
                    }
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    if (strlen($detailData[$schregno][$seq]["SCHREGNO"])) {
                        //UPDATE
                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                        $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                        $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                        $where .= "     SCHREGNO    = '".$schregno."' AND ";
                        $where .= "     SEQ         = '".$seq."' ";

                        $query = Query::updateSQL($data, "ATTEND_SEMES_DETAIL_DAT", $where);
                    } else {
                        //INSERT
                        $data["COPYCD"][TEXT]   = "0";
                        $data["YEAR"][TEXT]     = CTRL_YEAR;
                        $data["MONTH"][TEXT]    = $monthsem[0];
                        $data["SEMESTER"][TEXT] = $monthsem[1];
                        $data["SCHREGNO"][TEXT] = $schregno;
                        $data["SEQ"][TEXT]      = $seq;

                        $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                    }
                    $db->query($query);
                }
            }
            if (in_array($key, $useLine)) $line++;
        }

        if ($model->field["SELECT_CLASS_TYPE"] == 1) {
            /***********************/
            /*  ATTEND_CHKFIN_DAT  */
            /***********************/
            //削除
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_CHKFIN_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     MONTH       = '".$monthsem[0]."' AND ";
            $query .= "     SEMESTER    = '".$monthsem[1]."' AND ";
            $query .= "     ATTEND_DIV  = '1' AND ";
            $query .= "     GRADE       = '".$model->field["grade"]."' AND ";
            $query .= "     HR_CLASS    = '".$model->field["class"]."' ";

            $db->query($query);

            //追加
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["MONTH"][TEXT]        = $monthsem[0];
            $data["SEMESTER"][TEXT]     = $monthsem[1];
            $data["ATTEND_DIV"][TEXT]   = "1";
            $data["GRADE"][TEXT]        = $model->field["grade"];
            $data["HR_CLASS"][TEXT]     = $model->field["class"];
            $data["CHAIRCD"][TEXT]      = "0000000";
            $data["EXECUTED"][TEXT]     = $model->field["EXECUTED"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, "ATTEND_CHKFIN_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
