<?php

require_once('for_php7.php');

class knjc034dQuery extends Query
{

    //学校種別の取得
    public function getNameMst($nameCd1, $nameCd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL ";
        $query .= "   , NAMECD2 AS VALUE ";
        // $query .= "   , NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$nameCd1."' ";
        if ($nameCd2 !== "") {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        }

        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //学校種別の取得
    public function getNameMstA023($model)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //日付の学期
    public function getDateSemester($date)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER <> '9' ";
        $query .= "     AND '{$date}' BETWEEN SDATE AND EDATE ";
        return $query;
    }

    //年組取得
    public function getHrClass($model, $seme)
    {
        $where = "";
        if ($model->hr_class_type == "2") {
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $tablename = "SCHREG_REGD_FI_HDAT";
            } else {
                $tablename = "SCHREG_REGD_GHR_HDAT";
            }
        } elseif ($model->hr_class_type == "1" && $model->grade_mix == "1") {
            $tablename = "V_STAFF_HR_DAT";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $where  = " AND GRADE IN (  SELECT ";
                $where .= "                     GRADE ";
                $where .= "                 FROM ";
                $where .= "                     SCHREG_REGD_GDAT S1 ";
                $where .= "                 WHERE ";
                $where .= "                     S1.YEAR         = T1.YEAR AND ";
                $where .= "                     S1.SCHOOL_KIND  = '{$model->setSchoolKind}' ";
                $where .= "                 ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $where  = " AND GRADE IN (  SELECT ";
                $where .= "                     GRADE ";
                $where .= "                 FROM ";
                $where .= "                     SCHREG_REGD_GDAT S1 ";
                $where .= "                 WHERE ";
                $where .= "                     S1.YEAR         = T1.YEAR AND ";
                $where .= "                     S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
                $where .= "                 ) ";
            }
        } else {
            $tablename = "SCHREG_REGD_HDAT";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $where  = " AND GRADE IN (  SELECT ";
                $where .= "                     GRADE ";
                $where .= "                 FROM ";
                $where .= "                     SCHREG_REGD_GDAT S1 ";
                $where .= "                 WHERE ";
                $where .= "                     S1.YEAR         = T1.YEAR AND ";
                $where .= "                     S1.SCHOOL_KIND  = '{$model->setSchoolKind}' ";
                $where .= "                 ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $where  = " AND GRADE IN (  SELECT ";
                $where .= "                     GRADE ";
                $where .= "                 FROM ";
                $where .= "                     SCHREG_REGD_GDAT S1 ";
                $where .= "                 WHERE ";
                $where .= "                     S1.YEAR         = T1.YEAR AND ";
                $where .= "                     S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
                $where .= "                 ) ";
            }
        }

        $query  = " SELECT ";
        if ($model->hr_class_type == "2" && $model->Properties["useFi_Hrclass"] != '1') {
            $query .= "     T1.GHR_NAME AS LABEL, ";
            $query .= "     T1.GHR_CD || '-ghr' AS VALUE ";
        } elseif ($model->hr_class_type == "1" && $model->grade_mix == "1") {
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE ";
        } else {
            $query .= "     T1.HR_NAME AS LABEL, ";
            $query .= "     T1.GRADE || '-' || HR_CLASS AS VALUE ";
        }
        $query .= " FROM ";
        $query .=       $tablename." T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$seme."' ";
        $query .= $where;
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            if ($model->hr_class_type == "1" && $model->grade_mix == "1") {
                $query .= " AND T1.STAFFCD = '".STAFFCD."' ";
            } else {
                $query .= " AND '".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3, T1.SUBTR_CD1, T1.SUBTR_CD2, T1.SUBTR_CD3) ";
            }
        }
        if ($model->hr_class_type == "1" && $model->grade_mix == "1") {
            $query .= "     AND T1.HR_CLASS_NAME1 IN ( SELECT ";
            $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                FROM ";
            $query .=                                      $tablename." M1 ";
            $query .= "                               WHERE ";
            $query .= "                                    T1.YEAR     = M1.YEAR AND ";
            $query .= "                                    T1.SEMESTER = M1.SEMESTER AND ";
            $query .= "                                    T1.HR_CLASS = M1.HR_CLASS AND ";
            $query .= "                                    T1.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
            $query .= "                                    T1.STAFFCD  = M1.STAFFCD ";
            $query .= "                             ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_CLASS_NAME1, ";
            $query .= "     T1.SCHOOL_KIND ";
        }
        $query .= " ORDER BY ";
        if ($model->hr_class_type == "1" && $model->grade_mix == "1") {
            $query .= "     T1.SCHOOL_KIND DESC, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     LABEL ";
        } else {
            $query .= "     VALUE ";
        }
        return $query;
    }

    //学期開始月・終了月／学期開始日・終了日取得
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

    //学期開始月・終了月／学期開始日・終了日取得
    public function selectSemesGradeAll($grade, $seme = "")
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
            $query .= "     MONTH(SDATE) AS S_MONTH, ";
            $query .= "     DAY(SDATE) AS S_DAY, ";
            $query .= "     MONTH(EDATE) AS E_MONTH, ";
            $query .= "     DAY(EDATE) AS E_DAY ";
        }
        $query .= " FROM ";
        $query .= "     V_SEMESTER_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (!$seme) {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }
        $query .= "     AND GRADE = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月一覧取得
    public function selectMonthQuery($month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->setSchoolKind."05";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' AND ";
        $query .= "     NAMECD2 = '".sprintf('%02d', $month)."' AND ";
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     CONTROL_CODE ";
        $query .= "                 FROM ";
        $query .= "                     ADMIN_CONTROL_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                     CONTROL_FLG = '2') ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //生徒一覧取得
    public function getSchInfo($type, $mix, $seme, $grade_hr_class)
    {
        list($grade, $hr_class) = explode('-', $grade_hr_class);

        if ($type == "2") {
            if ($hr_class == 'ghr') {
                $tablename = "SCHREG_REGD_GHR_DAT";
            } else {
                $tablename = "SCHREG_REGD_FI_DAT";
            }
        } elseif ($type == "1" && $mix == "1") {
            $tablename  = "SCHREG_REGD_DAT";
            $tablename2 = "V_STAFF_HR_DAT";
        } else {
            $tablename = "SCHREG_REGD_DAT";
        }

        $query  = " SELECT ";
        if ($type == "1" && $mix == "1") {
            $query .= "     DISTINCT ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
        }
        $query .= "     T1.SCHREGNO, ";
        if ($type == "2" && $hr_class == 'ghr') {
            $query .= "     T1.GHR_ATTENDNO AS ATTENDNO, ";
        } elseif ($type == "1" && $mix == "1") {
            $query .= "     VALUE(L2.HR_NAMEABBV,'') || '-' || T1.ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "     T1.ATTENDNO, ";
        }
        $query .= "     L1.NAME_SHOW ";
        //訪問生フラグ
        $query .= "   , D004.BASE_REMARK2 VISIT_FLG ";

        $query .= " FROM ";
        $query .=       $tablename." T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        if ($type == "1" && $mix == "1") {
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR     = T1.YEAR ";
            $query .= "                                  AND L2.SEMESTER = T1.SEMESTER ";
            $query .= "                                  AND L2.GRADE    = T1.GRADE ";
            $query .= "                                  AND L2.HR_CLASS = T1.HR_CLASS, ";
            $query .= "     ".$tablename2." T3 ";
        }
        //訪問生フラグ
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST D004 ";
        $query .= "         ON D004.YEAR     = T1.YEAR ";
        $query .= "         AND D004.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND D004.BASE_SEQ    = '004' ";

        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$seme."' AND ";
        if ($type == "1" && $mix == "1") {
            $query .= "     T1.YEAR     = T3.YEAR AND ";
            $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
            $query .= "     T1.GRADE    = T3.GRADE AND ";
            $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        }
        if ($type == "2" && $hr_class == 'ghr') {
            $query .= "     T1.GHR_CD = '".$grade."' ";
        } elseif ($type == "1" && $mix == "1") {
            $query .= "     T3.SCHOOL_KIND  = '".$grade."' AND ";
            $query .= "     T1.HR_CLASS     = '".$hr_class."' ";
        } else {
            $query .= "     T1.GRADE    = '".$grade."' AND ";
            $query .= "     T1.HR_CLASS = '".$hr_class."' ";
        }
        $query .= " ORDER BY ";
        if ($type == "1" && $mix == "1") {
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
        }
        $query .= "     ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //休日取得 -- EVENT_DAT
    public function getHoliday($model, $type, $ghr, $sdate, $edate)
    {
        $query  = " SELECT ";
        $query .= "     EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "     GRADE || '-' || HR_CLASS = '".$ghr."' AND ";
        $query .= "     HR_CLASS_DIV    = '".$type."' AND ";
        $query .= "     HOLIDAY_FLG     = '1' AND ";
        $query .= "     EXECUTEDATE BETWEEN '".$sdate."' AND '".$edate."' ";
        $query .= " ORDER BY ";
        $query .= "     EXECUTEDATE ";

        return $query;
    }

    //休日取得 -- EVENT_MST
    public function getHoliday2($model, $type, $ghr, $sdate, $edate)
    {
        list($school_kind, $hr_class) = preg_split("/-/", $ghr);

        $query  = " SELECT ";
        $query .= "     EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
        $query .= "     DATA_DIV        = '1' AND ";
        $query .= "     GRADE           = '00' AND ";
        $query .= "     COURSECD        = '0' AND ";
        $query .= "     MAJORCD         = '000' AND ";
        $query .= "     HR_CLASS        = '000' AND ";
        $query .= "     HR_CLASS_DIV    = '".$type."' AND ";
        $query .= "     HOLIDAY_FLG     = '1' AND ";
        $query .= "     EXECUTEDATE BETWEEN '".$sdate."' AND '".$edate."' ";
        $query .= " ORDER BY ";
        $query .= "     EXECUTEDATE ";

        return $query;
    }

    //休日取得 -- PUBLIC_HOLIDAY_MST
    public function getPublicHoliday($month)
    {
        $query  = " SELECT  ";
        $query .= "     * ";
        $query .= " FROM  ";
        $query .= "     PUBLIC_HOLIDAY_MST  ";
        $query .= " WHERE  ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     HOLIDAY_MONTH   = '".$month."' ";
        return $query;
    }

    //個人の休日取得
    public function getSchHoliday($model, $month, $schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_YEAR_DETAIL_MST T1, ";
        $query .= "     EVENT_SCHREG_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.BASE_SEQ     = '004' AND ";
        $query .= "     T1.BASE_REMARK2 = '1' AND ";
        $query .= "     T1.SCHREGNO IN ('".implode("','", $schregno)."') AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     MONTH(T2.EXECUTEDATE) = INT('".$month."') ";

        return $query;
    }

    //出欠済みチェック
    public function checkExecuted($model, $date_from, $date_to)
    {
        $query  = " SELECT ";
        $query .= "     ATTENDDATE, ";
        $query .= "     EXECUTED ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_HRATE_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTENDDATE BETWEEN '".$date_from."' AND '".$date_to."' AND ";
        $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class."' ";

        return $query;
    }

    //出欠データ取得
    public function getAttendDayDat($model, $schregno, $date_from, $date_to)
    {
        //出欠（詳細）表示
        if ($model->Properties["attend_Shosai"] == 1) {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ATTENDDATE, ";
            $query .= "     T1.DI_CD || '-' || VALUE(L1.SUBL_CD,'0000') || '-' || VALUE(L2.SUBM_CD, '0000') AS DI_CD ";
            $query .= " FROM ";
            $query .= "     ATTEND_DAY_DAT T1 ";
            $query .= "     LEFT JOIN ATTEND_DAY_SUBL_DAT L1 ";
            $query .= "              ON T1.SCHREGNO     = L1.SCHREGNO ";
            $query .= "             AND T1.ATTENDDATE   = L1.ATTENDDATE ";
            $query .= "             AND T1.DI_CD        = L1.DI_CD ";
            $query .= "     LEFT JOIN ATTEND_DAY_SUBM_DAT L2 ";
            $query .= "              ON T1.SCHREGNO     = L2.SCHREGNO ";
            $query .= "             AND T1.ATTENDDATE   = L2.ATTENDDATE ";
            $query .= "             AND T1.DI_CD        = L2.DI_CD ";
            $query .= "             AND L1.SUBL_CD      = L2.SUBL_CD ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO IN ('".implode("','", $schregno)."') AND ";
            $query .= "     T1.ATTENDDATE BETWEEN '".$date_from."' AND '".$date_to."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ATTENDDATE, ";
            $query .= "     INT(VALUE(L2.SUBM_CD, '0000')), ";
            $query .= "     INT(VALUE(L1.SUBL_CD, '0000')), ";
            $query .= "     INT(T1.DI_CD) ";
        } else {
            $query  = " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     ATTENDDATE, ";
            $query .= "     DI_CD ";
            $query .= " FROM ";
            $query .= "     ATTEND_DAY_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO IN ('".implode("','", $schregno)."') AND ";
            $query .= "     ATTENDDATE BETWEEN '".$date_from."' AND '".$date_to."' ";
            $query .= " ORDER BY ";
            $query .= "     SCHREGNO, ";
            $query .= "     ATTENDDATE, ";
            $query .= "     INT(DI_CD) ";
        }

        return $query;
    }

    //異動日チェック
    public function checkIdou($schregno, $seme, $date)
    {
        $query  = " WITH BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST T1 ";
        $query .= "     WHERE ";
        $query .= "         ((ENT_DIV IN ('4','5') AND ENT_DATE > '".$date."') OR ";
        $query .= "          (GRD_DIV IN ('1','2','3') AND GRD_DATE < '".$date."')) AND ";
        $query .= "         NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_ENT_GRD_HIST_COMEBACK_DAT E1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.SCHREGNO = E1.SCHREGNO AND ";
        $query .= "                         '".$date."' BETWEEN ENT_DATE AND GRD_DATE ";
        $query .= "                     ) ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), TRANSFER AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_TRANSFER_DAT ";
        $query .= "     WHERE ";
        $query .= "         TRANSFERCD IN ('1','2') AND ";
        $query .= "         '".$date."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(L1.CNT,0) + VALUE(L2.CNT,0) AS IDOU_COLOR, ";
        $query .= "     CASE WHEN VALUE(L1.CNT,0) > 0 THEN '1' ELSE '0' END AS IDOU_DISABLE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN BASE L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN TRANSFER L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$seme."' AND ";
        $query .= "     T1.SCHREGNO IN ('".implode("','", $schregno)."') ";

        return $query;
    }

    //出欠コード一覧取得
    public function getAttendCode($model)
    {
        //出欠表示する参照フィールド切替
        $label_field = ($model->Properties["attend_Shosai_kigou"] == "1") ? "NAME1" : "ABBV1";
        $setC041 = "C041";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setC041 = "C".$model->setSchoolKind."41";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setC041 = "C".SCHOOLKIND."41";
        }

        $setC006 = "C006";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setC006 = "C".$model->setSchoolKind."06";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setC006 = "C".SCHOOLKIND."06";
        }

        $setC007 = "C007";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setC007 = "C".$model->setSchoolKind."07";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setC007 = "C".SCHOOLKIND."07";
        }

        $setC008 = "C008";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setC008 = "C".$model->setSchoolKind."08";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setC008 = "C".SCHOOLKIND."08";
        }

        //出欠（詳細）表示
        if ($model->Properties["attend_Shosai"] == 1) {
            $query  = " WITH PARENTS AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.NAMECD2 AS VALUE1, ";
            $query .= "         '0000' AS VALUE2, ";
            $query .= "         '0000' AS VALUE3, ";
            $query .= "         T1.".$label_field." AS LABEL, ";
            $query .= "         VALUE(T1.NAMESPARE2,'999') AS SORT1, ";
            $query .= "         '999' AS SORT2, ";
            $query .= "         '999' AS SORT3 ";
            $query .= "     FROM ";
            $query .= "         V_NAME_MST T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "         T1.NAMECD1  = '{$setC041}' ";
            if ($model->Properties["useVirus"] != "true") {
                $query .= "     AND T1.NAMECD2 != '19' ";
            }
            if ($model->Properties["useKoudome"] != "true") {
                $query .= "     AND T1.NAMECD2 != '25' ";
            }
            $query .= " ), CHILD AS ( ";
            $query .= "     SELECT ";
            $query .= "         T2.VALUE1 AS VALUE1, ";
            $query .= "         T1.NAMECD2 AS VALUE2, ";
            $query .= "         '0000' AS VALUE3, ";
            $query .= "         T1.".$label_field." AS LABEL, ";
            $query .= "         VALUE(T2.SORT1,'999') AS SORT1, ";
            $query .= "         VALUE(T1.NAMESPARE2,'999') AS SORT2, ";
            $query .= "         '999' AS SORT3 ";
            $query .= "     FROM ";
            $query .= "         V_NAME_MST T1, ";
            $query .= "         PARENTS T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
            $query .= "         T1.NAMECD1      = '{$setC006}' AND ";
            $query .= "         T1.NAMESPARE1   = T2.VALUE1 ";
            $query .= " ), GRANDCHILD AS ( ";
            $query .= "     SELECT ";
            $query .= "         T2.VALUE1 AS VALUE1, ";
            $query .= "         T1.NAMESPARE1 AS VALUE2, ";
            $query .= "         T1.NAMECD2 AS VALUE3, ";
            $query .= "         T1.".$label_field." AS LABEL, ";
            $query .= "         T2.SORT1 AS SORT1, ";
            $query .= "         T2.SORT2 AS SORT2, ";
            $query .= "         VALUE(T1.NAMESPARE2,'999') AS SORT3 ";
            $query .= "     FROM ";
            $query .= "         V_NAME_MST T1, ";
            $query .= "         CHILD T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
            $query .= "         T1.NAMECD1      = '{$setC007}' AND ";
            $query .= "         T1.NAMESPARE1   = T2.VALUE2 ";
            $query .= " ), LATE_EARLY AS ( ";
            $query .= "     SELECT ";
            $query .= "         '99' AS VALUE1, ";
            $query .= "         '0000' AS VALUE2, ";
            $query .= "         '0000' AS VALUE3, ";
            $query .= "         T3.".$label_field." AS LABEL, ";
            $query .= "         '999' AS SORT1, ";
            $query .= "         '999' AS SORT2, ";
            $query .= "         '999' AS SORT3 ";
            $query .= "     FROM ";
            $query .= "         V_NAME_MST T1, ";
            $query .= "         V_NAME_MST T2, ";
            $query .= "         V_NAME_MST T3 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = T2.YEAR AND ";
            $query .= "         T1.YEAR     = T3.YEAR AND ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "         T1.NAMECD1  = T2.NAMECD1 AND ";
            $query .= "         T1.NAMECD1  = '{$setC041}' AND ";
            $query .= "         T1.NAMECD2  = '15' AND ";
            $query .= "         T2.NAMECD2  = '16' AND ";
            $query .= "         T3.NAMECD1  = '{$setC008}' AND ";
            $query .= "         T3.NAMECD2  = '01' ";
            $query .= " ), MAIN AS ( ";
            $query .= "     SELECT * FROM PARENTS ";
            $query .= "     UNION ";
            $query .= "     SELECT * FROM CHILD ";
            $query .= "     UNION ";
            $query .= "     SELECT * FROM GRANDCHILD ";
            $query .= "     UNION ";
            $query .= "     SELECT * FROM LATE_EARLY ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     VALUE1 || '-' || VALUE2 || '-' || VALUE3 AS VALUE, ";
            $query .= "     LABEL ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= " ORDER BY ";
            $query .= "     INT(SORT1), ";
            $query .= "     INT(VALUE1), ";
            $query .= "     INT(SORT2), ";
            $query .= "     INT(VALUE2), ";
            $query .= "     INT(SORT3), ";
            $query .= "     INT(VALUE3) ";
        } else {
            $query  = " WITH MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         NAMECD2 AS VALUE, ";
            $query .= "         ".$label_field." AS LABEL, ";
            $query .= "         CASE WHEN VALUE(NAMESPARE2,'') = '' THEN '999' ELSE NAMESPARE2 END AS NAMESPARE2 ";
            $query .= "     FROM ";
            $query .= "         V_NAME_MST ";
            $query .= "     WHERE ";
            $query .= "         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "         NAMECD1 = '{$setC041}' ";
            if ($model->Properties["useVirus"] != "true") {
                $query .= "         AND NAMECD2 != '19' ";
            }
            if ($model->Properties["useKoudome"] != "true") {
                $query .= "         AND NAMECD2 != '25' ";
            }
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         '99' AS VALUE, ";
            $query .= "         T3.".$label_field." AS LABEL, ";
            $query .= "         '999' AS NAMESPARE2 ";
            $query .= "     FROM ";
            $query .= "         V_NAME_MST T1, ";
            $query .= "         V_NAME_MST T2, ";
            $query .= "         V_NAME_MST T3 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = T2.YEAR AND ";
            $query .= "         T1.YEAR     = T3.YEAR AND ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "         T1.NAMECD1  = T2.NAMECD1 AND ";
            $query .= "         T1.NAMECD1  = '{$setC041}' AND ";
            $query .= "         T1.NAMECD2  = '15' AND ";
            $query .= "         T2.NAMECD2  = '16' AND ";
            $query .= "         T3.NAMECD1  = '{$setC008}' AND ";
            $query .= "         T3.NAMECD2  = '01' ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= " ORDER BY ";
            $query .= "     INT(NAMESPARE2), ";
            $query .= "     INT(VALUE) ";
        }

        return $query;
    }

    //出欠の備考取得
    public function getAttendSemesRemarkDat($month, $sem, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$sem."' AND ";
        $query .= "     SCHREGNO IN ('".implode("','", $schregno)."') ";

        return $query;
    }

    //削除 -- ATTEND_DAY_HRATE_DAT
    public function deleteAttendDayHrateDat($model, $date)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAY_HRATE_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTENDDATE = '".$date."' AND ";
        $query .= "     GRADE || '-' || HR_CLASS = '".$model->grade_hr_class."' ";

        return $query;
    }

    //更新 -- ATTEND_DAY_HRATE_DAT
    public function updateAttendDayHrateDat($model, $date)
    {
        list($grade, $hr_class) = explode('-', $model->grade_hr_class);

        $data = array();
        $data["ATTENDDATE"][TEXT]       = $date;
        $data["GRADE"][TEXT]            = $grade;
        $data["HR_CLASS"][TEXT]         = $hr_class;
        $data["EXECUTED"][TEXT]         = "1";
        $data["ATTESTOR"][TEXT]         = STAFFCD;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_DAY_HRATE_DAT");

        return $query;
    }

    //削除 -- ATTEND_DAY_DAT
    public function deleteAttendDayDat($schregno, $date)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' ";

        return $query;
    }

    //更新 -- ATTEND_DAY_DAT
    public function updateAttendDayDat($schregno, $date, $di_cd)
    {
        $data = array();
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["ATTENDDATE"][TEXT]       = $date;
        $data["DI_CD"][TEXT]            = $di_cd;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_DAY_DAT");

        return $query;
    }

    //削除 -- ATTEND_DAY_SUBL_DAT
    public function deleteAttendDaySublDat($schregno, $date)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAY_SUBL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' ";

        return $query;
    }

    //更新 -- ATTEND_DAY_SUBL_DAT
    public function updateAttendDaySublDat($schregno, $date, $di_cd, $subl_cd)
    {
        $data = array();
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["ATTENDDATE"][TEXT]       = $date;
        $data["DI_CD"][TEXT]            = $di_cd;
        $data["SUBL_CD"][TEXT]          = $subl_cd;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_DAY_SUBL_DAT");

        return $query;
    }

    //削除 -- ATTEND_DAY_SUBM_DAT
    public function deleteAttendDaySubmDat($schregno, $date)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAY_SUBM_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' ";

        return $query;
    }

    //更新 -- ATTEND_DAY_SUBM_DAT
    public function updateAttendDaySubmDat($schregno, $date, $di_cd, $subl_cd, $subm_cd)
    {
        $data = array();
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["ATTENDDATE"][TEXT]       = $date;
        $data["DI_CD"][TEXT]            = $di_cd;
        $data["SUBL_CD"][TEXT]          = $subl_cd;
        $data["SUBM_CD"][TEXT]          = $subm_cd;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_DAY_SUBM_DAT");

        return $query;
    }

    //削除 -- ATTEND_SEMES_REMARK_DAT
    public function deleteAttendSemesRemarkDat($month, $sem, $schregno)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SEMES_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$sem."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //更新 -- ATTEND_SEMES_REMARK_DAT
    public function updateAttendSemesRemarkDat($month, $sem, $schregno, $remark)
    {
        $data = array();
        $data["COPYCD"][TEXT]       = 0;
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["MONTH"][TEXT]        = $month;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["REMARK1"][TEXT]      = $remark;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_SEMES_REMARK_DAT");

        return $query;
    }

    //締め日取得
    public function getAppointedDay($model, $month, $sem, $grade = "")
    {
        $table = ($grade) ? "V_APPOINTED_DAY_GRADE_MST" : "APPOINTED_DAY_MST";

        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if (!$grade && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        }
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$sem."' ";
        if ($grade) {
            $query .= " AND GRADE   = '".$grade."' ";
        }
        return $query;
    }

    //授業日数取得
    public function getLesson($month, $sem, $schregno, $flg = "")
    {
        $table = ($flg) ? "ATTEND_LESSON_MST" : "ATTEND_SEMES_LESSON_DAT";

        $query  = " SELECT ";
        $query .= "     LESSON ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .=       $table." T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.MONTH    = '".$month."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$sem."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        if ($flg == "grade") {
            $query .= "     T2.COURSECD = '0' AND ";
            $query .= "     T2.MAJORCD  = '000' ";
        } elseif ($flg == "course") {
            $query .= "     T2.COURSECD = T1.COURSECD AND ";
            $query .= "     T2.MAJORCD  = T1.MAJORCD ";
        } else {
            $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        }

        return $query;
    }

    //授業日数取得（訪問生）
    public function getLesson2($model, $schregno, $sdate, $edate)
    {
        $query  = " SELECT ";
        $query .= "     DATE('".$edate."') - DATE('".$sdate."') +1 - T1.HOLIDAY_CNT AS LESSON ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         S1.SCHREGNO, ";
        $query .= "         COUNT(S2.HOLIDAY_FLG) AS HOLIDAY_CNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_YEAR_DETAIL_MST S1, ";
        $query .= "         EVENT_SCHREG_DAT S2 ";
        $query .= "     WHERE ";
        $query .= "         S1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         S1.BASE_SEQ     = '004' AND ";
        $query .= "         S1.BASE_REMARK2 = '1' AND ";
        $query .= "         S1.SCHREGNO     = S2.SCHREGNO AND ";
        $query .= "         S2.EXECUTEDATE BETWEEN '".$sdate."' AND '".$edate."' ";
        $query .= "     GROUP BY ";
        $query .= "         S1.SCHREGNO ";
        $query .= "     ) T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //集計 -- ATTEND_DAY_DAT
    public function getSumSemesData($model, $month, $sem)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '1' THEN 1 ELSE 0 END) AS ABSENT, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '2' THEN 1 ELSE 0 END) AS SUSPEND, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '3' THEN 1 ELSE 0 END) AS MOURNING, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '4' THEN 1 ELSE 0 END) AS SICK, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '5' THEN 1 ELSE 0 END) AS NOTICE, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '6' THEN 1 ELSE 0 END) AS NONOTICE, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '15' THEN 1 ELSE 0 END) AS LATE, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '16' THEN 1 ELSE 0 END) AS EARLY, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '19' THEN 1 ELSE 0 END) AS VIRUS, ";
        $query .= "     SUM(CASE T1.DI_CD WHEN '25' THEN 1 ELSE 0 END) AS KOUDOME ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     MONTH(T1.ATTENDDATE) = INT('".$month."') AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$sem."' AND ";
        $query .= "     T1.ATTENDDATE BETWEEN T2.SDATE AND T2.EDATE AND ";
        $query .= "     T1.SCHREGNO IN ('".implode("','", $model->fields["SCHREGNO"])."') ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //集計 -- ATTEND_DAY_SUBL_DAT
    public function getSumSemesSublData($model, $month, $sem)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.DI_CD, ";
        $query .= "     T1.SUBL_CD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_SUBL_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     MONTH(T1.ATTENDDATE) = INT('".$month."') AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$sem."' AND ";
        $query .= "     T1.ATTENDDATE BETWEEN T2.SDATE AND T2.EDATE AND ";
        $query .= "     T1.SCHREGNO IN ('".implode("','", $model->fields["SCHREGNO"])."') ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.DI_CD, ";
        $query .= "     T1.SUBL_CD ";

        return $query;
    }

    //集計 -- ATTEND_DAY_SUBM_DAT
    public function getSumSemesSubMData($model, $month, $sem)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.DI_CD, ";
        $query .= "     T1.SUBL_CD, ";
        $query .= "     T1.SUBM_CD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_SUBM_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     MONTH(T1.ATTENDDATE) = INT('".$month."') AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$sem."' AND ";
        $query .= "     T1.ATTENDDATE BETWEEN T2.SDATE AND T2.EDATE AND ";
        $query .= "     T1.SCHREGNO IN ('".implode("','", $model->fields["SCHREGNO"])."') ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.DI_CD, ";
        $query .= "     T1.SUBL_CD, ";
        $query .= "     T1.SUBM_CD ";

        return $query;
    }

    //集計 -- ATTEND_DAY_DAT
    public function getSumSemesDetailData($model, $month, $sem)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.DI_CD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     MONTH(T1.ATTENDDATE) = INT('".$month."') AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$sem."' AND ";
        $query .= "     T1.ATTENDDATE BETWEEN T2.SDATE AND T2.EDATE AND ";
        $query .= "     T1.SCHREGNO IN ('".implode("','", $model->fields["SCHREGNO"])."') ";
        $query .= "     AND T1.DI_CD IN ('51','52','53') ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.DI_CD ";

        return $query;
    }

    //更新 -- ATTEND_SEMES_DAT
    public function updateAttendSemesDat($db, $model, $month, $sem, $schregno, $appointed_day, $lesson, $dataS)
    {
        //ATTEND_SEMES_DATのフィールド取得
        $setField = array();
        $query  = " SELECT COLUMN_NAME FROM SYSIBM.COLUMNS WHERE TABLE_NAME = 'ATTEND_SEMES_DAT' ";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["COLUMN_NAME"], array("COPYCD", "YEAR", "MONTH", "SEMESTER", "SCHREGNO", "APPOINTED_DAY", "LESSON", "REGISTERCD", "UPDATED"))) {
                continue;
            }
            $setField[] = $row["COLUMN_NAME"];
        }
        $result->free();

        //出欠コードとフィールドの対応一覧
        $dicd_Field = array();
        $dicd_Field["1"]    = "ABSENT";
        $dicd_Field["2"]    = "SUSPEND";
        $dicd_Field["3"]    = "MOURNING";
        $dicd_Field["4"]    = "SICK";
        $dicd_Field["5"]    = "NOTICE";
        $dicd_Field["6"]    = "NONOTICE";
        $dicd_Field["15"]   = "LATE";
        $dicd_Field["16"]   = "EARLY";
        if ($model->Properties["useVirus"] != "true") {
            $dicd_Field["19"]   = "VIRUS";
        }
        if ($model->Properties["useKoudome"] != "true") {
            $dicd_Field["25"]   = "KOUDOME";
        }

        //ATTEND_SEMES_DATの集計対象フィールド取得
        $useField = array();
        $query = knjc034dQuery::getAttendCode($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["VALUE"] == '99-0000-0000') {
                continue;
            }
            list($di_cd, $subl_cd, $subm_cd) = preg_split("/-/", $row["VALUE"]);

            if ($dicd_Field[$di_cd] && !in_array($dicd_Field[$di_cd], $useField)) {
                $useField[] = $dicd_Field[$di_cd];
            }
        }

        //データ存在チェック
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH    = '".$month."' AND ";
        $query .= "     SEMESTER = '".$sem."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $checkExists = $db->getOne($query);

        $data = array();
        if ($checkExists == 0) {
            //追加
            $data["COPYCD"][TEXT]           = 0;
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["MONTH"][TEXT]            = $month;
            $data["SEMESTER"][TEXT]         = $sem;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["APPOINTED_DAY"][TEXT]    = $appointed_day;
            $data["LESSON"][NUMBER]         = $lesson;

            for ($i = 0; $i < get_count($setField); $i++) {
                if (in_array($setField[$i], $useField)) {
                    if ($setField[$i] == "VIRUS" && $model->Properties["useVirus"] != "true") {
                        $data[$setField[$i]][NUMBER] = 0;
                    } elseif ($setField[$i] == "KOUDOME" && $model->Properties["useKoudome"] != "true") {
                        $data[$setField[$i]][NUMBER] = 0;
                    } else {
                        $data[$setField[$i]][NUMBER] = ($dataS[$schregno][$setField[$i]] > 0) ? $dataS[$schregno][$setField[$i]] : 0;
                    }
                } else {
                    $data[$setField[$i]][NUMBER] = 0;
                }
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
        } else {
            //更新
            $data["APPOINTED_DAY"][TEXT]    = $appointed_day;
            $data["LESSON"][NUMBER]         = $lesson;

            for ($i = 0; $i < get_count($setField); $i++) {
                if (in_array($setField[$i], $useField)) {
                    if ($setField[$i] == "VIRUS" && $model->Properties["useVirus"] != "true") {
                    } elseif ($setField[$i] == "KOUDOME" && $model->Properties["useKoudome"] != "true") {
                    } else {
                        $data[$setField[$i]][NUMBER] = ($dataS[$schregno][$setField[$i]] > 0) ? $dataS[$schregno][$setField[$i]] : 0;
                    }
                }
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     MONTH       = '".$month."' AND ";
            $where .= "     SEMESTER    = '".$sem."' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' ";

            $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
        }

        return $query;
    }

    //更新 -- ATTEND_SEMES_SUBL_DAT
    public function updateAttendSemesSublDat($month, $sem, $schregno, $di_cd, $subl_cd, $cntL)
    {
        $data = array();
        $data["COPYCD"][TEXT]       = 0;
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["MONTH"][TEXT]        = $month;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["DI_CD"][TEXT]        = $di_cd;
        $data["SUBL_CD"][TEXT]      = $subl_cd;
        $data["CNT"][NUMBER]        = $cntL;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_SEMES_SUBL_DAT");

        return $query;
    }

    //更新 -- ATTEND_SEMES_SUBM_DAT
    public function updateAttendSemesSubMDat($month, $sem, $schregno, $di_cd, $subl_cd, $subm_cd, $cntM)
    {
        $data = array();
        $data["COPYCD"][TEXT]       = 0;
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["MONTH"][TEXT]        = $month;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["DI_CD"][TEXT]        = $di_cd;
        $data["SUBL_CD"][TEXT]      = $subl_cd;
        $data["SUBM_CD"][TEXT]      = $subm_cd;
        $data["CNT"][NUMBER]        = $cntM;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_SEMES_SUBM_DAT");

        return $query;
    }

    //更新 -- ATTEND_SEMES_DETAIL_DAT
    public function updateAttendSemesDetailDat($month, $sem, $schregno, $di_cd, $dataD)
    {
        $data = array();
        $data["COPYCD"][TEXT]       = 0;
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["MONTH"][TEXT]        = $month;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["SEQ"][TEXT]          = sprintf("%03d", $di_cd);
        $data["CNT"][NUMBER]        = ($dataD[$schregno][$di_cd] > 0) ? $dataD[$schregno][$di_cd] : 0;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");

        return $query;
    }

    //削除 -- ATTEND_SEMES_DAT
    public function deleteAttendSemesDat($month, $sem, $schregno)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$sem."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //削除 -- ATTEND_SEMES_SUBL_DAT
    public function deleteAttendSemesSublDat($month, $sem, $schregno)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SEMES_SUBL_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$sem."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //削除 -- ATTEND_SEMES_SUBM_DAT
    public function deleteAttendSemesSubmDat($month, $sem, $schregno)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SEMES_SUBM_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$sem."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //削除 -- ATTEND_SEMES_DETAIL_DAT
    public function deleteAttendSemesDetailDat($month, $sem, $schregno)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SEMES_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$sem."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";
        $query .= "     AND SEQ IN ('051','052','053') ";

        return $query;
    }

    //更新処理
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //月、学期
        list($month, $sem) = explode('-', $model->month_sem);
        //年取得
        $year = ((int)$month < 4) ? CTRL_YEAR + 1 : CTRL_YEAR;

        for ($i = $model->day_from; $i <= $model->day_to; $i++) {
            for ($g = 0; $g < $model->data_cnt; $g++) {
                //日付
                $date = $year."-".$month."-".sprintf('%02d', $i);

                //削除 -- ATTEND_DAY_DAT
                $db->query(knjc034dQuery::deleteAttendDayDat($model->fields["SCHREGNO"][$g], $date));

                if ($model->Properties["attend_Shosai"] == 1) {
                    //削除 -- ATTEND_DAY_SUBL_DAT
                    $db->query(knjc034dQuery::deleteAttendDaySublDat($model->fields["SCHREGNO"][$g], $date));
                    //削除 -- ATTEND_DAY_SUBM_DAT
                    $db->query(knjc034dQuery::deleteAttendDaySubmDat($model->fields["SCHREGNO"][$g], $date));

                    list($di_cd, $subl_cd, $subm_cd) = explode('-', $model->fields[$date][$g]);
                } else {
                    $di_cd = $model->fields[$date][$g];
                }

                if ($di_cd == "99") {
                    //更新（遅刻） -- ATTEND_DAY_DAT
                    $db->query(knjc034dQuery::updateAttendDayDat($model->fields["SCHREGNO"][$g], $date, "15"));
                    //更新（早退） -- ATTEND_DAY_DAT
                    $db->query(knjc034dQuery::updateAttendDayDat($model->fields["SCHREGNO"][$g], $date, "16"));
                } elseif ($model->fields[$date][$g]) {
                    //更新 -- ATTEND_DAY_DAT
                    $db->query(knjc034dQuery::updateAttendDayDat($model->fields["SCHREGNO"][$g], $date, $di_cd));

                    if ($model->Properties["attend_Shosai"] == 1) {
                        //更新 -- ATTEND_DAY_SUBL_DAT
                        if ($subl_cd != '0000') {
                            $db->query(knjc034dQuery::updateAttendDaySublDat($model->fields["SCHREGNO"][$g], $date, $di_cd, $subl_cd));
                        }
                        //更新 -- ATTEND_DAY_SUBM_DAT
                        if ($subm_cd != '0000') {
                            $db->query(knjc034dQuery::updateAttendDaySubmDat($model->fields["SCHREGNO"][$g], $date, $di_cd, $subl_cd, $subm_cd));
                        }
                    }
                }

                //出欠の備考
                if ($i == $model->day_from) {
                    //備考の参照月取得
                    $remarkMonth = ($model->Properties["useAttendRemarkLastMonth"] == 1) ? $model->last_month : $month;
                    //削除 -- ATTEND_SEMES_REMARK_DAT
                    $db->query(knjc034dQuery::deleteAttendSemesRemarkDat($remarkMonth, $sem, $model->fields["SCHREGNO"][$g]));
                    //更新 -- ATTEND_SEMES_REMARK_DAT
                    $db->query(knjc034dQuery::updateAttendSemesRemarkDat($remarkMonth, $sem, $model->fields["SCHREGNO"][$g], $model->fields["REMARK"][$g]));
                }
            }

            //法定クラスのみ出欠状況を更新
            if ($model->hr_class_type == "1" && $model->grade_mix != "1") {
                //削除 -- ATTEND_DAY_HRATE_DAT
                $db->query(knjc034dQuery::deleteAttendDayHrateDat($model, $date));

                //出欠済みのとき、更新
                if ($model->fields["EXECUTED"][$date] == "1") {
                    //更新 -- ATTEND_DAY_HRATE_DAT
                    $db->query(knjc034dQuery::updateAttendDayHrateDat($model, $date));
                }
            }
        }

        if ($model->Properties["attend_Shosai"] == 1) {
            list($grade, $hr_class) = explode('-', $model->grade_hr_class);

            //開始日付・終了日付
            $sdate = $year."-".$month."-".sprintf('%02d', $model->day_from);
            $edate = $year."-".$month."-".sprintf('%02d', $model->day_to);

            $appdayG = "";
            if ($model->Properties["useAppointedDayGradeMst"] == '1') {
                //締め日取得 -- V_APPOINTED_DAY_GRADE_MST
                $appdayG = $db->getOne(knjc034dQuery::getAppointedDay($model, $month, $sem, $grade));
            }
            //締め日取得 -- APPOINTED_DAY_MST
            $appday = $db->getOne(knjc034dQuery::getAppointedDay($model, $month, $sem));

            //締め日取得
            if ($appdayG != "") {
                $appointed_day = $appdayG;
            } elseif ($appday != "") {
                $appointed_day = $appday;
            } else {
                $appointed_day = $model->day_to;
            }

            //集計データ取得 -- ATTEND_DAY_DAT
            $dataS = array();
            $query = knjc034dQuery::getSumSemesData($model, $month, $sem);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataS[$row["SCHREGNO"]] = $row;
            }
            $result->free();

            //集計データ取得 -- ATTEND_DAY_SUBL_DAT
            $dataL = array();
            $query = knjc034dQuery::getSumSemesSublData($model, $month, $sem);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataL[$row["SCHREGNO"]][$row["DI_CD"]][$row["SUBL_CD"]] = $row["CNT"];
            }
            $result->free();

            //集計データ取得 -- ATTEND_DAY_SUBM_DAT
            $dataM = array();
            $query = knjc034dQuery::getSumSemesSubMData($model, $month, $sem);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataM[$row["SCHREGNO"]][$row["DI_CD"]][$row["SUBL_CD"]][$row["SUBM_CD"]] = $row["CNT"];
            }
            $result->free();

            //集計データ取得 -- ATTEND_DAY_DAT
            $dataD = array();
            $query = knjc034dQuery::getSumSemesDetailData($model, $month, $sem);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataD[$row["SCHREGNO"]][$row["DI_CD"]] = $row["CNT"];
            }
            $result->free();

            for ($g = 0; $g < $model->data_cnt; $g++) {
                //削除（全日異動者）
                if ($model->fields["IDOU_TYPE"][$g] == 'all_day') {
                    //削除 -- ATTEND_SEMES_SUBL_DAT
                    $db->query(knjc034dQuery::deleteAttendSemesDat($month, $sem, $model->fields["SCHREGNO"][$g]));
                }
                //削除 -- ATTEND_SEMES_SUBL_DAT
                $db->query(knjc034dQuery::deleteAttendSemesSublDat($month, $sem, $model->fields["SCHREGNO"][$g]));
                //削除 -- ATTEND_SEMES_SUBM_DAT
                $db->query(knjc034dQuery::deleteAttendSemesSubmDat($month, $sem, $model->fields["SCHREGNO"][$g]));
                //削除 -- ATTEND_SEMES_DETAIL_DAT
                $db->query(knjc034dQuery::deleteAttendSemesDetailDat($month, $sem, $model->fields["SCHREGNO"][$g]));

                //授業日数取得
                $lessonH = $db->getOne(knjc034dQuery::getLesson($month, $sem, $model->fields["SCHREGNO"][$g]));
                $lessonG = $db->getOne(knjc034dQuery::getLesson($month, $sem, $model->fields["SCHREGNO"][$g], "grade"));
                $lessonC = $db->getOne(knjc034dQuery::getLesson($month, $sem, $model->fields["SCHREGNO"][$g], "course"));
                $lessonS = $db->getOne(knjc034dQuery::getLesson2($model, $model->fields["SCHREGNO"][$g], $sdate, $edate));

                //授業日数取得
                if ($model->fields["IDOU_TYPE"][$g] == 'some_day') {    //一部異動者
                    $lesson = $model->fields["LESSONCNT"][$g];
                } elseif ($lessonS != "") {    //訪問生
                    $lesson = $lessonS;
                } elseif ($lessonH != "") {    //年組
                    $lesson = $lessonH;
                } elseif ($lessonC != "") {    //コース
                    $lesson = $lessonC;
                } elseif ($lessonG != "") {    //学年
                    $lesson = $lessonG;
                } else {
                    $lesson = 0;
                }

                //更新（全日異動者は除く）
                if ($model->fields["IDOU_TYPE"][$g] != 'all_day') {
                    //更新 -- ATTEND_SEMES_DAT
                    $db->query(knjc034dQuery::updateAttendSemesDat($db, $model, $month, $sem, $model->fields["SCHREGNO"][$g], $appointed_day, $lesson, $dataS));

                    //更新 -- ATTEND_SEMES_SUBL_DAT
                    $tmpcd = "";
                    $query = knjc034dQuery::getAttendCode($model);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        list($di_cd, $subl_cd, $subm_cd) = preg_split("/-/", $row["VALUE"]);

                        if ($subl_cd == '0000') {
                            continue;
                        }
                        if ($tmpcd == $di_cd."-".$subl_cd) {
                            continue;
                        }

                        $cntL = $dataL[$model->fields["SCHREGNO"][$g]][$di_cd][$subl_cd];
                        if ($cntL > 0) {
                            $db->query(knjc034dQuery::updateAttendSemesSublDat($month, $sem, $model->fields["SCHREGNO"][$g], $di_cd, $subl_cd, $cntL));
                        }
                        $tmpcd = $di_cd."-".$subl_cd;
                    }
                    $result->free();

                    //更新 -- ATTEND_SEMES_SUBM_DAT
                    $query = knjc034dQuery::getAttendCode($model);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        list($di_cd, $subl_cd, $subm_cd) = preg_split("/-/", $row["VALUE"]);

                        if ($subm_cd == '0000') {
                            continue;
                        }

                        $cntM = $dataM[$model->fields["SCHREGNO"][$g]][$di_cd][$subl_cd][$subm_cd];
                        if ($cntM > 0) {
                            $db->query(knjc034dQuery::updateAttendSemesSubMDat($month, $sem, $model->fields["SCHREGNO"][$g], $di_cd, $subl_cd, $subm_cd, $cntM));
                        }
                    }
                    $result->free();

                    //更新 -- ATTEND_SEMES_DETAIL_DAT
                    $updDicd = array();
                    $query = knjc034dQuery::getAttendCode($model);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        list($di_cd, $subl_cd, $subm_cd) = preg_split("/-/", $row["VALUE"]);
                        if (in_array($di_cd, array('51','52','53')) && !in_array($di_cd, $updDicd)) {
                            $updDicd[] = $di_cd;
                        }
                    }
                    $result->free();

                    foreach ($updDicd as $key => $di_cd) {
                        $db->query(knjc034dQuery::updateAttendSemesDetailDat($month, $sem, $model->fields["SCHREGNO"][$g], $di_cd, $dataD));
                    }
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
