<?php

require_once('for_php7.php');

class knja050Query extends Query
{

    //台帳用校種
    public function getSchoolKinds($model)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //新入生学年取得
    public function getA023()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";

        return $query;
    }

    //フィールド有無チェック
    public function checkSchregAddressDat()
    {
        $query  = " WITH TMP AS (";
        $query .= " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHREG_ADDRESS_DAT' ";
        $query .= " ) SELECT NAME FROM TMP ";

        return $query;
    }

    //学校情報
    public function getNameMstA023($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='".$model->next_year."' AND ";
        $query .= "     NAMECD1='A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //学校情報
    public function getNameMstZ010()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='Z010' AND ";
        $query .= "     NAMECD2='00' ";
        return $query;
    }

    //処理学年コンボ
    public function getGrade($model, $next_year)
    {
        $query  = "";
        $query .= " SELECT  T1.GRADE, G1.GRADE_NAME1, G1.SCHOOL_KIND ";
        $query .= " FROM    CLASS_FORMATION_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T1.YEAR AND G1.GRADE = T1.GRADE ";
        $query .= " WHERE   T1.YEAR = '$next_year' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY T1.GRADE, G1.GRADE_NAME1, G1.SCHOOL_KIND ";
        $query .= " ORDER BY T1.GRADE ";
        return $query;
    }

    //新入生入学日付を取得
    public function getEntranceDate($year)
    {
        $query  = " SELECT ENTRANCE_DATE, GRADUATE_DATE ";
        $query .= "   FROM SCHOOL_MST ";
        $query .= "  WHERE YEAR ='".$year."'  ";
        return $query;
    }

    //台帳番号(grd_no)に文字が入っているか調べる
    public function getCaseGrdno($year, $mode, $model)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(T1.case_grd_no) ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        CASE WHEN translate(grd_no,'','0123456789') = '' THEN 0 ";
        $query .= "             WHEN grd_no IS NULL THEN 0 ";
        $query .= "             ELSE 1 END AS case_grd_no ";
        $query .= "    FROM ";
        $query .= "        SCHREG_ENT_GRD_HIST_DAT ";
        $query .= "    WHERE ";
        if ($model->school_kind) {
            $query .= "        SCHOOL_KIND = '{$model->school_kind}' AND ";
        }
        if ($mode == 1) {
            $query .= "        fiscalyear(grd_date) < '".$year."' AND ";
        } else {
            $query .= "        fiscalyear(grd_date) <= '".$year."' AND ";
        }
        $query .= "       (grd_div = '1' OR ";
        $query .= "        GRD_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'A003' AND NAMESPARE1 = '1')) ";
        $query .= "    ) T1 ";
        $query .= "WHERE ";
        $query .= "    case_grd_no = 1 ";

        return $query;
    }

    //台帳番号(grd_no)の最大値
    public function getMaxGrdno($year, $mode, $model)
    {
        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     GDAT.SCHOOL_KIND, ";
        $query .= "     REGD.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
        $query .= "          AND REGD.GRADE = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '{$year}' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->school_kind) {
            $query .= "     AND GDAT.SCHOOL_KIND = '{$model->school_kind}' ";
        }
        $query .= " ), GRD_NOS AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(VALUE(BASE.GRD_NO, '0')) AS MAX_NO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE, ";
        $query .= "     SCH_T ";
        $query .= " WHERE ";
        $query .= "     SCH_T.SCHREGNO = BASE.SCHREGNO ";
        if ($mode == 1) {
            $query .= "     AND fiscalyear(BASE.GRD_DATE) < '".$year."' ";
        } else {
            $query .= "     AND fiscalyear(BASE.GRD_DATE) <= '".$year."' ";
        }
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MAX(VALUE(EGHIST.GRD_NO, '0')) AS MAX_NO ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT EGHIST, ";
        $query .= "     SCH_T ";
        $query .= " WHERE ";
        $query .= "     SCH_T.SCHREGNO = EGHIST.SCHREGNO ";
        $query .= "     AND SCH_T.SCHOOL_KIND = EGHIST.SCHOOL_KIND ";
        if ($mode == 1) {
            $query .= "     AND fiscalyear(EGHIST.GRD_DATE) < '".$year."' ";
        } else {
            $query .= "     AND fiscalyear(EGHIST.GRD_DATE) <= '".$year."' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAX(INTEGER(MAX_NO)) AS MAX_NO ";
        $query .= " FROM ";
        $query .= "     GRD_NOS ";

        return $query;
    }

    //来年度に存在する学年・生徒数・未設定(組,番,コース)の人数
    public function readQuery($model, $next_year, $year)
    {
        $query  = " SELECT INTEGER(t1.grade) gradei,t1.stdcnt,t1.re_stdcnt,t2.hrcnt,t3.atcnt,t4.cscnt FROM ";
        $query .= "        (SELECT year,grade,COUNT(*) stdcnt,sum(integer(remaingrade_flg)) re_stdcnt  ";
        $query .= "         FROM class_formation_dat WHERE year = '".$next_year."' group by year,grade ";
        $query .= "         ) t1 ";
        $query .= "   LEFT JOIN ";
        $query .= "        (SELECT year,grade,COUNT(*) hrcnt " ;
        $query .= "         FROM class_formation_dat WHERE year = '".$next_year."' and hr_class is null group by year,grade ";
        $query .= "         ) t2 on t1.year=t2.year and t1.grade=t2.grade ";
        $query .= "   LEFT JOIN ";
        $query .= "        (SELECT year,grade,COUNT(*) atcnt ";
        $query .= "         FROM class_formation_dat WHERE year = '".$next_year."' and attendno is null group by year,grade ";
        $query .= "         ) t3 on t1.year=t3.year and t1.grade=t3.grade ";
        $query .= "   LEFT JOIN ";
        $query .= "        (SELECT year,grade,COUNT(*) cscnt ";
        $query .= "         FROM class_formation_dat WHERE year = '".$next_year."' and coursecode is null group by year,grade ";
        $query .= "         ) t4 on t1.year=t4.year and t1.grade=t4.grade ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = '".$next_year."' AND G1.GRADE = t1.GRADE ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT G2 ON G2.YEAR = '".$year."' AND G2.GRADE = t1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     (G1.SCHOOL_KIND IS NULL OR G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                $query .= "     OR (G2.SCHOOL_KIND IS NULL OR G2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     (G1.SCHOOL_KIND IS NULL OR G1.SCHOOL_KIND = '".SCHOOLKIND."') ";
            $query .= "     OR (G2.SCHOOL_KIND IS NULL OR G2.SCHOOL_KIND = '".SCHOOLKIND."') ";
        }
        $query .= "   ORDER BY t1.grade ";
        return $query;
    }

    //卒業生の人数
    public function getGrdStudent($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT T1 ";
        $query .= "     INNER JOIN (SELECT DISTINCT SCHREGNO, YEAR, GRADE FROM SCHREG_REGD_DAT) T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T2.YEAR AND G1.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "    (T1.GRD_DIV = '1' OR ";
        $query .= "     T1.GRD_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'A003' AND NAMESPARE1 = '1')) ";
        $query .= "     AND FISCALYEAR(T1.GRD_DATE) = '".$year."' ";
        $query .= "     AND T2.YEAR = '".$year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }

    //処理年度のMAX学期とMIN学期を取得
    public function getSemesterRange($year)
    {
        $query  = " SELECT ";
        $query .= " count(SEMESTER) as MAX_SEMESTER, ";
        $query .= " min(SEMESTER) as MIN_SEMESTER ";
        $query .= " FROM ";
        $query .= " SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NOT SEMESTER = '9' ";
        return $query;
    }

    //設定年度のMIN学期を取得
    public function getNextMinSemester($next_year)
    {
        $query .= " SELECT ";
        $query .= "     min(SEMESTER) as nextsemester";
        $query .= " FROM ";
        $query .= "     semester_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$next_year."' ";
        return $query;
    }

    //学校区分・学年数上限・創立年度を取得
    public function getSchInformation($year)
    {
        $query .= " SELECT ";
        $query .= "     schooldiv,grade_hval,integer(foundedyear) as founde ";
        $query .= " FROM ";
        $query .= "     school_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$year."' ";
        return $query;
    }

    //新入生移行データを設定年度で検索
    public function getFreshmanDat($model)
    {
        $query  = "SELECT COUNT(*) FROM FRESHMAN_DAT T1 WHERE T1.ENTERYEAR = '".$model->next_year."'";
        $query .= " AND     EXISTS(SELECT 'X' FROM CLASS_FORMATION_DAT W1 ";
        $query .= "                WHERE  W1.YEAR = '".$model->next_year."' ";
        $query .= "                AND    W1.SEMESTER = '".$model->next_semester."' ";
        $query .= "                AND    W1.GRADE = '".$model->grade."' ";
        $query .= "                AND    W1.SCHREGNO = T1.SCHREGNO) ";
        return $query;
    }

    //クラス編成データのHR_CLASS,ATTENDNO,COURSECODEが１項目でもnullがあるデータを取得
    public function getExClassFormation($model)
    {
        $query  = " SELECT ";
        $query .= " COUNT(*) ";
        $query .= " FROM ";
        $query .= " CLASS_FORMATION_DAT ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$model->next_year."' AND ";
        $query .= " SEMESTER = '".$model->next_semester."' AND ";
        $query .= " GRADE = '".$model->grade."' AND ";
        $query .= " (HR_CLASS is null or ATTENDNO is null or COURSECODE is null ) ";

        return $query;
    }

    //設定処理年度の学期の存在確認
    public function getNextSemester($next_year)
    {
        $query  = " SELECT COUNT(semester) FROM semester_mst WHERE year='".$next_year."' ";
        return $query;
    }

    //設定処理年度で新入生移行データを抽出し、その学籍番号が学籍基礎マスタにないか件数をチェック
    public function schregnoExists($model)
    {
        $query  = "SELECT COUNT(*)";
        $query .= "  FROM SCHREG_BASE_MST T1";
        $query .= " WHERE EXISTS (SELECT 'X' FROM FRESHMAN_DAT W1";
        $query .= "           WHERE W1.ENTERYEAR = '".$model->next_year."'";
        $query .= "             AND W1.SCHREGNO  = T1.SCHREGNO )";
        $query .= " AND   EXISTS(SELECT 'X' FROM CLASS_FORMATION_DAT W2 ";
        $query .= "              WHERE  W2.YEAR = '".$model->next_year."' ";
        $query .= "              AND    W2.SEMESTER = '".$model->next_semester."' ";
        $query .= "              AND    W2.GRADE = '".$model->grade."' ";
        $query .= "              AND    W2.SCHREGNO = T1.SCHREGNO) ";

        return $query;
    }

    //卒業生台帳番号未採番者存在チェック
    public function existsStillNo($model)
    {
        $query  = "SELECT COUNT(*)";
        $query .= "  FROM SCHREG_BASE_MST T1 ";
        $query .= " WHERE ";
        $query .= "  (T1.grd_div = '1' ";
        $query .= "    OR T1.GRD_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'A003' AND NAMESPARE1 = '1')) ";
        $query .= "   AND fiscalyear(T1.grd_date) = '".CTRL_YEAR."'";
        $query .= "   AND T1.grd_no IS NULL ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND EXISTS (SELECT 'X' ";
                $query .= "     FROM SCHREG_REGD_DAT I1 ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE ";
                $query .= "     WHERE I1.SCHREGNO = T1.SCHREGNO ";
                $query .= "       AND I1.YEAR = '".CTRL_YEAR."' ";
                $query .= "       AND (I2.SCHOOL_KIND IS NULL OR I2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                $query .= " ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND EXISTS (SELECT 'X' ";
            $query .= "     FROM SCHREG_REGD_DAT I1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE ";
            $query .= "     WHERE I1.SCHREGNO = T1.SCHREGNO ";
            $query .= "       AND I1.YEAR = '".CTRL_YEAR."' ";
            $query .= "       AND (I2.SCHOOL_KIND IS NULL OR I2.SCHOOL_KIND = '".SCHOOLKIND."') ";
            $query .= " ) ";
        }
        return $query;
    }

    //設定年度の期間を取得
    public function getYearRange($model)
    {
        $query  = " SELECT ";
        $query .= " SDATE,EDATE ";
        $query .= " FROM ";
        $query .= " SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= " YEAR= '".$model->next_year."' AND ";
        $query .= " SEMESTER='9' ";
        return $query;
    }


    //クラス編成データより学籍在籍データの削除と追加
    public function delSchregRegd($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->next_year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->next_semester."' AND ";
        $query .= "     T1.SCHREGNO IN (SELECT ";
        $query .= "                         S1.SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         S1.YEAR     = T1.YEAR AND ";
        $query .= "                         S1.SEMESTER = T1.SEMESTER AND ";
        $query .= "                         S1.GRADE    = '".$model->grade."' ";
        $query .= "                     UNION    ";
        $query .= "                     SELECT ";
        $query .= "                         S2.SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         CLASS_FORMATION_DAT S2 ";
        $query .= "                     WHERE ";
        $query .= "                         S2.YEAR     = T1.YEAR AND ";
        $query .= "                         S2.SEMESTER = T1.SEMESTER AND ";
        $query .= "                         S2.GRADE    = '".$model->grade."' ";
        $query .= "                     ) ";

        return $query;
    }
    public function addSchregRegd($model)
    {
        $query  = " INSERT INTO schreg_regd_dat ";
        $query .= " SELECT ";
        $query .= "     T1.schregno, ";
        $query .= "     T1.year, ";
        $query .= "     T1.semester, ";
        $query .= "     T1.grade, ";
        $query .= "     T1.hr_class, ";
        $query .= "     T1.attendno, ";

        //学年制の年次(annual)は学年(grade)と同じ
        if ($model->schooldiv == 0) {
            $query .= "    T1.grade, ";
        //単位制の年次(annual)はどんどん上がっていく2004-06-03Y.arakaki 0埋め追加
        } elseif ($model->schooldiv == 1) {
            //同じ年次に進級
            if ($model->Properties["useKeepGrade"] == "1" && $model->Properties["useKeepAnnual"] == "1") {
                $query .= "    T2.ANNUAL, ";
            } else {
                //SCHREG_REGD_DATがある時は、ログイン年度のSCHREG_REAGD_DATの年次＋1　をセットする。
                //SCHREG_REGD_DATがない時は、
                //  名称マスタ「Z010」「00」のNAMESPARE2=not　nullのときは、
                //      学年(grade)をセットする。
                //  それ以外は、
                //      0＋1をセットする。
                if ($model->secondarySchoolDiv == "1" || $model->secondarySchoolDiv == "2") {
                    $query .= "    case when T2.annual is null then T1.grade ";
                    $query .= "         else RIGHT(RTRIM('00'||char(smallint(coalesce(T2.annual,'0')) + 1)),2) ";
                    $query .= "         end as annual, ";
                } else {
                    $query .= "    RIGHT(RTRIM('00'||char(smallint(coalesce(T2.annual,'0')) + 1)),2), ";
                }
            }
        }

        $query .= "     conv_col_row(T1.attendno,2), ";
        $query .= "     conv_col_row(T1.attendno,1), ";
        $query .= "     T1.coursecd, ";
        $query .= "     T1.majorcd, ";
        $query .= "     T1.coursecode, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "      class_formation_dat T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     schreg_regd_dat T2 ON T1.schregno  = T2.schregno ";
        $query .= "                       AND T2.year      = '{$model->this_year}' ";
        $query .= "                       AND T2.semester  = '{$model->this_semester}' ";
        $query .= " WHERE ";
        $query .= "     T1.year     = '{$model->next_year}' AND ";
        $query .= "     T1.semester = '{$model->next_semester}' AND ";
        $query .= "     T1.grade    = '{$model->grade}' ";

        return $query;
    }

    //学籍学歴データ
    public function setEntGrdDat($db, $model)
    {
        $gradeIn = "('".implode("','", $model->sotugyouSinkyu)."')";
        $query  = " WITH TAISYOU AS ( ";
        $query .= " SELECT ";
        $query .= "     CASE WHEN OLD_SCHREGNO IS NOT NULL ";
        $query .= "          THEN OLD_SCHREGNO ";
        $query .= "          ELSE SCHREGNO ";
        $query .= "     END AS SCHREGNO, ";
        $query .= "     OLD_SCHREGNO ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->next_year."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        $query .= "     AND GRADE IN ".$gradeIn." ";
        $query .= "     AND REMAINGRADE_FLG != '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     C1.OLD_SCHREGNO, ";
        $query .= "     G1.SCHOOL_KIND AS SCHOOL_KIND1, ";
        $query .= "     G2.SCHOOL_KIND AS SCHOOL_KIND2, ";
        $query .= "     CASE WHEN F1.SCHREGNO IS NULL ";
        $query .= "          THEN L2.NAMESPARE1 ";
        $query .= "          ELSE T1.FINSCHOOLCD ";
        $query .= "     END AS NEW_FINSCHOOLCD, ";
        $query .= "     CASE WHEN F1.SCHREGNO IS NULL ";
        $query .= "          THEN '".str_replace("/", "-", $model->grd_syoribi)."' ";
        $query .= "          ELSE T1.FINISH_DATE ";
        $query .= "     END AS FINISH_DATE, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     CASE WHEN F1.SCHREGNO IS NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE T1.ENT_DIV ";
        $query .= "     END AS ENT_DIV, ";
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     T1.ENT_ADDR2, ";
        }
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     T1.GRD_ADDR2, ";
        }
        $query .= "     T1.GRD_NO, ";
        $query .= "     T1.GRD_TERM, ";
        if ($model->Properties["grdTermSetNyuugaku"] == "1") {
            $query .= "     CASE WHEN NEXT_A023.NAME2 IS NULL ";
            $query .= "          THEN T1.GRD_TERM ";
            $query .= "          ELSE SCHOOL_M.PRESENT_EST ";
            $query .= "     END AS GRD_TERM2, ";
        } else {
            $query .= "     T1.GRD_TERM AS GRD_TERM2, ";
        }
        $query .= "     CASE WHEN F1.SCHREGNO IS NULL ";
        $query .= "          THEN 'IS_NULL' ";
        $query .= "     END AS FRESHMAN_FLG, ";
        $query .= "     F1.CURRICULUM_YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT R1 ON T1.SCHREGNO = R1.SCHREGNO ";
        $query .= "           AND R1.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND R1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ON R1.YEAR = G1.YEAR ";
        $query .= "          AND R1.GRADE = G1.GRADE ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'A023' ";
        $query .= "          AND G1.SCHOOL_KIND = L2.NAME1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT R2 ON T1.SCHREGNO = R2.SCHREGNO ";
        $query .= "           AND R2.YEAR = '".$model->next_year."' ";
        $query .= "           AND R2.SEMESTER = '".$model->next_semester."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT G2 ON R2.YEAR = G2.YEAR ";
        $query .= "          AND R2.GRADE = G2.GRADE ";
        $query .= "     LEFT JOIN NAME_MST NEXT_A023 ON NEXT_A023.NAMECD1 = 'A023' ";
        $query .= "          AND G2.GRADE = NEXT_A023.NAME2 ";
        $query .= "    LEFT JOIN SCHOOL_MST SCHOOL_M ON G2.YEAR = SCHOOL_M.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND G2.SCHOOL_KIND = SCHOOL_M.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN FRESHMAN_DAT F1 ON F1.ENTERYEAR = '".$model->next_year."' ";
        $query .= "           AND T1.SCHREGNO = F1.SCHREGNO ";
        $query .= "     LEFT JOIN TAISYOU C1 ON T1.SCHREGNO = C1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             TAISYOU E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     ) ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //名称マスタ「Z010」「00」のNAMESPARE2に値がある場合のみ
            if (!$row["OLD_SCHREGNO"] || $model->secondarySchoolDiv) {
                $schregno[] = $row;
            }
        }
        $result->free();

        if (is_array($schregno)) {
            foreach ($schregno as $key => $schRow) {
                if (is_array($schRow)) {
                    if ($schRow["SCHOOL_KIND1"]) {
                        $insQuery = knja050Query::insUpdEntGrdDat($model, $schRow, $schRow["SCHOOL_KIND1"], "UPD");
                        $db->query($insQuery);
                    }
                    if ($schRow["SCHOOL_KIND2"]) {
                        if ($model->schooldiv == 1 && $schRow["SCHOOL_KIND1"] == $schRow["SCHOOL_KIND2"]) {
                            //単位制 かつ 処理年度と次年度の校種が同じとき、
                            //SCHREG_ENT_GRD_HIST_DAT の delete & insert は行わない。
                        } else {
                            $delQuery = knja050Query::delEntGrdDat($schRow, $schRow["SCHOOL_KIND2"]);
                            $db->query($delQuery);
                            $insQuery = knja050Query::insUpdEntGrdDat($model, $schRow, $schRow["SCHOOL_KIND2"], "INS");
                            $db->query($insQuery);
                        }
                        if ($schRow["SCHOOL_KIND1"] && $schRow["SCHOOL_KIND1"] != $schRow["SCHOOL_KIND2"]) {
                            $query = knja050Query::getNextYearAddrCnt($schRow);
                            $nextYearAddrCnt = $db->getOne($query);
                            if ($nextYearAddrCnt == 0) {
                                $insQuery = knja050Query::copyAddr($model, $schRow);
                                $db->query($insQuery);
                            }
                            $delQuery = knja050Query::delGuardianAddr($model, $schRow);
                            $db->query($delQuery);
                            $insQuery = knja050Query::copyGuardianAddr($model, $schRow);
                            $db->query($insQuery);
                            $delQuery = knja050Query::delGuarantorAddr($model, $schRow);
                            $db->query($delQuery);
                            $insQuery = knja050Query::copyGuarantorAddr($model, $schRow);
                            $db->query($insQuery);
                        }
                        if ($schRow["SCHOOL_KIND1"] != $schRow["SCHOOL_KIND2"]) {
                            $updQuery = knja050Query::updBaseMstFromEntGrdDate($model, $schRow, $schRow["SCHOOL_KIND2"]);
                            $db->query($updQuery);
                        }
                    }
                }
            }
        }
    }

    //学籍学歴削除
    public function delEntGrdDat($schRow, $kind)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schRow["SCHREGNO"]."' ";
        $query .= "     AND SCHOOL_KIND = '".$kind."' ";

        return $query;
    }

    //学籍学歴作成
    public function insUpdEntGrdDat($model, $schRow, $kind, $div)
    {
        if ($div == "INS") {
            //課程入学年度の追加処理
            //  ・FRESHMAN_DATがない生徒 ===> 入学日付より課程入学年度を算出
            //  ・FRESHMAN_DATがある生徒 ===> FRESHMAN_DATの課程入学年度（CURRICULUM_YEAR）をセット
            $entDateArray = preg_split("/\//", $model->syoribi);
            $year  = $entDateArray[0];
            $month = $entDateArray[1];
            if ($month == "01" || $month == "02" || $month == "03") {
                $year = $year - 1;
            }
            $curriculumYear = $schRow["FRESHMAN_FLG"] == "IS_NULL" ? $year : $schRow["CURRICULUM_YEAR"];
            $data["SCHREGNO"][TEXT]         = $schRow["SCHREGNO"];
            $data["SCHOOL_KIND"][TEXT]      = $kind;
            $data["FINSCHOOLCD"][TEXT]      = $schRow["NEW_FINSCHOOLCD"];
            $data["FINISH_DATE"][DATE]      = $schRow["FINISH_DATE"];
            $data["CURRICULUM_YEAR"][DATE]  = $curriculumYear;
            $data["ENT_DATE"][DATE]         = $model->syoribi;
            $data["ENT_DIV"][TEXT]          = $schRow["ENT_DIV"];
            if ($model->Properties["grdTermSetNyuugaku"] == "1") {
                $data["GRD_TERM"][TEXT]         = $schRow["GRD_TERM2"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_ENT_GRD_HIST_DAT");
        } else {
            if ($model->grade != "99" && $model->Properties["use_prg_schoolkind"] != "1" && $model->Properties["useSchool_KindField"] == "1" && $model->secondarySchoolDiv == "2" && $schRow["SCHOOL_KIND1"] != $schRow["SCHOOL_KIND2"]) {
                $data["GRD_DATE"][DATE]    = $schRow["GRD_DATE"];
                $data["GRD_DIV"][TEXT]     = $schRow["GRD_DIV"];
            } elseif ($model->grade != "99" && $kind == "J" && $model->secondarySchoolDiv == "2") {
                $data["GRD_DATE"][DATE]    = $model->grd_syoribi;
                $data["GRD_DIV"][TEXT]     = "5";
            } elseif ($model->grade != "99" && $model->secondarySchoolDiv == "1") {
                $data["GRD_DATE"][DATE]    = $model->grd_syoribi;
                $data["GRD_DIV"][TEXT]     = "1";
            } else {
                $data["GRD_DATE"][DATE]    = $schRow["GRD_DATE"];
                $data["GRD_DIV"][TEXT]     = $schRow["GRD_DIV"];
            }
            $data["GRD_REASON"][TEXT]  = $schRow["GRD_REASON"];
            $data["GRD_SCHOOL"][TEXT]  = $schRow["GRD_SCHOOL"];
            $data["GRD_ADDR"][TEXT]    = $schRow["GRD_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["GRD_ADDR2"][TEXT]   = $schRow["GRD_ADDR2"];
            }
            $data["GRD_NO"][TEXT]      = $schRow["GRD_NO"];
            $data["GRD_TERM"][TEXT]    = $schRow["GRD_TERM"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$schRow["SCHREGNO"]."' ";
            $where .= "     AND SCHOOL_KIND = '".$kind."' ";

            $query = Query::updateSQL($data, "SCHREG_ENT_GRD_HIST_DAT", $where);
        }

        return $query;
    }

    //次年度以降の住所データ有無
    public function getNextYearAddrCnt($schRow)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schRow["SCHREGNO"]."' ";
        $query .= "     AND EXPIREDATE > '".(CTRL_YEAR + 1)."-04-01' ";

        return $query;
    }

    public function copyAddr($model, $schRow)
    {
        $query  = " INSERT INTO SCHREG_ADDRESS_DAT (";
        $query .= "   SCHREGNO, ISSUEDATE, EXPIREDATE, ";
        $query .= "   ZIPCD, AREACD, ADDR1, ADDR2, ADDR_FLG, ADDR1_ENG, ADDR2_ENG,";
        $query .= "   TELNO, ";
        if (in_array("TELNO_MEMO", $model->schregAddressDatColumns)) {
            $query .= "     TELNO_MEMO, ";
        }
        $query .= "   TELNO2, ";
        if (in_array("TELNO2_MEMO", $model->schregAddressDatColumns)) {
            $query .= "     TELNO2_MEMO, ";
        }
        $query .= "   FAXNO, EMAIL, REGISTERCD, UPDATED)";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     date('".str_replace("/", "-", $model->syoribi)."') AS ISSUEDATE, ";
        $query .= "     date(rtrim(char(Integer('".$model->next_year."')+3))||'-'||'03'||'-'||'31') AS EXPIREDATE, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     T1.AREACD, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2, ";
        $query .= "     T1.ADDR_FLG, ";
        $query .= "     T1.ADDR1_ENG, ";
        $query .= "     T1.ADDR2_ENG, ";
        $query .= "     T1.TELNO, ";
        if (in_array("TELNO_MEMO", $model->schregAddressDatColumns)) {
            $query .= "     T1.TELNO_MEMO, ";
        }
        $query .= "     T1.TELNO2, ";
        if (in_array("TELNO2_MEMO", $model->schregAddressDatColumns)) {
            $query .= "     T1.TELNO2_MEMO, ";
        }
        $query .= "     T1.FAXNO, ";
        $query .= "     T1.EMAIL, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1, ";
        $query .= "     (SELECT ";
        $query .= "          TT2.SCHREGNO, ";
        $query .= "          MAX(TT2.ISSUEDATE) AS ISSUEDATE ";
        $query .= "      FROM ";
        $query .= "          SCHREG_ADDRESS_DAT TT2 ";
        $query .= "      WHERE ";
        $query .= "          TT2.SCHREGNO = '".$schRow["SCHREGNO"]."' ";
        $query .= "      GROUP BY ";
        $query .= "          TT2.SCHREGNO) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.ISSUEDATE = T2.ISSUEDATE ";

        return $query;
    }

    public function delGuardianAddr($model, $schRow)
    {
        $query  = " DELETE FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schRow["SCHREGNO"]."' AND ";
        $query .= "     ISSUEDATE   = date('".str_replace("/", "-", $model->syoribi)."') ";

        return $query;
    }
    public function copyGuardianAddr($model, $schRow)
    {
        $query  = " INSERT INTO GUARDIAN_ADDRESS_DAT ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     date('".str_replace("/", "-", $model->syoribi)."') AS ISSUEDATE, ";
        $query .= "     date(rtrim(char(Integer('".$model->next_year."')+3))||'-'||'03'||'-'||'31') AS EXPIREDATE, ";
        $query .= "     T1.GUARD_ZIPCD, ";
        $query .= "     T1.GUARD_ADDR1, ";
        $query .= "     T1.GUARD_ADDR2, ";
        $query .= "     T1.GUARD_ADDR_FLG, ";
        $query .= "     T1.GUARD_TELNO, ";
        $query .= "     T1.GUARD_TELNO2, ";
        $query .= "     T1.GUARD_FAXNO, ";
        $query .= "     T1.GUARD_E_MAIL, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT T1, ";
        $query .= "     (SELECT ";
        $query .= "          TT2.SCHREGNO, ";
        $query .= "          MAX(TT2.ISSUEDATE) AS ISSUEDATE ";
        $query .= "      FROM ";
        $query .= "          GUARDIAN_ADDRESS_DAT TT2 ";
        $query .= "      WHERE ";
        $query .= "          TT2.SCHREGNO = '".$schRow["SCHREGNO"]."' ";
        $query .= "      GROUP BY ";
        $query .= "          TT2.SCHREGNO) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.ISSUEDATE = T2.ISSUEDATE ";

        return $query;
    }

    public function delGuarantorAddr($model, $schRow)
    {
        $query  = " DELETE FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schRow["SCHREGNO"]."' AND ";
        $query .= "     ISSUEDATE   = date('".str_replace("/", "-", $model->syoribi)."') ";

        return $query;
    }
    public function copyGuarantorAddr($model, $schRow)
    {
        $query  = " INSERT INTO GUARANTOR_ADDRESS_DAT ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     date('".str_replace("/", "-", $model->syoribi)."') AS ISSUEDATE, ";
        $query .= "     date(rtrim(char(Integer('".$model->next_year."')+3))||'-'||'03'||'-'||'31') AS EXPIREDATE, ";
        $query .= "     T1.GUARANTOR_ZIPCD, ";
        $query .= "     T1.GUARANTOR_ADDR1, ";
        $query .= "     T1.GUARANTOR_ADDR2, ";
        $query .= "     T1.GUARANTOR_ADDR_FLG, ";
        $query .= "     T1.GUARANTOR_TELNO, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT T1, ";
        $query .= "     (SELECT ";
        $query .= "          TT2.SCHREGNO, ";
        $query .= "          MAX(TT2.ISSUEDATE) AS ISSUEDATE ";
        $query .= "      FROM ";
        $query .= "          GUARANTOR_ADDRESS_DAT TT2 ";
        $query .= "      WHERE ";
        $query .= "          TT2.SCHREGNO = '".$schRow["SCHREGNO"]."' ";
        $query .= "      GROUP BY ";
        $query .= "          TT2.SCHREGNO) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.ISSUEDATE = T2.ISSUEDATE ";

        return $query;
    }

    //学籍基礎更新
    public function updBaseMstFromEntGrdDate($model, $schRow, $kind)
    {
        $data["FINSCHOOLCD"][TEXT] = $schRow["NEW_FINSCHOOLCD"];
        $data["FINISH_DATE"][DATE] = $schRow["FINISH_DATE"];
        $data["ENT_DATE"][DATE]    = $model->syoribi;
        $data["ENT_DIV"][TEXT]     = $schRow["ENT_DIV"];
        $data["ENT_REASON"][TEXT]  = "";
        $data["ENT_SCHOOL"][TEXT]  = "";
        $data["ENT_ADDR"][TEXT]    = "";
        if ($model->Properties["useAddrField2"] == "1") {
            $data["ENT_ADDR2"][TEXT]   = "";
        }
        $data["GRD_DATE"][DATE]    = "";
        $data["GRD_DIV"][TEXT]     = "";
        $data["GRD_REASON"][TEXT]  = "";
        $data["GRD_SCHOOL"][TEXT]  = "";
        $data["GRD_ADDR"][TEXT]    = "";
        if ($model->Properties["useAddrField2"] == "1") {
            $data["GRD_ADDR2"][TEXT]   = "";
        }
        $data["GRD_NO"][TEXT]      = "";
        if ($model->Properties["grdTermSetNyuugaku"] == "1") {
            $data["GRD_TERM"][TEXT]    = $schRow["GRD_TERM2"];
        } else {
            $data["GRD_TERM"][TEXT]      = "";
        }
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO = '".$schRow["SCHREGNO"]."' ";

        $query = Query::updateSQL($data, "SCHREG_BASE_MST", $where);

        return $query;
    }

    //学籍基礎マスタの削除
    public function delSchregBm($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     schreg_base_mst w1 ";
        $query .= " WHERE ";
        $query .= "     exists(SELECT ";
        $query .= "                 schregno ";
        $query .= "             FROM ";
        $query .= "                 freshman_dat w2 ";
        $query .= "             WHERE ";
        $query .= "                 enteryear = '{$model->next_year}' AND ";
        $query .= "                 w1.schregno = w2.schregno ";
        $query .= "             ) ";
        $query .= " AND ent_div <> '4' ";
        $query .= " AND     EXISTS(SELECT 'X' FROM CLASS_FORMATION_DAT W3 ";
        $query .= "                WHERE  W3.YEAR     = '{$model->next_year}' ";
        $query .= "                AND    W3.SEMESTER = '{$model->next_semester}' ";
        $query .= "                AND    W3.GRADE    = '{$model->grade}' ";
        $query .= "                AND    W3.SCHREGNO = W1.SCHREGNO) ";
        return $query;
    }
    //学籍基礎マスタへ新入生移行データを追加
    public function addSchregBm($model)
    {
        $query  = " INSERT INTO SCHREG_BASE_MST ( ";
        $query .= "         SCHREGNO,INOUTCD,NAME,NAME_SHOW,NAME_KANA,NAME_ENG,BIRTHDAY, ";
        $query .= "         SEX,BLOODTYPE,BLOOD_RH,FINSCHOOLCD,FINISH_DATE,ENT_DATE,ENT_DIV, ";
        $query .= "         GRD_DATE,GRD_DIV,GRD_NO,EMERGENCYCALL,EMERGENCYTELNO, ";    //2005.10.19 alp m-yama
        $query .= "         PRISCHOOLCD, ";
        if ($model->Properties["grdTermSetNyuugaku"] == "1") {
            $query .= "         GRD_TERM, ";
        }
        $query .= "         REGISTERCD,UPDATED ) ";
        $query .= " SELECT ";
        $query .= "         W1.SCHREGNO,INOUTCD,NAME, ";
        $query .= "         CASE WHEN LENGTH(NAME) > 30 THEN substr(NAME, 1, 30) ELSE NAME END, ";
        $query .= "         NAME_KANA,CAST(null AS varchar(40)),BIRTHDAY, ";
        $query .= "         SEX,CAST(null AS varchar(2)),CAST(null AS varchar(1)),FINSCHOOLCD,FINSCHOOLGRADDATE, ";
        $query .= "         '".str_replace("/", "-", $model->syoribi)."',ENT_DIV, CAST(null AS varchar(6)), ";
        $query .= "         CAST(null AS varchar(1)),CAST(null AS varchar(5)),EMERGENCYCALL,EMERGENCYTELNO, ";    //2005.10.19 alp m-yama
        $query .= "         W1.PRISCHOOLCD, ";
        if ($model->Properties["grdTermSetNyuugaku"] == "1") {
            $query .= "         SCHOOL_M.PRESENT_EST, ";
        }
        $query .= "         '".STAFFCD."',sysdate() ";
        $query .= "   FROM FRESHMAN_DAT W1 ";
        $query .= "        LEFT JOIN SCHREG_REGD_GDAT G2 ON W1.ENTERYEAR = G2.YEAR ";
        $query .= "             AND G2.GRADE = '".$model->grade."' ";
        $query .= "        LEFT JOIN SCHOOL_MST SCHOOL_M ON G2.YEAR = SCHOOL_M.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND G2.SCHOOL_KIND = SCHOOL_M.SCHOOL_KIND ";
        }
        $query .= "  WHERE W1.ENTERYEAR ='".$model->next_year."' ";
        $query .= " AND     EXISTS(SELECT 'X' FROM CLASS_FORMATION_DAT W3 ";
        $query .= "                WHERE  W3.YEAR = '".$model->next_year."' ";
        $query .= "                AND    W3.SEMESTER = '".$model->next_semester."' ";
        $query .= "                AND    W3.GRADE = '".$model->grade."' ";
        $query .= "                AND    W3.SCHREGNO = W1.SCHREGNO) ";
        return $query;
    }

    //SCHREG_BASE_DETAIL_MST(BASE_SEQ=003)の削除
    public function delSchregBaseDetailMst($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST w1 ";
        $query .= " WHERE ";
        $query .= "     exists(SELECT ";
        $query .= "                 schregno ";
        $query .= "             FROM ";
        $query .= "                 freshman_dat w2 ";
        $query .= "             WHERE ";
        $query .= "                 enteryear = '{$model->next_year}' AND ";
        $query .= "                 w1.schregno = w2.schregno ";
        $query .= "             ) ";
        $query .= " AND BASE_SEQ = '003' ";
        $query .= " AND     EXISTS(SELECT 'X' FROM CLASS_FORMATION_DAT W3 ";
        $query .= "                WHERE  W3.YEAR     = '{$model->next_year}' ";
        $query .= "                AND    W3.SEMESTER = '{$model->next_semester}' ";
        $query .= "                AND    W3.GRADE    = '{$model->grade}' ";
        $query .= "                AND    W3.SCHREGNO = W1.SCHREGNO) ";
        return $query;
    }
    //SCHREG_BASE_DETAIL_MST(BASE_SEQ=003)へ新入生移行データを追加
    public function addSchregBaseDetailMst($model)
    {
        $query  = " INSERT INTO SCHREG_BASE_DETAIL_MST ( ";
        $query .= "         SCHREGNO,BASE_SEQ,BASE_REMARK1, ";
        $query .= "         REGISTERCD,UPDATED ) ";
        $query .= " SELECT ";
        $query .= "         W1.SCHREGNO,'003',W1.EXAMNO, ";
        $query .= "         '".STAFFCD."',sysdate() ";
        $query .= "   FROM FRESHMAN_DAT W1 ";
        $query .= "  WHERE W1.ENTERYEAR ='".$model->next_year."' ";
        $query .= " AND     EXISTS(SELECT 'X' FROM CLASS_FORMATION_DAT W3 ";
        $query .= "                WHERE  W3.YEAR     = '".$model->next_year."' ";
        $query .= "                AND    W3.SEMESTER = '".$model->next_semester."' ";
        $query .= "                AND    W3.GRADE    = '".$model->grade."' ";
        $query .= "                AND    W3.SCHREGNO = W1.SCHREGNO) ";
        return $query;
    }

    //学籍住所データの削除
    public function delSchregAddress($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     schreg_address_dat w1 ";
        $query .= " WHERE ";
        $query .= "     exists(SELECT ";
        $query .= "                 w2.schregno ";
        $query .= "             FROM ";
        $query .= "                 freshman_dat w2, ";
        $query .= "                 schreg_base_mst w3 ";
        $query .= "             WHERE ";
        $query .= "                 enteryear = '".$model->next_year."' ";
        $query .= "               AND w1.schregno = w2.schregno ";
        $query .= "               AND w3.ent_div <> '4' ";
        $query .= "               AND w1.schregno = w3.schregno ";
        $query .= "             ) ";
        $query .= " AND     EXISTS(SELECT 'X' FROM CLASS_FORMATION_DAT W3 ";
        $query .= "                WHERE  W3.YEAR = '".$model->next_year."' ";
        $query .= "                AND    W3.SEMESTER = '".$model->next_semester."' ";
        $query .= "                AND    W3.GRADE = '".$model->grade."' ";
        $query .= "                AND    W3.SCHREGNO = W1.SCHREGNO) ";
        return $query;
    }
    //学籍住所データへ新入生移行データを追加
    public function addSchregAddress($model)
    {
        $query  = " INSERT INTO SCHREG_ADDRESS_DAT (";
        $query .= "   SCHREGNO, ISSUEDATE, EXPIREDATE, ";
        $query .= "   ZIPCD, AREACD, ADDR1, ADDR2, ADDR_FLG,";
        $query .= "   TELNO, ";
        $query .= "   FAXNO, EMAIL, REGISTERCD, UPDATED)";
        $query .= " SELECT W1.SCHREGNO, ";
        $query .= "         CASE WHEN W1.ISSUEDATE IS NOT NULL THEN W1.ISSUEDATE ELSE ";
        $query .= "         date('".str_replace("/", "-", $model->syoribi)."') END AS ISSUEDATE, ";
        $query .= "         CASE WHEN W1.EXPIREDATE IS NOT NULL THEN W1.EXPIREDATE ELSE ";
        $query .= "         date(rtrim(char(Integer('".$model->next_year."')+3))||'-'||'03'||'-'||'31') END AS EXPIREDATE, ";
        $query .= "         ZIPCD,AREACD,ADDR1,ADDR2,W1.ADDR_FLG, ";
        $query .= "         TELNO,";
        $query .= "         FAXNO,EMAIL,'".STAFFCD."',sysdate() ";
        $query .= "   FROM FRESHMAN_DAT W1 ";
        $query .= "  WHERE W1.ENTERYEAR = '".$model->next_year."' ";
        $query .= " AND     EXISTS(SELECT 'X' FROM CLASS_FORMATION_DAT W3 ";
        $query .= "                WHERE  W3.YEAR = '".$model->next_year."' ";
        $query .= "                AND    W3.SEMESTER = '".$model->next_semester."' ";
        $query .= "                AND    W3.GRADE = '".$model->grade."' ";
        $query .= "                AND    W3.SCHREGNO = W1.SCHREGNO) ";
        return $query;
    }

    //次年度以外の新入生データ削除
    public function delFreshman($next_year)
    {
        $query  = " DELETE FROM freshman_dat WHERE enteryear <> '".$next_year."' ";
        return $query;
    }

    //卒業生以外の台帳番号(grd_no)をNULLにして卒業生に台帳番号を振る
    public function updDivNull($db, $model)
    {
        $query  = "UPDATE schreg_base_mst T1 ";
        $query .= "   SET grd_no   = NULL ";
        if ($model->Properties["grdTermSetNyuugaku"] != "1") {
            $query .= "       ,grd_term = NULL ";
        }
        $query .= " WHERE ";
        // 除籍区分が'1'ではない生徒
        // 名称マスタ「A003」の予備1が'1'の除籍区分ではない生徒
        $query .= "     VALUE(GRD_DIV, '') NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             NAMECD2 ";
        $query .= "         FROM ";
        $query .= "             NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             NAMECD1 = 'A003' ";
        $query .= "             AND (NAMECD2 = '1' OR NAMESPARE1 = '1') ";
        $query .= "     ) ";
        // 仮卒業フラグ(KNJA032で設定)が'1'ではない生徒
        $query .= "     AND NOT EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_BASE_YEAR_DETAIL_MST E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND E1.BASE_SEQ = '007' ";
        $query .= "             AND E1.BASE_REMARK1 = '1' ";
        $query .= "     ) ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_DAT S1, ";
            $query .= "                 SCHREG_REGD_GDAT S2 ";
            $query .= "             WHERE ";
            $query .= "                 T1.SCHREGNO     = S1.SCHREGNO AND ";
            $query .= "                 S1.YEAR         = S2.YEAR AND ";
            $query .= "                 S1.YEAR         = '".CTRL_YEAR."' AND ";
            $query .= "                 S1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
            $query .= "                 S1.GRADE        = S2.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "             AND S2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "             AND S2.SCHOOL_KIND  = '".SCHOOL_KIND."' ";
            }
            $query .= "     ) ";
        }
        if ($model->school_kind) {
            $query .= " AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_DAT S1, ";
            $query .= "                 SCHREG_REGD_GDAT S2 ";
            $query .= "             WHERE ";
            $query .= "                 T1.SCHREGNO         = S1.SCHREGNO ";
            $query .= "                 AND S1.YEAR         = S2.YEAR ";
            $query .= "                 AND S1.YEAR         = '".CTRL_YEAR."' ";
            $query .= "                 AND S1.SEMESTER     = '".CTRL_SEMESTER."' ";
            $query .= "                 AND S1.GRADE        = S2.GRADE ";
            $query .= "                 AND S2.SCHOOL_KIND  = '{$model->school_kind}' ";
            $query .= "     ) ";
        }
        $db->query($query);
    }

    public function getSchregno($db, $model, $schooldiv, $which_way, $which_way_div, $semester, $grd_no, $mode, $schoolName)
    {
        if ($model->field["WHICH_WAY_DIV"] == "3") {
            $query   = " SELECT ";
            $query  .= "   ASSESSHIGH ";
            $query  .= " FROM ";
            $query  .= "   ASSESS_MST ";
            $query  .= " WHERE ";
            $query  .= "   ASSESSCD = '3' ";
            $query  .= "   AND ASSESSLEVEL = '1' ";

            $assessHigh = $db->getOne($query);
        }

        $query  = "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    CASE WHEN T1.GRD_TERM IS NOT NULL THEN T1.GRD_TERM ELSE L2.PRESENT_EST END AS GRD_TERM, ";
        $query .= "    CASE WHEN L1.SCHREGNO IS NOT NULL THEN T1.REAL_NAME_KANA ELSE T1.NAME_KANA END AS SET_NAME_KANA ";
        if ($model->field["WHICH_WAY_DIV"] == "3") {
            //1科目でもASSESS_MST.ASSESSHIGH未満の得点があれば赤点とする
            $query  .= " , ( ";
            $query  .= " SELECT ";
            $query  .= "      CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END ";
            $query  .= " FROM ";
            $query  .= "      RECORD_RANK_SDIV_DAT SRANK ";
            $query  .= " WHERE ";
            $query  .= "          SRANK.YEAR            = '".CTRL_YEAR."' ";
            $query  .= "      AND SRANK.SEMESTER        = '9' ";
            $query  .= "      AND SRANK.TESTKINDCD      = '99' ";
            $query  .= "      AND SRANK.TESTITEMCD      = '00' ";
            $query  .= "      AND SRANK.SCORE_DIV       = '08' ";
            $query  .= "      AND SRANK.SCHOOL_KIND     = '".$model->school_kind."' ";
            $query  .= "      AND SRANK.SCHREGNO        = T1.SCHREGNO ";
            $query  .= "      AND VALUE(SRANK.SCORE, 0) < ".$assessHigh." ";        //※得点がNULLの場合は0点に換算する仕様
            $query  .= "  ) AS RED_POINT_FLG ";
        }
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST T1 ";
        $query .= "    LEFT JOIN SCHREG_NAME_SETUP_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                      AND L1.DIV = '06', ";
        if ($schooldiv == 0) {
            $query .= "    V_REGDYEAR_GRADE_DAT T2 ";
        } elseif ($schooldiv == 1) {
            $query .= "    V_REGDYEAR_UNIT_DAT T2 ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON T2.YEAR = G1.YEAR ";
            $query .= "         AND T2.GRADE = G1.GRADE ";
            if ($model->selectSchoolKind) {
                $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    INNER JOIN SCHREG_REGD_GDAT G1 ON T2.YEAR = G1.YEAR ";
            $query .= "         AND T2.GRADE        = G1.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else {
            $query .= "    LEFT JOIN SCHREG_REGD_GDAT G1 ON T2.YEAR = G1.YEAR ";
            $query .= "         AND T2.GRADE = G1.GRADE ";
        }
        $query .= "    LEFT JOIN SCHOOL_MST L2 ON G1.YEAR = L2.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND G1.SCHOOL_KIND = L2.SCHOOL_KIND ";
        }
        if ($model->field["WHICH_WAY_DIV"] == "3") {
            $query  .= " LEFT JOIN RECORD_RANK_SDIV_DAT SRANK ON SRANK.YEAR = '".CTRL_YEAR."'";
            $query  .= "                                     AND SRANK.SEMESTER        = '9' ";
            $query  .= "                                     AND SRANK.TESTKINDCD      = '99' ";
            $query  .= "                                     AND SRANK.TESTITEMCD      = '00' ";
            $query  .= "                                     AND SRANK.SCORE_DIV       = '08' ";
            $query  .= "                                     AND SRANK.CLASSCD         = '99' ";
            $query  .= "                                     AND SRANK.SCHOOL_KIND     = '".$model->school_kind."' ";
            $query  .= "                                     AND SRANK.CURRICULUM_CD   = '99' ";
            $query  .= "                                     AND SRANK.SUBCLASSCD      = '999999' ";
            $query  .= "                                     AND SRANK.SCHREGNO        = T1.SCHREGNO ";
        }
        $query .= "WHERE ";
        $query .= "   T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "   T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "   (((T1.GRD_DIV = '1' OR ";
        $query .= "    T1.GRD_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'A003' AND NAMESPARE1 = '1')) AND ";
        $query .= "    FISCALYEAR(T1.GRD_DATE) = '".CTRL_YEAR."'  AND ";
        $query .= "    T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     ) ";
        $query .= "     OR ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_BASE_YEAR_DETAIL_MST E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND E1.BASE_SEQ = '007' ";
        $query .= "             AND E1.BASE_REMARK1 = '1' ";
        $query .= "    )) ";

        //未採番者のみ採番の場合
        if ($mode == 2) {
            $query .= " AND T1.GRD_NO IS NULL ";
        }
        if ($model->school_kind) {
            $query .= "        AND G1.SCHOOL_KIND = '{$model->school_kind}' ";
        }

        $query .= "ORDER BY ";
        if ($which_way_div == "1") {
            $query .= "    T1.GRD_DATE, ";
            $query .= "    T2.GRADE, ";
            $query .= "    T2.HR_CLASS, ";
            $query .= "    T2.ATTENDNO ";
        } elseif ($which_way_div == "3") {
            $query .= "    RED_POINT_FLG, ";
            $query .= "    CASE WHEN SRANK.SCORE IS NOT NULL THEN SRANK.SCORE ELSE -1 END DESC, ";
            $query .= "    T2.GRADE, ";
            $query .= "    T2.HR_CLASS, ";
            $query .= "    T2.ATTENDNO ";
        } elseif ($which_way_div == "4") {
            $aryGrdNoDaichouNoOrder = explode("-", $model->Properties["grdNo_DaichouNoOrder"]);
            $sep = "";
            foreach ($aryGrdNoDaichouNoOrder as $val) {
                if (isset($model->setGrdNoDaichouNoOrder[$val])) {
                    $cols = explode(':', $model->setGrdNoDaichouNoOrder[$val]["column"]);
                    foreach ($cols as $col) {
                        if ($col == "SET_NAME_KANA") {
                            if ($schoolName == 'KINDAI' || $schoolName == 'KINJUNIOR') {
                                $col2 = "SET_NAME_KANA";
                            } else {
                                $col2 = "TRANSLATE_KANA(SET_NAME_KANA)";
                            }
                            $query .= "    ".$sep.$col2." ";
                            $sep = ",";
                        }
                        $query .= "    ".$sep.$col." ";
                        $sep = ",";
                    }
                }
            }
        } else {
            $query .= "    t2.COURSECD, ";
            $query .= "    t2.MAJORCD, ";
            if ($schoolName == 'KINDAI' || $schoolName == 'KINJUNIOR') {
                $query .= "    SET_NAME_KANA, ";
            } else {
                $query .= "    TRANSLATE_KANA(SET_NAME_KANA), ";
            }
            $query .= "    SET_NAME_KANA ";
        }
        $result = $db->query($query);

        //取ってきたschregno達をそれぞれ条件に合わせて(grd_no,grd_termを)UPDATE
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $max_grd_no = $grd_no;

            $query  = "UPDATE schreg_base_mst ";

            if ($which_way == 1) {
                $query .= "   SET grd_no = CHAR($max_grd_no) ";
            } elseif ($which_way == 2) {
                $query .= "   SET grd_no = '".$row["SCHREGNO"]."' ";
            }

            if ($model->Properties["grdTermSetNyuugaku"] != "1") {
                $query .= "   , grd_term = '".$row["GRD_TERM"]."' ";
            }

            $query .= "   , REGISTERCD = '".STAFFCD ."' ";
            $query .= "   , updated = sysdate() ";
            $query .= " WHERE schregno in ('".$row["SCHREGNO"]."') ";

            $db->query($query);

            $grd_no++;
        }
        return;
    }

    //コントロールマスタの学籍処理年度と処理学期を更新
    public function updateControl($model)
    {
        $query  = " UPDATE ";
        $query .= "     CONTROL_MST ";
        $query .= "    SET ";
        $query .= "     CTRL_YEAR = '".$model->next_year."', ";
        $query .= "     CTRL_SEMESTER = (SELECT min(SEMESTER) FROM SEMESTER_MST ";
        $query .= "                       WHERE YEAR = '".$model->next_year."' AND NOT SEMESTER = '9') ";
        $query .= "  WHERE ";
        $query .= "     CTRL_YEAR = '".$model->this_year."' ";
        return $query;
    }

    //更新前チェック
    public function checkClassForm($model)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    CLASS_FORMATION_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->next_year."' ";
        $query .= "    AND T1.SEMESTER = '".$model->next_semester."' ";
        $query .= "    AND T1.GRADE = '".$model->grade."' ";
        $query .= "    AND EXISTS (SELECT ";
        $query .= "                    * ";
        $query .= "                FROM ";
        $query .= "                    SCHREG_BASE_MST W1 ";
        $query .= "                WHERE ";
        $query .= "                    FISCALYEAR(W1.GRD_DATE) = '".CTRL_YEAR."' AND ";
        $query .= "                    T1.SCHREGNO = W1.SCHREGNO ";
        if ($model->secondarySchoolDiv == "2" && $model->Properties["use_prg_schoolkind"] != "1" && $model->Properties["useSchool_KindField"] == "1") {
            $query .= "                AND W1.GRD_DIV != '5' ";
        }
        $query .= "               ) ";

        return $query;
    }

    //学籍基礎データの履歴を作る対称となる生徒を取得
    public function getSchregNoForHistDat($model)
    {
        if ($model->grade == '99') {
            $query  = " SELECT ";
            $query .= "     SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_BASE_MST W1 ";
            $query .= " WHERE ";
            $query .= "     fiscalyear(W1.GRD_DATE) = '".CTRL_YEAR."' ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "      class_formation_dat T1 ";
            $query .= " LEFT OUTER JOIN ";
            $query .= "     schreg_regd_dat T2 ON T1.schregno  = T2.schregno ";
            $query .= "                       AND T2.year      = '{$model->this_year}' ";
            $query .= "                       AND T2.semester  = '{$model->this_semester}' ";
            $query .= " WHERE ";
            $query .= "     T1.year     = '{$model->next_year}' AND ";
            $query .= "     T1.semester = '{$model->next_semester}' AND ";
            $query .= "     T1.grade    = '{$model->grade}' ";
        }

        return $query;
    }

    //更新するときに履歴に入れるデータを取ってくる
    public function getStudentDataBefore($schregno, $model)
    {
        $query  = " SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T1.INOUTCD, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BLOODTYPE, ";
        $query .= "     T1.BLOOD_RH, ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINISH_DATE, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.ENT_DIV, ";
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     T1.ENT_ADDR2, ";
        }
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     T1.GRD_ADDR2, ";
        }
        $query .= "     T1.GRD_TERM, ";
        $query .= "     T1.PRISCHOOLCD, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "      T1.SCHREGNO = '{$schregno}' ";
        $query .= "  AND T2.YEAR     = '{$model->this_year}'     ";
        $query .= "  AND T2.SEMESTER = '{$model->this_semester}' ";

        return $query;
    }

    //更新の際内容が変更されていれば、
    //履歴テーブルを更新するので、日付を取得する。
    public function getEdate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     E_APPDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     INT(SEQ) DESC";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //シーケンス番号の取得
    public function getSeq()
    {
        $query  = " SELECT ";
        $query .= "     MAX(INT(SEQ)) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT ";

        return $query;
    }

    //履歴テーブルの更新
    public function insertHist($histArray, $seq, $edate, $model)
    {
        //$edate ⇒ 前回の履歴の終了日
        //$start_day ⇒ 今回入力された変更開始日付
        //$seq ⇒ このシーケンス番号でインサートする
        if ($edate) { //$edateには前回の終了日が入っている
            $edateArray = explode('-', $edate);
            $edateYear  = $edateArray[0];
            $edateMonth = $edateArray[1];
            $edateDay   = $edateArray[2];

            $time = mktime(0, 0, 0, $edateMonth, (int)$edateDay + 1, $edateYear); //一日後を取得
            $sdate = date("Y-m-d", $time); //それを今回の(履歴の)開始日とする
        } else {
            $sdate = CTRL_YEAR . '-04-01';
        }
        $start_dayArray = explode('/', $model->field["E_APPDATE"]);
        $start_dayYear  = $start_dayArray[0];
        $start_dayMonth = $start_dayArray[1];
        $start_dayDay   = $start_dayArray[2];
        $time = mktime(0, 0, 0, $start_dayMonth, $start_dayDay - 1, $start_dayYear); //一日前を取得
        $konkaiEdate = date("Y-m-d", $time); //今日から見た前日(一日前)

        $data["SEQ"][TEXT]         = $seq;
        $data["S_APPDATE"][TEXT]   = $sdate;
        $data["E_APPDATE"][TEXT]   = $konkaiEdate;
        $data["YEAR"][TEXT]        = $histArray["YEAR"];
        $data["SEMESTER"][TEXT]    = $histArray["SEMESTER"];
        $data["GRADE"][TEXT]       = $histArray["GRADE"];
        $data["HR_CLASS"][TEXT]    = $histArray["HR_CLASS"];
        $data["ATTENDNO"][TEXT]    = $histArray["ATTENDNO"];
        $data["ANNUAL"][TEXT]      = $histArray["ANNUAL"];
        $data["COURSECD"][TEXT]    = $histArray["COURSECD"];
        $data["MAJORCD"][TEXT]     = $histArray["MAJORCD"];
        $data["COURSECODE"][TEXT]  = $histArray["COURSECODE"];
        $data["SCHREGNO"][TEXT]    = $histArray["SCHREGNO"];
        $data["INOUTCD"][TEXT]     = $histArray["INOUTCD"];
        $data["BIRTHDAY"][TEXT]    = $histArray["BIRTHDAY"];
        $data["NAME"][TEXT]        = $histArray["NAME"];
        $data["NAME_SHOW"][TEXT]   = $histArray["NAME_SHOW"];
        $data["NAME_KANA"][TEXT]   = $histArray["NAME_KANA"];
        $data["NAME_ENG"][TEXT]    = $histArray["NAME_ENG"];
        $data["SEX"][TEXT]         = $histArray["SEX"];
        $data["BLOODTYPE"][TEXT]   = $histArray["BLOODTYPE"];
        $data["BLOOD_RH"][TEXT]    = $histArray["BLOOD_RH"];
        $data["FINSCHOOLCD"][TEXT] = $histArray["FINSCHOOLCD"];
        $data["FINISH_DATE"][TEXT] = $histArray["FINISH_DATE"];
        $data["ENT_DATE"][TEXT]    = $histArray["ENT_DATE"];
        $data["ENT_DIV"][TEXT]     = $histArray["ENT_DIV"];
        $data["ENT_REASON"][TEXT]  = $histArray["ENT_REASON"];
        $data["ENT_SCHOOL"][TEXT]  = $histArray["ENT_SCHOOL"];
        $data["ENT_ADDR"][TEXT]    = $histArray["ENT_ADDR"];
        if ($model->Properties["useAddrField2"] == "1") {
            $data["ENT_ADDR2"][TEXT]   = $histArray["ENT_ADDR2"];
        }
        if ($model->grade == '99') {
            $data["GRD_DATE"][TEXT]    = str_replace("/", "-", $model->syoribi);
        } else {
            $data["GRD_DATE"][TEXT]    = $histArray["GRD_DATE"];
        }
        $data["GRD_DIV"][TEXT]     = $histArray["GRD_DIV"];
        $data["GRD_REASON"][TEXT]  = $histArray["GRD_REASON"];
        $data["GRD_SCHOOL"][TEXT]  = $histArray["GRD_SCHOOL"];
        $data["GRD_ADDR"][TEXT]    = $histArray["GRD_ADDR"];
        if ($model->Properties["useAddrField2"] == "1") {
            $data["GRD_ADDR2"][TEXT]   = $histArray["GRD_ADDR2"];
        }
        $data["GRD_TERM"][TEXT]    = $histArray["GRD_TERM"];
        $data["PRISCHOOLCD"][TEXT] = $histArray["PRISCHOOLCD"];
        $data["REMARK1"][TEXT]     = $histArray["REMARK1"];
        $data["REMARK2"][TEXT]     = $histArray["REMARK2"];
        $data["REMARK3"][TEXT]     = $histArray["REMARK3"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_BASE_HIST_DAT");
        return $query;
    }

    //最終履歴の終了日取得
    public function getEappDate($schregno, $model)
    {
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     L1.E_APPDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " LEFT JOIN SCHREG_BASE_HIST_DAT L1 ON  T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "                                   AND L1.YEAR     = '{$model->next_year}' ";
        $query .= "                                   AND L1.SEMESTER = '{$model->next_semester}' ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY INT(SEQ) DESC ";

        return $query;
        ;
    }

    //入学日を取得
    public function getEntDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //学籍基礎データの履歴を作る
    public function insertHistDat($model, $db)
    {
        $query = knja050Query::getSchregNoForHistDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $histArray = array();
            $query = knja050Query::getStudentDataBefore($row["SCHREGNO"], $model);
            $histArray = $db->getRow($query, DB_FETCHMODE_ASSOC); //今までのデータ

            if (get_count($histArray) == 0) { //SCHREG_BASE_MST か SCHREG_REGD_DATに存在しないデータはインサートできない
                continue;
            }

            $query = knja050Query::getEdate($row["SCHREGNO"]); //前回の最終日をとってくる
            $edate = $db->getOne($query);

            if ($edate) { //履歴がない人は入学日をその履歴の開始日とする
                $query = knja050Query::getEntDate($row["SCHREGNO"]);
                $edate = $db->getOne($query);
            }

            $query = knja050Query::getSeq(); //シーケンス番号を取得
            $seq   = $db->getOne($query);
            $seq   = $seq + 1;

            $query = knja050Query::insertHist($histArray, $seq, $edate, $model); //履歴に保存
            $db->query($query);
        }
    }

    //更新処理
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //処理学年が卒業生以外の場合
        if ($model->grade != "99") {
            //更新する前に履歴を作っておく
//            knja050Query::insertHistDat($model, $db);
            if ($model->Freshman_dat != '0') { //新入生がいなければ実行しなくても良い処理
                $db->query(knja050Query::delSchregBm($model)); //学籍基礎マスタの削除
                $db->query(knja050Query::addSchregBm($model)); //学籍基礎マスタへ新入生のデータ追加

                $db->query(knja050Query::delSchregBaseDetailMst($model)); //SCHREG_BASE_DETAIL_MST(BASE_SEQ=003)の削除
                $db->query(knja050Query::addSchregBaseDetailMst($model)); //SCHREG_BASE_DETAIL_MST(BASE_SEQ=003)へ新入生のデータ追加

                $db->query(knja050Query::delSchregAddress($model)); //学籍住所データの削除
                $db->query(knja050Query::addSchregAddress($model)); //学籍住所データへ新入生のデータ追加

                $db->query(knja050Query::delFreshman($model->next_year)); //次年度以外の新入生データ削除
            }

            $db->query(knja050Query::delSchregRegd($model)); //クラス在籍データの削除
            $db->query(knja050Query::addSchregRegd($model)); //クラス在籍データへの追加

            knja050Query::setEntGrdDat($db, $model);
        } else {
            //更新する前に履歴を作っておく
//            knja050Query::insertHistDat($model, $db);

            //卒業生データ移行(17テーブル移行)
            $whereBase = knja050Query::getWhereBase();

            $query  = "UPDATE schreg_base_mst T1 ";
            $query .= "   SET grd_no   = NULL, ";
            if ($model->Properties["grdTermSetNyuugaku"] != "1") {
                $query .= "       GRD_TERM   = NULL, ";
            }
            $query .= "       REGISTERCD = '".STAFFCD ."', ";
            $query .= "       updated = sysdate() ";
            $query .= " WHERE schregno IN( ";
            $query .= " SELECT DISTINCT schregno FROM schreg_regd_dat where year='".$model->next_year."' )";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND EXISTS (SELECT ";
                $query .= "                 'X' ";
                $query .= "             FROM ";
                $query .= "                 SCHREG_REGD_DAT S1, ";
                $query .= "                 SCHREG_REGD_GDAT S2 ";
                $query .= "             WHERE ";
                $query .= "                 T1.SCHREGNO     = S1.SCHREGNO AND ";
                $query .= "                 S1.YEAR         = S2.YEAR AND ";
                $query .= "                 S1.YEAR         = '".CTRL_YEAR."' AND ";
                $query .= "                 S1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
                $query .= "                 S1.GRADE        = S2.GRADE ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= "             AND S2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1") {
                    $query .= "             AND S2.SCHOOL_KIND  = '".SCHOOL_KIND."' ";
                }
                $query .= "     ) ";
            }

            $db->query($query);

            //学籍学歴更新
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     G1.SCHOOL_KIND AS SCHOOL_KIND1, ";
            $query .= "     T1.GRD_DATE, ";
            $query .= "     T1.GRD_DIV, ";
            $query .= "     T1.GRD_REASON, ";
            $query .= "     T1.GRD_SCHOOL, ";
            $query .= "     T1.GRD_ADDR, ";
            if ($model->Properties["useAddrField2"] == "1") {
                $query .= "     T1.GRD_ADDR2, ";
            }
            $query .= "     T1.GRD_NO, ";
            $query .= "     T1.GRD_TERM ";
            $query .= " FROM ";
            $query .= "     SCHREG_BASE_MST T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT R1 ON T1.SCHREGNO = R1.SCHREGNO ";
            $query .= "           AND R1.YEAR = '".CTRL_YEAR."' ";
            $query .= "           AND R1.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON R1.YEAR = G1.YEAR ";
                $query .= "          AND R1.GRADE = G1.GRADE ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= "             AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1") {
                    $query .= "             AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
                }
            } else {
                $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ON R1.YEAR = G1.YEAR ";
                $query .= "          AND R1.GRADE = G1.GRADE ";
            }
            $query .= " WHERE ";
            $query .= "     FISCALYEAR(GRD_DATE) = '".CTRL_YEAR."' ";

            $resultHist = $db->query($query);
            while ($rowHist = $resultHist->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schregno[] = $rowHist;
            }
            $resultHist->free();

            if (is_array($schregno)) {
                foreach ($schregno as $key => $schRow) {
                    if (is_array($schRow)) {
                        if ($schRow["SCHOOL_KIND1"]) {
                            $insQuery = knja050Query::insUpdEntGrdDat($model, $schRow, $schRow["SCHOOL_KIND1"], "UPD");
                            $db->query($insQuery);
                        }
                    }
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //対象Where区を取得
    public function getWhereBase()
    {
        $query  = " EXISTS (SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_BASE_MST W1 ";
        $query .= "         WHERE ";
        $query .= "             W1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "             fiscalyear(W1.GRD_DATE) = '".CTRL_YEAR."' ";
        $query .= " ) ";

        return $query;
    }

    //MAX学期名を抽出
    public function getMaxSemester($model)
    {
        $db    = Query::dbCheckOut();
        $query  = "SELECT SEMESTERNAME FROM SEMESTER_MST ";
        $query .= "WHERE YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER = '".$model->max_semester."'";
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);
        return $row[0];
    }
}
