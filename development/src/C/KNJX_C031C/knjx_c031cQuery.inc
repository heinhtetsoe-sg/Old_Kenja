<?php

require_once('for_php7.php');

class knjx_c031cQuery extends Query {

    function getSecurityHigh($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$model->getPrgId}' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度学期一覧
    function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
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

    //学期取得
    function selectSemesAll($model)
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
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月データの取得
    function selectMonthQuery($year, $month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, NAME1, NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$year."' ";
        $query .= "     AND NAMECD1 = '{$setNameCd}' ";
        $query .= "     AND NAMECD2 IN (SELECT CONTROL_CODE FROM ADMIN_CONTROL_DAT WHERE YEAR = '".$year."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '2') ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

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
        $query .= "     AND NAMECD2 IN ('001', '002', '003', '004') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $data = array();
            $appointed_day = $db->getOne(knjx_c031cQuery::getAppointedDay($data_arr[$i]["YEAR"], $model->grade, $data_arr[$i]["MONTH"], $data_arr[$i]["SEMESTER"]));

            $data["COPYCD"][TEXT]           = '0';
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["APPOINTED_DAY"][TEXT]    = $appointed_day;
            $data["LESSON"][NUMBER]         = ($data_arr[$i]["LESSON"]) ? $data_arr[$i]["LESSON"] : 0;
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                $data["OFFDAYS"][NUMBER]        = $data_arr[$i]["OFFDAYS"];
                $data["ABSENT"][NUMBER]         = $data_arr[$i]["ABSENT"];
                $data["SUSPEND"][NUMBER]        = $data_arr[$i]["SUSPEND"];
                if ($model->Properties["useKoudome"] == "true") {
                    $data["KOUDOME"][NUMBER]    = $data_arr[$i]["KOUDOME"];
                }
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]      = $data_arr[$i]["VIRUS"];
                }
                $data["MOURNING"][NUMBER]       = $data_arr[$i]["MOURNING"];
                $data["ABROAD"][NUMBER]         = $data_arr[$i]["ABROAD"];
                $data["SICK"][NUMBER]           = $data_arr[$i]["SICK"];
                $data["NOTICE"][NUMBER]         = $data_arr[$i]["NOTICE"];
                $data["NONOTICE"][NUMBER]       = $data_arr[$i]["NONOTICE"];
                $data["LATE"][NUMBER]           = $data_arr[$i]["LATE"];
                $data["EARLY"][NUMBER]          = $data_arr[$i]["EARLY"];
            } else {
                $data["OFFDAYS"][NUMBER]        = ($data_arr[$i]["OFFDAYS"]) ?  $data_arr[$i]["OFFDAYS"] : 0;
                $data["ABSENT"][NUMBER]         = ($data_arr[$i]["ABSENT"]) ?   $data_arr[$i]["ABSENT"] : 0;
                $data["SUSPEND"][NUMBER]        = ($data_arr[$i]["SUSPEND"]) ?  $data_arr[$i]["SUSPEND"] : 0;
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
                    $data["KOUDOME"][NUMBER]    = ($data_arr[$i]["KOUDOME"]) ?  $data_arr[$i]["KOUDOME"] : 0;
                }
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]      = ($data_arr[$i]["VIRUS"]) ?    $data_arr[$i]["VIRUS"] : 0;
                }
                $data["MOURNING"][NUMBER]       = ($data_arr[$i]["MOURNING"]) ? $data_arr[$i]["MOURNING"] : 0;
                $data["ABROAD"][NUMBER]         = ($data_arr[$i]["ABROAD"]) ?   $data_arr[$i]["ABROAD"] : 0;
                $data["SICK"][NUMBER]           = ($data_arr[$i]["SICK"]) ?     $data_arr[$i]["SICK"] : 0;
                $data["NOTICE"][NUMBER]         = ($data_arr[$i]["NOTICE"]) ?   $data_arr[$i]["NOTICE"] : 0;
                $data["NONOTICE"][NUMBER]       = ($data_arr[$i]["NONOTICE"]) ? $data_arr[$i]["NONOTICE"] : 0;
                $data["LATE"][NUMBER]           = ($data_arr[$i]["LATE"]) ?     $data_arr[$i]["LATE"] : 0;
                $data["EARLY"][NUMBER]          = ($data_arr[$i]["EARLY"]) ?    $data_arr[$i]["EARLY"] : 0;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $check  = " SELECT COUNT(*) FROM ATTEND_SEMES_DAT ";
            $check .= " WHERE COPYCD    = '0' AND ";
            $check .= "       YEAR      = '".$data_arr[$i]["YEAR"]."' AND ";
            $check .= "       MONTH     = '".$data_arr[$i]["MONTH"]."' AND ";
            $check .= "       SEMESTER  = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $check .= "       SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";

            if (1 > $db->getOne($check)) {
                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                $where .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
                $where .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "     COPYCD      = '0' ";

                $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
            }
            $db->query($query);

            //DETAIL更新
            //詳細種別
            $titleDetailName = array();
            $query = knjx_c031cQuery::getDetailDiv();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $titleDetailName[$row["VALUE"]] = $row["LABEL"];
            }

            foreach ($titleDetailName as $c002Key => $c002Val) {
                $data = array();
                $data["COPYCD"][TEXT]           = '0';
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
                $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["SEQ"][TEXT]              = $c002Key;
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $data["CNT"][NUMBER]            = $data_arr[$i]["DETAIL_".$c002Key];
                } else {
                    $data["CNT"][NUMBER]            = ($data_arr[$i]["DETAIL_".$c002Key]) ? $data_arr[$i]["DETAIL_".$c002Key] : 0;
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $check  = " SELECT COUNT(*) FROM ATTEND_SEMES_DETAIL_DAT ";
                $check .= " WHERE COPYCD    = '0' ";
                $check .= "       AND YEAR      = '".$data_arr[$i]["YEAR"]."' ";
                $check .= "       AND MONTH     = '".$data_arr[$i]["MONTH"]."' ";
                $check .= "       AND SEMESTER  = '".$data_arr[$i]["SEMESTER"]."' ";
                $check .= "       AND SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                $check .= "       AND SEQ  = '".$c002Key."' ";

                if (1 > $db->getOne($check)) {
                    $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' ";
                    $where .= "     AND MONTH       = '".$data_arr[$i]["MONTH"]."' ";
                    $where .= "     AND SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' ";
                    $where .= "     AND SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $where .= "     AND COPYCD      = '0' ";
                    $where .= "     AND SEQ  = '".$c002Key."' ";

                    $query = Query::updateSQL($data, "ATTEND_SEMES_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
            $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     COPYCD      = '0' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //存在チェック
    function checkRegdDat($year, $semester, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;    
    }

    //締め日の取得
    function getAppointedDay($year, $grade, $month, $semester) {
        $query .= "  SELECT ";
        $query .= "    APPOINTED_DAY ";
        $query .= "  FROM ";
        $query .= "    APPOINTED_DAY_MST AM ";
        $query .= "    INNER JOIN SCHREG_REGD_GDAT SGD ";
        $query .= "      ON AM.YEAR = SGD.YEAR ";
        $query .= "  	 AND AM.SCHOOL_KIND = SGD.SCHOOL_KIND ";
        $query .= "  WHERE ";
        $query .= "    AM.YEAR = '". $year ."' ";
        $query .= "    AND AM.MONTH = '".$month."' ";
        $query .= "    AND AM.SEMESTER = '".$semester."' ";
        $query .= "    AND SGD.GRADE = '".$grade."' ";

        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    function selectMainQuery($model, $titleName, $titleDetailName)
    {
        $query  = " SELECT ";
        $query .= "     S2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     S1.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     S4.MONTH, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S5.APPOINTED_DAY, ";
        $query .= "     S4.LESSON, ";
        $query .= "     S4.OFFDAYS, ";
        $query .= "     S4.ABROAD, ";
        $query .= "     S4.ABSENT, ";
        $query .= "     S4.SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     S4.KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     S4.VIRUS, ";
        }
        $query .= "     S4.MOURNING, ";

        $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
        foreach ($titleName as $key => $val) {
            $query .= "     S4.".$setFieldName[$key].", ";
        }

        $query .= "     S4.LATE, ";
        $query .= "     S4.EARLY, ";

        $setDetailName = array("001" => "REIHAI_KEKKA", "002" => "M_KEKKA_JISU", "003" => "REIHAI_TIKOKU", "004" => "JYUGYOU_TIKOKU");
        foreach ($titleDetailName as $key => $val) {
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                $query .= "     L".$key.".CNT AS DETAIL_".$key.", ";
            } else {
                $query .= "     S4.".$setDetailName[$key].", ";
            }
        }
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ON T1.YEAR = S2.YEAR ";
        $query .= "                                  AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "                                  AND T1.GRADE = S2.GRADE ";
        $query .= "                                  AND T1.HR_CLASS = S2.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT S3 ON T1.YEAR = S3.YEAR ";
        $query .= "                                  AND T1.GRADE = S3.GRADE ";
        $query .= "     LEFT JOIN V_ATTEND_SEMES_DAT S4 ON T1.YEAR = S4.YEAR ";
        $query .= "                                  AND T1.SCHREGNO = S4.SCHREGNO ";
        $query .= "                                  AND S4.MONTH || '-' || S4.SEMESTER  = '".$model->field["MONTH"]."' ";
        $query .= "     LEFT JOIN APPOINTED_DAY_MST S5 ON T1.YEAR = S5.YEAR ";
        $query .= "                                  AND S3.SCHOOL_KIND = S5.SCHOOL_KIND ";
        $query .= "                                  AND S4.MONTH || '-' || S4.SEMESTER = S5.MONTH || '-' || S5.SEMESTER ";
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            foreach ($titleDetailName as $key => $val) {
                $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L".$key." ";
                $query .= "                              ON S4.COPYCD       = L".$key.".COPYCD ";
                $query .= "                             AND S4.YEAR         = L".$key.".YEAR ";
                $query .= "                             AND S4.MONTH        = L".$key.".MONTH ";
                $query .= "                             AND S4.SEMESTER     = L".$key.".SEMESTER ";
                $query .= "                             AND S4.SCHREGNO     = L".$key.".SCHREGNO ";
                $query .= "                             AND L".$key.".SEQ   = '".$key."' ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
?>
