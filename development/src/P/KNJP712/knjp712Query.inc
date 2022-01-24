<?php

require_once('for_php7.php');

class knjp712Query extends Query {

    /****************/
    /*    左画面    */
    /****************/

    //校種取得
    function getSchKind($model, $val="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        if ($val) {
            $query .= " AND NAME1 = '".$val."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入金グループマスタ取得
    function getCollectGrpMst($model) {
        $query  = " SELECT ";
        $query .= "     COLLECT_GRP_CD || ' ' || COLLECT_GRP_NAME AS LABEL, ";
        $query .= "     COLLECT_GRP_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' AND ";
        $query .= "     YEAR        = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年・課程・学科取得
    function getGradeCourseMajor($model) {
        $yearField = ($model->schdiv == "1") ? "ENTERYEAR" : "YEAR";

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD AS VALUE, ";
        if ($model->schdiv == "1") {
            $query .= "     '新入生' || VALUE(T1.GRADE,'00') || '  (' || T1.COURSECD || T1.MAJORCD || ') ' || T3.COURSENAME || T3.MAJORNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
        } else {
            $query .= "     T2.GRADE_NAME1 || '  (' || T1.COURSECD || T1.MAJORCD || ') ' || T3.COURSENAME || T3.MAJORNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
        }
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "          ON T1.{$yearField} = T2.YEAR ";
        $query .= "         AND T1.GRADE        = T2.GRADE ";
        $query .= "         AND T2.SCHOOL_KIND  = '".$model->schoolkind."' ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST T3 ";
        $query .= "          ON T1.{$yearField} = T3.YEAR ";
        $query .= "         AND T1.COURSECD     = T3.COURSECD ";
        $query .= "         AND T1.MAJORCD      = T3.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.{$yearField} = '".$model->year."' ";
        if ($model->schdiv != "1") {
            $query .= " AND T1.SEMESTER     = '".CTRL_SEMESTER."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースコード取得
    function getCourseCode($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECODE || ' ' || T1.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST T1 ";
        if ($model->schdiv == "1") {
            $query .= "     INNER JOIN FRESHMAN_DAT F1 ";
            $query .= "          ON T1.YEAR         = F1.ENTERYEAR ";
            $query .= "         AND T1.COURSECODE   = F1.COURSECODE ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($model) {
        $yearField = ($model->schdiv == "1") ? "ENTERYEAR" : "YEAR";

        $query  = " SELECT DISTINCT ";
        $query .= "     VALUE(T1.GRADE,'00') || '-' || VALUE(T1.HR_CLASS,'000') AS VALUE, ";
        if ($model->schdiv == "1") {
            $query .= "     '新入生' || VALUE(T1.HR_CLASS,'000') AS LABEL ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
        } else {
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
        }
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "          ON T1.{$yearField} = T2.YEAR ";
        $query .= "         AND T1.GRADE        = T2.GRADE ";
        $query .= "         AND T2.SCHOOL_KIND  = '".$model->schoolkind."' ";
        $query .= " WHERE ";
        $query .= "     T1.{$yearField} = '".$model->year."' ";
        if ($model->schdiv != "1") {
            $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        if (strlen($model->grade_cm)) {
            list ($grade, $course, $major) = explode("-", $model->grade_cm);
            $query .= " AND T1.GRADE = '".$grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラブ取得
    function getClub($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLUBCD AS VALUE, ";
        $query .= "     T1.CLUBCD || ' ' || T1.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST T1, ";
        $query .= "     CLUB_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD   = T2.CLUBCD AND ";
        $query .= "     T2.YEAR     = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テーブル存在チェック
    function checkTableExist($table) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = '{$table}' ";

        return $query;
    }

    //寮取得
    function getDomitory($model, $cnt="") {
        $query  = " SELECT ";
        if ($cnt != '') {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     YDAT.DOMI_CD as VALUE, ";
            $query .= "     YDAT.DOMI_CD || ' ' || DMST.DOMI_NAME as LABEL ";
        }
        $query .= " FROM ";
        $query .= "     DOMITORY_YDAT YDAT ";
        $query .= "     LEFT JOIN DOMITORY_MST DMST ON DMST.DOMI_CD = YDAT.DOMI_CD ";
        $query .= " WHERE ";
        $query .= "     YDAT.YEAR = '{$model->year}' ";
        if ($cnt != '') {
        } else {
            $query .= " ORDER BY ";
            $query .= "     YDAT.DOMI_CD ";
        }

        return $query;
    }

    //入金済 or キャンセル 伝票番号取得
    function getPaidSlipNo($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' AND ";
        $query .= "     YEAR        = '".$model->year."' ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= "     AND YEAR        = '".$model->year."' ";
        $query .= "     AND CANCEL_DATE IS NOT NULL ";

        return $query;
    }

    //一覧取得
    function getList($model) {
        $yearField = ($model->schdiv == "1") ? "ENTERYEAR" : "YEAR";

        $query  = " WITH SCH_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        if ($model->schdiv == "1") {
            $query .= "         T1.NAME AS NAME_SHOW, ";
            $query .= "         '新入生' || VALUE(T1.HR_CLASS,'000') AS HR_NAME, ";
        } else {
            $query .= "         T2.NAME_SHOW, ";
            $query .= "         T4.HR_NAME, ";
        }
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE ";
        $query .= "     FROM ";
        if ($model->schdiv == "1") {
            $query .= "         FRESHMAN_DAT T1 ";
        } else {
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "              ON T1.{$yearField} = T3.YEAR ";
        $query .= "             AND T1.GRADE        = T3.GRADE ";
        $query .= "             AND T3.SCHOOL_KIND  = '".$model->schoolkind."' ";
        if ($model->schdiv != "1") {
            $query .= "         INNER JOIN SCHREG_REGD_HDAT T4 ";
            $query .= "              ON T1.{$yearField} = T4.YEAR ";
            $query .= "             AND T1.SEMESTER     = T4.SEMESTER ";
            $query .= "             AND T1.GRADE        = T4.GRADE ";
            $query .= "             AND T1.HR_CLASS     = T4.HR_CLASS ";
        }
        if (strlen($model->clubcd)) {
            $query .= "         INNER JOIN (SELECT ";
            $query .= "                         SCHREGNO, ";
            $query .= "                         COUNT(*) AS CLUB_CNT ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_CLUB_HIST_DAT ";
            $query .= "                     WHERE ";
            $query .= "                         CLUBCD = '".$model->clubcd."' ";
            $query .= "                     GROUP BY ";
            $query .= "                         SCHREGNO ";
            $query .= "                     ) T5 ON T1.SCHREGNO = T5.SCHREGNO AND T5.CLUB_CNT > 0 ";
        }
        //寮
        if (strlen($model->domi_cd)) {
            $query .= "         INNER JOIN (SELECT ";
            $query .= "                         SCHREGNO, ";
            $query .= "                         COUNT(*) AS DOMI_CNT ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_DOMITORY_HIST_DAT ";
            $query .= "                     WHERE ";
            $query .= "                         DOMI_CD = '".$model->domi_cd."' ";
            $query .= "                     GROUP BY ";
            $query .= "                         SCHREGNO ";
            $query .= "                     ) T6 ON T1.SCHREGNO = T6.SCHREGNO AND T6.DOMI_CNT > 0 ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.{$yearField}     = '".$model->year."' ";
        if ($model->schdiv != "1") {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SLIP_NO, ";
        $query .= "     T2.*, ";
        if ($model->schdiv == "1") {
            $query .= "     VALUE(T2.GRADE,'00') || '-' || VALUE(T2.HR_CLASS,'000') || '-' || VALUE(T2.ATTENDNO,'000') || '番' AS GHA, ";
        } else {
            $query .= "     T2.HR_NAME || ' ' || T2.ATTENDNO || '番' AS GHA, ";
        }
        $query .= "     CASE WHEN T1.COLLECT_PATTERN_CD = '00' THEN '00:パターンなし' ELSE T1.COLLECT_PATTERN_CD || ':' || L1.COLLECT_PATTERN_NAME END AS COLLECT_PATTERN_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT T1 ";
        $query .= "     INNER JOIN SCH_DATA T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN COLLECT_DEFAULT_SETTINGS_MST L1 ";
        $query .= "          ON T1.SCHOOLCD         = L1.SCHOOLCD ";
        $query .= "         AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        $query .= "         AND T1.YEAR             = L1.YEAR ";
        $query .= "         AND T1.COLLECT_PATTERN_CD = L1.COLLECT_PATTERN_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->schoolkind."' AND ";
        $query .= "     T1.YEAR         = '".$model->year."' AND ";
        $query .= "     T1.COLLECT_GRP_CD = '".$model->collect_grp_cd."' ";
        if (strlen($model->grade_cm)) {
            $query .= " AND T2.GRADE || '-' || T2.COURSECD || '-' || T2.MAJORCD = '".$model->grade_cm."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND T2.COURSECODE = '".$model->coursecode."' ";
        }
        if (strlen($model->grade_hr_class)) {
            $query .= " AND T2.GRADE || '-' || T2.HR_CLASS = '".$model->grade_hr_class."' ";
        }
        $query .= " ORDER BY ";
        if ($model->schdiv == "1") {
            $query .= "     T1.SCHREGNO, ";
        } else {
            $query .= "     T2.GRADE, ";
            $query .= "     T2.HR_CLASS, ";
            $query .= "     T2.ATTENDNO, ";
        }
        $query .= "     T1.SLIP_NO ";

        return $query;
    }


    /****************/
    /*    右画面    */
    /****************/

    //生徒一覧取得
    function getSchList($model) {
        $yearField = ($model->schdiv == "1") ? "ENTERYEAR" : "YEAR";

        $query  = " SELECT ";
        if ($model->schdiv == "1") {
            $query .= "     '00000000_' || T1.SCHREGNO AS VALUE, ";
            $query .= "     T1.SCHREGNO || '　' || VALUE(T1.GRADE,'00') || '-' || VALUE(T1.HR_CLASS,'000') || '-' || VALUE(T1.ATTENDNO,'000') || '番　' || T1.NAME ";
            $query .= "     || CASE WHEN VALUE(T1.INOUTCD, '') = '0' THEN '(内)' ELSE '' END AS LABEL, ";
        } else {
            $query .= "     VALUE(T1.GRADE,'00') || VALUE(T1.HR_CLASS,'000') || VALUE(T1.ATTENDNO,'000') || '_' || T1.SCHREGNO AS VALUE, ";
            $query .= "     T4.HR_NAME || '　' || T1.ATTENDNO || '番　' || T2.NAME_SHOW ";
            $query .= "     || CASE WHEN VALUE(T2.INOUTCD, '') = '0' THEN '(内)' ELSE '' END AS LABEL, ";
        }
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        if ($model->schdiv == "1") {
            $query .= "     FRESHMAN_DAT T1 ";
        } else {
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "          ON T1.{$yearField} = T3.YEAR ";
        $query .= "         AND T1.GRADE        = T3.GRADE ";
        $query .= "         AND T3.SCHOOL_KIND  = '".$model->schoolkind."' ";
        if ($model->schdiv != "1") {
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ";
            $query .= "          ON T1.{$yearField} = T4.YEAR ";
            $query .= "         AND T1.SEMESTER     = T4.SEMESTER ";
            $query .= "         AND T1.GRADE        = T4.GRADE ";
            $query .= "         AND T1.HR_CLASS     = T4.HR_CLASS ";
        }
        if (strlen($model->clubcd)) {
            $query .= "     INNER JOIN (SELECT ";
            $query .= "                     SCHREGNO, ";
            $query .= "                     COUNT(*) AS CLUB_CNT ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_CLUB_HIST_DAT ";
            $query .= "                 WHERE ";
            $query .= "                     CLUBCD = '".$model->clubcd."' ";
            $query .= "                 GROUP BY ";
            $query .= "                     SCHREGNO ";
            $query .= "                 ) T5 ON T1.SCHREGNO = T5.SCHREGNO AND T5.CLUB_CNT > 0 ";
        }
        //寮
        if (strlen($model->domi_cd)) {
            $query .= "         INNER JOIN (SELECT ";
            $query .= "                         SCHREGNO, ";
            $query .= "                         COUNT(*) AS DOMI_CNT ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_DOMITORY_HIST_DAT ";
            $query .= "                     WHERE ";
            $query .= "                         DOMI_CD = '".$model->domi_cd."' ";
            $query .= "                     GROUP BY ";
            $query .= "                         SCHREGNO ";
            $query .= "                     ) T6 ON T1.SCHREGNO = T6.SCHREGNO AND T6.DOMI_CNT > 0 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.{$yearField}     = '".$model->year."' AND ";
        if ($model->schdiv != "1") {
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        }
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     COLLECT_SLIP_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "                     T3.SCHOOL_KIND  = S1.SCHOOL_KIND AND ";
        $query .= "                     T1.{$yearField} = S1.YEAR AND ";
        $query .= "                     T1.SCHREGNO     = S1.SCHREGNO AND ";
        $query .= "                     S1.COLLECT_GRP_CD = '".$model->collect_grp_cd."' ";
        $query .= "                 ) ";
        if ($model->schdiv == "1") {
            $query .= "     AND T1.SCHREGNO NOT IN (SELECT ";
            $query .= "                                 SCHREGNO ";
            $query .= "                             FROM ";
            $query .= "                                 SCHREG_BASE_MST ";
            $query .= "                             ) ";
        }
        if (strlen($model->grade_cm)) {
            $query .= " AND VALUE(T1.GRADE,'00') || '-' || T1.COURSECD || '-' || T1.MAJORCD = '".$model->grade_cm."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND T1.COURSECODE = '".$model->coursecode."' ";
        }
        if (strlen($model->grade_hr_class)) {
            $query .= " AND VALUE(T1.GRADE,'00') || '-' || VALUE(T1.HR_CLASS,'000') = '".$model->grade_hr_class."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入金パターン取得
    function getCollectPattern($model, $collect_pattern_cd="") {
        $query  = " SELECT ";
        if ($collect_pattern_cd) {
            $query .= "     * ";
        } else {
            $query .= "     COLLECT_PATTERN_CD AS VALUE, ";
            $query .= "     COLLECT_PATTERN_CD || ' ' || COLLECT_PATTERN_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' AND ";
        $query .= "     YEAR        = '".$model->year."' ";
        if ($collect_pattern_cd) {
            $query .= " AND COLLECT_PATTERN_CD = '".$collect_pattern_cd."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }


    /******************/
    /*    更新処理    */
    /******************/

    //max伝票番号取得
    function getMaxSlipNo($model) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(SLIP_NO) AS SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";

        return $query;
    }

    //グループ情報取得
    function getGrpData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.COLLECT_L_CD, ";
        $query .= "     T2.COLLECT_M_CD, ";
        $query .= "     T3.COLLECT_M_MONEY, ";
        $query .= "     T3.IS_CREDITCNT, ";
        $query .= "     T3.SHOW_ORDER, ";
        $query .= "     T3.DIVIDE_PROCESS, ";
        $query .= "     T3.TEXTBOOKDIV, ";
        $query .= "     T3.ROUND_DIGIT ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST T1, ";
        $query .= "     COLLECT_GRP_DAT T2, ";
        $query .= "     COLLECT_M_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD         = T2.SCHOOLCD AND ";
        $query .= "     T1.SCHOOLCD         = T3.SCHOOLCD AND ";
        $query .= "     T1.SCHOOLCD         = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
        $query .= "     T1.SCHOOL_KIND      = T3.SCHOOL_KIND AND ";
        $query .= "     T1.SCHOOL_KIND      = '".$model->schoolkind."' AND ";
        $query .= "     T1.YEAR             = T2.YEAR AND ";
        $query .= "     T1.YEAR             = T3.YEAR AND ";
        $query .= "     T1.YEAR             = '".$model->year."' AND ";
        $query .= "     T1.COLLECT_GRP_CD   = T2.COLLECT_GRP_CD AND ";
        $query .= "     T1.COLLECT_GRP_CD   = '".$model->collect_grp_cd."' AND ";
        $query .= "     T2.COLLECT_L_CD     = T3.COLLECT_L_CD AND ";
        $query .= "     T2.COLLECT_M_CD     = T3.COLLECT_M_CD ";
        $query .= " ORDER BY ";
        $query .= "     T3.SHOW_ORDER, ";
        $query .= "     T2.COLLECT_L_CD, ";
        $query .= "     T2.COLLECT_M_CD ";

        return $query;
    }

    //入金計画取得
    function getCollectMonthGrpDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->schoolkind."' AND ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     COLLECT_GRP_CD  = '".$model->collect_grp_cd."' ";

        return $query;
    }

    //入金計画、入金グループデータ取得
    function getCollectGrpDatMonthDat($model, $table) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->schoolkind."' AND ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     COLLECT_GRP_CD  = '".$model->collect_grp_cd."' ";

        return $query;
    }

    //単位数取得
    function getCredits($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(T1.CREDITS) AS CREDITS ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD, ";
        $query .= "         SUM(VALUE(CRE.CREDITS, 0)) AS CREDITS ";
        $query .= "     FROM ";
        if ($model->schdiv == "1") {
            $query .= "         FRESHMAN_DAT SCHREG ";
        } else {
            $query .= "         SCHREG_REGD_DAT SCHREG ";
        }
        $query .= "         INNER JOIN (SELECT ";
        $query .= "                         I_SUB_STD.SCHREGNO, ";
        $query .= "                         I_SUB_STD.CLASSCD, ";
        $query .= "                         I_SUB_STD.SCHOOL_KIND, ";
        $query .= "                         I_SUB_STD.CURRICULUM_CD, ";
        $query .= "                         I_SUB_STD.SUBCLASSCD ";
        $query .= "                     FROM ";
        if ($model->Properties["UnUseSubclassStdSelectRirekiDat"] == "1") {
            $query .= "                         SUBCLASS_STD_SELECT_DAT I_SUB_STD ";
        } else {
            $query .= "                     (SELECT ";
            $query .= "                         RIREKI.* ";
            $query .= "                     FROM ";
            $query .= "                         SUBCLASS_STD_SELECT_RIREKI_DAT RIREKI, ";
            $query .= "                         (SELECT ";
            $query .= "                              MAX_T.YEAR, ";
            $query .= "                              MAX_T.SCHREGNO, ";
            $query .= "                              MAX(MAX_T.RIREKI_CODE) AS RIREKI_CODE ";
            $query .= "                          FROM ";
            $query .= "                              SUBCLASS_STD_SELECT_RIREKI_DAT MAX_T ";
            $query .= "                          WHERE ";
            $query .= "                              MAX_T.YEAR = '".$model->year."' ";
            $query .= "                              AND MAX_T.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= "                          GROUP BY ";
            $query .= "                              MAX_T.YEAR, ";
            $query .= "                              MAX_T.SCHREGNO ";
            $query .= "                         ) RIREKI_MAX ";
            $query .= "                     WHERE ";
            $query .= "                         RIREKI.YEAR = RIREKI_MAX.YEAR ";
            $query .= "                         AND RIREKI.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= "                         AND RIREKI.SCHREGNO = RIREKI_MAX.SCHREGNO ";
            $query .= "                         AND RIREKI.RIREKI_CODE = RIREKI_MAX.RIREKI_CODE ";
            $query .= "                     ) I_SUB_STD ";
        }
        $query .= "                     WHERE ";
        $query .= "                         I_SUB_STD.YEAR = '".$model->year."' ";
        $query .= "                         AND I_SUB_STD.SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= "                     GROUP BY ";
        $query .= "                         I_SUB_STD.SCHREGNO, ";
        $query .= "                         I_SUB_STD.CLASSCD, ";
        $query .= "                         I_SUB_STD.SCHOOL_KIND, ";
        $query .= "                         I_SUB_STD.CURRICULUM_CD, ";
        $query .= "                         I_SUB_STD.SUBCLASSCD ";
        $query .= "                     ) SUB_STD ON SCHREG.SCHREGNO = SUB_STD.SCHREGNO ";
        $query .= "         LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = '".$model->year."' ";
        $query .= "              AND SCHREG.COURSECD      = CRE.COURSECD ";
        $query .= "              AND SCHREG.MAJORCD       = CRE.MAJORCD ";
        $query .= "              AND SCHREG.GRADE         = CRE.GRADE ";
        $query .= "              AND SCHREG.COURSECODE    = CRE.COURSECODE ";
        $query .= "              AND SUB_STD.CLASSCD    = CRE.CLASSCD ";
        $query .= "              AND SUB_STD.SCHOOL_KIND = CRE.SCHOOL_KIND ";
        $query .= "              AND SUB_STD.CURRICULUM_CD = CRE.CURRICULUM_CD ";
        $query .= "              AND SUB_STD.SUBCLASSCD = CRE.SUBCLASSCD ";
        $query .= "     WHERE ";
        if ($model->schdiv == "1") {
            $query .= "         SCHREG.ENTERYEAR = '".$model->year."' ";
        } else {
            $query .= "         SCHREG.YEAR = '".$model->year."' ";
            $query .= "         AND SCHREG.SEMESTER = (SELECT MAX(E1.SEMESTER) FROM SCHREG_REGD_DAT E1 WHERE E1.YEAR = SCHREG.YEAR AND E1.SCHREGNO = SCHREG.SCHREGNO) ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD ";
        $query .= "     ) T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //銀行休業日取得
    function getBankHolidayDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BANK_HOLIDAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' ";

        return $query;
    }

    //入金計画取得
    function getTextBook($model, $schregNo, $textbookDiv) {
        $query  = " SELECT ";
        $query .= "     TEXT_SUB.TEXTBOOKCD, ";
        $query .= "     TEXT_M.TEXTBOOKUNITPRICE ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_SUBCLASS_DAT TEXT_SUB ";
        $query .= "     INNER JOIN TEXTBOOK_MST TEXT_M ON TEXT_SUB.TEXTBOOKCD = TEXT_M.TEXTBOOKCD ";
        $query .= "           AND TEXT_M.TEXTBOOKDIV = '{$textbookDiv}' ";
        $query .= " WHERE ";
        $query .= "     TEXT_SUB.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND TEXT_SUB.YEAR = '{$model->year}' ";
        $query .= "     AND NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         COLLECT_SLIP_TEXTBOOK_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         S1.SCHOOLCD             = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "                         AND S1.SCHOOL_KIND      = '{$model->schoolkind}' ";
        $query .= "                         AND TEXT_SUB.YEAR       = S1.YEAR ";
        $query .= "                         AND TEXT_SUB.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                         AND TEXT_SUB.TEXTBOOKCD = S1.TEXTBOOKCD ";
        $query .= "                     ) ";
        $query .= " ORDER BY ";
        $query .= "     TEXT_SUB.TEXTBOOKCD ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //MAX伝票番号取得
        $slipNoArray = array();
        $result = $db->query(knjp712Query::getMaxSlipNo($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $slipNoArray[$row["SCHREGNO"]] = substr($row["SLIP_NO"],0,12).sprintf("%03d", (substr($row["SLIP_NO"],12) + 1));
        }
        $result->free();

        //パターン情報取得
        $ptn = $db->getRow(knjp712Query::getCollectPattern($model, $fields["COLLECT_PATTERN_CD"]), DB_FETCHMODE_ASSOC);

        //グループ情報取得
        $grpData = array();
        $result = $db->query(knjp712Query::getGrpData($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grpData[$row["COLLECT_L_CD"]."-".$row["COLLECT_M_CD"]] = array("COLLECT_M_MONEY"   => $row["COLLECT_M_MONEY"],
                                                                            "IS_CREDITCNT"      => $row["IS_CREDITCNT"],
                                                                            "DIVIDE_PROCESS"    => $row["DIVIDE_PROCESS"],
                                                                            "TEXTBOOKDIV"       => $row["TEXTBOOKDIV"],
                                                                            "ROUND_DIGIT"       => $row["ROUND_DIGIT"]);
        }
        $result->free();

        //入金計画取得
        $monthGData = array();
        $result = $db->query(knjp712Query::getCollectMonthGrpDat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $monthGData[$row["COLLECT_L_CD"]."-".$row["COLLECT_M_CD"]] = $row;
        }
        $result->free();

        //単位数取得
        $credit = array();
        $result = $db->query(knjp712Query::getCredits($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $credit[$row["SCHREGNO"]] = $row["CREDITS"];
        }
        $result->free();

        //銀行休業日取得
        $holidayArray = array();
        $result = $db->query(knjp712Query::getBankHolidayDat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $holidayArray[] = $row["EXECUTEDATE"];
        }
        $result->free();

        //収納額のまるめ変換用
        $round_digit = array("1" => "1", "2" => "10", "3" => "100");

        //更新
        if (strlen($model->selectdata)) {
            $selectdata = explode(",", $model->selectdata);
            foreach ($selectdata as $schregno) {
                //伝票番号
                $slipNo = ($slipNoArray[$schregno]) ? $slipNoArray[$schregno] : $model->year.$schregno.'001';

                // COLLECT_SLIP_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]          = $model->schoolkind;
                $data["YEAR"][TEXT]                 = $model->year;
                $data["SLIP_NO"][TEXT]              = $slipNo;
                $data["SCHREGNO"][TEXT]             = $schregno;
                $data["PAY_DIV"][TEXT]              = $ptn["PAY_DIV"];
                $data["SLIP_DATE"][TEXT]            = str_replace("/", "-", $fields["SLIP_DATE"]);
                $data["SLIP_STAFFCD"][TEXT]         = STAFFCD;
                $data["COLLECT_GRP_CD"][TEXT]       = $model->collect_grp_cd;
                $data["COLLECT_PATTERN_CD"][TEXT]   = $fields["COLLECT_PATTERN_CD"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_SLIP_DAT");
                $db->query($query);

                foreach ($grpData as $key => $val) {
                    list ($collect_l_cd, $collect_m_cd) = explode("-", $key);
                    $collect_cnt = ($val["IS_CREDITCNT"] == "1") ? ($credit[$schregno] > 0 ? $credit[$schregno] : 0) : 1;
                    $textBookArray = array();
                    $totalTextBookMoney = 0;
                    if ($val["TEXTBOOKDIV"]) {
                        $query = knjp712Query::getTextBook($model, $schregno, $val["TEXTBOOKDIV"]);
                        $result = $db->query($query);
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $textBookArray[$row["TEXTBOOKCD"]] = $row["TEXTBOOKUNITPRICE"];
                            $totalTextBookMoney += $row["TEXTBOOKUNITPRICE"];
                        }
                        $result->free();

                        foreach ($textBookArray as $textBookCd => $textBookMoney) {

                            // COLLECT_SLIP_TEXTBOOK_DAT
                            $data = array();
                            $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                            $data["SCHOOL_KIND"][TEXT]          = $model->schoolkind;
                            $data["YEAR"][TEXT]                 = $model->year;
                            $data["SLIP_NO"][TEXT]              = $slipNo;
                            $data["TEXTBOOKCD"][TEXT]           = $textBookCd;
                            $data["SCHREGNO"][TEXT]             = $schregno;
                            $data["TEXTBOOKUNITPRICE"][NUMBER]  = $textBookMoney;
                            $data["REGISTERCD"][TEXT]           = STAFFCD;
                            $data["UPDATED"][NUMBER]            = "sysdate()";

                            $query = Query::insertSQL($data, "COLLECT_SLIP_TEXTBOOK_DAT");
                            $db->query($query);
                        }
                        $val["COLLECT_M_MONEY"] = $totalTextBookMoney;
                    }

                    // COLLECT_SLIP_M_DAT
                    $data = array();
                    $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]      = $model->schoolkind;
                    $data["YEAR"][TEXT]             = $model->year;
                    $data["SLIP_NO"][TEXT]          = $slipNo;
                    $data["SCHREGNO"][TEXT]         = $schregno;
                    $data["COLLECT_L_CD"][TEXT]     = $collect_l_cd;
                    $data["COLLECT_M_CD"][TEXT]     = $collect_m_cd;
                    $data["COLLECT_MONEY"][NUMBER]  = $val["COLLECT_M_MONEY"];
                    $data["COLLECT_CNT"][NUMBER]    = $collect_cnt;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_SLIP_M_DAT");
                    $db->query($query);

                    if ($monthGData[$key]["MONTH_CNT"]) {
                        //年額
                        $total_money = $val["COLLECT_M_MONEY"] * $collect_cnt;

                        //分割端数：１円未満を繰越、整数分を加算
                        if ($val["DIVIDE_PROCESS"] == "3") {
                            $tukiwari = number_format($total_money / $monthGData[$key]["MONTH_CNT"], 4, '.', '');
                            list ($int, $decimal) = explode(".", $tukiwari);
                            $decimal = "0.".$decimal;
                        } else {
                            $amari = $total_money % ($monthGData[$key]["MONTH_CNT"] * $round_digit[$val["ROUND_DIGIT"]]);
                            $tukiwari = ($total_money - $amari) / $monthGData[$key]["MONTH_CNT"];
                        }

                        $hasuu = $decimal;
                        $counter = 0;
                        $money_sum = 0;
                        for ($i = 4; $i <= 15; $i++) {
                            $month = ($i > 12) ? ($i - 12) : $i;

                            if ($monthGData[$key]["COLLECT_MONTH_".$month] != "1") {
                                $plan_year0 = ($i > 12) ? ($model->year + 1) : $model->year;
                                $plan_month0 = sprintf("%02d", $month);

                                // COLLECT_SLIP_PLAN_M_DAT
                                $data = array();
                                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                                $data["SCHOOL_KIND"][TEXT]      = $model->schoolkind;
                                $data["YEAR"][TEXT]             = $model->year;
                                $data["SCHREGNO"][TEXT]         = $schregno;
                                $data["SLIP_NO"][TEXT]          = $slipNo;
                                $data["COLLECT_L_CD"][TEXT]     = $collect_l_cd;
                                $data["COLLECT_M_CD"][TEXT]     = $collect_m_cd;
                                $data["PLAN_YEAR"][TEXT]        = $plan_year0;
                                $data["PLAN_MONTH"][TEXT]       = $plan_month0;
                                $data["PLAN_MONEY"][NUMBER]     = 0;
                                $data["REGISTERCD"][TEXT]       = STAFFCD;
                                $data["UPDATED"][NUMBER]        = "sysdate()";

                                $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_M_DAT");
                                $db->query($query);
                                continue;
                            }

                            $plan_year = ($i > 12) ? ($model->year + 1) : $model->year;
                            $plan_month = sprintf("%02d", $month);

                            $plan_money = "";

                            if ($val["DIVIDE_PROCESS"] == "1") {
                                //分割端数：初月
                                $plan_money = ($counter == 0) ? ($tukiwari + $amari) : $tukiwari;
                            } else if ($val["DIVIDE_PROCESS"] == "2") {
                                //分割端数：最終月
                                $plan_money = $tukiwari;
                            } else if ($val["DIVIDE_PROCESS"] == "3") {
                                //分割端数：１円未満を繰越、整数分を加算
                                $plan_money = $int + floor($hasuu);
                            }

                            // COLLECT_SLIP_PLAN_M_DAT
                            $data = array();
                            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                            $data["SCHOOL_KIND"][TEXT]      = $model->schoolkind;
                            $data["YEAR"][TEXT]             = $model->year;
                            $data["SCHREGNO"][TEXT]         = $schregno;
                            $data["SLIP_NO"][TEXT]          = $slipNo;
                            $data["COLLECT_L_CD"][TEXT]     = $collect_l_cd;
                            $data["COLLECT_M_CD"][TEXT]     = $collect_m_cd;
                            $data["PLAN_YEAR"][TEXT]        = $plan_year;
                            $data["PLAN_MONTH"][TEXT]       = $plan_month;
                            $data["PLAN_MONEY"][NUMBER]     = $plan_money;
                            $data["REGISTERCD"][TEXT]       = STAFFCD;
                            $data["UPDATED"][NUMBER]        = "sysdate()";

                            $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_M_DAT");
                            $db->query($query);

                            $money_sum += $plan_money;
                            $hasuu -= floor($hasuu);
                            $hasuu += $decimal;

                            $counter++;
                        }

                        if (($val["DIVIDE_PROCESS"] == "2" || ($val["DIVIDE_PROCESS"] == "3" && $total_money != $money_sum)) && $counter > 0) {
                            $plan_money = ($val["DIVIDE_PROCESS"] == "2") ? ($tukiwari + $amari) : ($total_money - $money_sum + $plan_money);

                            $data = array();
                            $data["PLAN_MONEY"][NUMBER]     = $plan_money;
                            $data["REGISTERCD"][TEXT]       = STAFFCD;
                            $data["UPDATED"][FUNC]          = "sysdate()";

                            $where  = " WHERE ";
                            $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' AND ";
                            $where .= "     SCHOOL_KIND     = '".$model->schoolkind."' AND ";
                            $where .= "     YEAR            = '".$model->year."' AND ";
                            $where .= "     SCHREGNO        = '".$schregno."' AND ";
                            $where .= "     SLIP_NO         = '".$slipNo."' AND ";
                            $where .= "     COLLECT_L_CD    = '".$collect_l_cd."' AND ";
                            $where .= "     COLLECT_M_CD    = '".$collect_m_cd."' AND ";
                            $where .= "     PLAN_YEAR       = '".$plan_year."' AND ";
                            $where .= "     PLAN_MONTH      = '".$plan_month."' ";

                            $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                            $db->query($query);
                        }
                    }
                }

                for ($i = 1; $i <= 12; $i++) {
                    $paid_limit_date = $month = "";
                    if (strlen($ptn["COLLECT_MONTH_".$i])) {
                        $month = intval($ptn["COLLECT_MONTH_".$i], 10);
                        $year = ($month <= 3) ? ($model->year + 1) : $model->year;
                        $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year));
                        $day = ($ptn["DIRECT_DEBIT_DATE"] > $lastday) ? $lastday : $ptn["DIRECT_DEBIT_DATE"];

                        $date = $year."-".sprintf("%02d", $month)."-".sprintf("%02d", $day);

                        //土日祝日の場合は翌営業日
                        $paid_limit_date = $date;
                        for ($d = 0; $d < 100; $d++) {
                            $paid_limit_date = date("Y-m-d", strtotime("$date $d day"));
                            //土(6)・日(0)・祝日以外
                            if (!in_array(date('w', strtotime($paid_limit_date)), array(0,6)) && !in_array($paid_limit_date, $holidayArray)) {
                                break;
                            }
                        }
                    }

                    // COLLECT_SLIP_PLAN_LIMITDATE_DAT
                    $data = array();
                    $data["SCHOOLCD"][TEXT]              = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]           = $model->schoolkind;
                    $data["YEAR"][TEXT]                  = $model->year;
                    $data["SCHREGNO"][TEXT]              = $schregno;
                    $data["SLIP_NO"][TEXT]               = $slipNo;
                    $data["PLAN_YEAR"][TEXT]             = ($i <= 3) ? ($model->year + 1) : $model->year;
                    $data["PLAN_MONTH"][TEXT]            = sprintf("%02d", $i);
                    $data["PAID_LIMIT_MONTH"][TEXT]      = ($month) ? sprintf("%02d", $month) : "";
                    $data["PAID_LIMIT_DATE"][TEXT]       = $paid_limit_date;
                    $data["PAID_LIMIT_MONTH_CALC"][TEXT] = ($month) ? sprintf("%02d", $month) : "";
                    $data["PAID_LIMIT_DATE_CALC"][TEXT]  = $paid_limit_date;
                    $data["REGISTERCD"][TEXT]            = STAFFCD;
                    $data["UPDATED"][NUMBER]             = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_LIMITDATE_DAT");
                    $db->query($query);
                }

                //納入月の変更
                //例：4月～9月 11月支払い、10月～03月 01月支払い
                //4月～11月 11月支払いに変更
                //4月～11月 11月支払い、12月～03月 01月支払い
                $query = knjp712Query::getLimit($model, $schregno, $slipNo);
                $result = $db->query($query);
                $limitArray = array();
                $setLimitCnt = 0;
                while ($limitRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($setLimitCnt == 0 && $limitRow["MAX_YM"] < $limitRow["PAID_LIMIT_Y"].$limitRow["PAID_LIMIT_MONTH"]) {
                        $limitArray[] = $limitRow;
                    }
                    if ($setLimitCnt > 0 && $limitRow["MIN_YM"] < $limitRow["PAID_LIMIT_Y"].$limitRow["PAID_LIMIT_MONTH"]) {
                        $limitArray[] = $limitRow;
                    }
                    $setLimitCnt++;
                }
                $result->free();

                if (get_count($limitArray) > 1) {
                    $limitCnt = 1;
                    $befLimit = "";
                    foreach ($limitArray as $limitKey => $limitVal) {
                        $limitYM = $limitVal["PAID_LIMIT_Y"].$limitVal["PAID_LIMIT_MONTH"];
                        $data = array();
                        $data["PAID_LIMIT_MONTH"][TEXT]      = $limitVal["PAID_LIMIT_MONTH"];
                        $data["PAID_LIMIT_DATE"][TEXT]       = $limitVal["PAID_LIMIT_DATE"];
                        $data["REGISTERCD"][TEXT]            = STAFFCD;
                        $data["UPDATED"][NUMBER]             = "sysdate()";

                        $where  = " WHERE ";
                        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
                        $where .= "     AND SCHOOL_KIND     = '".$model->schoolkind."' ";
                        $where .= "     AND YEAR            = '".$model->year."' ";
                        $where .= "     AND SCHREGNO        = '".$schregno."' ";
                        $where .= "     AND SLIP_NO         = '".$slipNo."' ";
                        $where .= "     AND PLAN_YEAR || PLAN_MONTH <= '{$limitYM}' ";
                        if ($befLimit) {
                            $where .= "     AND PLAN_YEAR || PLAN_MONTH > '{$befLimit}' ";
                        }

                        $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_LIMITDATE_DAT", $where);
                        $db->query($query);

                        $befLimit = $limitYM;
                        $limitCnt++;
                        if (get_count($limitArray) <= $limitCnt) {
                            break;
                        }
                    }
                }

                /************************/
                /* 金額を支払月に寄せる */
                /************************/
                $query = knjp712Query::getMonthMoney($model, $schregno, $slipNo);
                $result = $db->query($query);
                $updMonthMoney = array();
                while ($updMMrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $updMonthMoney[] = $updMMrow;
                }
                $result->free();
                //金額クリア
                $data = array();
                $data["PLAN_MONEY"][NUMBER]     = 0;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' AND ";
                $where .= "     SCHOOL_KIND     = '{$model->schoolkind}' AND ";
                $where .= "     YEAR            = '{$model->year}' AND ";
                $where .= "     SCHREGNO        = '{$schregno}' AND ";
                $where .= "     SLIP_NO         = '{$slipNo}' ";

                $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                $db->query($query);

                //支払月の金額を変更
                foreach ($updMonthMoney as $key => $updMoneyArray) {
                    $data = array();
                    $data["PLAN_MONEY"][NUMBER]     = $updMoneyArray["PLAN_MONEY"];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $where  = " WHERE ";
                    $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' AND ";
                    $where .= "     SCHOOL_KIND     = '{$model->schoolkind}' AND ";
                    $where .= "     YEAR            = '{$model->year}' AND ";
                    $where .= "     SCHREGNO        = '{$schregno}' AND ";
                    $where .= "     SLIP_NO         = '{$slipNo}' AND ";
                    $where .= "     COLLECT_L_CD    = '{$updMoneyArray["COLLECT_L_CD"]}' AND ";
                    $where .= "     COLLECT_M_CD    = '{$updMoneyArray["COLLECT_M_CD"]}' AND ";
                    $where .= "     PLAN_YEAR       = '{$updMoneyArray["PLAN_YEAR"]}' AND ";
                    $where .= "     PLAN_MONTH      = '{$updMoneyArray["PLAN_MONTH"]}' ";

                    $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                    $db->query($query);
                }

            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //金額を支払月に寄せる
    function getMonthMoney($model, $schregno, $slipNo) {
        $query .= " SELECT ";
        $query .= "     PLAN_M.SCHOOLCD, ";
        $query .= "     PLAN_M.SCHOOL_KIND, ";
        $query .= "     PLAN_LIMIT.YEAR, ";
        $query .= "     PLAN_M.SCHREGNO, ";
        $query .= "     PLAN_M.SLIP_NO, ";
        $query .= "     PLAN_M.COLLECT_L_CD, ";
        $query .= "     PLAN_M.COLLECT_M_CD, ";
        $query .= "     YEAR(PAID_LIMIT_DATE) AS PLAN_YEAR, ";
        $query .= "     PLAN_LIMIT.PAID_LIMIT_MONTH AS PLAN_MONTH, ";
        $query .= "     PLAN_LIMIT.PAID_LIMIT_DATE, ";
        $query .= "     SUM(PLAN_M.PLAN_MONEY) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "     INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT PLAN_LIMIT ON PLAN_M.SCHOOLCD = PLAN_LIMIT.SCHOOLCD ";
        $query .= "           AND PLAN_M.SCHOOL_KIND = PLAN_LIMIT.SCHOOL_KIND ";
        $query .= "           AND PLAN_M.YEAR = PLAN_LIMIT.YEAR ";
        $query .= "           AND PLAN_M.SCHREGNO = PLAN_LIMIT.SCHREGNO ";
        $query .= "           AND PLAN_M.SLIP_NO = PLAN_LIMIT.SLIP_NO ";
        $query .= "           AND PLAN_M.PLAN_YEAR = PLAN_LIMIT.PLAN_YEAR ";
        $query .= "           AND PLAN_M.PLAN_MONTH = PLAN_LIMIT.PLAN_MONTH ";
        $query .= "           AND PLAN_LIMIT.PAID_LIMIT_MONTH IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "     PLAN_M.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND PLAN_M.SCHOOL_KIND = '{$model->schoolkind}' ";
        $query .= "     AND PLAN_M.YEAR = '{$model->year}' ";
        $query .= "     AND PLAN_M.SCHREGNO = '{$schregno}' ";
        $query .= "     AND PLAN_M.SLIP_NO = '{$slipNo}' ";
        $query .= " GROUP BY ";
        $query .= "     PLAN_M.SCHOOLCD, ";
        $query .= "     PLAN_M.SCHOOL_KIND, ";
        $query .= "     PLAN_LIMIT.YEAR, ";
        $query .= "     PLAN_M.SCHREGNO, ";
        $query .= "     PLAN_M.SLIP_NO, ";
        $query .= "     PLAN_M.COLLECT_L_CD, ";
        $query .= "     PLAN_M.COLLECT_M_CD, ";
        $query .= "     YEAR(PAID_LIMIT_DATE), ";
        $query .= "     PLAN_LIMIT.PAID_LIMIT_MONTH, ";
        $query .= "     PLAN_LIMIT.PAID_LIMIT_DATE ";

        return $query;
    }

    //支払期限取得
    function getLimit($model, $schregno, $slipNo) {

        $query  = " SELECT ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     SLIP_NO, ";
        $query .= "     MIN(PLAN_YEAR || PLAN_MONTH) AS MIN_YM, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS MAX_YM, ";
        $query .= "     YEAR(PAID_LIMIT_DATE) AS PAID_LIMIT_Y, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     PAID_LIMIT_DATE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_LIMITDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolkind}' ";
        $query .= "     AND YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND SLIP_NO = '{$slipNo}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     SLIP_NO, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     PAID_LIMIT_DATE ";
        $query .= " ORDER BY ";
        $query .= "     PAID_LIMIT_DATE ";

        return $query;
    }

    //削除処理
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        //対象テーブル
        $tableArray = array();
        $tableArray[] = "COLLECT_SLIP_DAT";
        $tableArray[] = "COLLECT_SLIP_M_DAT";
        $tableArray[] = "COLLECT_SLIP_PLAN_LIMITDATE_DAT";
        $tableArray[] = "COLLECT_SLIP_PLAN_M_DAT";
        $tableArray[] = "COLLECT_SLIP_TEXTBOOK_DAT";

        if (get_count($model->checked) > 0) {
            foreach ($model->checked as $slip_no) {
                foreach ($tableArray as $table) {
                    $query  = " DELETE FROM ";
                    $query .=       $table;
                    $query .= " WHERE ";
                    $query .= "     SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' AND ";
                    $query .= "     SCHOOL_KIND     = '".$model->schoolkind."' AND ";
                    $query .= "     YEAR            = '".$model->year."' AND ";
                    $query .= "     SLIP_NO         = '".$slip_no."'  ";
                    $db->query($query);
                }
            }
        }

        Query::dbCheckIn($db);
        return true;
    }
}
?>
