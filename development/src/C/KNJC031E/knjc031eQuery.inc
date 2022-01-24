<?php

require_once('for_php7.php');

class knjc031eQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJC031E' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSemesterName($year, $semester) {
        $query .= " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " AND SEMESTER = '{$semester}' ";

        return $query;
    }

    /* 学級名称取得 */
    function selectHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     grade, hr_class, hr_name ";
        $query .= " FROM ";
        $query .= "     schreg_regd_hdat ";
        $query .= " WHERE ";
        $query .= "     year = '".CTRL_YEAR."' AND";
        $query .= "     semester = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "              ) ";
        }
        $query .= "   ORDER BY grade, hr_class ";

        return $query;
    }

    //複式学級取得
    function getGroupHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GHR_CD AS VALUE, ";
        $query .= "     GHR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータ作成
    function selectSemesAll($seme = "")
    {
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

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$model->field["grade"]."' ";

        return $query;
    }

    /* 対象月データの取得 */
    function selectMonthQuery($month, $model, $schoolKind) {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && $schoolKind) {
            $setNameCd = "Z".$schoolKind."05";
        }

        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$setNameCd}' ";
        $query .= "     AND NAMECD2 IN (SELECT ";
        $query .= "                         CONTROL_CODE ";
        $query .= "                     FROM ";
        $query .= "                         ADMIN_CONTROL_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            if ($schoolKind) {
                $query .= "                     AND SCHOOL_KIND = '".$schoolKind."' ";
            } else {
                $query .= "                     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "                         AND CONTROL_FLG = '2') ";
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

    //欠課種別取得
    function getSickDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND NAMECD1 = 'C001' ";
        $query .= "     AND NAMECD2 IN ('4', '5', '6') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //詳細テーブル用種別
    function getDetailDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'C002' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //締め日の取得
    function getAppointedDay($tuki, $gakki, $model) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST AM";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT SGD";
            $query .= "     ON";
            $query .= "     AM.YEAR         = SGD.YEAR AND ";
            $query .= "     AM.SCHOOL_KIND  = SGD.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     AM.YEAR         = '". CTRL_YEAR ."' AND ";
        $query .= "     AM.MONTH        = '{$tuki}' AND ";
        $query .= "     AM.SEMESTER     = '{$gakki}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            if ($model->field["SELECT_CLASS_TYPE"] == 1) {
                $query .= "     AND SGD.GRADE       = '{$model->field["grade"]}' ";
            }
        }

        return $query;
    }

    //C001
    function getC001() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //A004
    function getA004() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A004' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学校マスタ取得
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //異動データ取得
    function getIdouData($schregno, $date) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRD_DIV IN ('1', '2', '3', '6', '7') AND ";
        $query .= "     GRD_DATE < '".$date."' ";

        return $query;
    }

    //異動データ（留学・休学）取得
    function getTransferData1($schregno, $date) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     '".$date."' BETWEEN TRANSFER_SDATE AND CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END ";

        return $query;
    }

    //異動日付（留学・休学）取得
    function getTransferData2($schregno, $month) {

        $query  = " SELECT ";
        if($month == "s") {
            $query .= "     MONTH(TRANSFER_SDATE) AS S_MONTH ";
        } else {
            $query .= "     MONTH(TRANSFER_EDATE) AS E_MONTH ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     TRANSFER_SDATE BETWEEN '".CTRL_YEAR."-04-01' AND '".(CTRL_YEAR+1)."-03-31' ";

        return $query;
    }

    /* 出欠月別累積データ・出欠累積データ・出欠科目別累積データ・学籍異動データ */
    function selectAttendQuery($model, $schoolMst)
    {
        $monthsem = array();
        $monthsem = preg_split("/-/", $model->field["month"]);
        //累積期間月を配列にする。2004/08/27 arakaki
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

        $query  = " SELECT ";
        $query .= "     sd.schregno, ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     S1.GHR_ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "     sd.ATTENDNO, ";
        }
        $query .= "     sd.GRADE, ";
        $query .= "     sd.COURSECD, ";
        $query .= "     sd.MAJORCD, ";
        $query .= "     SG.SCHOOL_KIND, ";
        $query .= "     sm.NAME_SHOW, ";
        $query .= "     am.APPOINTED_DAY, ";
        $query .= "     ad.LESSON, ";
        $query .= "     ad.OFFDAYS, ";
        $query .= "     ad.ABROAD, ";
        $query .= "     ad.ABSENT, ";
        $query .= "     ad.suspend, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     ad.KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     ad.VIRUS, ";
        }
        $query .= "     ad.mourning, ";
        $query .= "     VALUE(ad.LESSON, 0) - VALUE(ad.suspend, 0) - VALUE(ad.mourning, 0) - VALUE(ad.ABROAD, 0) - VALUE(ad.OFFDAYS, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(ad.KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(ad.VIRUS, 0) ";
        }
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(ad.OFFDAYS, 0) ";
        }
        $query .= "     AS classdays2, ";
        $query .= "     ad.sick, ";
        $query .= "     ad.NOTICE, ";
        $query .= "     ad.NONOTICE, ";
        $query .= "     VALUE(ad.LESSON, 0) - VALUE(ad.suspend, 0) - VALUE(ad.mourning, 0) - VALUE(ad.ABROAD, 0) - VALUE(ad.OFFDAYS, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(ad.KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(ad.VIRUS, 0) ";
        }
        $query .= "               - VALUE(ad.sick, 0) - VALUE(ad.NOTICE, 0) - VALUE(ad.NONOTICE, 0) ";
        $query .= "     AS classdays3, ";
        $query .= "     ad.late, ";
        $query .= "     ad.early, ";
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $query .= "     L1.CNT AS DETAIL_001, ";
            $query .= "     L2.CNT AS DETAIL_002, ";
            $query .= "     L3.CNT AS DETAIL_003, ";
            $query .= "     L4.CNT AS DETAIL_004, ";
            $query .= "     L101.CNT_DECIMAL AS DETAIL_101, ";
        } else {
            $query .= "     ad.REIHAI_KEKKA AS DETAIL_001, ";
            $query .= "     ad.M_KEKKA_JISU AS DETAIL_002, ";
            $query .= "     ad.REIHAI_TIKOKU AS DETAIL_003, ";
            $query .= "     ad.JYUGYOU_TIKOKU AS DETAIL_004, ";
            $query .= "     ad.JYUGYOU_JISU_DECIMAL AS DETAIL_101, ";
        }
        $query .= "     sumad.sum_classdays, ";
        $query .= "     sumad.SUM_SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     sumad.SUM_KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     sumad.SUM_VIRUS, ";
        }
        $query .= "     sumad.sum_mourning, ";
        $query .= "     VALUE(sumad.sum_classdays, 0) - VALUE(sumad.sum_suspend, 0) - VALUE(sumad.sum_mourning, 0) - VALUE(sumad.SUM_OFFDAYS, 0) - VALUE(sumad.SUM_ABROAD, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(sumad.SUM_KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(sumad.SUM_VIRUS, 0) ";
        }
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(sumad.SUM_OFFDAYS, 0) ";
        }
        $query .= "     AS sum_classdays2, ";
        $query .= "     (VALUE(sumad.sum_sick, 0) + VALUE(sumad.SUM_NOTICE, 0) + VALUE(sumad.SUM_NONOTICE, 0) ";
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(sumad.SUM_OFFDAYS, 0) ";
        }
        $query .= "     ) AS SUM_SICK, ";
        $query .= "     ((VALUE(sumad.sum_classdays, 0) - (VALUE(sumad.sum_suspend, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     + VALUE(sumad.SUM_KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     + VALUE(sumad.SUM_VIRUS, 0) ";
        }
        $query .= "      + VALUE(sumad.sum_mourning, 0) + VALUE(sumad.SUM_OFFDAYS, 0) + VALUE(sumad.SUM_ABROAD, 0))) - (VALUE(sumad.sum_sick, 0) + VALUE(sumad.SUM_NOTICE, 0) + VALUE(sumad.SUM_NONOTICE, 0))) AS sum_classdays3, ";
        $query .= "     sumad.sum_late, ";
        $query .= "     sumad.sum_early, ";
        $query .= "     sumad.SUM_DETAIL_001, ";
        $query .= "     sumad.SUM_DETAIL_002, ";
        $query .= "     sumad.SUM_DETAIL_003, ";
        $query .= "     sumad.SUM_DETAIL_004, ";
        $query .= "     sumad.SUM_DETAIL_101, ";
        $query .= "     RMK.REMARK1 AS REMARK, ";
        $query .= "     ad.schregno AS SEM_SCHREGNO ";
        $query .= " FROM ";
        $query .= "     schreg_regd_dat sd ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT SG ON sd.YEAR = SG.YEAR ";
        $query .= "      AND sd.GRADE = SG.GRADE ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     schreg_base_mst sm ";
        $query .= " ON ";
        $query .= "     sd.schregno = sm.schregno ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         * ";
        $query .= "      FROM ";
        $query .= "         V_ATTEND_SEMES_DAT ";
        $query .= "      WHERE ";
        $query .= "         year = '".CTRL_YEAR."' ";
        $query .= "         AND month = '".$monthsem[0]."' ";
        $query .= "         AND SEMESTER = '".$monthsem[1]."' ) AS ad ";
        $query .= " ON ";
        $query .= "     ad.schregno = sd.schregno ";
        
        $query .= " LEFT OUTER JOIN ";
        $query .= "     APPOINTED_DAY_MST AM ";
        $query .= " ON ";
        $query .= "       AD.YEAR         = AM.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SG.SCHOOL_KIND = AM.SCHOOL_KIND ";
        }
        $query .= "   AND AD.MONTH        = AM.MONTH ";
        $query .= "   AND AD.SEMESTER     = AM.SEMESTER ";

        $query .= " LEFT JOIN ATTEND_SEMES_DETAIL_DAT L1 ";
        $query .= "      ON ad.COPYCD   = L1.COPYCD ";
        $query .= "     AND ad.YEAR     = L1.YEAR ";
        $query .= "     AND ad.MONTH    = L1.MONTH ";
        $query .= "     AND ad.SEMESTER = L1.SEMESTER ";
        $query .= "     AND ad.SCHREGNO = L1.SCHREGNO ";
        $query .= "     AND L1.SEQ      = '001' ";
        $query .= " LEFT JOIN ATTEND_SEMES_DETAIL_DAT L2 ";
        $query .= "      ON ad.COPYCD   = L2.COPYCD ";
        $query .= "     AND ad.YEAR     = L2.YEAR ";
        $query .= "     AND ad.MONTH    = L2.MONTH ";
        $query .= "     AND ad.SEMESTER = L2.SEMESTER ";
        $query .= "     AND ad.SCHREGNO = L2.SCHREGNO ";
        $query .= "     AND L2.SEQ      = '002' ";
        $query .= " LEFT JOIN ATTEND_SEMES_DETAIL_DAT L3 ";
        $query .= "      ON ad.COPYCD   = L3.COPYCD ";
        $query .= "     AND ad.YEAR     = L3.YEAR ";
        $query .= "     AND ad.MONTH    = L3.MONTH ";
        $query .= "     AND ad.SEMESTER = L3.SEMESTER ";
        $query .= "     AND ad.SCHREGNO = L3.SCHREGNO ";
        $query .= "     AND L3.SEQ      = '003' ";
        $query .= " LEFT JOIN ATTEND_SEMES_DETAIL_DAT L4 ";
        $query .= "      ON ad.COPYCD   = L4.COPYCD ";
        $query .= "     AND ad.YEAR     = L4.YEAR ";
        $query .= "     AND ad.MONTH    = L4.MONTH ";
        $query .= "     AND ad.SEMESTER = L4.SEMESTER ";
        $query .= "     AND ad.SCHREGNO = L4.SCHREGNO ";
        $query .= "     AND L4.SEQ      = '004' ";
        $query .= " LEFT JOIN ATTEND_SEMES_DETAIL_DAT L101 ";
        $query .= "      ON ad.COPYCD   = L101.COPYCD ";
        $query .= "     AND ad.YEAR     = L101.YEAR ";
        $query .= "     AND ad.MONTH    = L101.MONTH ";
        $query .= "     AND ad.SEMESTER = L101.SEMESTER ";
        $query .= "     AND ad.SCHREGNO = L101.SCHREGNO ";
        $query .= "     AND L101.SEQ    = '101' ";

        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(LESSON) AS SUM_CLASSDAYS, ";
        $query .= "         SUM(OFFDAYS) AS SUM_OFFDAYS, ";
        $query .= "         SUM(ABROAD) AS SUM_ABROAD, ";
        $query .= "         SUM(SUSPEND) AS SUM_SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(KOUDOME) AS SUM_KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VIRUS) AS SUM_VIRUS, ";
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
        $query .= "         SUM(JYUGYOU_JISU_DECIMAL) AS SUM_DETAIL_101 ";
        $query .= "      FROM ";
        $query .= "         V_ATTEND_SEMES_DAT ";
        $query .= "      WHERE ";
        $query .= "            YEAR = '".CTRL_YEAR."' ";
        if($monthsem[0] != "" && $monthsem[0] != NULL){
            $query .= "        AND MONTH IN( ".$range_month[$monthsem[0]].")";    //2004/08/27 arakaki
            $query .= "        AND SEMESTER <= '".$monthsem[1]."' ";
        }
        $query .= "      GROUP BY ";
        $query .= "         SCHREGNO) AS sumad ";
        $query .= " ON ";
        $query .= "     sumad.schregno = sd.schregno ";

        $query .= " LEFT JOIN ATTEND_SEMES_REMARK_DAT RMK ON sd.YEAR = RMK.YEAR ";
        $query .= "      AND sd.SEMESTER = RMK.SEMESTER ";
        $query .= "      AND RMK.MONTH = '".$monthsem[0]."' ";
        $query .= "      AND sd.SCHREGNO = RMK.SCHREGNO ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= " INNER JOIN SCHREG_REGD_GHR_DAT S1 ON sd.YEAR     = S1.YEAR ";
            $query .= "                                  AND sd.SEMESTER = S1.SEMESTER ";
            $query .= "                                  AND sd.SCHREGNO = S1.SCHREGNO ";
        }

        $query .= " WHERE ";
        $query .= "     sd.year = '".CTRL_YEAR."' AND ";
        
        #2005/05/11 attend_semes_datにデータが存在しない場合でも、表示可能へ変更
        if($monthsem[0] != "" && $monthsem[0] != NULL){
                $query .= "     sd.semester = '".$monthsem[1]."' AND";
        }else{
                $query .= "     sd.semester IS NULL  AND";
        }
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     S1.GHR_CD   = '".$model->field["GROUP_HR_CLASS"]."' ";
        } else {
            $query .= "     sd.grade = '".$model->field["grade"]."' AND ";
            $query .= "     sd.hr_class = '".$model->field["class"]."' ";
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
        $query .= "         NAME NOT IN ('APPOINTED_DAY', 'LESSON', 'REGISTERCD', 'UPDATED') ";
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

    /* 出欠月別累積データの更新 */
    function getUpdateQuery($model)
    {
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
        $query = knjc031eQuery::getFieldList();
        $table_field = array();
        $table_field = $db->getCol($query);

        foreach($model->field["SCHREGNO"] as $key => $schregno){
            /* 更新対象データが存在するかをチェック */
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     attend_semes_dat ";
            $query .= " WHERE ";
            $query .= "     year = '".CTRL_YEAR."' AND ";
            $query .= "     month = '".$monthsem[0]."' AND ";
            $query .= "     SEMESTER = '".$monthsem[1]."' AND ";
            $query .= "     schregno = '".$schregno."' ";

            $flag = $db->getOne($query);

            $data = array();

            /* 更新対象データがある時 - UPDATE */
            if($flag){
                $where  = " WHERE ";
                $where .= "     year = '".CTRL_YEAR."' AND ";
                $where .= "     month = '".$monthsem[0]."' AND ";
                $where .= "     SEMESTER = '".$monthsem[1]."' AND ";
                $where .= "     schregno = '".$schregno."' ";
            }
            /* 更新対象データがない時 - INSERT */
            else{
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["MONTH"][TEXT]    = $monthsem[0];
                $data["SEMESTER"][TEXT] = $monthsem[1];
                $data["SCHREGNO"][TEXT] = $schregno;
            }

            /* 更新データをセット */
            $data["APPOINTED_DAY"][TEXT] = $model->appointed_day;              //締め日
            $data["LESSON"][NUMBER]      = ($model->field["LESSON"][$key])  ?  $model->field["LESSON"][$key] : 0;      //授業日数

            foreach ($table_field as $fld) {
                if (in_array($fld, $model->a004)) {
                    foreach ($model->a004 as $keyA004 => $val) {
                        if ($val) {
                            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                                $data[$val][NUMBER] = $model->field[$val][$key];
                            } else {
                                $data[$val][NUMBER] = ($model->field[$val][$key]) ?  $model->field[$val][$key] : 0;
                            }
                        }
                    }
                } else if (in_array($fld, $model->c001)) {
                    foreach ($model->c001 as $keyC001 => $val) {
                        if ($val) {
                            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                                $data[$val][NUMBER] = $model->field[$val][$key];
                            } else {
                                $data[$val][NUMBER] = ($model->field[$val][$key]) ?  $model->field[$val][$key] : 0;
                            }
                        }
                    }
                } else {
                    if ($fld == $val) {
                        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                            $data[$fld][NUMBER] = $model->field[$val][$key];
                        } else {
                            $data[$fld][NUMBER] = ($model->field[$fld][$key]) ?  $model->field[$val][$key] : 0;
                        }
                    } else {
                        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                            $data[$fld][NUMBER] = "";
                        } else {
                            $data[$fld][NUMBER] = 0;
                        }
                    }
                }
            }

            $data["REGISTERCD"][TEXT]    = STAFFCD ;                           //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()";                        //更新日付
            /* 更新対象データがある時 - UPDATE */
            if($flag){
                $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
            }
            /* 更新対象データがない時 - INSERT */
            else{
                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
            }
            $db->query($query);

            /********備考*********/
            /* 更新対象データが存在するかをチェック */
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ATTEND_SEMES_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     MONTH = '".$monthsem[0]."' AND ";
            $query .= "     SEMESTER = '".$monthsem[1]."' AND ";
            $query .= "     SCHREGNO = '".$schregno."' ";

            $flag = $db->getOne($query);

            $data = array();

            /* 更新対象データがある時 - UPDATE */
            if ($flag) {
                $where  = " WHERE ";
                $where .= "     year = '".CTRL_YEAR."' AND ";
                $where .= "     month = '".$monthsem[0]."' AND ";
                $where .= "     SEMESTER = '".$monthsem[1]."' AND ";
                $where .= "     schregno = '".$schregno."' ";
            } else{
            /* 更新対象データがない時 - INSERT */
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["MONTH"][TEXT]    = $monthsem[0];
                $data["SEMESTER"][TEXT] = $monthsem[1];
                $data["SCHREGNO"][TEXT] = $schregno;
            }

            /* 更新データをセット */
            $data["REMARK1"][TEXT]      = $model->field["REMARK"][$key];
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            /* 更新対象データがある時 - UPDATE */
            if ($flag) {
                $query = Query::updateSQL($data, "ATTEND_SEMES_REMARK_DAT", $where);
            } else{
            /* 更新対象データがない時 - INSERT */
                $query = Query::insertSQL($data, "ATTEND_SEMES_REMARK_DAT");
            }
            $db->query($query);

            foreach ($model->c002 as $keyC002 => $val) {
                if (strlen($val) > 0) {
                    /* 更新対象データが存在するかをチェック */
                    $query  = " SELECT ";
                    $query .= "     COUNT(*) ";
                    $query .= " FROM ";
                    $query .= "     ATTEND_SEMES_DETAIL_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR = '".CTRL_YEAR."' ";
                    $query .= "     AND MONTH = '".$monthsem[0]."' ";
                    $query .= "     AND SEMESTER = '".$monthsem[1]."' ";
                    $query .= "     AND SCHREGNO = '".$schregno."' ";
                    $query .= "     AND SEQ = '".$keyC002."' ";

                    $flag = $db->getOne($query);

                    $data = array();

                    /* 更新対象データがある時 - UPDATE */
                    if ($flag) {
                        $where  = " WHERE ";
                        $where .= "     YEAR = '".CTRL_YEAR."' ";
                        $where .= "     AND MONTH = '".$monthsem[0]."' ";
                        $where .= "     AND SEMESTER = '".$monthsem[1]."' ";
                        $where .= "     AND SCHREGNO = '".$schregno."' ";
                        $where .= "     AND SEQ = '".$keyC002."' ";
                    } else {
                        $data["COPYCD"][TEXT]   = "0";
                        $data["YEAR"][TEXT]     = CTRL_YEAR;
                        $data["MONTH"][TEXT]    = $monthsem[0];
                        $data["SEMESTER"][TEXT] = $monthsem[1];
                        $data["SCHREGNO"][TEXT] = $schregno;
                        $data["SEQ"][TEXT]      = $keyC002;
                    }

                    /* 更新データをセット */
                    if ($keyC002 == "101") {
                        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                            $data["CNT_DECIMAL"][NUMBER] = $model->field[$val][$key];
                        } else {
                            $data["CNT_DECIMAL"][NUMBER] = ($model->field[$val][$key])  ?  $model->field[$val][$key] : 0.0;
                        }
                    } else {
                        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                            $data["CNT"][NUMBER]        = $model->field[$val][$key];
                        } else {
                            $data["CNT"][NUMBER]        = ($model->field[$val][$key])  ?  $model->field[$val][$key] : 0;
                        }
                    }
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    /* 更新対象データがある時 - UPDATE */
                    if($flag){
                        $query = Query::updateSQL($data, "ATTEND_SEMES_DETAIL_DAT", $where);
                    }
                    /* 更新対象データがない時 - INSERT */
                    else{
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
