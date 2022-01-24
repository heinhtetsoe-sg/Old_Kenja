<?php

require_once('for_php7.php');
class knjc031dQuery extends Query {

    //学期名取得
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
        if ($model->field["HR_CLASS_TYPE"] == "2") {
            //FI複式
            if ($model->Properties["useFi_Hrclass"] == "1") {
                $tablename = "SCHREG_REGD_FI_HDAT";
            //特別支援
            } else {
                $tablename = "SCHREG_REGD_GHR_HDAT";
            }
        } else {
            $tablename = "SCHREG_REGD_HDAT";
        }

        $query  = " SELECT ";
        //特別支援のみ
        if ($model->field["HR_CLASS_TYPE"] == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $query .= "     T1.GHR_CD, ";
            $query .= "     T1.GHR_NAME ";
        } else {
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_NAME ";
        }
        $query .= " FROM ";
        $query .=       $tablename." T1 ";
        if ($tablename != "SCHREG_REGD_GHR_HDAT") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                    $query .= "          ON G1.YEAR     = T1.YEAR ";
                    $query .= "         AND G1.GRADE    = T1.GRADE ";
                    $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && $tablename != "SCHREG_REGD_GHR_HDAT") {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "          ON G1.YEAR         = T1.YEAR ";
                $query .= "         AND G1.GRADE        = T1.GRADE ";
                $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD3 = '".STAFFCD."') ";
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
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
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
    function getSickDiv($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
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
        $query .= "     AND NAMECD2 IN ('101') ";
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
            $query .= "     SCHOOL_KIND = '". $model->schoolkind ."' AND ";
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
        $query .= "     GRD_DIV IN ('1', '2', '3') AND ";
        $query .= "     GRD_DATE < '".$date."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $namespare1) {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMESPARE1  = '".$namespare1."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst2($namecd1) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

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

    /* 出欠月別累積データ・出欠累積データ・出欠科目別累積データ・学籍異動データ */
    function selectAttendQuery($model, $schoolMst, $subl_data="")
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

        if ($model->field["HR_CLASS_TYPE"] == "2") {
            //FI複式
            if ($model->Properties["useFi_Hrclass"] == "1") {
                $tablename = "SCHREG_REGD_FI_DAT";
            //特別支援
            } else {
                $tablename = "SCHREG_REGD_GHR_DAT";
            }
        } else {
            $tablename = "SCHREG_REGD_DAT";
        }
        $query  = " SELECT ";
        $query .= "     sd.schregno, ";
        //特別支援のみ
        if ($model->field["HR_CLASS_TYPE"] == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $query .= "     sd.GHR_ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "     sd.attendno, ";
        }
        $query .= "     sm.name_show, ";
        $query .= "     am.APPOINTED_DAY, ";
        $query .= "     ad.LESSON, ";
        $query .= "     ad.OFFDAYS, ";
        $query .= "     ad.ABROAD, ";
        $query .= "     ad.ABSENT, ";
        $query .= "     ad.suspend, ";
        $query .= "     ad.mourning, ";
        $query .= "     VALUE(ad.LESSON, 0) - VALUE(ad.suspend, 0) - VALUE(ad.mourning, 0) - VALUE(ad.ABROAD, 0) - VALUE(ad.OFFDAYS, 0) ";
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(ad.OFFDAYS, 0) ";
        }
        $query .= "     AS classdays2, ";
        $query .= "     ad.sick, ";
        $query .= "     ad.NOTICE, ";
        $query .= "     ad.NONOTICE, ";

        foreach ($subl_data as $key) {
            $query .= " SUBL".$key.".CNT".$key.",";
        }

        $query .= "     VALUE(ad.LESSON, 0) - VALUE(ad.suspend, 0) - VALUE(ad.mourning, 0) - VALUE(ad.ABROAD, 0) - VALUE(ad.OFFDAYS, 0) ";
        $query .= "               - VALUE(ad.sick, 0) - VALUE(ad.NOTICE, 0) - VALUE(ad.NONOTICE, 0) ";
        $query .= "     AS classdays3, ";
        $query .= "     ad.late, ";
        $query .= "     ad.early, ";
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $query .= "     L101.CNT_DECIMAL AS DETAIL_101, ";
        } else {
            $query .= "     ad.JYUGYOU_JISU_DECIMAL AS DETAIL_101, ";
        }
        $query .= "     ad.schregno AS SEM_SCHREGNO ";
        $query .= " FROM ";
        $query .=       $tablename." sd ";
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
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND MONTH = '".$monthsem[0]."' ";
        $query .= "         AND SEMESTER = '".$monthsem[1]."' ) AS AD ";
        $query .= " ON ";
        $query .= "     AD.SCHREGNO = SD.SCHREGNO ";
        
        
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_REGD_GDAT SGD ";
        $query .= " ON ";
        $query .= "       SD.YEAR = SGD.YEAR ";
        $query .= "   AND SD.GRADE = SGD.GRADE ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     APPOINTED_DAY_MST AM ";
        $query .= " ON ";
        $query .= "       AD.YEAR = AM.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SGD.SCHOOL_KIND = AM.SCHOOL_KIND ";
        }
        $query .= "   AND AD.MONTH = AM.MONTH ";
        $query .= "   AND AD.SEMESTER = AM.SEMESTER ";

        $query .= " LEFT JOIN ATTEND_SEMES_DETAIL_DAT L101 ";
        $query .= "      ON AD.COPYCD   = L101.COPYCD ";
        $query .= "     AND AD.YEAR     = L101.YEAR ";
        $query .= "     AND AD.MONTH    = L101.MONTH ";
        $query .= "     AND AD.SEMESTER = L101.SEMESTER ";
        $query .= "     AND AD.SCHREGNO = L101.SCHREGNO ";
        $query .= "     AND L101.SEQ    = '101' ";


        foreach ($subl_data as $key) {
            $query .= " LEFT OUTER JOIN ";
            $query .= "     (SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         CNT AS CNT".$key." ";
            $query .= "      FROM ";
            $query .= "         ATTEND_SEMES_SUBL_DAT ";
            $query .= "      WHERE ";
            $query .= "         YEAR = '".CTRL_YEAR."' ";
            $query .= "         AND MONTH = '".$monthsem[0]."' ";
            $query .= "         AND SEMESTER = '".$monthsem[1]."' ";
            $query .= "         AND DI_CD = '6' ";
            $query .= "         AND SUBL_CD = '".$key."' ) AS SUBL".$key." ";
            $query .= " ON ";
            $query .= "     SUBL".$key.".SCHREGNO = SD.SCHREGNO ";
        }

        $query .= " WHERE ";
        $query .= "     sd.year = '".CTRL_YEAR."' AND ";
        
        if($monthsem[0] != "" && $monthsem[0] != NULL){
                $query .= "     sd.semester = '".$monthsem[1]."' AND";
        }else{
                $query .= "     sd.semester IS NULL  AND";
        }
        //特別支援のみ
        if ($model->field["HR_CLASS_TYPE"] == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $query .= "     sd.GHR_CD = '".$model->field["hr_class"]."' ";
        } else {
            $query .= "     sd.grade = '".$model->field["grade"]."' AND ";
            $query .= "     sd.hr_class = '".$model->field["class"]."' ";
        }
        $query .= " ORDER BY ";
        //特別支援のみ
        if ($model->field["HR_CLASS_TYPE"] == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $query .= "     sd.GHR_ATTENDNO ";
        } else {
            $query .= "     sd.attendno ";
        }
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

            $total_sublcnt = 0;
            $sickdiv = array();
            $c001 = ($model->schoolkind) ? 'C'.$model->schoolkind.'01' : 'C001';
            $sickdiv = $db->getCol(knjc031dQuery::getSickDiv($c001));
            if(in_array('6', $sickdiv)) {

                $subl_cd = $db->getCol(knjc031dQuery::getNameMst('C006', '6'));

                foreach($subl_cd as $sublKey) {

                    //中分類存在チェック
                    $subm_cnt = get_count($db->getCol(knjc031dQuery::getNameMst('C007', $sublKey)));
                    if($subm_cnt > "0") break;

                    $dataL = array();

                    // 更新対象データが存在するかをチェック--ATTEND_SEMES_SUBL_DAT
                    $chk_subl = $db->getOne(knjc031dQuery::checkAttendSemesSublDat($model, $schregno, $sublKey));
                    /* ATTEND_SEMES_SUBL_DAT */
                    // 更新対象データがある時 - UPDATE
                    if($chk_subl){
                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                        $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                        $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                        $where .= "     SCHREGNO    = '".$schregno."' AND ";
                        $where .= "     DI_CD       = '6' AND ";
                        $where .= "     SUBL_CD     = '".$sublKey."' ";
                    }
                    // 更新対象データがない時 - INSERT
                    else{
                        $dataL["COPYCD"][TEXT]      = "0";
                        $dataL["YEAR"][TEXT]        = CTRL_YEAR;
                        $dataL["MONTH"][TEXT]       = $monthsem[0];
                        $dataL["SEMESTER"][TEXT]    = $monthsem[1];
                        $dataL["SCHREGNO"][TEXT]    = $schregno;
                        $dataL["DI_CD"][TEXT]       = '6';
                        $dataL["SUBL_CD"][TEXT]     = $sublKey;
                    }

                    if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                        $dataL["CNT"][NUMBER]       = $model->field["CNT".$sublKey][$key];
                    } else {
                        $dataL["CNT"][NUMBER]       = ($model->field["CNT".$sublKey][$key])  ?  $model->field["CNT".$sublKey][$key] : 0;
                    }
                    $dataL["REGISTERCD"][TEXT]  = STAFFCD ;
                    $dataL["UPDATED"][FUNC]     = "sysdate()";

                    // 更新対象データがある時 - UPDATE
                    if($chk_subl){
                        $query = Query::updateSQL($dataL, "ATTEND_SEMES_SUBL_DAT", $where);
                    }
                    // 更新対象データがない時 - INSERT
                    else {
                        $query = Query::insertSQL($dataL, "ATTEND_SEMES_SUBL_DAT");
                    }
                    $db->query($query);
                }
            }

            //大分類の値を合算する
            $sublcd_Array = $db->getCol(knjc031dQuery::getNameMst('C006', '6'));
            foreach($sublcd_Array as $sublKey) {
                $sublcnt = $db->getOne(knjc031dQuery::checkAttendSemesSublDat($model, $schregno, $sublKey, "1"));
                $total_sublcnt = (($sublcnt != "") ? $sublcnt : 0) + $total_sublcnt;
            }

            // 更新対象データが存在するかをチェック--ATTEND_SEMES_DAT
            $chk_semes = $db->getOne(knjc031dQuery::checkAttendSemesDat($model, $schregno));

            //出欠詳細入力対象項目は更新対象外
            $notUpdateItem = ($model->notUpdateItem) ? explode(',', $model->notUpdateItem) : array();

            $data = array();

            /* 更新対象データがある時 - UPDATE */
            if($chk_semes){
                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                $where .= "     SCHREGNO    = '".$schregno."' ";
            }
            /* 更新対象データがない時 - INSERT */
            else{
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["MONTH"][TEXT]    = $monthsem[0];
                $data["SEMESTER"][TEXT] = $monthsem[1];
                $data["SCHREGNO"][TEXT] = $schregno;

                if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                    $attItem = array("ABSENT", "SUSPEND", "MOURNING", "SICK", "NOTICE", "LATE", "EARLY");
                    foreach($attItem as $attKey) {
                        if (in_array($attKey, $notUpdateItem)) {
                            $data[$attKey][NUMBER]    = 0;
                        }
                    }
                }
            }

            /* 更新データをセット */
            $data["APPOINTED_DAY"][TEXT] = $model->appointed_day;              //締め日
            $data["LESSON"][NUMBER]      = ($model->field["LESSON"][$key])  ?  $model->field["LESSON"][$key] : 0;      //授業日数
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                $data["OFFDAYS"][NUMBER]    = $model->field["OFFDAYS"][$key];       //休学日数
                $data["ABROAD"][NUMBER]     = $model->field["ABROAD"][$key];        //留学日数
                if (!in_array("ABSENT", $notUpdateItem))    $data["ABSENT"][NUMBER]     = $model->field["ABSENT"][$key];        //公欠日数
                if (!in_array("SUSPEND", $notUpdateItem))   $data["SUSPEND"][NUMBER]    = $model->field["SUSPEND"][$key];       //出停日数
                if (!in_array("MOURNING", $notUpdateItem))  $data["MOURNING"][NUMBER]   = $model->field["MOURNING"][$key];      //忌引日数
                if (!in_array("SICK", $notUpdateItem))      $data["SICK"][NUMBER]       = $model->field["SICK"][$key];          //欠席
                if (!in_array("NOTICE", $notUpdateItem))    $data["NOTICE"][NUMBER]     = $model->field["NOTICE"][$key];        //事故欠届
                $data["NONOTICE"][NUMBER]   = (in_array('6', $sickdiv)) ? $total_sublcnt : $model->field["NONOTICE"][$key];     //事故欠無
                if (!in_array("LATE", $notUpdateItem))      $data["LATE"][NUMBER]       = $model->field["LATE"][$key];          //遅刻回数
                if (!in_array("EARLY", $notUpdateItem))     $data["EARLY"][NUMBER]      = $model->field["EARLY"][$key];         //早退回数
            } else {
                $data["OFFDAYS"][NUMBER]    = ($model->field["OFFDAYS"][$key]) ?  $model->field["OFFDAYS"][$key] : 0;       //休学日数
                $data["ABROAD"][NUMBER]     = ($model->field["ABROAD"][$key]) ?   $model->field["ABROAD"][$key] : 0;        //留学日数
                if (!in_array("ABSENT", $notUpdateItem))    $data["ABSENT"][NUMBER]     = ($model->field["ABSENT"][$key]) ?     $model->field["ABSENT"][$key] : 0;      //公欠日数
                if (!in_array("SUSPEND", $notUpdateItem))   $data["SUSPEND"][NUMBER]    = ($model->field["SUSPEND"][$key]) ?    $model->field["SUSPEND"][$key] : 0;     //出停日数
                if (!in_array("MOURNING", $notUpdateItem))  $data["MOURNING"][NUMBER]   = ($model->field["MOURNING"][$key]) ?   $model->field["MOURNING"][$key] : 0;    //忌引日数
                if (!in_array("SICK", $notUpdateItem))      $data["SICK"][NUMBER]       = ($model->field["SICK"][$key]) ?       $model->field["SICK"][$key] : 0;        //欠席
                if (!in_array("NOTICE", $notUpdateItem))    $data["NOTICE"][NUMBER]     = ($model->field["NOTICE"][$key]) ?     $model->field["NOTICE"][$key] : 0;      //事故欠届
                if(in_array('6', $sickdiv)) {
                    $data["NONOTICE"][NUMBER]   = ($total_sublcnt) ? $total_sublcnt : 0;    //事故欠無
                } else {
                    $data["NONOTICE"][NUMBER]   = ($model->field["NONOTICE"][$key]) ? $model->field["NONOTICE"][$key] : 0;      //事故欠無
                }
                if (!in_array("LATE", $notUpdateItem))      $data["LATE"][NUMBER]       = ($model->field["LATE"][$key]) ?       $model->field["LATE"][$key] : 0;        //遅刻回数
                if (!in_array("EARLY", $notUpdateItem))     $data["EARLY"][NUMBER]      = ($model->field["EARLY"][$key]) ?        $model->field["EARLY"][$key] : 0;       //早退回数
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD ;                           //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()";                        //更新日付

            /* 更新対象データがある時 - UPDATE */
            if($chk_semes){
                $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
            }
            /* 更新対象データがない時 - INSERT */
            else{
                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
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
                    $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
                    $query .= "     MONTH       = '".$monthsem[0]."' AND ";
                    $query .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                    $query .= "     SCHREGNO    = '".$schregno."' AND ";
                    $query .= "     SEQ         = '".$keyC002."' ";

                    $flag = $db->getOne($query);

                    $data = array();

                    /* 更新対象データがある時 - UPDATE */
                    if ($flag) {
                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                        $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                        $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                        $where .= "     SCHREGNO    = '".$schregno."' AND ";
                        $where .= "     SEQ         = '".$keyC002."' ";
                    } else {
                        $data["COPYCD"][TEXT]   = "0";
                        $data["YEAR"][TEXT]     = CTRL_YEAR;
                        $data["MONTH"][TEXT]    = $monthsem[0];
                        $data["SEMESTER"][TEXT] = $monthsem[1];
                        $data["SCHREGNO"][TEXT] = $schregno;
                        $data["SEQ"][TEXT]      = $keyC002;
                    }

                    /* 更新データをセット */
                    if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                        if ($keyC002 == "101") {
                            $data["CNT_DECIMAL"][NUMBER] = $model->field["DETAIL_".$keyC002][$key];
                        } else {
                            $data["CNT"][NUMBER]         = $model->field["DETAIL_".$keyC002][$key];
                        }
                    } else {
                        if ($keyC002 == "101") {
                            $data["CNT_DECIMAL"][NUMBER] = ($model->field["DETAIL_".$keyC002][$key])  ?  $model->field["DETAIL_".$keyC002][$key] : 0.0;
                        } else {
                            $data["CNT"][NUMBER]         = ($model->field["DETAIL_".$keyC002][$key])  ?  $model->field["DETAIL_".$keyC002][$key] : 0;
                        }
                    }
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    /* 更新対象データがある時 - UPDATE */
                    if ($flag) {
                        $query = Query::updateSQL($data, "ATTEND_SEMES_DETAIL_DAT", $where);
                    }
                    /* 更新対象データがない時 - INSERT */
                    else {
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

    //ATTEND_SEMES_DATチェック
    function checkAttendSemesDat($model, $schregno) {

        list($month, $semester) =  explode('-', $model->field["month"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //ATTEND_SEMES_SUBL_DATチェック
    function checkAttendSemesSublDat($model, $schregno, $subl_cd, $div="") {

        list($month, $semester) =  explode('-', $model->field["month"]);

        $query  = " SELECT ";
        if($div) {
            $query .= "     CNT ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_SUBL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DI_CD       = '6' AND ";
        $query .= "     SUBL_CD     = '".$subl_cd."' ";

        return $query;
    }
}
?>
