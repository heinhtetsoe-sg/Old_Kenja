<?php

require_once('for_php7.php');
class knjc031aQuery extends Query
{
    public function getSemesterName($year, $semester)
    {
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
    public function getSchoolKind($model)
    {
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
    public function selectHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     grade, hr_class, hr_name ";
        $query .= " FROM ";
        $query .= "     schreg_regd_hdat ";
        $query .= " WHERE ";
        $query .= "     year = '".CTRL_YEAR."' AND";
        $query .= "     semester = '".CTRL_SEMESTER."' ";
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND (TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN  (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN   (SELECT ";
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
    public function selectSemesAll($seme = "")
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
    public function selectMonthQuery($month)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     namecd2, name1, namespare1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year  = '".CTRL_YEAR."' ";
        $query .= "     AND namecd1 = 'Z005' ";
        $query .= "     AND namecd2 IN (SELECT CONTROL_CODE FROM ADMIN_CONTROL_DAT WHERE YEAR='".CTRL_YEAR."' AND CONTROL_FLG ='2') ";
        $query .= "     AND namecd2 = '".sprintf('%02d', $month)."' ";
        $query .= " ORDER BY ";
        $query .= "     namespare1 ";

        return $query;
    }

    //締め日の取得
    public function getAppointedDay($tuki, $gakki, $model)
    {
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

    //学校マスタの取得
    public function getSchoolMst()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //異動データ取得
    public function getIdouData($schregno, $date)
    {
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

    /* 出欠月別累積データ・出欠累積データ・出欠科目別累積データ・学籍異動データ */
    public function selectAttendQuery($model, $schoolMst)
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
        $query .= "     SD.SCHREGNO, ";          //学籍番号
        $query .= "     SD.ATTENDNO, ";
        $query .= "     SM.NAME_SHOW, ";         //名前
        $query .= "     AM.APPOINTED_DAY, ";     //締め日
        $query .= "     AD.LESSON, ";            //授業日数
        $query .= "     AD.OFFDAYS, ";           //休学日数
        $query .= "     AD.ABROAD, ";            //留学日数
        $query .= "     AD.ABSENT, ";            //公欠日数
        $query .= "     AD.SUSPEND, ";           //出停
        $query .= "     AD.VIRUS, ";             //伝染病
        $query .= "     AD.MOURNING, ";          //忌引
        $query .= "     AD.LESSON - AD.SUSPEND - AD.MOURNING - AD.OFFDAYS - AD.ABROAD - AD.VIRUS ";
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + AD.OFFDAYS ";
        }
        $query .= "     AS CLASSDAYS2, "; //出席すべき日数
        $query .= "     AD.NONOTICE, ";          //欠席
        $query .= "     ((AD.LESSON - (AD.SUSPEND + AD.MOURNING + AD.OFFDAYS + AD.ABROAD + AD.VIRUS)) - (AD.SICK + AD.NOTICE + AD.NONOTICE)) AS CLASSDAYS3, ";  //出席日数
        $query .= "     AD.LATEDETAIL, ";        //遅刻回数
        $query .= "     AD.KEKKA_JISU, ";        //早退時数
        $query .= "     AD.KEKKA, ";             //早退回数
        $query .= "     SUMAD.SUM_CLASSDAYS, ";  //累積・授業日数
        $query .= "     SUMAD.SUM_SUSPEND, ";    //累積・出停
        $query .= "     SUMAD.SUM_VIRUS, ";      //累積・伝染病
        $query .= "     SUMAD.SUM_MOURNING, ";   //累積・忌引
        $query .= "     SUMAD.SUM_CLASSDAYS - SUMAD.SUM_SUSPEND - SUMAD.SUM_MOURNING - SUMAD.SUM_OFFDAYS - SUMAD.SUM_ABROAD - SUMAD.SUM_VIRUS ";
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + SUMAD.SUM_OFFDAYS ";
        }
        $query .= "     AS SUM_CLASSDAYS2, "; //累積・出席すべき日数
        $query .= "     SUMAD.SUM_SICK + SUMAD.SUM_NOTICE + SUMAD.SUM_NONOTICE ";
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + SUMAD.SUM_OFFDAYS ";
        }
        $query .= "     AS SUM_SICK, ";  //累積・欠席日数
        $query .= "     ((SUMAD.SUM_CLASSDAYS - (SUMAD.SUM_SUSPEND + SUMAD.SUM_MOURNING + SUMAD.SUM_OFFDAYS + SUMAD.SUM_ABROAD + SUMAD.SUM_VIRUS)) - (SUMAD.SUM_SICK + SUMAD.SUM_NOTICE + SUMAD.SUM_NONOTICE)) AS SUM_CLASSDAYS3, ";  //累積・出席日数
        $query .= "     SUMAD.SUM_LATEDETAIL, "; //累積・遅刻回数
        $query .= "     SUMAD.SUM_KEKKA_JISU, "; //累積・欠課時数
        $query .= "     SUMAD.SUM_KEKKA ";       //累積・早退回数
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT SD ";

        //校種対応
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_REGD_GDAT SGD ";
        $query .= " ON ";
        $query .= "     SD.YEAR = SGD.YEAR AND ";
        $query .= "     SD.GRADE = SGD.GRADE ";

        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_BASE_MST SM ";
        $query .= " ON ";
        $query .= "     SD.SCHREGNO = SM.SCHREGNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         * ";
        $query .= "      FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "      WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND MONTH = '".$monthsem[0]."' ";
        $query .= "         AND SEMESTER = '".$monthsem[1]."' ) AS AD ";
        $query .= " ON ";
        $query .= "     AD.SCHREGNO = SD.SCHREGNO ";
                    
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
        $query .= "         SUM(SUSPEND) AS SUM_SUSPEND, ";
        $query .= "         SUM(VIRUS) AS SUM_VIRUS, ";
        $query .= "         SUM(MOURNING) AS SUM_MOURNING, ";
        $query .= "         SUM(SICK) AS SUM_SICK, ";
        $query .= "         SUM(NOTICE) AS SUM_NOTICE, ";
        $query .= "         SUM(NONOTICE) AS SUM_NONOTICE, ";
        $query .= "         SUM(LATEDETAIL) AS SUM_LATEDETAIL, ";
        $query .= "         SUM(KEKKA_JISU) AS SUM_KEKKA_JISU, ";
        $query .= "         SUM(KEKKA) AS SUM_KEKKA ";
        $query .= "      FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "      WHERE ";
        $query .= "            YEAR = '".CTRL_YEAR."' ";
        if ($monthsem[0] != "" && $monthsem[0] != null) {
            $query .= "        AND MONTH IN( ".$range_month[$monthsem[0]].")";
            $query .= "        AND SEMESTER <= '".$monthsem[1]."' ";
        }
        $query .= "      GROUP BY ";
        $query .= "         SCHREGNO) AS SUMAD ";
        $query .= " ON ";
        $query .= "     SUMAD.SCHREGNO = SD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     SD.YEAR = '".CTRL_YEAR."' AND ";
        
        #2005/05/11 attend_semes_datにデータが存在しない場合でも、表示可能へ変更
        if ($monthsem[0] != "" && $monthsem[0] != null) {
            $query .= "     SD.SEMESTER = '".$monthsem[1]."' AND";
        } else {
            $query .= "     SD.SEMESTER IS NULL  AND";
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     SGD.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SGD.SCHOOL_KIND IN ('".SCHOOLKIND."') AND ";
        }

        $query .= "     SD.GRADE = '".$model->field["grade"]."' AND ";
        $query .= "     SD.HR_CLASS = '".$model->field["class"]."' ";
        $query .= " ORDER BY ";
        $query .= "     SD.ATTENDNO ";

        return $query;
    }

    /* 出欠月別累積データの更新 */
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $monthsem = array();
        $monthsem = preg_split("/-/", $model->field["month"]);

        foreach ($model->field["SCHREGNO"] as $key => $schregno) {
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

            if ($flag) {
                /* 更新対象データがある時 - UPDATE */
                $where  = " WHERE ";
                $where .= "     year = '".CTRL_YEAR."' AND ";
                $where .= "     month = '".$monthsem[0]."' AND ";
                $where .= "     SEMESTER = '".$monthsem[1]."' AND ";
                $where .= "     schregno = '".$schregno."' ";
            } else {
                /* 更新対象データがない時 - INSERT */
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["MONTH"][TEXT]    = $monthsem[0];
                $data["SEMESTER"][TEXT] = $monthsem[1];
                $data["SCHREGNO"][TEXT] = $schregno;
                $data["LATE"][NUMBER]   = 0;    /*遅刻回数*/    /* 今までの遅刻回数と早退回数は    */
                $data["EARLY"][NUMBER]  = 0;    /*早退回数*/    /* インサートの時だけ0をセットする */
                $data["SUSPEND"][NUMBER] = 0;   //出停日数
            }

            /* 更新データをセット */
            $data["APPOINTED_DAY"][TEXT] = $model->appointed_day; //締め日
            $data["LESSON"][NUMBER]      = ($model->field["LESSON"][$key])  ?    $model->field["LESSON"][$key] : 0;     //授業日数
            $data["OFFDAYS"][NUMBER]     = ($model->field["OFFDAYS"][$key]) ?    $model->field["OFFDAYS"][$key] : 0;    //休学日数
            $data["ABROAD"][NUMBER]      = ($model->field["ABROAD"][$key]) ?     $model->field["ABROAD"][$key] : 0;     //留学日数
            $data["ABSENT"][NUMBER]      = ($model->field["ABSENT"][$key]) ?     $model->field["ABSENT"][$key] : 0;     //公欠日数
            $data["VIRUS"][NUMBER]       = ($model->field["VIRUS"][$key]) ?      $model->field["VIRUS"][$key] : 0;      //伝染病
            $data["MOURNING"][NUMBER]    = ($model->field["MOURNING"][$key]) ?   $model->field["MOURNING"][$key] : 0;   //忌引日数
            $data["SICK"][NUMBER]        = ($model->field["SICK"][$key]) ?       $model->field["SICK"][$key] : 0;       //欠席
            $data["NOTICE"][NUMBER]      = ($model->field["NOTICE"][$key]) ?     $model->field["NOTICE"][$key] : 0;     //事故欠届
            $data["NONOTICE"][NUMBER]    = ($model->field["NONOTICE"][$key]) ?   $model->field["NONOTICE"][$key] : 0;   //事故欠無
            $data["LATEDETAIL"][NUMBER]  = ($model->field["LATEDETAIL"][$key]) ? $model->field["LATEDETAIL"][$key] : 0; //遅刻回数
            $data["KEKKA_JISU"][NUMBER]  = ($model->field["KEKKA_JISU"][$key]) ? $model->field["KEKKA_JISU"][$key] : 0; //欠課時数
            $data["KEKKA"][NUMBER]       = ($model->field["KEKKA"][$key]) ?      $model->field["KEKKA"][$key] : 0;      //早退回数
            $data["REGISTERCD"][TEXT]    = STAFFCD ;    //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()"; //更新日付

            if ($flag) {
                /* 更新対象データがある時 - UPDATE */
                $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
            } else {
                /* 更新対象データがない時 - INSERT */
                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
            }

            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
