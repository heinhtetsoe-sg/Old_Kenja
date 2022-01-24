<?php

require_once('for_php7.php');

class knjc031kQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJC031K' ";
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

    //校種取得
    function getSchoolKind($model) {
        $query  = "  SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_GDAT GDAT ";
        $query .= "  WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$model->field["grade"]."' ";

        return $query;
    }

    /* 学級名称取得 */
    function selectHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.grade, T1.hr_class, T1.hr_name ";
        $query .= " FROM ";
        $query .= "     schreg_regd_hdat T1 ";
        $query .= " WHERE ";
        $query .= "     T1.year = '".CTRL_YEAR."' AND";
        $query .= "     T1.semester = '".CTRL_SEMESTER."' ";
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND (TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR AND ";
            $query .= "                     S1.SCHOOL_KIND  IN ('".implode("','", explode(":", $model->selectSchoolKind))."') ";
            $query .= "                 ) ";

        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR AND ";
            $query .= "                     S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

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

    /* 対象月データの取得 */
    function selectMonthQuery($month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $kind = ($model->school_kind) ? $model->school_kind : SCHOOLKIND;
            $setNameCd = "Z".$kind."05";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     namecd2, name1, namespare1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year  = '".CTRL_YEAR."' ";
        $query .= "     AND namecd1 = '{$setNameCd}' ";
        $query .= "     AND namecd2 IN (SELECT CONTROL_CODE FROM ADMIN_CONTROL_DAT WHERE YEAR='".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG ='2') ";
        $query .= "     AND namecd2 = '".sprintf('%02d',$month)."' ";
        $query .= " ORDER BY ";
        $query .= "     namespare1 ";

        return $query;
    }

    //ATTEND_SEMES_DATのMAX(LESSON)取得
    function getMaxLesson1($model) {
        $query  = " SELECT ";
        $query .= "     MAX(T2.LESSON) AS LESSON ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     ATTEND_SEMES_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T2.MONTH || '-' || T2.SEMESTER = '".$model->field["month"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.GRADE    = '".$model->field["grade"]."' AND ";
        $query .= "     T1.HR_CLASS = '".$model->field["class"]."' AND ";
        $query .= "     T2.COPYCD   = '0' ";

        return $query;
    }

    //ATTEND_LESSON_MSTのMAX(LESSON)取得
    function getMaxLesson2($model) {
        $query  = " WITH LESSON_LIST AS ( ";
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
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T2.LESSON ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         ATTEND_LESSON_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T2.MONTH || '-' || T2.SEMESTER = '".$model->field["month"]."' AND ";
        $query .= "         T1.GRADE    = T2.GRADE AND ";
        $query .= "         T1.GRADE    = '".$model->field["grade"]."' AND ";
        $query .= "         T1.HR_CLASS = '".$model->field["class"]."' AND ";
        $query .= "         T1.COURSECD = T2.COURSECD AND ";
        $query .= "         T1.MAJORCD  = T2.MAJORCD AND ";
        $query .= "         T1.GRADE    = T2.GRADE ";
        $query .= " ) ";

        $query .= " SELECT MAX(LESSON) FROM LESSON_LIST ";

        return $query;
    }

    //欠課種別取得
    function getSickDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'C001' ";
        $query .= "     AND NAMECD2 IN ('4', '5', '6') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //締め日の取得
    function getAppointedDay($tuki, $gakki, $model) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST AM ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT SGD ";
            $query .= "     ON ";
            $query .= "     AM.YEAR = SGD.YEAR AND ";
            $query .= "     AM.SCHOOL_KIND = SGD.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     AM.YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND AM.MONTH = '{$tuki}' ";
        $query .= "     AND AM.SEMESTER = '{$gakki}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SGD.GRADE = '{$model->field["grade"]}' ";
        }

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
        $query .= "     GRD_DIV IN ('1', '2', '3', '6') AND ";
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
        $query .= "     sd.attendno, ";
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
        $query .= "         attend_semes_dat ";
        $query .= "      WHERE ";
        $query .= "         year = '".CTRL_YEAR."' ";
        $query .= "         AND month = '".$monthsem[0]."' ";
        $query .= "         AND SEMESTER = '".$monthsem[1]."' ) AS ad ";
        $query .= " ON ";
        $query .= "     ad.schregno = sd.schregno ";
        
        $query .= " LEFT OUTER JOIN ";
        $query .= "     appointed_day_mst am ";
        $query .= " ON ";
        $query .= "       ad.year = am.year ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND sg.SCHOOL_KIND = am.SCHOOL_KIND ";
        }
        $query .= "   AND ad.month = am.month ";
        $query .= "   AND ad.SEMESTER = am.semester ";

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
        $query .= "         SUM(SICK) AS SUM_SICK, ";
        $query .= "         SUM(NOTICE) AS SUM_NOTICE, ";
        $query .= "         SUM(NONOTICE) AS SUM_NONOTICE, ";
        $query .= "         SUM(LATE) AS SUM_LATE, ";
        $query .= "         SUM(EARLY) AS SUM_EARLY ";
        $query .= "      FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
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

        $query .= " WHERE ";
        $query .= "     sd.year = '".CTRL_YEAR."' AND ";
        
        #2005/05/11 attend_semes_datにデータが存在しない場合でも、表示可能へ変更
        if($monthsem[0] != "" && $monthsem[0] != NULL){
                $query .= "     sd.semester = '".$monthsem[1]."' AND";
        }else{
                $query .= "     sd.semester IS NULL  AND";
        }

        $query .= "     sd.grade = '".$model->field["grade"]."' AND ";
        $query .= "     sd.hr_class = '".$model->field["class"]."' ";
        $query .= " ORDER BY ";
        $query .= "     sd.attendno ";
        return $query;
    }

    /* 出欠月別累積データの更新 */
    function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $monthsem = array();
        $monthsem = preg_split("/-/", $model->field["month"]);

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
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                $data["OFFDAYS"][NUMBER]     = $model->field["OFFDAYS"][$key];      //休学日数
                $data["ABROAD"][NUMBER]      = $model->field["ABROAD"][$key];       //留学日数
                $data["ABSENT"][NUMBER]      = $model->field["ABSENT"][$key];       //公欠日数
                $data["SUSPEND"][NUMBER]     = $model->field["SUSPEND"][$key];      //出停日数
                if ($model->Properties["useKoudome"] == "true") {
                    $data["KOUDOME"][NUMBER]     = $model->field["KOUDOME"][$key];      //交止
                }
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]       = $model->field["VIRUS"][$key];        //伝染病
                }
                $data["MOURNING"][NUMBER]    = $model->field["MOURNING"][$key];     //忌引日数
                $data["SICK"][NUMBER]        = $model->field["SICK"][$key];         //欠席
                $data["NOTICE"][NUMBER]      = $model->field["NOTICE"][$key];       //事故欠届
                $data["NONOTICE"][NUMBER]    = $model->field["NONOTICE"][$key];     //事故欠無
                $data["LATE"][NUMBER]        = $model->field["LATE"][$key];         //遅刻回数
                $data["EARLY"][NUMBER]       = $model->field["EARLY"][$key];        //早退回数
            } else {
                $data["OFFDAYS"][NUMBER]     = ($model->field["OFFDAYS"][$key]) ?  $model->field["OFFDAYS"][$key] : 0;     //休学日数
                $data["ABROAD"][NUMBER]      = ($model->field["ABROAD"][$key]) ?   $model->field["ABROAD"][$key] : 0;      //留学日数
                $data["ABSENT"][NUMBER]      = ($model->field["ABSENT"][$key]) ?   $model->field["ABSENT"][$key] : 0;      //公欠日数
                $data["SUSPEND"][NUMBER]     = ($model->field["SUSPEND"][$key]) ?  $model->field["SUSPEND"][$key] : 0;     //出停日数
                if ($model->Properties["useKekkaJisu"] == "true") {
                    $data["KEKKA_JISU"][NUMBER] = 0; //使用しない
                }
                if ($model->Properties["useKekka"] == "true") {
                    $data["KEKKA"][NUMBER]      = 0; //使用しない
                }
                if ($model->Properties["useLatedetail"] == "true") {
                    $data["LATEDETAIL"][NUMBER] = 0; //使用しない
                }
                if ($model->Properties["useKoudome"] == "true") {
                    $data["KOUDOME"][NUMBER]     = ($model->field["KOUDOME"][$key]) ?  $model->field["KOUDOME"][$key] : 0;   //交止
                }
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]       = ($model->field["VIRUS"][$key]) ?    $model->field["VIRUS"][$key] : 0;     //伝染病
                }
                $data["MOURNING"][NUMBER]    = ($model->field["MOURNING"][$key]) ? $model->field["MOURNING"][$key] : 0;    //忌引日数
                $data["SICK"][NUMBER]        = ($model->field["SICK"][$key]) ?     $model->field["SICK"][$key] : 0;        //欠席
                $data["NOTICE"][NUMBER]      = ($model->field["NOTICE"][$key]) ?   $model->field["NOTICE"][$key] : 0;      //事故欠届
                $data["NONOTICE"][NUMBER]    = ($model->field["NONOTICE"][$key]) ? $model->field["NONOTICE"][$key] : 0;    //事故欠無
                $data["LATE"][NUMBER]        = ($model->field["LATE"][$key]) ?     $model->field["LATE"][$key] : 0;        //遅刻回数
                $data["EARLY"][NUMBER]       = ($model->field["EARLY"][$key]) ?    $model->field["EARLY"][$key] : 0;       //早退回数
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
        }

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

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
