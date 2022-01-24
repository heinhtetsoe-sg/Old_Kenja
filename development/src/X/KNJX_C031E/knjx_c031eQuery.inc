<?php

require_once('for_php7.php');
class knjx_c031eQuery extends Query
{
    public function getSecurityHigh($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$model->getPrgId}' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
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
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1" && SCHOOLKIND != "") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "              ON T1.YEAR         = T3.YEAR ";
                $query .= "             AND T1.GRADE        = T3.GRADE ";
                $query .= "             AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "              ON T1.YEAR         = T3.YEAR ";
            $query .= "             AND T1.GRADE        = T3.GRADE ";
            $query .= "             AND T3.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        if ($model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " WHERE ";
            $query .= "     T1.TR_CD1       = '".STAFFCD."' OR ";
            $query .= "     T1.TR_CD2       = '".STAFFCD."' OR ";
            $query .= "     T1.TR_CD3       = '".STAFFCD."' OR ";
            $query .= "     T1.SUBTR_CD1    = '".STAFFCD."' OR ";
            $query .= "     T1.SUBTR_CD2    = '".STAFFCD."' OR ";
            $query .= "     T1.SUBTR_CD3    = '".STAFFCD."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1" && SCHOOLKIND != "") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR = '".substr($model->field["YEAR"], 0, 4)."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "              ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR = '".substr($model->field["YEAR"], 0, 4)."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "              ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //複式学級取得
    public function getGroupHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GHR_CD AS VALUE, ";
        $query .= "     GHR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER    = '".$model->field["YEAR"]."' ";
        if ($model->auth == DEF_UPDATE_RESTRICT) {
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

    //学期取得
    public function selectSemesAll($model)
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

    //校種取得
    public function getSchoolKind($year, $grade)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     GRADE   = '".$grade."' ";

        return $query;
    }

    //対象月データの取得
    public function selectMonthQuery($year, $month, $model, $schoolKind)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && $schoolKind) {
            $setNameCd = "Z".$schoolKind."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, NAME1, NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$year."' ";
        $query .= "     AND NAMECD1 = '{$setNameCd}' ";
        $query .= "     AND NAMECD2 IN (SELECT CONTROL_CODE FROM ADMIN_CONTROL_DAT WHERE YEAR = '".$year."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            if ($schoolKind) {
                $query .= "             AND SCHOOL_KIND = '".$schoolKind."' ";
            } else {
                $query .= "             AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "                     AND CONTROL_FLG = '2') ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d', $month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //A004
    public function getA004()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A004' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //C001
    public function getC001()
    {
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

    //C002
    public function getC002()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'C002' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //フィールド一覧取得
    public function getFieldList()
    {
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

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //フィールド取得
        $query = knjx_c031eQuery::getFieldList();
        $table_field = array();
        $table_field = $db->getCol($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $appointed_day = $db->getOne(knjx_c031eQuery::getAppointedDay($data_arr[$i]["YEAR"], $data_arr[$i]["MONTH"], $data_arr[$i]["SEMESTER"], $model->grade));

            $data = array();
            $data["COPYCD"][TEXT]           = '0';
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["APPOINTED_DAY"][TEXT]    = $appointed_day;
            $data["LESSON"][NUMBER]         = $data_arr[$i]["LESSON"];

            foreach ($table_field as $fld) {
                if ($fld == "OFFDAYS" && $model->titleValA004["2"]) {
                    $data["OFFDAYS"][NUMBER]    = $data_arr[$i]["OFFDAYS"];
                } elseif ($fld == "ABROAD" && $model->titleValA004["1"]) {
                    $data["ABROAD"][NUMBER]     = $data_arr[$i]["ABROAD"];
                } elseif ($fld == "ABSENT" && $model->titleValC001["1"]) {
                    $data["ABSENT"][NUMBER]     = $data_arr[$i]["ABSENT"];
                } elseif ($fld == "SUSPEND" && $model->titleValC001["2"]) {
                    $data["SUSPEND"][NUMBER]    = $data_arr[$i]["SUSPEND"];
                } elseif ($fld == "KOUDOME" && $model->titleValC001["25"]) {
                    $data["KOUDOME"][NUMBER]    = $data_arr[$i]["KOUDOME"];
                } elseif ($fld == "VIRUS" && $model->titleValC001["19"]) {
                    $data["VIRUS"][NUMBER]      = $data_arr[$i]["VIRUS"];
                } elseif ($fld == "MOURNING" && $model->titleValC001["3"]) {
                    $data["MOURNING"][NUMBER]   = $data_arr[$i]["MOURNING"];
                } elseif ($fld == "SICK" && $model->titleValC001["4"]) {
                    $data["SICK"][NUMBER]       = $data_arr[$i]["SICK"];
                } elseif ($fld == "NOTICE" && $model->titleValC001["5"]) {
                    $data["NOTICE"][NUMBER]     = $data_arr[$i]["NOTICE"];
                } elseif ($fld == "NONOTICE" && $model->titleValC001["6"]) {
                    $data["NONOTICE"][NUMBER]   = $data_arr[$i]["NONOTICE"];
                } elseif ($fld == "LATE" && $model->titleValC001["15"]) {
                    $data["LATE"][NUMBER]       = $data_arr[$i]["LATE"];
                } elseif ($fld == "EARLY" && $model->titleValC001["16"]) {
                    $data["EARLY"][NUMBER]      = $data_arr[$i]["EARLY"];
                } else {
                    if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                        $data[$fld][NUMBER]         = $data_arr[$i][$fld];
                    } else {
                        $data[$fld][NUMBER]         = $data_arr[$i][$fld] ? $data_arr[$i][$fld] : 0;
                    }
                }
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

            //ATTEND_SEMES_DETAIL_DAT
            $query  = " DELETE FROM ATTEND_SEMES_DETAIL_DAT ";
            $query .= "     WHERE YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
            $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     COPYCD      = '0' ";
            $db->query($query);

            $data = array();
            $data["COPYCD"][TEXT]           = '0';
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            if ($model->titleValC002["001"]) {
                $data["SEQ"][TEXT]          = "001";
                $data["CNT"][NUMBER]        = $data_arr[$i]["DETAIL_001"];
                $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                $db->query($query);
            }
            if ($model->titleValC002["002"]) {
                $data["SEQ"][TEXT]          = "002";
                $data["CNT"][NUMBER]        = $data_arr[$i]["DETAIL_002"];
                $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                $db->query($query);
            }
            if ($model->titleValC002["003"]) {
                $data["SEQ"][TEXT]          = "003";
                $data["CNT"][NUMBER]        = $data_arr[$i]["DETAIL_003"];
                $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                $db->query($query);
            }
            if ($model->titleValC002["004"]) {
                $data["SEQ"][TEXT]          = "004";
                $data["CNT"][NUMBER]        = $data_arr[$i]["DETAIL_004"];
                $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                $db->query($query);
            }
            if ($model->titleValC002["101"]) {
                $data["SEQ"][TEXT]              = "101";
                $data["CNT"][NUMBER]            = "";
                $data["CNT_DECIMAL"][NUMBER]    = $data_arr[$i]["DETAIL_101"];
                $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //ATTEND_SEMES_DAT
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
            $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     COPYCD      = '0' ";

            $db->query($query);

            //ATTEND_SEMES_DETAIL_DAT
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_DETAIL_DAT ";
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
    public function checkRegdDat($year, $semester, $schregno, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1" && SCHOOLKIND != "") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".$year."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "              ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".$year."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "              ) ";
        }

        return $query;
    }

    //校種取得
    public function getSchoolKind2($year, $semester, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$year."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE ";

        return $query;
    }

    //制限チェック
    public function checkRegdHdat($model, $year, $semester, $schregno)
    {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_REGD_HDAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".$year."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        $query .= "         T1.GRADE    = T2.GRADE AND ";
        $query .= "         T1.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "         T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "         '".STAFFCD."' IN (T2.TR_CD1, T2.TR_CD2, T2.TR_CD3, T2.SUBTR_CD1, T2.SUBTR_CD2, T2.SUBTR_CD3) ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1) {
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         SCHREGNO ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_GHR_DAT T1, ";
            $query .= "         SCHREG_REGD_GHR_HDAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = T2.YEAR AND ";
            $query .= "         T1.YEAR     = '".$year."' AND ";
            $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "         T1.SEMESTER = '".$semester."' AND ";
            $query .= "         T1.GHR_CD   = T2.GHR_CD AND ";
            $query .= "         T1.SCHREGNO = '".$schregno."' AND ";
            $query .= "         '".STAFFCD."' IN (T2.TR_CD1, T2.TR_CD2, T2.TR_CD3, T2.SUBTR_CD1, T2.SUBTR_CD2, T2.SUBTR_CD3) ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MAIN ";

        return $query;
    }

    //締め日の取得
    public function getAppointedDay($year, $month, $semester, $model)
    {
        $query  = "  SELECT ";
        $query .= "    APPOINTED_DAY ";
        $query .= "  FROM ";
        $query .= "    APPOINTED_DAY_MST AM ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    INNER JOIN ";
            $query .= "    SCHREG_REGD_GDAT SGD ";
            $query .= "    ON ";
            $query .= "    AM.YEAR = SGD.YEAR AND ";
            $query .= "    AM.SCHOOL_KIND = SGD.SCHOOL_KIND ";
        }
        $query .= "  WHERE ";
        $query .= "    AM.YEAR = '". $year ."' ";
        $query .= "    AND AM.MONTH = '". $month ."' ";
        $query .= "    AND AM.SEMESTER = '". $semester ."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    AND SGD.GRADE = '". $model->grade ."' ";
        }

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     G2.GHR_NAME AS HR_NAME, ";
            $query .= "     G1.GHR_ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "     S2.HR_NAME, ";
            $query .= "     T1.ATTENDNO, ";
        }
        $query .= "     S1.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     S3.MONTH, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S4.APPOINTED_DAY, ";
        $query .= "     S3.LESSON, ";
        if ($model->titleValA004["2"]) {
            $query .= "     S3.OFFDAYS, ";
        }
        if ($model->titleValA004["1"]) {
            $query .= "     S3.ABROAD, ";
        }
        if ($model->titleValC001["1"]) {
            $query .= "     S3.ABSENT, ";
        }
        if ($model->titleValC001["2"]) {
            $query .= "     S3.SUSPEND, ";
        }
        if ($model->titleValC001["25"]) {
            $query .= "     S3.KOUDOME, ";
        }
        if ($model->titleValC001["19"]) {
            $query .= "     S3.VIRUS, ";
        }
        if ($model->titleValC001["3"]) {
            $query .= "     S3.MOURNING, ";
        }
        if ($model->titleValC001["4"]) {
            $query .= "     S3.SICK, ";
        }
        if ($model->titleValC001["5"]) {
            $query .= "     S3.NOTICE, ";
        }
        if ($model->titleValC001["6"]) {
            $query .= "     S3.NONOTICE, ";
        }
        if ($model->titleValC001["15"]) {
            $query .= "     S3.LATE, ";
        }
        if ($model->titleValC001["16"]) {
            $query .= "     S3.EARLY, ";
        }
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            if ($model->titleValC002["001"]) {
                $query .= "     L1.CNT AS REIHAI_KEKKA, ";
            }
            if ($model->titleValC002["002"]) {
                $query .= "     L2.CNT AS M_KEKKA_JISU, ";
            }
            if ($model->titleValC002["003"]) {
                $query .= "     L3.CNT AS REIHAI_TIKOKU, ";
            }
            if ($model->titleValC002["004"]) {
                $query .= "     L4.CNT AS JYUGYOU_TIKOKU, ";
            }
            if ($model->titleValC002["101"]) {
                $query .= "     L101.CNT_DECIMAL AS JYUGYOU_JISU_DECIMAL, ";
            }
        } else {
            if ($model->titleValC002["001"]) {
                $query .= "     S3.REIHAI_KEKKA, ";
            }
            if ($model->titleValC002["002"]) {
                $query .= "     S3.M_KEKKA_JISU, ";
            }
            if ($model->titleValC002["003"]) {
                $query .= "     S3.REIHAI_TIKOKU, ";
            }
            if ($model->titleValC002["004"]) {
                $query .= "     S3.JYUGYOU_TIKOKU, ";
            }
            if ($model->titleValC002["101"]) {
                $query .= "     S3.JYUGYOU_JISU_DECIMAL, ";
            }
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ON T1.YEAR = S2.YEAR ";
        $query .= "                                  AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "                                  AND T1.GRADE = S2.GRADE ";
        $query .= "                                  AND T1.HR_CLASS = S2.HR_CLASS ";
        $query .= "     LEFT JOIN V_ATTEND_SEMES_DAT S3 ON T1.YEAR = S3.YEAR ";
        $query .= "                                  AND T1.SCHREGNO = S3.SCHREGNO ";
        $query .= "                                  AND S3.MONTH || '-' || S3.SEMESTER  = '".$model->field["MONTH"]."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT SGD ON T1.YEAR = SGD.YEAR ";
        $query .= "                                  AND T1.GRADE = SGD.GRADE ";
        $query .= "     LEFT JOIN APPOINTED_DAY_MST S4 ON T1.YEAR = S4.YEAR AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                                     SGD.SCHOOL_KIND = S4.SCHOOL_KIND AND ";
        }
        $query .= "                                  S3.MONTH || '-' || S3.SEMESTER = S4.MONTH || '-' || S4.SEMESTER ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT G1 ON T1.YEAR     = G1.YEAR ";
            $query .= "                                      AND T1.SEMESTER = G1.SEMESTER ";
            $query .= "                                      AND T1.SCHREGNO = G1.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_GHR_HDAT G2 ON T1.YEAR     = G2.YEAR ";
            $query .= "                                      AND T1.SEMESTER = G2.SEMESTER ";
            $query .= "                                      AND G1.GHR_CD   = G2.GHR_CD ";
        }
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L1 ON S3.COPYCD   = L1.COPYCD ";
            $query .= "                                         AND S3.YEAR     = L1.YEAR ";
            $query .= "                                         AND S3.MONTH    = L1.MONTH ";
            $query .= "                                         AND S3.SEMESTER = L1.SEMESTER ";
            $query .= "                                         AND S3.SCHREGNO = L1.SCHREGNO ";
            $query .= "                                         AND L1.SEQ      = '001' ";
            $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L2 ON S3.COPYCD   = L2.COPYCD ";
            $query .= "                                         AND S3.YEAR     = L2.YEAR ";
            $query .= "                                         AND S3.MONTH    = L2.MONTH ";
            $query .= "                                         AND S3.SEMESTER = L2.SEMESTER ";
            $query .= "                                         AND S3.SCHREGNO = L2.SCHREGNO ";
            $query .= "                                         AND L2.SEQ      = '002' ";
            $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L3 ON S3.COPYCD   = L3.COPYCD ";
            $query .= "                                         AND S3.YEAR     = L3.YEAR ";
            $query .= "                                         AND S3.MONTH    = L3.MONTH ";
            $query .= "                                         AND S3.SEMESTER = L3.SEMESTER ";
            $query .= "                                         AND S3.SCHREGNO = L3.SCHREGNO ";
            $query .= "                                         AND L3.SEQ      = '003' ";
            $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L4 ON S3.COPYCD   = L4.COPYCD ";
            $query .= "                                         AND S3.YEAR     = L4.YEAR ";
            $query .= "                                         AND S3.MONTH    = L4.MONTH ";
            $query .= "                                         AND S3.SEMESTER = L4.SEMESTER ";
            $query .= "                                         AND S3.SCHREGNO = L4.SCHREGNO ";
            $query .= "                                         AND L4.SEQ      = '004' ";
            $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L101 ON S3.COPYCD = L101.COPYCD ";
            $query .= "                                         AND S3.YEAR     = L101.YEAR ";
            $query .= "                                         AND S3.MONTH    = L101.MONTH ";
            $query .= "                                         AND S3.SEMESTER = L101.SEMESTER ";
            $query .= "                                         AND S3.SCHREGNO = L101.SCHREGNO ";
            $query .= "                                         AND L101.SEQ    = '101' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     G1.GHR_CD = '".$model->field["GROUP_HR_CLASS"]."' ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ORDER BY ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            $query .= "     G1.GHR_CD, ";
            $query .= "     G1.GHR_ATTENDNO, ";
        } else {
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
        }
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
