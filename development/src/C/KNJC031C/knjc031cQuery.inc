<?php

require_once('for_php7.php');

class knjc031cQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJC031C' ";
        $query .= "     AND INVALID_FLG = '0' ";

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
        $query .= "     T1.grade, T1.hr_class, T1.hr_name ";
        $query .= " FROM ";
        $query .= "     schreg_regd_hdat T1 ";
        $query .= " WHERE ";
        $query .= "     T1.year = '".CTRL_YEAR."' AND";
        $query .= "     T1.semester = '".CTRL_SEMESTER."' ";
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD3 = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "   AND GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   AND GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
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
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     SCHOOL_KIND = '".$model->school_kind."' AND ";
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

    //欠課種別取得
    function getSickDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
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
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND = '". $model->school_kind ."' AND ";
        }
        $query .= "     MONTH = '{$tuki}' AND ";
        $query .= "     SEMESTER = '{$gakki}' ";

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
        $query .= "     sm.name_show, ";
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
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(ad.VIRUS, 0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(ad.KOUDOME, 0) ";
        }
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(ad.OFFDAYS, 0) ";
        }
        $query .= "     AS classdays2, ";
        $query .= "     ad.sick, ";
        $query .= "     ad.NOTICE, ";
        $query .= "     ad.NONOTICE, ";
        $query .= "     VALUE(ad.LESSON, 0) - VALUE(ad.suspend, 0) - VALUE(ad.mourning, 0) - VALUE(ad.ABROAD, 0) - VALUE(ad.OFFDAYS, 0) ";
        $query .= "               - VALUE(ad.sick, 0) - VALUE(ad.NOTICE, 0) - VALUE(ad.NONOTICE, 0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(ad.VIRUS, 0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(ad.KOUDOME, 0) ";
        }
        $query .= "     AS classdays3, ";
        $query .= "     ad.late, ";
        $query .= "     ad.early, ";
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $query .= "     L1.CNT AS DETAIL_001, ";
            $query .= "     L2.CNT AS DETAIL_002, ";
            $query .= "     L3.CNT AS DETAIL_003, ";
            $query .= "     L4.CNT AS DETAIL_004, ";
        } else {
            $query .= "     ad.REIHAI_KEKKA AS DETAIL_001, ";
            $query .= "     ad.M_KEKKA_JISU AS DETAIL_002, ";
            $query .= "     ad.REIHAI_TIKOKU AS DETAIL_003, ";
            $query .= "     ad.JYUGYOU_TIKOKU AS DETAIL_004, ";
        }
        $query .= "     sumad.sum_classdays, ";
        $query .= "     sumad.sum_suspend, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     sumad.SUM_KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     sumad.SUM_VIRUS, ";
        }
        $query .= "     sumad.sum_mourning, ";
        $query .= "     VALUE(sumad.sum_classdays, 0) - VALUE(sumad.sum_suspend, 0) - VALUE(sumad.sum_mourning, 0) - VALUE(sumad.SUM_OFFDAYS, 0) - VALUE(sumad.SUM_ABROAD, 0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(sumad.SUM_VIRUS, 0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(sumad.SUM_KOUDOME, 0) ";
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
        $query .= "     (VALUE(sumad.sum_classdays, 0) ";
        $query .= "         - (VALUE(sumad.sum_suspend, 0) + VALUE(sumad.sum_mourning, 0) + VALUE(sumad.SUM_OFFDAYS, 0) + VALUE(sumad.SUM_ABROAD, 0)) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(sumad.SUM_VIRUS, 0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(sumad.SUM_KOUDOME, 0) ";
        }
        $query .= "         - (VALUE(sumad.sum_sick, 0) + VALUE(sumad.SUM_NOTICE, 0) + VALUE(sumad.SUM_NONOTICE, 0)) ";
        $query .= "     ) AS sum_classdays3, ";
        $query .= "     sumad.sum_late, ";
        $query .= "     sumad.sum_early, ";
        $query .= "     sumad.SUM_DETAIL_001, ";
        $query .= "     sumad.SUM_DETAIL_002, ";
        $query .= "     sumad.SUM_DETAIL_003, ";
        $query .= "     sumad.SUM_DETAIL_004, ";
        $query .= "     ad.schregno AS SEM_SCHREGNO ";
        $query .= " FROM ";
        $query .= "     schreg_regd_dat sd ";

        //校種対応
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_REGD_GDAT SGD ";
        $query .= " ON ";
        $query .= "     SD.YEAR = SGD.YEAR AND ";
        $query .= "     SD.GRADE = SGD.GRADE ";

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
        $query .= "       AD.YEAR = AM.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SGD.SCHOOL_KIND = AM.SCHOOL_KIND ";
        }
        $query .= "   AND AD.MONTH = AM.MONTH ";
        $query .= "   AND AD.SEMESTER = AM.SEMESTER ";
                    
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(LESSON) AS SUM_CLASSDAYS, ";
        $query .= "         SUM(OFFDAYS) AS SUM_OFFDAYS, ";
        $query .= "         SUM(ABROAD) AS SUM_ABROAD, ";
        $query .= "         SUM(suspend) AS sum_suspend, ";
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
        $query .= "         SUM(EARLY) AS SUM_EARLY, ";
        $query .= "         SUM(REIHAI_KEKKA) AS SUM_DETAIL_001, ";
        $query .= "         SUM(M_KEKKA_JISU) AS SUM_DETAIL_002, ";
        $query .= "         SUM(REIHAI_TIKOKU) AS SUM_DETAIL_003, ";
        $query .= "         SUM(JYUGYOU_TIKOKU) AS SUM_DETAIL_004 ";
        $query .= "      FROM ";
        $query .= "         V_ATTEND_SEMES_DAT ";
        $query .= "      WHERE ";
        $query .= "            year = '".CTRL_YEAR."' ";
        if($monthsem[0] != "" && $monthsem[0] != NULL){
            $query .= "        AND month IN( ".$range_month[$monthsem[0]].")";    //2004/08/27 arakaki
            $query .= "        AND SEMESTER <= '".$monthsem[1]."' ";
        }
        $query .= "      GROUP BY ";
        $query .= "         schregno) AS sumad ";
        $query .= " ON ";
        $query .= "     sumad.schregno = sd.schregno ";

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

        $query .= " WHERE ";
        $query .= "     sd.year = '".CTRL_YEAR."' AND ";
        
        if($monthsem[0] != "" && $monthsem[0] != NULL){
                $query .= "     sd.semester = '".$monthsem[1]."' AND";
        }else{
                $query .= "     sd.semester IS NULL  AND";
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     SGD.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SGD.SCHOOL_KIND IN ('".SCHOOLKIND."') AND ";
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
            $data["APPOINTED_DAY"][TEXT]    = $model->appointed_day;              //締め日
            $data["LESSON"][NUMBER]         = ($model->field["LESSON"][$key])  ?  $model->field["LESSON"][$key] : 0;      //授業日数
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                $data["OFFDAYS"][NUMBER]    = $model->field["OFFDAYS"][$key];       //休学日数
                $data["ABROAD"][NUMBER]     = $model->field["ABROAD"][$key];        //留学日数
                $data["ABSENT"][NUMBER]     = $model->field["ABSENT"][$key];        //公欠日数
                $data["SUSPEND"][NUMBER]    = $model->field["SUSPEND"][$key];       //出停日数
                $data["MOURNING"][NUMBER]   = $model->field["MOURNING"][$key];      //忌引日数
                $data["SICK"][NUMBER]       = $model->field["SICK"][$key];          //欠席
                $data["NOTICE"][NUMBER]     = $model->field["NOTICE"][$key];        //事故欠届
                $data["NONOTICE"][NUMBER]   = $model->field["NONOTICE"][$key];      //事故欠無
                $data["LATE"][NUMBER]       = $model->field["LATE"][$key];          //遅刻回数
                $data["EARLY"][NUMBER]      = $model->field["EARLY"][$key];         //早退回数
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]      = $model->field["VIRUS"][$key];         //出停（伝染病）日数
                }
                if ($model->Properties["useKoudome"] == "true") {
                    $data["KOUDOME"][NUMBER]    = $model->field["KOUDOME"][$key];       //出停（交止）日数
                }
            } else {
                $data["OFFDAYS"][NUMBER]     = ($model->field["OFFDAYS"][$key]) ?  $model->field["OFFDAYS"][$key] : 0;     //休学日数
                $data["ABROAD"][NUMBER]      = ($model->field["ABROAD"][$key]) ?   $model->field["ABROAD"][$key] : 0;      //留学日数
                $data["ABSENT"][NUMBER]      = ($model->field["ABSENT"][$key]) ?   $model->field["ABSENT"][$key] : 0;      //公欠日数
                $data["SUSPEND"][NUMBER]     = ($model->field["SUSPEND"][$key]) ?  $model->field["SUSPEND"][$key] : 0;     //出停日数
                $data["MOURNING"][NUMBER]    = ($model->field["MOURNING"][$key]) ? $model->field["MOURNING"][$key] : 0;    //忌引日数
                $data["SICK"][NUMBER]        = ($model->field["SICK"][$key]) ?     $model->field["SICK"][$key] : 0;        //欠席
                $data["NOTICE"][NUMBER]      = ($model->field["NOTICE"][$key]) ?   $model->field["NOTICE"][$key] : 0;      //事故欠届
                $data["NONOTICE"][NUMBER]    = ($model->field["NONOTICE"][$key]) ? $model->field["NONOTICE"][$key] : 0;    //事故欠無
                $data["LATE"][NUMBER]        = ($model->field["LATE"][$key]) ?     $model->field["LATE"][$key] : 0;        //遅刻回数
                $data["EARLY"][NUMBER]       = ($model->field["EARLY"][$key]) ?    $model->field["EARLY"][$key] : 0;       //早退回数
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]   = ($model->field["VIRUS"][$key]) ?    $model->field["VIRUS"][$key] : 0;       //出停（伝染病）日数
                }
                if ($model->Properties["useKoudome"] == "true") {
                    $data["KOUDOME"][NUMBER] = ($model->field["KOUDOME"][$key]) ?  $model->field["KOUDOME"][$key] : 0;     //出停（交止）日数
                }
                if ($model->Properties["useKekkaJisu"] == "true") {
                    $data["KEKKA_JISU"][NUMBER]  = 0;     //使用しない
                }
                if ($model->Properties["useKekka"] == "true") {
                    $data["KEKKA"][NUMBER]       = 0;     //使用しない
                }
                if ($model->Properties["useLatedetail"] == "true") {
                    $data["LATEDETAIL"][NUMBER]  = 0;     //使用しない
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

            //DETAIL更新
            $c002Field = preg_split("/,/", $model->set_detail_field);
            $c002Field = is_array($c002Field) ? $c002Field : array();
            foreach ($c002Field as $c002Key => $c002Val) {
                $c002Val = str_replace("DETAIL_", "", $c002Val);
                $c002Val = str_replace("[]", "", $c002Val);
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
                $query .= "     AND SEQ = '".$c002Val."' ";

                $flag = $db->getOne($query);

                $data = array();

                /* 更新対象データがある時 - UPDATE */
                if ($flag) {
                    $where  = " WHERE ";
                    $where .= "     YEAR = '".CTRL_YEAR."' ";
                    $where .= "     AND MONTH = '".$monthsem[0]."' ";
                    $where .= "     AND SEMESTER = '".$monthsem[1]."' ";
                    $where .= "     AND SCHREGNO = '".$schregno."' ";
                    $where .= "     AND SEQ = '".$c002Val."' ";
                } else {
                    $data["COPYCD"][TEXT]   = "0";
                    $data["YEAR"][TEXT]     = CTRL_YEAR;
                    $data["MONTH"][TEXT]    = $monthsem[0];
                    $data["SEMESTER"][TEXT] = $monthsem[1];
                    $data["SCHREGNO"][TEXT] = $schregno;
                    $data["SEQ"][TEXT]      = $c002Val;
                }

                /* 更新データをセット */
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $data["CNT"][NUMBER]        = $model->field["DETAIL_".$c002Val][$key];
                } else {
                    $data["CNT"][NUMBER]        = ($model->field["DETAIL_".$c002Val][$key])  ?  $model->field["DETAIL_".$c002Val][$key] : 0;
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

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
