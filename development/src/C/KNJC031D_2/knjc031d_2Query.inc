<?php

require_once('for_php7.php');
class knjc031d_2Query extends Query {

    //学期名取得
    function getSemesterName($year, $semester) {
        $query .= " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$year}' AND";
        $query .= "     SEMESTER    = '{$semester}' ";

        return $query;
    }

    //クラス名取得
    function getHrName($model)
    {
        if ($model->hr_class_type == "2") {
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
        if ($model->hr_class_type == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $query .= "     GHR_NAME ";
        } else {
            $query .= "     HR_NAME ";
        }
        $query .= " FROM ";
        $query .=       $tablename;
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        //特別支援のみ
        if ($model->hr_class_type == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $query .= "     GHR_CD    = '".$model->hr_class."' ";
        } else {
            $query .= "     GRADE       = '".$model->grade."' AND ";
            $query .= "     HR_CLASS    = '".$model->hr_class."' ";
        }

        return $query;
    }

    //対象月取得
    function getMonthName($month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' AND ";
        $query .= "     NAMECD2 = '".sprintf('%02d',$month)."' ";

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

    //締め日の取得
    function getAppointedDay($tuki, $gakki, $model) {
        $query  = "  SELECT ";
        $query .= "    AM.APPOINTED_DAY ";
        $query .= "  FROM ";
        $query .= "    APPOINTED_DAY_MST AM ";
        $query .= "    INNER JOIN ";
        $query .= "    SCHREG_REGD_GDAT SGD ";
        $query .= "    ON ";
        $query .= "    AM.YEAR = SGD.YEAR AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    AM.SCHOOL_KIND = SGD.SCHOOL_KIND ";
        }
        $query .= "  WHERE ";
        $query .= "     AM.YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     AM.MONTH = '{$tuki}' AND ";
        $query .= "     AM.SEMESTER = '{$gakki}' AND";
        $query .= "     SGD.GRADE = '{$model->grade}' ";

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
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst2($namecd1, $namecd2) {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMECD2     = '".$namecd2."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst3($namecd1, $namecd2) {

        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMECD2     = '".$namecd2."' ";

        return $query;
    }

    /* 出欠月別累積データ・出欠累積データ・出欠科目別累積データ・学籍異動データ */
    function selectAttendQuery($model, $schoolMst, $sub_data, $namespare1)
    {
        $monthsem = array();
        $monthsem = preg_split("/-/", $model->month);
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

        if ($model->hr_class_type == "2") {
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
        if ($model->hr_class_type == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $query .= "     sd.GHR_ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "     sd.attendno, ";
        }
        $query .= "     sm.name_show ";

        foreach ($sub_data as $key) {
            $query .= "    ,SUB".$key.".CNT".$key." ";
            $query .= "    ,SUB".$key.".SCHREGNO AS SUB_SCHREGNO".$key." ";
        }

        $query .= " FROM ";
        $query .=       $tablename." sd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     schreg_base_mst sm ";
        $query .= " ON ";
        $query .= "     sd.schregno = sm.schregno ";

        if (substr($model->title,0,3) != 'CNT') {
            foreach ($sub_data as $key) {
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
                $query .= "         AND DI_CD = '".$namespare1."' ";
                $query .= "         AND SUBL_CD = '".$key."' ) AS SUB".$key." ";
                $query .= " ON ";
                $query .= "     SUB".$key.".SCHREGNO = SD.SCHREGNO ";
            }
        } else {
            foreach ($sub_data as $key) {
                $query .= " LEFT OUTER JOIN ";
                $query .= "     (SELECT ";
                $query .= "         SCHREGNO, ";
                $query .= "         CNT AS CNT".$key." ";
                $query .= "      FROM ";
                $query .= "         ATTEND_SEMES_SUBM_DAT ";
                $query .= "      WHERE ";
                $query .= "         YEAR = '".CTRL_YEAR."' ";
                $query .= "         AND MONTH = '".$monthsem[0]."' ";
                $query .= "         AND SEMESTER = '".$monthsem[1]."' ";
                $query .= "         AND DI_CD = (SELECT ";
                $query .= "                         NAMESPARE1 ";
                $query .= "                      FROM ";
                $query .= "                         V_NAME_MST ";
                $query .= "                      WHERE ";
                $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
                $query .= "                         NAMECD1 = 'C006' AND ";
                $query .= "                         NAMECD2 = '".$namespare1."') ";
                $query .= "         AND SUBL_CD = '".$namespare1."' ";
                $query .= "         AND SUBM_CD = '".$key."' ) AS SUB".$key." ";
                $query .= " ON ";
                $query .= "     SUB".$key.".SCHREGNO = SD.SCHREGNO ";
            }
        }
        $query .= " WHERE ";
        $query .= "     sd.year = '".CTRL_YEAR."' AND ";
        
        if($monthsem[0] != "" && $monthsem[0] != NULL){
                $query .= "     sd.semester = '".$monthsem[1]."' AND";
        }else{
                $query .= "     sd.semester IS NULL  AND";
        }
        //特別支援のみ
        if ($model->hr_class_type == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $query .= "     sd.GHR_CD = '".$model->hr_class."' ";
        } else {
            $query .= "     sd.grade    = '".$model->grade."' AND ";
            $query .= "     sd.hr_class = '".$model->hr_class."' ";
        }
        $query .= " ORDER BY ";
        //特別支援のみ
        if ($model->hr_class_type == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
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
        $monthsem = preg_split("/-/", $model->month);

        foreach($model->field["SCHREGNO"] as $key => $schregno){
            $data = $dataL = $dataM = array();
            $total_sublcnt = $total_submcnt = 0;

            // 更新対象データが存在するかをチェック--ATTEND_SEMES_DAT
            $chk_semes = $db->getOne(knjc031d_2Query::checkAttendSemesDat($model, $schregno));

            if(substr($model->title,0,3) == 'CNT') {

                $subl_cd = substr($model->title,3);
                $subm_cd = $db->getCol(knjc031d_2Query::getNameMst('C007', $subl_cd));

                foreach($subm_cd as $submKey) {
                    $data = $dataL = $dataM = array();

                    // 更新対象データが存在するかをチェック--ATTEND_SEMES_SUBM_DAT
                    $chk_subm = $db->getOne(knjc031d_2Query::checkAttendSemesSubmDat($model, $schregno, $subl_cd, $submKey));

                    /* ATTEND_SEMES_SUBM_DAT */
                    // 更新対象データがある時 - UPDATE
                    if($chk_subm){
                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                        $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                        $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                        $where .= "     SCHREGNO    = '".$schregno."' AND ";
                        $where .= "     DI_CD       = '".$model->field["DI_CD"]."' AND ";
                        $where .= "     SUBL_CD     = '".$subl_cd."' AND ";
                        $where .= "     SUBM_CD     = '".$submKey."' ";
                    }
                    // 更新対象データがない時 - INSERT
                    else {
                        $dataM["COPYCD"][TEXT]      = "0";
                        $dataM["YEAR"][TEXT]        = CTRL_YEAR;
                        $dataM["MONTH"][TEXT]       = $monthsem[0];
                        $dataM["SEMESTER"][TEXT]    = $monthsem[1];
                        $dataM["SCHREGNO"][TEXT]    = $schregno;
                        $dataM["DI_CD"][TEXT]       = $model->field["DI_CD"];
                        $dataM["SUBL_CD"][TEXT]     = $subl_cd;
                        $dataM["SUBM_CD"][TEXT]     = $submKey;
                    }

                    if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                        $dataM["CNT"][NUMBER]       = $model->field["CNT".$submKey][$key];
                    } else {
                        $dataM["CNT"][NUMBER]       = ($model->field["CNT".$submKey][$key])  ?  $model->field["CNT".$submKey][$key] : 0;
                    }
                    $dataM["REGISTERCD"][TEXT]  = STAFFCD ;
                    $dataM["UPDATED"][FUNC]     = "sysdate()";

                    // 更新対象データがある時 - UPDATE
                    if($chk_subm){
                        $query = Query::updateSQL($dataM, "ATTEND_SEMES_SUBM_DAT", $where);
                    }
                    // 更新対象データがない時 - INSERT
                    else {
                        $query = Query::insertSQL($dataM, "ATTEND_SEMES_SUBM_DAT");
                    }
                    $db->query($query);

                    $total_submcnt = (($model->field["CNT".$submKey][$key])  ?  $model->field["CNT".$submKey][$key] : 0) + $total_submcnt;
                }


                // 更新対象データが存在するかをチェック--ATTEND_SEMES_SUBL_DAT
                $chk_subl = $db->getOne(knjc031d_2Query::checkAttendSemesSublDat($model, $schregno, $subl_cd));

                /* ATTEND_SEMES_SUBL_DAT */
                // 更新対象データがある時 - UPDATE
                if($chk_subl){
                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                    $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                    $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                    $where .= "     SCHREGNO    = '".$schregno."' AND ";
                    $where .= "     DI_CD       = '".$model->field["DI_CD"]."' AND ";
                    $where .= "     SUBL_CD     = '".$subl_cd."' ";
                }
                // 更新対象データがない時 - INSERT
                else {
                    $dataL["COPYCD"][TEXT]      = "0";
                    $dataL["YEAR"][TEXT]        = CTRL_YEAR;
                    $dataL["MONTH"][TEXT]       = $monthsem[0];
                    $dataL["SEMESTER"][TEXT]    = $monthsem[1];
                    $dataL["SCHREGNO"][TEXT]    = $schregno;
                    $dataL["DI_CD"][TEXT]       = $model->field["DI_CD"];
                    $dataL["SUBL_CD"][TEXT]     = $subl_cd;
                }

                $dataL["CNT"][NUMBER]       = $total_submcnt;
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

                $sublcd_Array = $db->getCol(knjc031d_2Query::getNameMst('C006', $model->field["DI_CD"]));
                foreach($sublcd_Array as $sublKey) {
                    $sublcnt = $db->getOne(knjc031d_2Query::checkAttendSemesSublDat($model, $schregno, $sublKey, "1"));
                    $total_sublcnt = (($sublcnt != "") ? $sublcnt : 0) + $total_sublcnt;
                }
            } else {

                $subl_cd = $db->getCol(knjc031d_2Query::getNameMst('C006', $model->field["DI_CD"]));

                //出欠詳細入力対象項目
                $notUpdateItem = ($model->notUpdateItem) ? explode(',', $model->notUpdateItem) : array();

                foreach($subl_cd as $sublKey) {

                    //出欠詳細入力対象項目は更新対象外
                    if (in_array($sublKey, $notUpdateItem)) {
                        //中分類のCNT合計を取得
                        $sum_subm = $db->getOne(knjc031d_2Query::getAttendSemesSubmDat($model, $schregno, $sublKey));
                        //合算
                        $total_sublcnt = (($sum_subm) ? $sum_subm : 0) + $total_sublcnt;
                        continue;
                    } else {
                        //合算
                        $total_sublcnt = (($model->field["CNT".$sublKey][$key]) ? $model->field["CNT".$sublKey][$key] : 0) + $total_sublcnt;
                    }

                    $data = $dataL = $dataM = array();

                    // 更新対象データが存在するかをチェック--ATTEND_SEMES_SUBL_DAT
                    $chk_subl = $db->getOne(knjc031d_2Query::checkAttendSemesSublDat($model, $schregno, $sublKey));
                    /* ATTEND_SEMES_SUBL_DAT */
                    // 更新対象データがある時 - UPDATE
                    if($chk_subl){
                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                        $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                        $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                        $where .= "     SCHREGNO    = '".$schregno."' AND ";
                        $where .= "     DI_CD       = '".$model->field["DI_CD"]."' AND ";
                        $where .= "     SUBL_CD     = '".$sublKey."' ";
                    }
                    // 更新対象データがない時 - INSERT
                    else{
                        $dataL["COPYCD"][TEXT]      = "0";
                        $dataL["YEAR"][TEXT]        = CTRL_YEAR;
                        $dataL["MONTH"][TEXT]       = $monthsem[0];
                        $dataL["SEMESTER"][TEXT]    = $monthsem[1];
                        $dataL["SCHREGNO"][TEXT]    = $schregno;
                        $dataL["DI_CD"][TEXT]       = $model->field["DI_CD"];
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

            /* ATTEND_SEMES_DAT */
            // 更新対象データがある時 - UPDATE
            if($chk_semes){
                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     MONTH       = '".$monthsem[0]."' AND ";
                $where .= "     SEMESTER    = '".$monthsem[1]."' AND ";
                $where .= "     SCHREGNO    = '".$schregno."' ";
            }
            // 更新対象データがない時 - INSERT
            else {
                $data["COPYCD"][TEXT]       = "0";
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["MONTH"][TEXT]        = $monthsem[0];
                $data["SEMESTER"][TEXT]     = $monthsem[1];
                $data["SCHREGNO"][TEXT]     = $schregno;

                //更新項目以外は"0"をセット
                if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                    $attItem = array("LESSON", "OFFDAYS", "ABROAD", "ABSENT", "SUSPEND", "MOURNING", "SICK", "NOTICE", "NONOTICE", "LATE", "EARLY");
                    foreach($attItem as $attKey) {
                        if ($attKey != $model->field["DI_NAME"]) {
                            $data[$attKey][NUMBER] = 0;
                        }
                    }
                }
            }

            $data["APPOINTED_DAY"][TEXT]            = $model->appointed_day;
            $data[$model->field["DI_NAME"]][NUMBER] = $total_sublcnt;
            $data["REGISTERCD"][TEXT]               = STAFFCD ;
            $data["UPDATED"][FUNC]                  = "sysdate()";

            // 更新対象データがある時 - UPDATE
            if($chk_semes){
                $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
            }
            // 更新対象データがない時 - INSERT
            else {
                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //ATTEND_SEMES_DATチェック
    function checkAttendSemesDat($model, $schregno) {

        list($month, $semester) =  explode('-', $model->month);

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

        list($month, $semester) =  explode('-', $model->month);

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
        $query .= "     DI_CD       = '".$model->field["DI_CD"]."' AND ";
        $query .= "     SUBL_CD     = '".$subl_cd."' ";

        return $query;
    }

    //ATTEND_SEMES_SUBM_DATチェック
    function checkAttendSemesSubmDat($model, $schregno, $subl_cd, $subm_cd) {

        list($month, $semester) =  explode('-', $model->month);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_SUBM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DI_CD       = '".$model->field["DI_CD"]."' AND ";
        $query .= "     SUBL_CD     = '".$subl_cd."' AND ";
        $query .= "     SUBM_CD     = '".$subm_cd."' ";

        return $query;
    }

    //ATTEND_SEMES_SUBM_DATのCNT合計取得
    function getAttendSemesSubmDat($model, $schregno, $subl_cd) {

        list($month, $semester) =  explode('-', $model->month);

        $query  = " SELECT ";
        $query .= "     SUM(CNT) ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_SUBM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DI_CD       = '".$model->field["DI_CD"]."' AND ";
        $query .= "     SUBL_CD     = '".$subl_cd."' ";

        return $query;
    }
}
?>
