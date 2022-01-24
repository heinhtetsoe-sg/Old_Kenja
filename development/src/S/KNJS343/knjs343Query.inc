<?php

require_once('for_php7.php');

class knjs343Query extends Query
{

    /****************/
    /* ヘッダー部分 */
    /****************/
    //年組取得
    public function getGradeHrClass($model)
    {
        $query = "";
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
            }
            $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR     = M1.YEAR AND ";
            $query .= "                                     T1.SEMESTER = M1.SEMESTER AND ";
            $query .= "                                     T1.HR_CLASS = M1.HR_CLASS AND ";
            $query .= "                                     T1.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
            $query .= "                                     T1.STAFFCD  = M1.STAFFCD ";
            $query .= "                                 ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_CLASS_NAME1, ";
            $query .= "     T1.SCHOOL_KIND ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND DESC, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     LABEL ";
        } elseif ($model->field["HR_CLASS_TYPE"] == "2") {
            $query  = " SELECT DISTINCT ";
            if ($model->Properties["useFi_Hrclass"] != '1' && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     GHR_CD || '-ghr' AS VALUE, ";
                $query .= "     GHR_NAME AS LABEL ";
            } else {
                $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
                $query .= "     HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_FI_HDAT ";
            } elseif ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT ";
            }
            $query .= " WHERE ";
            $query .= "     YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND GRADE IN (SELECT ";
                    $query .= "                       REGD_G.GRADE ";
                    $query .= "                   FROM ";
                    $query .= "                       SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                   WHERE ";
                    $query .= "                       REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                       AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                }
            } elseif ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                       REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                       SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                       REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                       AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR           = '".CTRL_YEAR."' ";
            $query .= "     AND REGD_H.SEMESTER   = '".CTRL_SEMESTER."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                              REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                              AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                              AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    /**********************/
    /* 対象月コンボで使用 */
    /**********************/
    //学期情報取得
    public function getSemesAll()
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
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月データ取得
    public function getMonthName($month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                if ($model->field["GAKUNEN_KONGOU"] == "1" || $model->field["HR_CLASS_TYPE"] != "1") {
                    $setNameCd = "Z".SCHOOLKIND."05";
                } else {
                    $setNameCd  = "Z".$model->schKind."05";
                }
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' AND ";
        $query .= "     NAMECD2 = '".sprintf('%02d', $month)."' ";

        return $query;
    }

    /**************/
    /* データ部分 */
    /**************/
    //DI_CD取得
    public function getDiCd($model)
    {
        $query  = " WITH T_COMMON(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('{$model->koteiDiCd}', '病・事故欠') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "    NAMECD2 as VALUE, ";
        $query .= "    NAME1 as LABEL ";
        $query .= " FROM ";
        $query .= "    V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "        YEAR    = '".CTRL_YEAR."' ";
        $query .= "    AND NAMECD1 = 'C001' ";
        $query .= "    AND NAMECD2 IN ({$model->sqlInDiCd}) ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //生徒 + 保護者氏名取得
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
            $query .= "    DISTINCT ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
        }
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCHREGNO || '-' || value(GURD.GUARD_NAME, '') as VALUE, ";
        $query .= "     value(GURD.GUARD_NAME, '') as GNAME, ";
        if ($type == "2" && $hr_class == 'ghr') {
            $query .= "     T1.GHR_ATTENDNO AS ATTENDNO, ";
        } elseif ($type == "1" && $mix == "1") {
            $query .= "     VALUE(L2.HR_NAMEABBV,'') || '-' || T1.ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "     T1.ATTENDNO, ";
        }
        $query .= "     L1.NAME_SHOW as LABEL ";
        $query .= " FROM ";
        $query .=       $tablename." T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT GURD ON GURD.SCHREGNO = T1.SCHREGNO ";//保護者データ
        if ($type == "1" && $mix == "1") {
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR     = T1.YEAR ";
            $query .= "                                  AND L2.SEMESTER = T1.SEMESTER ";
            $query .= "                                  AND L2.GRADE    = T1.GRADE ";
            $query .= "                                  AND L2.HR_CLASS = T1.HR_CLASS, ";
            $query .= "     ".$tablename2." T3 ";
        }
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
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //メインデータ
    public function getAttendAbsenseMonthRemarkDat($month_sem)
    {
        list($month, $sem) = explode('-', $month_sem);

        $query  = " SELECT ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.DI_CD, ";
        $query .= "     MAIN.TOTAL_DAY, ";
        $query .= "     MAIN.REMARK, ";
        $query .= "     MAIN.SCHREGNO || '-' || value(GURD.GUARD_NAME, '') as NAME, ";
        $query .= "     value(GURD.GUARD_NAME, '') as GNAME, ";
        $query .= "     MAIN.TREATMENT,     ";
        $query .= "     MAIN.SEQ ";
        $query .= " FROM ";
        $query .= "     ATTEND_ABSENCE_MONTH_REMARK_DAT MAIN ";
        $query .= "     LEFT JOIN GUARDIAN_DAT GURD ON GURD.SCHREGNO = MAIN.SCHREGNO ";//保護者データ
        $query .= " WHERE ";
        $query .= "         MAIN.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND MAIN.MONTH    = '{$month}' ";
        $query .= "     AND MAIN.SEMESTER = '{$sem}' ";
        $query .= " ORDER BY ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.SEQ ";

        return $query;
    }

    /******************/
    /* デフォルト設定 */
    /******************/
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

    //休日データ取得
    public function getEventDat($grade_hr_class, $hrClassDiv, $date_from, $date_to)
    {
        list($grade, $hrClass) = explode('-', $grade_hr_class);
        $hrClass = ($hrClass == 'ghr') ? '00': $hrClass;

        $query  = " SELECT ";
        $query .= "     EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "         GRADE        = '{$grade}' ";
        $query .= "     AND HR_CLASS     = '{$hrClass}' ";
        $query .= "     AND HR_CLASS_DIV = '{$hrClassDiv}' ";
        $query .= "     AND EXECUTEDATE between '{$date_from}' and '{$date_to}' ";
        $query .= "     AND HOLIDAY_FLG  = '1' ";
        $query .= " ORDER BY ";
        $query .= "     EXECUTEDATE ";

        return $query;
    }

    //出欠データ取得
    public function getAttendDayDat($model, $sqlInSchregNo, $date_from, $date_to)
    {
        $query  = " SELECT ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.ATTENDDATE, ";
        $query .= "     MAIN.DI_CD ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_DAT MAIN ";
        $query .= " WHERE ";
        $query .= "         MAIN.SCHREGNO in ('{$sqlInSchregNo}') ";
        $query .= "     AND MAIN.DI_CD    in ({$model->sqlInDiCd}) ";
        $query .= "     AND MAIN.ATTENDDATE between '{$date_from}' and '{$date_to}' ";
        $query .= " ORDER BY ";
        $query .= "     MAIN.ATTENDDATE ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //月、学期セット
        list($month, $sem) = explode("-", $model->field["TARGET_MONTH"]);

        $seqs = array();
        for ($i=1; $i <= $model->rowCnt; $i++) {
            list($schregNo, $gName) = explode("-", $model->field["NAME-".$i]);

            //名前無い行は更新しない
            if ($schregNo == '') {
                continue;
            }

            $seqs[$schregNo][] = $i;
        }

        foreach ($seqs as $schregNo => $ilist) {
            knjs343Query::getDeleteQuery($db, $month, $sem, $schregNo);

            for ($j = 0; $j < get_count($ilist); $j++) {
                $i = $ilist[$j];

                //削除にチェックありは削除
                if ($model->field["CHECK-".$i] == '1') {
                    continue;
                }

                //ATTEND_ABSENCE_MONTH_REMARK_DAT
                $data = array();
                $data["DI_CD"][TEXT]        = $model->field["DI_CD-".$i];
                $data["TOTAL_DAY"][TEXT]    = $model->field["TOTAL_DAY-".$i];
                $data["REMARK"][TEXT]       = $model->field["REMARK-".$i];
                $data["TREATMENT"][TEXT]    = $model->field["TREATMENT-".$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["MONTH"][TEXT]        = $month;
                $data["SEMESTER"][TEXT]     = $sem;
                $data["SCHREGNO"][TEXT]     = $schregNo;
                $data["SEQ"][NUMBER]        = $j + 1;

                $query = Query::insertSQL($data, "ATTEND_ABSENCE_MONTH_REMARK_DAT");

                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //データカウント
    public function getCntData($month, $sem, $schregNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_ABSENCE_MONTH_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND MONTH    = '{$month}' ";
        $query .= "     AND SEMESTER = '{$sem}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    //削除
    public function getDeleteQuery($db, $month, $sem, $schregNo)
    {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_ABSENCE_MONTH_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND MONTH    = '{$month}' ";
        $query .= "     AND SEMESTER = '{$sem}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";

        $db->query($query);
        return;
    }
}
