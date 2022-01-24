<?php

require_once('for_php7.php');
class knjx_c031kQuery extends Query
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

    //校種取得
    public function getSchoolKind($model)
    {
        $query  = "  SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_GDAT GDAT ";
        $query .= "  WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$model->grade."' ";

        return $query;
    }


    //年度学期一覧
    public function getSelectFieldSQL()
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
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR AND ";
            $query .= "                     S1.SCHOOL_KIND  IN ('".implode("','", explode(":", $model->send_selectSchoolKind))."') ";
            $query .= "                 ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
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
        $query .= "     VALUE";

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

    //対象月データの取得
    public function selectMonthQuery($year, $month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $kind = ($model->school_kind) ? $model->school_kind : SCHOOLKIND;
            $setNameCd = "Z".$kind."05";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '2') ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d', $month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //欠課種別取得
    public function getSickDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'C001' ";
        $query .= "     AND NAMECD2 IN ('4', '5', '6') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $appointed_day = $db->getOne(knjx_c031kQuery::getAppointedDay($data_arr[$i]["YEAR"], $data_arr[$i]["MONTH"], $data_arr[$i]["SEMESTER"], $model));

            $data = array();
            $data["COPYCD"][TEXT]           = '0';
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["APPOINTED_DAY"][TEXT]    = $appointed_day;
            $data["LESSON"][NUMBER]         = $data_arr[$i]["LESSON"] ? $data_arr[$i]["LESSON"] : 0;
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                $data["OFFDAYS"][NUMBER]        = $data_arr[$i]["OFFDAYS"];
                $data["ABSENT"][NUMBER]         = $data_arr[$i]["ABSENT"];
                $data["SUSPEND"][NUMBER]        = $data_arr[$i]["SUSPEND"];
                if ($model->Properties["useKoudome"] == "true") {
                    $data["KOUDOME"][NUMBER]        = $data_arr[$i]["KOUDOME"];
                }
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]          = $data_arr[$i]["VIRUS"];
                }
                $data["MOURNING"][NUMBER]       = $data_arr[$i]["MOURNING"];
                $data["ABROAD"][NUMBER]         = $data_arr[$i]["ABROAD"];
                $data["SICK"][NUMBER]           = $data_arr[$i]["SICK"];
                $data["NOTICE"][NUMBER]         = $data_arr[$i]["NOTICE"];
                $data["NONOTICE"][NUMBER]       = $data_arr[$i]["NONOTICE"];
                $data["LATE"][NUMBER]           = $data_arr[$i]["LATE"];
                $data["EARLY"][NUMBER]          = $data_arr[$i]["EARLY"];
            } else {
                $data["OFFDAYS"][NUMBER]        = $data_arr[$i]["OFFDAYS"] ? $data_arr[$i]["OFFDAYS"] : 0;
                $data["ABSENT"][NUMBER]         = $data_arr[$i]["ABSENT"] ? $data_arr[$i]["ABSENT"] : 0;
                $data["SUSPEND"][NUMBER]        = $data_arr[$i]["SUSPEND"] ? $data_arr[$i]["SUSPEND"] : 0;
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
                    $data["KOUDOME"][NUMBER]      = $data_arr[$i]["KOUDOME"] ? $data_arr[$i]["KOUDOME"] : 0;
                }
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]        = $data_arr[$i]["VIRUS"] ? $data_arr[$i]["VIRUS"] : 0;
                }
                $data["MOURNING"][NUMBER]       = $data_arr[$i]["MOURNING"] ? $data_arr[$i]["MOURNING"] : 0;
                $data["ABROAD"][NUMBER]         = $data_arr[$i]["ABROAD"] ? $data_arr[$i]["ABROAD"] : 0;
                $data["SICK"][NUMBER]           = $data_arr[$i]["SICK"] ? $data_arr[$i]["SICK"] : 0;
                $data["NOTICE"][NUMBER]         = $data_arr[$i]["NOTICE"] ? $data_arr[$i]["NOTICE"] : 0;
                $data["NONOTICE"][NUMBER]       = $data_arr[$i]["NONOTICE"] ? $data_arr[$i]["NONOTICE"] : 0;
                $data["LATE"][NUMBER]           = $data_arr[$i]["LATE"] ? $data_arr[$i]["LATE"] : 0;
                $data["EARLY"][NUMBER]          = $data_arr[$i]["EARLY"] ? $data_arr[$i]["EARLY"] : 0;
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
    public function checkRegdDat($year, $semester, $schregno)
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
    public function getAppointedDay($year, $month, $semester, $model)
    {
        $query .= "  SELECT ";
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
        $query .= "     S2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     S1.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     S3.MONTH, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S4.APPOINTED_DAY, ";
        $query .= "     S3.LESSON, ";
        $query .= "     S3.OFFDAYS, ";
        $query .= "     S3.ABROAD, ";
        $query .= "     S3.ABSENT, ";
        $query .= "     S3.SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     S3.KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     S3.VIRUS, ";
        }
        $query .= "     S3.MOURNING, ";
        /* C001 */
        foreach ($model->titleVal as $key => $val) {
            $query .= "     S3.".$model->c001[$key].", ";
        }
        $query .= "     S3.LATE, ";
        $query .= "     S3.EARLY, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ON T1.YEAR = S2.YEAR ";
        $query .= "                                  AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "                                  AND T1.GRADE = S2.GRADE ";
        $query .= "                                  AND T1.HR_CLASS = S2.HR_CLASS ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DAT S3 ON T1.YEAR = S3.YEAR ";
        $query .= "                                  AND T1.SCHREGNO = S3.SCHREGNO ";
        $query .= "                                  AND S3.MONTH || '-' || S3.SEMESTER  = '".$model->field["MONTH"]."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT SGD ON T1.YEAR = SGD.YEAR ";
        $query .= "                                  AND T1.GRADE = SGD.GRADE ";
        $query .= "     LEFT JOIN APPOINTED_DAY_MST S4 ON T1.YEAR = S4.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                                  AND SGD.SCHOOL_KIND = S4.SCHOOL_KIND ";
        }
        $query .= "                                  AND S3.MONTH || '-' || S3.SEMESTER = S4.MONTH || '-' || S4.SEMESTER ";
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
