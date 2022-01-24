<?php

require_once('for_php7.php');
class knjh400_TyousasyoSyokenQuery extends Query
{
    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE011D' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //京都判定
    public function getNameMst($cd)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$cd."' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }

    //学年判定(GRADE_CD取得)
    public function getGradeCd($model, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     L1.GRADE_CD, ";
        $query .= "     L1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                  AND L1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        if (!$flg) {
            $query .= "     T1.YEAR = '{$model->exp_year}' ";
        } else {
            $query .= "     T1.YEAR = '{$model->annual["YEAR"]}' ";
            $query .= " AND T1.ANNUAL = '{$model->annual["ANNUAL"]}' ";
        }
        $query .= " AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T1.SEMESTER = '{$model->exp_semester}' ";

        return $query;
    }

    //追加可能年度取得
    public function selectYearQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= " WHERE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     YEAR < '".$model->entYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //追加可能学年取得
    public function selectGradeQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE AS VALUE, ";
        $query .= "     S1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT S1 ";
        $query .= "                  ON T2.YEAR         = S1.YEAR ";
        $query .= "                 AND T2.GRADE        = S1.GRADE ";
        $query .= "                 AND S1.SCHOOL_KIND  = 'H' ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '{$model->addYear}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //臘八摂心皆勤
    public function getRouhatsuKaikin($model)
    {
        $query  = " WITH SCHREG AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T2.SCHOOL_KIND ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "       ON T1.YEAR = T2.YEAR ";
        $query .= "       AND T1.GRADE = T2.GRADE ";
        $query .= "   WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ) ";
        $query .= " , SCHREGYEAR AS ( ";
        $query .= "   SELECT YEAR ";
        $query .= "   FROM SCHREG ";
        $query .= "   WHERE SCHOOL_KIND IN ( ";
        $query .= "       SELECT SCHOOL_KIND ";
        $query .= "       FROM SCHREG ";
        $query .= "       WHERE YEAR = '{$model->annual["YEAR"]}' ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   COUNT(*) REC_CNT ";
        $query .= "   , SUM(CASE WHEN KAIKIN_FLG = '1' THEN 1 ELSE 0 END) KAIKIN_CNT ";
        $query .= " FROM SCHREGYEAR ";
        $query .= " LEFT JOIN ROU_HATSU_SESSIN_KAI_DAT ROUHATSU ";
        $query .= "    ON SCHREGYEAR.YEAR = ROUHATSU.YEAR ";
        $query .= "   AND ROUHATSU.SCHREGNO = '".$model->schregno."' ";
        $query .= " WHERE SCHREGYEAR.YEAR <= '{$model->annual["YEAR"]}' ";

        return $query;
    }

    //SCHREG_BASE_MST 取得
    public function getSchregBase($model, $name, $fmt = "")
    {
        $query  = " SELECT";
        if ($fmt == "") {
            $query .= " ".$name." ";
        } elseif ($fmt == "year") {
            $query .= " YEAR(".$name.") ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //学校名取得の取得(テストコンボで使う)
    public function getSchoolName($JH = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        if ($JH) {
            $query .= "     NAMECD1 = 'Z010' AND ";
            $query .= "     NAMECD2 = '00' AND ";
            $query .= "     NAMESPARE2 IS NOT NULL ";
        } else {
            $query .= "     NAMECD1 = 'Z011' AND ";
            $query .= "     NAMECD2 = '00' ";
        }

        return $query;
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    public function getSchoolDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //RECORD_TOTALSTUDYTIME_DAT から読込む
    public function getRecordTotalstudytimeDat($model, $getYear = "")
    {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     TOTALSTUDYACT ";
        $query .= " FROM ";
        $query .= "     RECORD_TOTALSTUDYTIME_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        if ($getYear) {
            $query .= " AND YEAR =  '{$getYear}' ";
        }
        if ($model->Properties["tutisyoSougoushokengakki"] == '1') {
            $query .= " AND SEMESTER = '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";

        return $query;
    }

    public function getPatternSeq($model)
    {
        if ($model->mode == "grd") {     //卒業
            $tableName1 = "GRD_HEXAM_ENTREMARK_SEQ_HDAT";
            $tableName2 = "GRD_HEXAM_ENTREMARK_SEQ_DAT";
        } else {
            $tableName1 = "HEXAM_ENTREMARK_SEQ_HDAT";
            $tableName2 = "HEXAM_ENTREMARK_SEQ_DAT";
        }

        $query  = " WITH PATTERN_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     PATTERN_SEQ ";
        $query .= " FROM ";
        $query .= "     {$tableName1} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     PATTERN_SEQ ";
        $query .= " FROM ";
        $query .= "     {$tableName2} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY PATTERN_SEQ ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     PATTERN_SEQ ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_TRAINREF_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY PATTERN_SEQ ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     'パターン' || PATTERN_SEQ AS LABEL, ";
        $query .= "     PATTERN_SEQ AS VALUE ";
        $query .= " FROM ";
        $query .= "     PATTERN_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function selectQuery($model)
    {
        $query  =" SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_HDAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    public function selectQueryPattern($model)
    {
        $query  =" SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_SEQ_HDAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_SEQ_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND PATTERN_SEQ = '{$model->selectPattern}' ";

        return $query;
    }

    public function getYouroku($model, $getYear = "")
    {
        $query  = " SELECT ";
        $query .= "     T3.GRADE_NAME1, ";
        $query .= "     T1.TOTALSTUDYACT, ";
        $query .= "     T1.TOTALSTUDYVAL ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT T1 ";
        $query .= "     LEFT JOIN (SELECT DISTINCT SCHREGNO, YEAR, GRADE ";
        $query .= "              FROM SCHREG_REGD_DAT ";
        $query .= "              WHERE SCHREGNO = '{$model->schregno}') T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND VALUE(T3.SCHOOL_KIND, '') <> 'J' ";
        if ($getYear) {
            $query .= " AND T1.YEAR = '{$getYear}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";

        return $query;
    }

    //フォーム2の値を取得
    public function selectQueryForm2($model)
    {
        $query  = " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   ANNUAL, ";
        $query .= "   ATTENDREC_REMARK, ";
        $query .= "   SPECIALACTREC, ";
        $query .= "   TOTALSTUDYACT, ";
        $query .= "   TOTALSTUDYVAL, ";
        $query .= "   TRAIN_REF, ";
        $query .= "   TRAIN_REF1, ";
        $query .= "   TRAIN_REF2, ";
        $query .= "   TRAIN_REF3 ";
        if ($model->Properties["useTotalstudySlashFlg"] == 1) {
            $query .= "     , TOTALSTUDYACT_SLASH_FLG ";
            $query .= "     , TOTALSTUDYVAL_SLASH_FLG ";
        }
        if ($model->Properties["useAttendrecRemarkSlashFlg"] == 1) {
            $query .= "     , ATTENDREC_REMARK_SLASH_FLG ";
        }
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_DAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_DAT ";
        }
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$model->annual["YEAR"]}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //フォーム2の値を取得 (パターン用)
    public function selectQueryForm2Pattern($model)
    {
        $query  = " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   ANNUAL, ";
        $query .= "   ATTENDREC_REMARK, ";
        $query .= "   SPECIALACTREC, ";
        $query .= "   TOTALSTUDYACT, ";
        $query .= "   TOTALSTUDYVAL, ";
        $query .= "   TRAIN_REF, ";
        $query .= "   TRAIN_REF1, ";
        $query .= "   TRAIN_REF2, ";
        $query .= "   TRAIN_REF3 ";
        if ($model->Properties["useTotalstudySlashFlg"] == 1) {
            $query .= "     , TOTALSTUDYACT_SLASH_FLG ";
            $query .= "     , TOTALSTUDYVAL_SLASH_FLG ";
        }
        if ($model->Properties["useAttendrecRemarkSlashFlg"] == 1) {
            $query .= "     , ATTENDREC_REMARK_SLASH_FLG ";
        }
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_SEQ_DAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_SEQ_DAT ";
        }
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$model->annual["YEAR"]}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND PATTERN_SEQ = '{$model->selectPattern}' ";

        return $query;
    }

    // 諸事項取込み事前チェック
    public function getHrShojikouTorikomiDataCheck($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.HR_NAME ";
        $query .= "  ,T3.YEAR ";
        $query .= "  ,COUNT(DISTINCT T3.SCHREGNO) AS COUNT "; // 1項目でもデータのある生徒の数
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_REGD_HDAT T1 ";
            $query .= "   INNER JOIN GRD_REGD_DAT T2 ON ";
        } else {
            $query .= "   SCHREG_REGD_HDAT T1 ";
            $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ON ";
        }
        $query .= "           T2.YEAR = T1.YEAR ";
        $query .= "           AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "           AND T2.GRADE = T1.GRADE ";
        $query .= "           AND T2.HR_CLASS = T1.HR_CLASS ";
        if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $query .= "       LEFT JOIN HEXAM_ENTREMARK_TRAINREF_SEQ_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO ";
            $query .= "           AND T3.PATTERN_SEQ = '{$model->selectPattern}' ";
            $query .= "           AND T3.TRAIN_SEQ IN ('101', '102', '103', '104', '105', '106') ";
            $query .= "           AND T3.REMARK IS NOT NULL ";
        } else {
            $query .= "       LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO ";
            //$query .= "           AND T3.YEAR = T2.YEAR ";
            $query .= "           AND T3.TRAIN_SEQ IN ('101', '102', '103', '104', '105', '106') ";
            $query .= "           AND T3.REMARK IS NOT NULL ";
        }
        $query .= " WHERE ";
        $query .= "       T1.YEAR     = '{$model->exp_year}' ";
        $query .= "   AND T1.SEMESTER = '{$model->exp_semester}' ";
        $query .= "   AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->gradeHrClass}' ";
        $query .= " GROUP BY ";
        $query .= "       T1.HR_NAME ";
        $query .= "      ,T3.YEAR ";
        return $query;
    }

    //成績参照画面
    public function selectQueryForm3($model, $JH = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     CASE WHEN T1.CLASSABBV IS NULL OR T1.CLASSABBV = '' ";
        $query .= "         THEN T3.CLASSABBV ";
        $query .= "         ELSE T1.CLASSABBV ";
        $query .= "     END AS CLASSNAME, ";
        $query .= "     CASE WHEN T1.SUBCLASSNAME IS NULL OR T1.SUBCLASSNAME = '' ";
        $query .= "         THEN T4.SUBCLASSNAME ";
        $query .= "         ELSE T1.SUBCLASSNAME ";
        $query .= "     END AS SUBCLASSNAME, ";
        $query .= "     T1.VALUATION, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.ADD_CREDIT, ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "     GRD_STUDYREC_DAT T1 ";
            $query .= " LEFT JOIN GRD_STUDYRECREMARK_DAT T2 ON  ";
        } else {
            $query .= "     SCHREG_STUDYREC_DAT T1 ";
            $query .= " LEFT JOIN STUDYRECREMARK_DAT T2 ON  ";
        }
        $query .= "                   T1.YEAR       = T2.YEAR AND  ";
        $query .= "                   T1.SCHREGNO   = T2.SCHREGNO AND  ";
        $query .= "                   T1.CLASSCD    = T2.CLASSCD AND  ";
        $query .= "                   T1.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T2.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD  = T2.CURRICULUM_CD";
        }
        $query .= " LEFT JOIN CLASS_MST    T3 ON  ";
        $query .= "                   T1.CLASSCD    = T3.CLASSCD  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND ";
        }
        $query .= " LEFT JOIN SUBCLASS_MST T4 ON  ";
        $query .= "                   T1.SUBCLASSCD = T4.SUBCLASSCD  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD        = T4.CLASSCD ";
            $query .= "               AND T1.SCHOOL_KIND    = T4.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD  = T4.CURRICULUM_CD";
        }
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' AND ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0") {
            $query .= "     T1.YEAR   = '{$model->annual["YEAR"]}' AND ";
            $query .= "     T1.ANNUAL = '{$model->annual["ANNUAL"]}' AND";
        }
        if ($JH) {
            $query .= "   T1.ANNUAL >= '04' AND ";
        }
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";

        return $query;
    }

    //学年（年次）コンボボックス用
    public function selectQueryAnnual($model, $JH = "")
    {
        $tableName = ($model->mode == "grd") ? "GRD_STUDYREC_DAT" : "SCHREG_STUDYREC_DAT";

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     {$tableName} T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.GRADE = T1.ANNUAL ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND VALUE(T2.SCHOOL_KIND, '') NOT IN ('K', 'P', 'J') ";
        if ($JH) {
            $query .= "     AND T1.ANNUAL >= '04' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";

        return $query;
    }

    //「出欠の記録～」の学年（年次）コンボボックス
    public function selectQueryAnnualKnjh400TyousasyoSyokenForm2($model)
    {
        $query  = " WITH SUB_MAIN AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = 'H' ";
        }
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT S1 ";
        $query .= "                  ON T2.YEAR         = S1.YEAR ";
        $query .= "                 AND T2.GRADE        = S1.GRADE ";
        $query .= "                 AND S1.SCHOOL_KIND  = 'H' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     VALUE(S1.GRADE_CD, T1.ANNUAL) AS ANNUAL ";
        $query .= " FROM ";
        $query .= "     SUB_MAIN T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT S1 ";
        $query .= "                  ON T1.YEAR         = S1.YEAR ";
        $query .= "                 AND T1.ANNUAL       = S1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     VALUE(S1.GRADE_CD, T1.ANNUAL) ";

        return $query;
    }

    //「指導要録参照」の学年（年次）コンボボックス
    public function selectQueryAnnualHtrainremarkDAT($model)
    {
        if ($model->mode == "grd") {
            $tableName1 = "GRD_REGD_DAT";
            $tableName2 = "GRD_HTRAINREMARK_DAT";
        } else {
            $tableName1 = "SCHREG_REGD_DAT";
            $tableName2 = "HTRAINREMARK_DAT";
        }

        $query  = " WITH SCHREG_YEAR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T2.GRADE_CD ";
        $query .= "     FROM ";
        $query .= "         ".$tableName1." T1, ";
        $query .= "         SCHREG_REGD_GDAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.GRADE    = T2.GRADE AND ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "         T2.SCHOOL_KIND IN (SELECT DISTINCT ";
        $query .= "                                 S2.SCHOOL_KIND ";
        $query .= "                             FROM ";
        $query .= "                                 ".$tableName1." S1, ";
        $query .= "                                 SCHREG_REGD_GDAT S2 ";
        $query .= "                             WHERE ";
        $query .= "                                 S1.YEAR     = S2.YEAR AND ";
        if ($model->mode == "grd") {
            $query .= "                                 S1.YEAR     = (SELECT ";
            $query .= "                                                     MAX(SS1.YEAR) ";
            $query .= "                                                 FROM ";
            $query .= "                                                     ".$tableName1." SS1 ";
            $query .= "                                                 WHERE ";
            $query .= "                                                     SS1.SCHREGNO = S1.SCHREGNO ) AND ";
        } else {
            $query .= "                                 S1.YEAR     = '".CTRL_YEAR."' AND ";
        }
        $query .= "                                 S1.GRADE    = S2.GRADE AND ";
        $query .= "                                 S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             ) ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     VALUE(T2.GRADE_CD, T1.ANNUAL) AS ANNUAL ";
        $query .= " FROM ";
        $query .= "     ".$tableName2." T1, ";
        $query .= "     SCHREG_YEAR T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";

        return $query;
    }

    //指導要録参照画面
    public function selectQueryHtrainremarkDat2($model)
    {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_DAT ";
        } else {
            $query .= "   HTRAINREMARK_DAT ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR      = '{$model->annual["YEAR"]}' ";
        $query .= "     AND SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //備考
    public function getHexamEntremarkRemarkHdat($model)
    {
        $query  = " SELECT ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_REMARK_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //学習指導要録より再読込
    public function selectQueryHtrainremarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL, ";
        $query .= "     SPECIALACTREMARK AS SPECIALACTREC, ";
        $query .= "     TOTALREMARK AS TRAIN_REF, ";
        $query .= "     ATTENDREC_REMARK, ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_DAT ";
        } else {
            $query .= "   HTRAINREMARK_DAT ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR      = '{$model->annual["YEAR"]}' ";
        $query .= "     AND SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //指導要録データ
    public function yorokuTrainref123($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     TRAIN_REF1 ";
        $query .= "   , TRAIN_REF2 ";
        $query .= "   , TRAIN_REF3 ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     YEAR     = '{$year}' ";

        return $query;
    }

    //指導要録データ
    public function yorokuSogoshoken($model)
    {
        $query  = " SELECT ";
        $query .= "     TOTALREMARK ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     YEAR     = '{$model->annual["YEAR"]}' ";

        return $query;
    }

    //指導要録参照画面
    public function selectQueryHtrainremarkHdat($model)
    {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_HDAT ";
        } else {
            $query .= "   HTRAINREMARK_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //セット日々出欠備考
    public function getSemesRemark($model, $db, $year)
    {
        $query  = "  SELECT ";
        $query .= "      REMARK1, ";
        $query .= "      CASE WHEN MONTH = '01' THEN '13' WHEN MONTH = '02' THEN '14' WHEN MONTH = '03' THEN '15' ELSE MONTH END AS SORT_MONTH ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      COPYCD = '0' ";
        $query .= "  AND YEAR = '".$year."' ";
        $query .= "  AND SCHREGNO = '{$model->schregno}' ";
        $query .= "  ORDER BY ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      SORT_MONTH ";

        $result = $db->query($query);
        $set_remark = "";
        $count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($count == 0) {
                $set_remark .= $row["REMARK1"];
            } else {
                if ($row["REMARK1"] != "") {
                    $set_remark .= "／".$row["REMARK1"];
                }
            }
            $count++;
        }

        return $set_remark;
    }

    //出欠備考 -- 通知票取込
    public function getHreportremarkDetailDat($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->annual["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV         = '06' AND ";
        $query .= "     CODE IN ('01','02','03','04','05') AND ";
        $query .= "     REMARK2     = '1' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        $result = $db->query($query);
        $set_remark = "";
        $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (strlen($row["REMARK1"]) > 0) {
                $set_remark .= $sep.$row["REMARK1"];
                $sep = "、";
            }
        }

        return $set_remark;
    }

    public function getHexamEntremarkTrainrefDat($model, $year, $trainSeq)
    {
        if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $tableName = "HEXAM_ENTREMARK_TRAINREF_SEQ_DAT";
        } else {
            $tableName = "HEXAM_ENTREMARK_TRAINREF_DAT";
        }

        $query  =" SELECT ";
        $query .= "   YEAR, TRAIN_SEQ, REMARK ";
        $query .= " FROM ";
        $query .= "   {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND YEAR = '{$year}' ";
        if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $query .= "     AND PATTERN_SEQ = '{$model->selectPattern}' ";
        }
        $query .= "     AND TRAIN_SEQ = '{$trainSeq}' ";

        return $query;
    }

    public function getHexamEntremarkLearningDat($model)
    {
        $query  =" SELECT ";
        $query .= "   YEAR, REMARK ";
        $query .= " FROM ";
        $query .= "   HEXAM_ENTREMARK_LEARNING_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        return $query;
    }

    //校種取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$model->annual["YEAR"]."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.SEMESTER IN (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = S1.YEAR AND ";
        $query .= "                         T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                     ) ";

        return $query;
    }

    public function getGradeComb($model)
    {
        $query  = " WITH GDAT AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T2.SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_REGD_GDAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.GRADE    = T2.GRADE AND ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "         T1.SEMESTER IN (SELECT ";
        $query .= "                             MAX(SEMESTER) ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_REGD_DAT S1 ";
        $query .= "                         WHERE ";
        $query .= "                             T1.YEAR     = S1.YEAR AND ";
        $query .= "                             T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                         ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     INNER JOIN GDAT ";
        $query .= "          ON GDAT.YEAR = T1.YEAR ";
        $query .= "         AND GDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    public function getSchList($model)
    {
        if ($model->mode == "grd") {     //卒業
            $tableName1 = "GRD_REGD_DAT";
            $tableName2 = "GRD_REGD_HDAT";
        } else {
            $tableName1 = "SCHREG_REGD_DAT";
            $tableName2 = "SCHREG_REGD_HDAT";
        }

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T1.SCHREGNO || '-' || T1.GRADE || T1.HR_CLASS || T1.ATTENDNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     {$tableName1} T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN {$tableName2} T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.GRADE    = T1.GRADE ";
        $query .= "         AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->exp_year."' ";
        $query .= "     AND T1.SEMESTER = '".$model->exp_semester."' ";
        $query .= "     AND T3.GRADE || '-' || T3.HR_CLASS = '".$model->gradeHrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //年度、学籍番号の取得
    public function getYearSchregnoQuery($model, $selectSchregnos)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   T2.SCHREGNO ";
        $query .= "  ,T1.GRADE ";
        $query .= "  ,T2.ANNUAL ";
        if ($model->selectGrade == "ALL") {
            $query .= "  ,T4.YEAR ";
        } else {
            $query .= "  ,MAX(T4.YEAR) AS YEAR ";
        }
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_REGD_HDAT T1 ";
            $query .= "   INNER JOIN GRD_REGD_DAT T2 ON ";
            $query .= "           T2.YEAR = T1.YEAR ";
            $query .= "           AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "           AND T2.GRADE = T1.GRADE ";
            $query .= "           AND T2.HR_CLASS = T1.HR_CLASS ";
            $query .= "   LEFT JOIN GRD_REGD_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO ";
        } else {
            $query .= "   SCHREG_REGD_HDAT T1 ";
            $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ON ";
            $query .= "           T2.YEAR = T1.YEAR ";
            $query .= "           AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "           AND T2.GRADE = T1.GRADE ";
            $query .= "           AND T2.HR_CLASS = T1.HR_CLASS ";
            $query .= "   LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ";
        $query .= "           AND GDAT.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR     = '{$model->exp_year}' ";
        $query .= "   AND T1.SEMESTER = '{$model->exp_semester}' ";
        $query .= "   AND T2.SCHREGNO  IN (".implode(", ", $selectSchregnos).") ";
        if ($model->selectGrade != "ALL") {
            $query .= "   AND T1.GRADE = '{$model->selectGrade}' ";
            $query .= " GROUP BY ";
            $query .= "     T2.SCHREGNO,T1.GRADE,T2.ANNUAL ";
        }

        return $query;
    }

    public function getEntremarkSeqDatQuery($model, $schregnoYearAnnualsPattern)
    {
        if ($model->mode == "grd") {
            $tableEntremarkSeq = "GRD_HEXAM_ENTREMARK_SEQ_DAT";
        } else {
            $tableEntremarkSeq = "HEXAM_ENTREMARK_SEQ_DAT";
        }

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= "     FROM ";
        $query .= "     ".$tableEntremarkSeq ." ";
        $query .= " WHERE ";
        $query .= "     (SCHREGNO, YEAR, ANNUAL, PATTERN_SEQ) IN (VALUES ".implode(", ", $schregnoYearAnnualsPattern).") ";

        return $query;
    }

    public function getHtrainRemarkDatQuery($model, $schregnoYearAnnuals)
    {
        if ($model->mode == "grd") {
            $tableName    = "GRD_HTRAINREMARK_DAT";
        } else {
            $tableName    = "HTRAINREMARK_DAT";
        }

        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     ATTENDREC_REMARK, ";
        $query .= "     SPECIALACTREMARK AS SPECIALACTREC, ";
        $query .= "     TOTALREMARK AS TRAIN_REF, ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     (SCHREGNO, YEAR, ANNUAL) IN (VALUES ".implode(", ", $schregnoYearAnnuals).") ";

        return $query;
    }
}
