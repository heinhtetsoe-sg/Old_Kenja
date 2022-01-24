<?php

require_once('for_php7.php');
class knjz020Query extends Query
{

    //校種取得
    public function getKeikokutenKubun($model)
    {
        $query .= "  select ";
        $query .= "      NAMECD2 || ':' ||NAME1 as LABEL, ";
        $query .= "      NAMECD2 as VALUE ";
        $query .= "  from ";
        $query .= "      NAME_MST ";
        $query .= "  where ";
        $query .= "      NAMECD1='Z058' ";
        $query .= "  order by ";
        $query .= "      NAMECD2; ";

        return $query;
    }
    //校種取得
    public function getSchkind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    public function getC001()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'C001' ";
        $query .= "     AND NAMECD2 IN ('2', '19', '25') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    public function checkControlMst($db)
    {
        if ($db->getOne("SELECT COUNT(*) FROM CONTROL_MST") > 0) {
            return true;
        } else {
            return false;
        }
    }

    public function selectYearQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";
        return $query;
    }

    public function checkAssessMst($db)
    {
        if ($db->getOne("SELECT COUNT(*) FROM ASSESS_HDAT") > 0) {
            return true;
        } else {
            return false;
        }
    }

    public function selectQuery($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        if($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     ,L1.SCHOOL_REMARK1 as KEIKOKUTEN_KUBUN ";
            $query .= "     ,L1.SCHOOL_REMARK2 as RUIKEIHEIKIN_BUNSI ";
            $query .= "     ,L1.SCHOOL_REMARK3 as RUIKEIHEIKIN_BUNBO ";
        }
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST T1";
        if($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     LEFT JOIN SCHOOL_DETAIL_DAT L1 ";
        $query .= "          ON T1.YEAR         = L1.YEAR ";
        $query .= "          AND T1.SCHOOLCD    = L1.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND L1.SCHOOL_SEQ  = '009' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". $year ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    public function getAssesscd1()
    {
        $query = "SELECT ASSESSCD, ASSESSMEMO,ASSESSLEVELCNT FROM ASSESS_HDAT WHERE ASSESSCD = '2'";
        return $query;
    }

    public function getAssesscd2()
    {
        $query = "SELECT ASSESSCD || ':' || ASSESSMEMO FROM ASSESS_HDAT WHERE ASSESSCD = '3'";
        return $query;
    }

    public function getSchooldiv()
    {
        return "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z001'";
    }

    //評定計算方法
    public function getGvalCalc()
    {
        return "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z017' ORDER BY NAMECD2";
    }

    public function getSemester($model, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     MAX(SEMESTER) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND SEMESTER <> '9' ";
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     SEMESTER ";
        }

        return $query;
    }

    //都道府県
    public function getPrefCd()
    {
        $query  = " SELECT ";
        $query .= "     PREF_CD || ':' || PREF_NAME AS LABEL, ";
        $query .= "     PREF_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "     PREF_CD ";

        return $query;
    }

    //教育委員会、学校サーバ情報
    public function getDataBaseInfo()
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //教育委員側DB　教育委員会設定学校マスタを取得
    public function getEdboardSchoolcd()
    {
        $query .= " SELECT ";
        $query .= "     EDBOARD_SCHOOLCD AS VALUE, ";
        $query .= "     EDBOARD_SCHOOLCD || ':' || EDBOARD_SCHOOLNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期情報取得
    public function selectSemesAll($year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //月毎の授業週数データ取得
    public function getAttendSyusuMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     MONTH, ";
        $query .= "     SEMESTER, ";
        $query .= "     SYUSU ";
        $query .= " FROM ";
        $query .= "     ATTEND_SYUSU_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     GRADE       = '00' AND ";
        $query .= "     COURSECD    = '0' AND ";
        $query .= "     MAJORCD     = '000' ";

        return $query;
    }

    public function getVNameMstZ042($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'Z042' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //月一覧取得
    public function getMonthList($model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd  = "Z".$model->field["SCHKIND"]."05";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' ";

        return $query;
    }

    //SCHOOL_DETAIL_DATチェック
    public function &getCountDetailMst($model, $year, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$year ."' ";
        $query .= " AND SCHOOL_SEQ = '".$seq."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //CSV 書き出し
    public function getCsvData($model)
    {
        $schoolKind = "";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $schoolKind = $model->field["SCHKIND"];
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $schoolKind = SCHOOLKIND;
        }

        $mflg = false;
        //月別週数
        if (get_count($model->headerMArray) > 0) {
            $query .= "     WITH M_SYUSU AS ( ";
            $query .= "     SELECT ";
            foreach ($model->headerMArray as $semMonth => $label) {
                list($semester, $month) = explode("_", $semMonth);
                $query .= "       SUM(CASE WHEN SEMESTER = '{$semester}' AND MONTH = '{$month}' THEN SYUSU ELSE NULL END) AS HOUTEI_SYUSU_MONTH_{$semMonth}, ";
            }
            $query .= "       YEAR ";
            $query .= "     FROM ";
            $query .= "       ATTEND_SYUSU_MST ";
            $query .= "     WHERE ";
            $query .= "       YEAR = '".CTRL_YEAR."' ";
            $query .= "       AND GRADE = '00' ";
            $query .= "       AND COURSECD = '0' ";
            $query .= "       AND MAJORCD = '000' ";
            $query .= "     GROUP BY YEAR ";
            $query .= "     ) ";

            $mflg = true;
        }

        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.FOUNDEDYEAR, ";
        $query .= "     T1.PRESENT_EST, ";
        $query .= "     T1.CLASSIFICATION, ";
        $query .= "     T1.SCHOOLNAME1, ";
        $query .= "     T1.SCHOOLNAME2, ";
        $query .= "     T1.SCHOOLNAME3, ";
        $query .= "     T1.SCHOOLNAME_ENG, ";
        $query .= "     T1.SCHOOLZIPCD, ";
        $query .= "     T1.SCHOOLADDR1, ";
        $query .= "     T1.SCHOOLADDR2, ";
        $query .= "     T1.SCHOOLADDR1_ENG, ";
        $query .= "     T1.SCHOOLADDR2_ENG, ";
        $query .= "     T1.SCHOOLTELNO, ";
        $query .= "     T1.SCHOOLFAXNO, ";
        $query .= "     T1.SCHOOLMAIL, ";
        $query .= "     T1.SCHOOLURL, ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     T1.SEMESTERDIV, ";
        $query .= "     T1.GRADE_HVAL, ";
        $query .= "     T1.ENTRANCE_DATE, ";
        $query .= "     T1.GRADUATE_DATE, ";
        $query .= "     T1.GRAD_CREDITS, ";
        $query .= "     T1.GRAD_COMP_CREDITS, ";
        $query .= "     T1.SEMES_ASSESSCD, ";
        $query .= "     T1.SEMES_FEARVAL, ";
        $query .= "     T1.GRADE_FEARVAL, ";
        $query .= "     T1.SEM_OFFDAYS, ";
        $query .= "     T1.GVAL_CALC, ";
        $query .= "     T1.SUB_OFFDAYS, ";
        $query .= "     T1.SUB_ABSENT, ";
        $query .= "     T1.SUB_SUSPEND, ";
        $query .= "     T1.SUB_KOUDOME, ";
        $query .= "     T1.SUB_MOURNING, ";
        $query .= "     T1.SUB_VIRUS, ";
        $query .= "     T1.SYUKESSEKI_HANTEI_HOU, ";
        $query .= "     T1.ABSENT_COV, ";
        $query .= "     T1.ABSENT_COV_LATE, ";
        $query .= "     T1.AMARI_KURIAGE, ";
        $query .= "     T1.TOKUBETU_KATUDO_KANSAN, ";
        $query .= "     T1.JUGYOU_JISU_FLG, ";
        $query .= "     T1.RISYU_BUNSI, ";
        $query .= "     T1.RISYU_BUNBO, ";
        $query .= "     T1.SYUTOKU_BUNSI, ";
        $query .= "     T1.SYUTOKU_BUNBO, ";
        $query .= "     T1.RISYU_BUNSI_SPECIAL, ";
        $query .= "     T1.RISYU_BUNBO_SPECIAL, ";
        $query .= "     T1.JOUGENTI_SANSYUTU_HOU, ";
        $query .= "     T1.JITU_JIFUN, ";
        $query .= "     T1.JITU_JIFUN_SPECIAL, ";
        $query .= "     T1.JITU_SYUSU, ";
        $query .= "     T1.KESSEKI_WARN_BUNSI, ";
        $query .= "     T1.KESSEKI_WARN_BUNBO, ";
        $query .= "     T1.KESSEKI_OUT_BUNSI, ";
        $query .= "     T1.KESSEKI_OUT_BUNBO, ";
        $query .= "     T1.PREF_CD, ";
        $query .= "     T1.KYOUIKU_IINKAI_SCHOOLCD, ";
        $query .= "     T1.PARTS_HYOUKA_HYOUTEI_KEISAN, ";
        if ($model->Properties["use_school_Detail_009"]==='1') {
            $query .= "     L1.SCHOOL_REMARK1 as KEIKOKUTEN_KUBUN, ";
            $query .= "     L1.SCHOOL_REMARK2 as RUIKEIHEIKIN_BUNSI, ";
            $query .= "     L1.SCHOOL_REMARK3 as RUIKEIHEIKIN_BUNBO, ";
        }
        if ($model->Properties["hibiNyuuryokuNasi"] === '1') {
            if ($model->Properties["use_school_detail_gcm_dat"] != "1" && $model->Properties["use_Month_Syusu"] == "1") {
                foreach ($model->headerMArray as $semeMonth => $label) {
                    $query .= "     T2.HOUTEI_SYUSU_MONTH_{$semeMonth}, ";
                }
            } else {
                foreach ($model->headerSArray as $semester => $label) {
                    $query .= "     T1.HOUTEI_SYUSU_SEMESTER{$semester}, ";
                }
            }
        }
        $query .= "     '{$model->lastColumn}' AS LASTCOLUMN ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST T1";
        if ($mflg) {
            $query .= " LEFT JOIN M_SYUSU T2 ON T2.YEAR = T1.YEAR ";
        }
        if ($model->Properties["use_school_Detail_009"]==='1') {
            $query .= "     LEFT JOIN SCHOOL_DETAIL_DAT L1 ";
            $query .= "          ON T1.YEAR         = L1.YEAR ";
            $query .= "          AND T1.SCHOOLCD    = L1.SCHOOLCD ";
            $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "          AND L1.SCHOOL_SEQ  = '009' ";
        }
        if ($schoolKind != "") {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     T1.SCHOOL_KIND = '{$schoolKind}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //指定年度情報取得
    public function getCopyData($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR_HIDDEN"]."' ";
        if($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        }
        return $query;
    }

    //月別授業週数データ取得
    public function getCopyDataSyusu($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_SYUSU_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR_HIDDEN"]."' ";

        return $query;
    }

    /*****************************************
        学校情報マスタ更新(全レコード）
     *****************************************/
    public function updateSchoolMst($db, $model, $recordData)
    {
        $query  = " DELETE FROM SCHOOL_MST WHERE YEAR = '{$recordData["YEAR"]}'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]                 = $recordData["YEAR"];
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]          = $model->field["SCHKIND"];
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]          = SCHOOLKIND;
        }
        $data["FOUNDEDYEAR"][TEXT]          = $recordData["FOUNDEDYEAR"];
        $data["PRESENT_EST"][TEXT]          = $recordData["PRESENT_EST"];
        $data["CLASSIFICATION"][TEXT]       = $recordData["CLASSIFICATION"];
        $data["SCHOOLNAME1"][TEXT]          = $recordData["SCHOOLNAME1"];
        $data["SCHOOLNAME2"][TEXT]          = $recordData["SCHOOLNAME2"];
        $data["SCHOOLNAME3"][TEXT]          = $recordData["SCHOOLNAME3"];
        $data["SCHOOLNAME_ENG"][TEXT]       = $recordData["SCHOOLNAME_ENG"];
        $data["SCHOOLZIPCD"][TEXT]          = $recordData["SCHOOLZIPCD"];
        $data["SCHOOLADDR1"][TEXT]          = $recordData["SCHOOLADDR1"];
        $data["SCHOOLADDR2"][TEXT]          = $recordData["SCHOOLADDR2"];
        $data["SCHOOLADDR1_ENG"][TEXT]      = $recordData["SCHOOLADDR1_ENG"];
        $data["SCHOOLADDR2_ENG"][TEXT]      = $recordData["SCHOOLADDR2_ENG"];
        $data["SCHOOLTELNO"][TEXT]          = $recordData["SCHOOLTELNO"];
        $data["SCHOOLFAXNO"][TEXT]          = $recordData["SCHOOLFAXNO"];
        $data["SCHOOLMAIL"][TEXT]           = $recordData["SCHOOLMAIL"];
        $data["SCHOOLURL"][TEXT]            = $recordData["SCHOOLURL"];
        $data["SCHOOLDIV"][TEXT]            = $recordData["SCHOOLDIV"];
        $data["SEMESTERDIV"][TEXT]          = $recordData["SEMESTERDIV"];
        $data["GRADE_HVAL"][TEXT]           = $recordData["GRADE_HVAL"];
        $data["ENTRANCE_DATE"][FUNC]        = ($recordData["ENTRANCE_DATE"] != "") ? "DATE('".str_replace("/", "-", $recordData["ENTRANCE_DATE"])."')" : "NULL";
        $data["GRADUATE_DATE"][FUNC]        = ($recordData["GRADUATE_DATE"] != "") ? "DATE('".str_replace("/", "-", $recordData["GRADUATE_DATE"])."')" : "NULL";
        $data["GRAD_CREDITS"][NUMBER]       = $recordData["GRAD_CREDITS"];
        $data["GRAD_COMP_CREDITS"][NUMBER]  = $recordData["GRAD_COMP_CREDITS"];
        $data["SEMES_ASSESSCD"][TEXT]       = $recordData["SEMES_ASSESSCD"];
        $data["SEMES_FEARVAL"][NUMBER]      = $recordData["SEMES_FEARVAL"];
        $data["GRADE_FEARVAL"][NUMBER]      = $recordData["GRADE_FEARVAL"];
        $data["ABSENT_COV"][TEXT]           = $recordData["ABSENT_COV"];
        $data["ABSENT_COV_LATE"][NUMBER]    = $recordData["ABSENT_COV_LATE"];
        $data["GVAL_CALC"][TEXT]            = $recordData["GVAL_CALC"];
        $data["SEM_OFFDAYS"][TEXT]          = strlen($recordData["SEM_OFFDAYS"]) ? "1" : "";
        $data["SUB_OFFDAYS"][TEXT]          = strlen($recordData["SUB_OFFDAYS"]) ? "1" : "";
        $data["SUB_MOURNING"][TEXT]         = strlen($recordData["SUB_MOURNING"]) ? "1" : "";
        $data["SUB_SUSPEND"][TEXT]          = strlen($recordData["SUB_SUSPEND"]) ? "1" : "";
        if ($model->virus == 'true') {
            $data["SUB_VIRUS"][TEXT]        = strlen($recordData["SUB_VIRUS"]) ? "1" : "";
        }
        $data["SUB_ABSENT"][TEXT]           = strlen($recordData["SUB_ABSENT"]) ? "1" : "";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_MST");
        $db->query($query);

        return true;
    }

    /***************************************************
      学期マスタ更新(SEMESERDIVの数だけレコードを作成)
     ***************************************************/
    public function updateSemesterMst($db, $semesterdiv, $year)
    {
        $query  = " SELECT COUNT(*) FROM SEMESTER_MST";
        $query .= " WHERE YEAR = '". $year ."'";
        $query .= "   AND SEMESTER <> '9'";

        $cur_num = $db->getOne($query);

        //テーブルにデータがない場合すべての学期数のレコードを作成
        if ($cur_num == 0) {
            for ($i = 1; $i <= $semesterdiv; $i++) {
                $query  = "INSERT INTO SEMESTER_MST (YEAR, SEMESTER, REGISTERCD, UPDATED) ";
                $query .= " VALUES ('". $year . "','".$i."','".STAFFCD."',sysdate())";
                $db->query($query);
            }
            $query  = "INSERT INTO SEMESTER_MST (YEAR, SEMESTER, REGISTERCD, UPDATED) ";
            $query .= " VALUES ('". $year . "','9','".STAFFCD."',sysdate())";
            $db->query($query);

        //学期数が既存学期数より多い場合はレコードを追加
        } elseif ($cur_num < $semesterdiv) {
            for ($i = $cur_num + 1; $i <= $semesterdiv; $i++) {
                $query  = "INSERT INTO SEMESTER_MST (YEAR, SEMESTER, REGISTERCD, UPDATED) ";
                $query .= " VALUES ('". $year . "','".$i."','".STAFFCD."',sysdate())";
                $db->query($query);
            }
            //学期数が既存学期数より少ない場合はレコードを削除
        } elseif ($cur_num > $semesterdiv) {
            for ($i = $semesterdiv + 1; $i <= $cur_num; $i++) {
                $query  = "DELETE FROM SEMESTER_MST";
                $query .= " WHERE YEAR = '". $year ."' AND SEMESTER = '" .$i ."'";
                $db->query($query);
            }
        }
    }

    //更新 --SCHOOL_DETAIL_DAT
    public function updateSchoolDetailDat($db, $model, $recordData)
    {

        //削除条件
        $where = "";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $where .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $where .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $where .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        //SEQ毎にDELETE INSERT
        $seqArray = array("001", "002", "003", "004", "005", "006", "007", "009");
        foreach ($seqArray as $seq) {
            //削除
            $db->query("DELETE FROM SCHOOL_DETAIL_DAT WHERE YEAR = '{$recordData["YEAR"]}' AND SCHOOL_SEQ = '{$seq}'".$where);

            //追加
            $data = array();
            $data["YEAR"][TEXT]                 = $recordData["YEAR"];
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHKIND"];
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
            }
            $data["SCHOOL_SEQ"][TEXT]      = $seq;
            if ($seq == "001") {
                $data["SCHOOL_REMARK1"][TEXT]  = $recordData["JUGYOU_JISU_FLG"];
                $data["SCHOOL_REMARK2"][TEXT]  = $recordData["RISYU_BUNSI"];
                $data["SCHOOL_REMARK3"][TEXT]  = $recordData["RISYU_BUNBO"];
                $data["SCHOOL_REMARK4"][TEXT]  = $recordData["SYUTOKU_BUNSI"];
                $data["SCHOOL_REMARK5"][TEXT]  = $recordData["SYUTOKU_BUNBO"];
                $data["SCHOOL_REMARK6"][TEXT]  = $recordData["JITU_JIFUN"];
                $data["SCHOOL_REMARK7"][TEXT]  = $recordData["JITU_SYUSU"];
                $data["SCHOOL_REMARK8"][TEXT]  = $recordData["JOUGENTI_SANSYUTU_HOU"];
                $data["SCHOOL_REMARK9"][TEXT]  = $recordData["AMARI_KURIAGE"];
            } elseif ($seq == "002") {
                $data["SCHOOL_REMARK1"][TEXT]  = $recordData["PREF_CD"];
                if ($model->dataBaseinfo === '2') {
                    $data["SCHOOL_REMARK3"][TEXT]  = $recordData["KYOUIKU_IINKAI_SCHOOLCD"];
                }
            } elseif ($seq == "003") {
                $data["SCHOOL_REMARK2"][TEXT]  = $recordData["RISYU_BUNSI_SPECIAL"];
                $data["SCHOOL_REMARK3"][TEXT]  = $recordData["RISYU_BUNBO_SPECIAL"];
                $data["SCHOOL_REMARK4"][TEXT]  = $recordData["RISYU_BUNSI_SPECIAL"];
                $data["SCHOOL_REMARK5"][TEXT]  = $recordData["RISYU_BUNBO_SPECIAL"];
                $data["SCHOOL_REMARK6"][TEXT]  = $recordData["TOKUBETU_KATUDO_KANSAN"];
                $data["SCHOOL_REMARK7"][TEXT]  = $recordData["JITU_JIFUN_SPECIAL"];
            } elseif ($seq == "004") {
                $data["SCHOOL_REMARK2"][TEXT]  = $recordData["KESSEKI_WARN_BUNSI"];
                $data["SCHOOL_REMARK3"][TEXT]  = $recordData["KESSEKI_WARN_BUNBO"];
                $data["SCHOOL_REMARK4"][TEXT]  = $recordData["KESSEKI_OUT_BUNSI"];
                $data["SCHOOL_REMARK5"][TEXT]  = $recordData["KESSEKI_OUT_BUNBO"];
            } elseif ($seq == "005") {
                $data["SCHOOL_REMARK1"][TEXT] = $recordData["SYUKESSEKI_HANTEI_HOU"];
                if ($model->koudome == 'true') {
                    $data["SCHOOL_REMARK2"][TEXT] = $recordData["SUB_KOUDOME"];
                }
            } elseif ($seq == "006") {
                for ($sem = 1; $sem <= $recordData["SEMESTERDIV"]; $sem++) {
                    $data["SCHOOL_REMARK".$sem][TEXT] = $recordData["HOUTEI_SYUSU_SEMESTER".$sem];
                }
            } elseif ($seq == "007") {
                $data["SCHOOL_REMARK1"][TEXT]  = $recordData["PARTS_HYOUKA_HYOUTEI_KEISAN"];
            } elseif ($seq == "009") {
                $data["SCHOOL_REMARK1"][TEXT]  = $recordData["KEIKOKUTEN_KUBUN"];
                $data["SCHOOL_REMARK2"][TEXT]  = $recordData["RUIKEIHEIKIN_BUNSI"];
                $data["SCHOOL_REMARK3"][TEXT]  = $recordData["RUIKEIHEIKIN_BUNBO"];
            }
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
            $db->query($query);
        }

        return true;
    }

    public function updateSyusuMst($db, $recordData)
    {
        //削除
        $db->query("DELETE FROM ATTEND_SYUSU_MST  WHERE YEAR = '{$recordData["YEAR"]}'");

        $semMonthInfo = array();
        $semMonthInfo = knjz020Query::getSemesterMonth($db, $recordData["YEAR"]);
        //追加
        foreach ($semMonthInfo as $key => $semeMonth) {
            //HOUTEI_SYUSU_MONTHの入力値があればINSERT
            list($month, $semester) = explode("_", $semeMonth);

            $data = array();
            $data["YEAR"][TEXT]         = $recordData["YEAR"];
            $data["MONTH"][TEXT]        = $month;
            $data["SEMESTER"][TEXT]     = $semester;
            $data["GRADE"][TEXT]        = "00";
            $data["COURSECD"][TEXT]     = "0";
            $data["MAJORCD"][TEXT]      = "000";
            $data["SYUSU"][NUMBER]      = $recordData["HOUTEI_SYUSU_MONTH_{$semester}_{$month}"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ATTEND_SYUSU_MST");
            $db->query($query);
        }
    }

    public function getSemesterMonth($db, $year)
    {
        //月別授業週数
        $semMonthInfo = array();
        $query = knjz020Query::selectSemesAll($year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                $month = $i;
                if ($i > 12) {
                    $month = $i - 12;
                }
                $month = sprintf('%02d', $month);

                $semMonthInfo[] = $month."_".$row["SEMESTER"];
            }
        }
        $result->free();

        return $semMonthInfo;
    }


    /* ヘッダ作成用関数 */ //※ヘッダ項目の数は年度によって可変のため、前年度のなかでの最大項目数の場合で考える。

    //月別学期週数
    public function getHeaderMSyusu($model)
    {
        $db = Query::dbCheckOut();

        $monthNameArray = array();
        $query = knjz020Query::getMonthList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $monthNameArray[$row["NAMECD2"]] = $row["NAME1"];
        }

        $semesNameArray = array();
        $query = knjz020Query::selectSemesAll($model->year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semesNameArray[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }

        $query  = "     SELECT ";
        $query .= "       MONTH, ";
        $query .= "       SEMESTER ";
        $query .= "     FROM ";
        $query .= "       ATTEND_SYUSU_MST ";
        $query .= "     WHERE ";
        $query .= "       GRADE = '00' ";
        $query .= "       AND COURSECD = '0' ";
        $query .= "       AND MAJORCD = '000' ";
        $query .= "     GROUP BY ";
        $query .= "       SEMESTER, ";
        $query .= "       MONTH ";
        $query .= "     ORDER BY ";
        $query .= "       CASE WHEN MONTH >= '04' THEN 1 ELSE 2 END, ";
        $query .= "       MONTH, ";
        $query .= "       SEMESTER ";

        $headerArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (strlen($monthNameArray[$row["MONTH"]]) > 0) {
                $headerArray[$row["SEMESTER"]."_".$row["MONTH"]] = $monthNameArray[$row["MONTH"]]." (".$semesNameArray[$row["SEMESTER"]].") 週数";
            }
        }

        Query::dbCheckIn($db);

        return $headerArray;
    }

    //学期別週数
    public function getHeaderSSyusu($model)
    {
        $db = Query::dbCheckOut();

        $query  = "     WITH SEMESTER_HEAD AS ( ";
        $query .= "       SELECT ";
        $query .= "         SEMESTER ";
        $query .= "       FROM ";
        $query .= "         SEMESTER_MST ";
        $query .= "       WHERE ";
        $query .= "         SEMESTER <> '9' ";
        $query .= "       GROUP BY ";
        $query .= "         SEMESTER ";
        $query .= "     ) ";
        $query .= "     SELECT ";
        $query .= "       T1.SEMESTER, ";
        $query .= "       T2.SEMESTERNAME ";
        $query .= "     FROM ";
        $query .= "       SEMESTER_HEAD T1 ";
        $query .= "       LEFT JOIN SEMESTER_MST T2 ";
        $query .= "         ON T2.YEAR = '{$model->year}' "; //名称は当年度のものを使う
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     ORDER BY ";
        $query .= "       T1.SEMESTER ";

        $headerArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headerArray[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }

        Query::dbCheckIn($db);

        return $headerArray;
    }
    public function getDBDuplicate($db, $semesterdiv, $year)
    {
        $query  = " SELECT COUNT(*) FROM SEMESTER_MST";
        $query .= " WHERE YEAR = '". $year ."'";
        $query .= "   AND SEMESTER <> '9'";

        return $query;
    }
}
