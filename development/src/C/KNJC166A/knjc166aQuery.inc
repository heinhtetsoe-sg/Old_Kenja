<?php

require_once('for_php7.php');
class knjc166aQuery extends Query
{
    public function getSemesBaseDate($model)
    {
        $baseDate = str_replace("/", "-", $model->base_date);

        $query  = " SELECT ";
        $query .= "   SEMESTER ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER <> '9' ";
        $query .= "   AND '{$baseDate}' BETWEEN SDATE AND EDATE ";

        return $query;
    }

    public function getGrade()
    {
        $query  = " SELECT  ";
        $query .= "     SCHOOL_KIND AS RET_VAL, ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE || ':' || GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getHist()
    {
        $query  = " SELECT  ";
        $query .= "     EXEC_D.EXEC_TIME, ";
        $query .= "     GDAT.GRADE_NAME1, ";
        $query .= "     EXEC_D.BASE_DATE, ";
        $query .= "     CASE WHEN EXEC_D.HR_CLASS = '000' THEN '全クラス' ELSE HDAT.HR_NAME END AS HR_NAME ";
        $query .= " FROM ";
        $query .= "     KAIKIN_EXEC_DAT EXEC_D ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON EXEC_D.YEAR = GDAT.YEAR ";
        $query .= "          AND EXEC_D.GRADE = GDAT.GRADE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = EXEC_D.YEAR ";
        $query .= "          AND HDAT.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND HDAT.GRADE = EXEC_D.GRADE AND HDAT.HR_CLASS = EXEC_D.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     EXEC_D.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     EXEC_D.EXEC_TIME DESC, ";
        $query .= "     EXEC_D.GRADE ";

        return $query;
    }

    public function getHRClass($model)
    {
        $query  = " SELECT ";
        $query .= "   HR_NAME AS LABEL,";
        $query .= "   HR_CLASS AS VALUE";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   AND GRADE = '".$model->grade."' ";
        $query .= " ORDER BY ";
        $query .= "   HR_CLASS ";

        return $query;
    }

    public function getGradeOrLess($grade)
    {
        $query  = " WITH TAISHO_T AS ( ";
        $query .= " SELECT ";
        $query .= "     A023.ABBV2, ";
        $query .= "     GDAT.SCHOOL_KIND, ";
        $query .= "     GDAT.GRADE, ";
        $query .= "     GDAT.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= "     INNER JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ";
        $query .= "           AND GDAT.SCHOOL_KIND = A023.NAME1 ";
        $query .= " WHERE ";
        $query .= "     GDAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GDAT.GRADE = '{$grade}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     A023.ABBV2, ";
        $query .= "     GDAT.SCHOOL_KIND, ";
        $query .= "     GDAT.GRADE, ";
        $query .= "     GDAT.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= "     INNER JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ";
        $query .= "           AND GDAT.SCHOOL_KIND = A023.NAME1, ";
        $query .= "     TAISHO_T ";
        $query .= " WHERE ";
        $query .= "     GDAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND A023.ABBV2 < TAISHO_T.ABBV2 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     A023.ABBV2, ";
        $query .= "     GDAT.SCHOOL_KIND, ";
        $query .= "     GDAT.GRADE, ";
        $query .= "     GDAT.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= "     INNER JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ";
        $query .= "           AND GDAT.SCHOOL_KIND = A023.NAME1, ";
        $query .= "     TAISHO_T ";
        $query .= " WHERE ";
        $query .= "     GDAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GDAT.SCHOOL_KIND = TAISHO_T.SCHOOL_KIND ";
        $query .= "     AND GDAT.GRADE <= TAISHO_T.GRADE ";
        $query .= " ORDER BY ";
        $query .= "     ABBV2 DESC, ";
        $query .= "     GRADE DESC ";

        return $query;
    }

    public function getKaikinInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     KAIKIN_M.KAIKIN_CD, ";
        $query .= "     KAIKIN_M.KAIKIN_NAME, ";
        $query .= "     KAIKIN_M.KAIKIN_DIV, ";
        $query .= "     KAIKIN_M.REF_YEAR, ";
        $query .= "     KAIKIN_M.KESSEKI_CONDITION, ";
        $query .= "     KAIKIN_M.TIKOKU_CONDITION, ";
        $query .= "     KAIKIN_M.SOUTAI_CONDITION, ";
        $query .= "     KAIKIN_M.KESSEKI_KANSAN, ";
        $query .= "     KAIKIN_M.KEKKA_JISU_CONDITION ";
        $query .= " FROM ";
        $query .= "     KAIKIN_MST KAIKIN_M ";
        $query .= "     INNER JOIN KAIKIN_GRADE_MST KAIKIN_G ON KAIKIN_M.KAIKIN_CD = KAIKIN_G.KAIKIN_CD ";
        $query .= "           AND KAIKIN_G.GRADE = '{$model->grade}' ";
        $query .= " WHERE ";
        $query .= "     KAIKIN_M.KAIKIN_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     KAIKIN_M.REF_YEAR DESC, ";
        $query .= "     KAIKIN_M.KAIKIN_DIV, ";
        $query .= "     KAIKIN_M.PRIORITY, ";
        $query .= "     KAIKIN_G.GRADE ";

        return $query;
    }

    public function getSchoolMst($model)
    {
        $query  = " SELECT  ";
        $query .= "     SEM_OFFDAYS, ";
        $query .= "     SUB_OFFDAYS, ";
        $query .= "     SUB_MOURNING, ";
        $query .= "     SUB_ABSENT, ";
        $query .= "     SUB_SUSPEND, ";
        $query .= "     SUB_KOUDOME, ";
        $query .= "     SUB_VIRUS ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->gradeSchoolKindArray[$model->grade]}' ";

        return $query;
    }

    public function getStudentSql($model, $kaikinMst, $shoolMst)
    {

        //基準日
        $baseDate = str_replace("/", "-", $model->base_date);
        //基準日学期
        $baseDateSemes = $model->baseDateSemester;

        $semeAdd = "";
        if ($shoolMst["SEM_OFFDAYS"] == "1") {
            $semeAdd = " + SUM(VALUE(AT_SEME.OFFDAYS, 0)) ";
        }

        $subAdd = "";
        if ($shoolMst["SUB_OFFDAYS"] == "1") {
            $subAdd .= " + SUM(VALUE(AT_SUB.OFFDAYS, 0)) ";
        }
        if ($shoolMst["SUB_MOURNING"] == "1") {
            $subAdd .= " + SUM(VALUE(AT_SUB.MOURNING, 0)) ";
        }
        if ($shoolMst["SUB_ABSENT"] == "1") {
            $subAdd .= " + SUM(VALUE(AT_SUB.ABSENT, 0)) ";
        }
        if ($shoolMst["SUB_SUSPEND"] == "1") {
            $subAdd .= " + SUM(VALUE(AT_SUB.SUSPEND, 0)) ";
        }
        if ($shoolMst["SUB_KOUDOME"] == "1") {
            $subAdd .= " + SUM(VALUE(AT_SUB.KOUDOME, 0)) ";
        }
        if ($shoolMst["SUB_VIRUS"] == "1") {
            $subAdd .= " + SUM(VALUE(AT_SUB.VIRUS, 0)) ";
        }

        $query .= " WITH SEMES_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER <> '9' ";
        $query .= "     AND YEAR || '-' || SEMESTER <= '".CTRL_YEAR."' || '-' || '".$baseDateSemes."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ), REGISTER_STD AS ( ";
        $query .= " SELECT ";
        $query .= "     KAIKIN_D.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     KAIKIN_DAT KAIKIN_D ";
        $query .= "     INNER JOIN KAIKIN_MST KAIKIN_M ON KAIKIN_D.KAIKIN_CD = KAIKIN_M.KAIKIN_CD ";
        $query .= "           AND KAIKIN_M.REF_YEAR = {$kaikinMst["REF_YEAR"]} ";
        $query .= " WHERE ";
        $query .= "     KAIKIN_D.YEAR = '".CTRL_YEAR."' ";
        $query .= " ), TARGET_SCHREG_ALL AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '{$model->grade}' ";
        $query .= "     AND NOT EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     REGISTER_STD REGIST_STD ";
        $query .= "                 WHERE ";
        $query .= "                     REGD.SCHREGNO = REGIST_STD.SCHREGNO) ";
        $query .= "     AND NOT EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     KAIKIN_DAT KAIKIN_D ";
        $query .= "                 WHERE ";
        $query .= "                     REGD.SCHREGNO = KAIKIN_D.SCHREGNO ";
        $query .= "                     AND KAIKIN_D.YEAR = '".CTRL_YEAR."' ";
        $query .= "                     AND KAIKIN_D.KAIKIN_CD = '{$kaikinMst["KAIKIN_CD"]}' ";
        $query .= "                     AND KAIKIN_D.INVALID_FLG = '1') ";
        $query .= " GROUP BY ";
        $query .= "     REGD.SCHREGNO ";
        $query .= " ), GRADE_MAX_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     MAX(REGD.YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     TARGET_SCHREG_ALL SCH_ALL ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON SCH_ALL.SCHREGNO = REGD.SCHREGNO ";
        $query .= "           AND REGD.GRADE IN ({$kaikinMst["GRADE_IN_STATEMENT"]}) ";
        $query .= " GROUP BY ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE ";
        $query .= " ), GRADE_MAX_SEMES AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.YEAR, ";
        $query .= "     MAX(REGD.SEMESTER) AS MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= "     GRADE_MAX_YEAR G_YEAR ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR     = G_YEAR.YEAR ";
        $query .= "                                    AND REGD.SCHREGNO = G_YEAR.SCHREGNO  ";
        $query .= " GROUP BY ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.YEAR ";
        $query .= " ), GRADE_CNT_PASS AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     GRADE_MAX_YEAR ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " HAVING ";
        $query .= "     COUNT(*) = {$kaikinMst["REF_YEAR"]} ";
        $query .= " ), SEMES_CNT_PASS AS ( ";
        $query .= " SELECT ";
        $query .= "     SCH_SEME_CNT.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.YEAR, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "      FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN GRADE_MAX_YEAR ON REGD.SCHREGNO = GRADE_MAX_YEAR.SCHREGNO ";
        $query .= "               AND REGD.YEAR = GRADE_MAX_YEAR.YEAR ";
        $query .= "               AND REGD.GRADE = GRADE_MAX_YEAR.GRADE ";
        $query .= "      WHERE ";
        $query .= "         EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 GRADE_CNT_PASS G_PASS ";
        $query .= "             WHERE ";
        $query .= "                 REGD.SCHREGNO = G_PASS.SCHREGNO) ";
        $query .= "      GROUP BY ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.YEAR ";
        $query .= "     ) AS SCH_SEME_CNT ";
        $query .= "     INNER JOIN SEMES_CNT ON SCH_SEME_CNT.YEAR = SEMES_CNT.YEAR ";
        $query .= "           AND SCH_SEME_CNT.CNT = SEMES_CNT.CNT ";
        $query .= " GROUP BY ";
        $query .= "     SCH_SEME_CNT.SCHREGNO ";
        $query .= " HAVING ";
        $query .= "     COUNT(*) = {$kaikinMst["REF_YEAR"]} ";
        $query .= " ), SCHREG_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     G_YEAR.SCHREGNO, ";
        $query .= "     G_YEAR.YEAR, ";
        $query .= "     G_SEMS.MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMES_CNT_PASS S_PASS ";
        $query .= "     INNER JOIN GRADE_MAX_YEAR G_YEAR  ON S_PASS.SCHREGNO  = G_YEAR.SCHREGNO ";
        $query .= "     INNER JOIN GRADE_MAX_SEMES G_SEMS ON G_YEAR.SCHREGNO  = G_SEMS.SCHREGNO ";
        $query .= "                                      AND G_YEAR.GRADE     = G_SEMS.GRADE ";
        $query .= " ), AT_SEME AS ( ";
        $query .= " SELECT ";
        $query .= "     SCH_Y.SCHREGNO AS SEM_SCH, ";
        $query .= "     SUM(VALUE(AT_SEME.SICK, 0)) + SUM(VALUE(AT_SEME.NOTICE, 0)) + SUM(VALUE(AT_SEME.NONOTICE, 0)) {$semeAdd} AS SICK, ";
        if (strlen($kaikinMst["KESSEKI_KANSAN"]) > 0) {
            $query .= "     (SUM(VALUE(AT_SEME.LATE, 0)) + SUM(VALUE(AT_SEME.EARLY, 0)) {$semeAdd}) * 1.0 / {$kaikinMst["KESSEKI_KANSAN"]} AS LE_AVG, ";
        }
        $query .= "     SUM(VALUE(AT_SEME.LATE, 0)) AS LATE, ";
        $query .= "     SUM(VALUE(AT_SEME.EARLY, 0)) AS EARLY ";
        $query .= " FROM ";
        $query .= "     SCHREG_YEAR SCH_Y ";
        $query .= "     INNER JOIN ATTEND_SEMES_DAT AT_SEME ON SCH_Y.YEAR = AT_SEME.YEAR ";
        $query .= "           AND SCH_Y.MAX_SEMESTER >= AT_SEME.SEMESTER ";
        $query .= "           AND SCH_Y.SCHREGNO = AT_SEME.SCHREGNO ";
        $query .= " WHERE AT_SEME.YEAR || '-' || AT_SEME.MONTH || '-' || AT_SEME.APPOINTED_DAY <= '".$baseDate."' ";
        $query .= " GROUP BY ";
        $query .= "     SCH_Y.SCHREGNO ";
        $query .= " ), AT_SUB AS ( ";
        $query .= " SELECT ";
        $query .= "     SCH_Y.SCHREGNO AS SUB_SCH, ";
        $query .= "     SUM(VALUE(AT_SUB.SICK, 0)) + SUM(VALUE(AT_SUB.NOTICE, 0)) + SUM(VALUE(AT_SUB.NONOTICE, 0)) {$subAdd} AS SICK ";
        $query .= " FROM ";
        $query .= "     SCHREG_YEAR SCH_Y ";
        $query .= "     INNER JOIN ATTEND_SUBCLASS_DAT AT_SUB ON SCH_Y.YEAR = AT_SUB.YEAR ";
        $query .= "           AND SCH_Y.MAX_SEMESTER >= AT_SUB.SEMESTER ";
        $query .= "           AND SCH_Y.SCHREGNO = AT_SUB.SCHREGNO ";
        $query .= " WHERE AT_SUB.YEAR || '-' || AT_SUB.MONTH || '-' || AT_SUB.APPOINTED_DAY <= '".$baseDate."' ";
        $query .= " GROUP BY ";
        $query .= "     SCH_Y.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     S_PASS.SCHREGNO, ";
        $query .= "     AT_SEME.*, ";
        $query .= "     AT_SUB.* ";
        $query .= " FROM ";
        $query .= "     SEMES_CNT_PASS S_PASS ";
        $query .= "     LEFT JOIN AT_SEME ON S_PASS.SCHREGNO = AT_SEME.SEM_SCH ";
        $query .= "     LEFT JOIN AT_SUB ON S_PASS.SCHREGNO = AT_SUB.SUB_SCH ";
        $query .= " WHERE ";
        if ($model->Properties["KNJZ173_DISPPATTERN = 1"]) {
            $query .= "     VALUE(AT_SEME.SICK, 0) ";
            $query .= "     + VALUE(AT_SEME.LATE, 0) ";
            $query .= "     + VALUE(AT_SEME.EARLY, 0) ";
            $query .= "     + VALUE(AT_SUB.SICK, 0) <= {$kaikinMst["KESSEKI_CONDITION"]} ";
        } elseif (strlen($kaikinMst["KESSEKI_KANSAN"]) > 0) {
            $query .= "     VALUE(AT_SEME.SICK, 0) + VALUE(AT_SEME.LE_AVG, 0) <= {$kaikinMst["KESSEKI_CONDITION"]} ";
        } else {
            $query .= "     VALUE(AT_SEME.SICK, 0) <= {$kaikinMst["KESSEKI_CONDITION"]} ";
            $query .= "     AND VALUE(AT_SEME.LATE, 0) <= {$kaikinMst["TIKOKU_CONDITION"]} ";
            $query .= "     AND VALUE(AT_SEME.EARLY, 0) <= {$kaikinMst["SOUTAI_CONDITION"]} ";
        }
        if (strlen($kaikinMst["KEKKA_JISU_CONDITION"]) > 0) {
            $query .= "     AND VALUE(AT_SUB.SICK, 0) <= {$kaikinMst["KEKKA_JISU_CONDITION"]} ";
        }

        return $query;
    }

    public function getStudentSqlKaikinDat($model, $kaikinMst, $shoolMst)
    {

        //基準日
        $baseDate = str_replace("/", "-", $model->base_date);
        //基準日学期
        $baseDateSemes = $model->baseDateSemester;

        $query .= " WITH SEMES_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER <> '9' ";
        $query .= "     AND YEAR || '-' || SEMESTER <= '".CTRL_YEAR."' || '-' || '".$baseDateSemes."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ), REGISTER_STD AS ( ";
        $query .= " SELECT ";
        $query .= "     KAIKIN_D.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     KAIKIN_DAT KAIKIN_D ";
        $query .= "     INNER JOIN KAIKIN_MST KAIKIN_M ON KAIKIN_D.KAIKIN_CD = KAIKIN_M.KAIKIN_CD ";
        $query .= "           AND KAIKIN_M.REF_YEAR = {$kaikinMst["REF_YEAR"]} ";
        $query .= " WHERE ";
        $query .= "     KAIKIN_D.YEAR = '".CTRL_YEAR."' ";
        $query .= " ), TARGET_SCHREG_ALL AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '{$model->grade}' ";
        $query .= "     AND NOT EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     REGISTER_STD REGIST_STD ";
        $query .= "                 WHERE ";
        $query .= "                     REGD.SCHREGNO = REGIST_STD.SCHREGNO) ";
        $query .= "     AND NOT EXISTS ( ";
        $query .= "                 SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     KAIKIN_DAT KAIKIN_D ";
        $query .= "                 WHERE ";
        $query .= "                     REGD.SCHREGNO = KAIKIN_D.SCHREGNO ";
        $query .= "                     AND KAIKIN_D.YEAR = '".CTRL_YEAR."' ";
        $query .= "                     AND KAIKIN_D.KAIKIN_CD = '{$kaikinMst["KAIKIN_CD"]}' ";
        $query .= "                     AND KAIKIN_D.INVALID_FLG = '1') ";
        $query .= " GROUP BY ";
        $query .= "     REGD.SCHREGNO ";
        $query .= " ), GRADE_MAX_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     MAX(REGD.YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     TARGET_SCHREG_ALL SCH_ALL ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON SCH_ALL.SCHREGNO = REGD.SCHREGNO ";
        $query .= "           AND REGD.GRADE IN ({$kaikinMst["GRADE_IN_STATEMENT"]}) ";
        $query .= " GROUP BY ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE ";
        $query .= " ), GRADE_MAX_SEMES AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.YEAR, ";
        $query .= "     MAX(REGD.SEMESTER) AS MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= "     GRADE_MAX_YEAR G_YEAR ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR     = G_YEAR.YEAR ";
        $query .= "                                    AND REGD.SCHREGNO = G_YEAR.SCHREGNO  ";
        $query .= " GROUP BY ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.YEAR ";
        $query .= " ), GRADE_CNT_PASS AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     GRADE_MAX_YEAR ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " HAVING ";
        $query .= "     COUNT(*) = {$kaikinMst["REF_YEAR"]} ";
        $query .= " ), SEMES_CNT_PASS AS ( ";
        $query .= " SELECT ";
        $query .= "     SCH_SEME_CNT.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.YEAR, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "      FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN GRADE_MAX_YEAR ON REGD.SCHREGNO = GRADE_MAX_YEAR.SCHREGNO ";
        $query .= "               AND REGD.YEAR = GRADE_MAX_YEAR.YEAR ";
        $query .= "               AND REGD.GRADE = GRADE_MAX_YEAR.GRADE ";
        $query .= "      WHERE ";
        $query .= "         EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 GRADE_CNT_PASS G_PASS ";
        $query .= "             WHERE ";
        $query .= "                 REGD.SCHREGNO = G_PASS.SCHREGNO) ";
        $query .= "      GROUP BY ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.YEAR ";
        $query .= "     ) AS SCH_SEME_CNT ";
        $query .= "     INNER JOIN SEMES_CNT ON SCH_SEME_CNT.YEAR = SEMES_CNT.YEAR ";
        $query .= "           AND SCH_SEME_CNT.CNT = SEMES_CNT.CNT ";
        $query .= " GROUP BY ";
        $query .= "     SCH_SEME_CNT.SCHREGNO ";
        $query .= " HAVING ";
        $query .= "     COUNT(*) = {$kaikinMst["REF_YEAR"]} ";
        $query .= " ), SCHREG_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     G_YEAR.SCHREGNO, ";
        $query .= "     G_YEAR.YEAR, ";
        $query .= "     G_SEMS.MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMES_CNT_PASS S_PASS ";
        $query .= "     INNER JOIN GRADE_MAX_YEAR G_YEAR  ON S_PASS.SCHREGNO  = G_YEAR.SCHREGNO ";
        $query .= "     INNER JOIN GRADE_MAX_SEMES G_SEMS ON G_YEAR.SCHREGNO  = G_SEMS.SCHREGNO ";
        $query .= "                                      AND G_YEAR.GRADE     = G_SEMS.GRADE ";
        $query .= " ), KAIKIN_ALL AS ( ";
        $query .= " SELECT ";
        $query .= "     SCH_Y.SCHREGNO, ";
        $query .= "     SUM(CASE WHEN KAIKIN_M  .KAIKIN_DIV = '1' ";
        $query .= "              THEN 1 ";
        $query .= "              ELSE 0 ";
        $query .= "         END) AS KAIKIN, ";
        $query .= "     SUM(CASE WHEN KAIKIN_M.KAIKIN_DIV = '2' ";
        $query .= "              THEN 1 ";
        $query .= "              ELSE 0 ";
        $query .= "         END) AS SEIKIN ";
        $query .= " FROM ";
        $query .= "     SCHREG_YEAR SCH_Y ";
        $query .= "     INNER JOIN KAIKIN_DAT KAIKIN_D ON SCH_Y.YEAR = KAIKIN_D.YEAR ";
        $query .= "           AND SCH_Y.SCHREGNO = KAIKIN_D.SCHREGNO ";
        $query .= "           AND VALUE(KAIKIN_D.INVALID_FLG, '0') = '0' ";
        $query .= "     INNER JOIN KAIKIN_MST KAIKIN_M ON KAIKIN_D.KAIKIN_CD = KAIKIN_M.KAIKIN_CD ";
        $query .= " GROUP BY ";
        $query .= "     SCH_Y.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     S_PASS.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SEMES_CNT_PASS S_PASS ";
        $query .= "     INNER JOIN KAIKIN_ALL ON S_PASS.SCHREGNO = KAIKIN_ALL.SCHREGNO ";
        $query .= " WHERE ";
        //KAIKIN_DIV:1=皆勤、2=精勤
        if ($kaikinMst["KAIKIN_DIV"] == '1') {
            $query .= "     KAIKIN_ALL.KAIKIN = {$kaikinMst["REF_YEAR"]} ";
        } else {
            $query .= "     KAIKIN_ALL.KAIKIN + KAIKIN_ALL.SEIKIN = {$kaikinMst["REF_YEAR"]} ";
        }

        return $query;
    }

    //データ更新
    public function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjc166aQuery::getSchoolMst($model);
        $shoolMst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //対象学年以下
        $query = knjc166aQuery::getGradeOrLess($model->grade);
        $result = $db->query($query);
        $gradeOrLess = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gradeOrLess[] = $row;
        }
        $result->free();

        //皆勤マスタ
        $query = knjc166aQuery::getKaikinInfo($model);
        $result = $db->query($query);
        $kaikinInfo = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $kaikinInfo[$row["KAIKIN_CD"]] = $row;
            $targetGrade = array();
            for ($gradeCnt = 0; $gradeCnt < $row["REF_YEAR"]; $gradeCnt++) {
                $targetGrade[] = $gradeOrLess[$gradeCnt]["GRADE"];
            }
            $kaikinInfo[$row["KAIKIN_CD"]]["GRADE_IN_STATEMENT"] = "'".implode("', '", $targetGrade)."'";
        }
        $result->free();

        $query  = " DELETE FROM ";
        $query .= "     KAIKIN_DAT KAIKIN_D ";
        $query .= " WHERE ";
        $query .= "     KAIKIN_D.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT REGD ";
        $query .= "         WHERE ";
        $query .= "             KAIKIN_D.SCHREGNO = REGD.SCHREGNO ";
        $query .= "             AND KAIKIN_D.YEAR = REGD.YEAR ";
        $query .= "             AND REGD.GRADE = '{$model->grade}' ";
        $query .= "         ) ";
        $query .= "     AND VALUE(KAIKIN_D.INVALID_FLG, '0') = '0' ";
        $db->query($query);

        foreach ($kaikinInfo as $kaikinCd => $kaikinMst) {
            //KNJZ173_DISPPATTERN = 1の時
            //KAIKIN_MST.KEKKA_JISU_CONDITIONが０の為、単年度を先に処理しておく
            if ($model->Properties["KNJZ173_DISPPATTERN"] = "1" && $kaikinMst["REF_YEAR"] > 1) {
                continue;
            } else {
                $query = knjc166aQuery::getStudentSql($model, $kaikinMst, $shoolMst);
            }
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["KAIKIN_CD"][TEXT]        = $kaikinCd;
                $data["KAIKIN_FLG"][TEXT]       = "1";
                $data["KAIKIN_AUTO_FLG"][TEXT]  = "1";
                $data["INVALID_FLG"][TEXT]      = "0";
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "KAIKIN_DAT");
                $db->query($query);
            }
            $result->free();
        }

        //KNJZ173_DISPPATTERN = 1の時
        //KAIKIN_MST.KEKKA_JISU_CONDITIONが０の為、単年度を処理した後に複数年度を処理する。
        //例：３年間のKAIKIN_DATが全て皆勤なら、３年皆勤とする。
        if ($model->Properties["KNJZ173_DISPPATTERN"] = "1") {
            foreach ($kaikinInfo as $kaikinCd => $kaikinMst) {
                if ($kaikinMst["REF_YEAR"] > 1) {
                    $query = knjc166aQuery::getStudentSqlKaikinDat($model, $kaikinMst, $shoolMst);
                } else {
                    continue;
                }
    
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $query  = " DELETE FROM ";
                    $query .= "     KAIKIN_DAT KAIKIN_D ";
                    $query .= " WHERE ";
                    $query .= "     KAIKIN_D.YEAR = '".CTRL_YEAR."' ";
                    $query .= "     AND KAIKIN_D.SCHREGNO = '{$row["SCHREGNO"]}' ";
                    $db->query($query);
    
                    $data = array();
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                    $data["KAIKIN_CD"][TEXT]        = $kaikinCd;
                    $data["KAIKIN_FLG"][TEXT]       = "1";
                    $data["KAIKIN_AUTO_FLG"][TEXT]  = "1";
                    $data["INVALID_FLG"][TEXT]      = "0";
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";
    
                    $query = Query::insertSQL($data, "KAIKIN_DAT");
                    $db->query($query);
                }
                $result->free();
            }
        }

        if (get_count($model->selClass) > 0) {
            foreach ($model->selClass as $val) {
                $query = knjc166aQuery::insertExecDat($model, $val);
                $db->query($query);
            }
        } else {
            $query = knjc166aQuery::insertExecDat($model, "000");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function insertExecDat($model, $hrClass)
    {
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["GRADE"][TEXT]        = $model->grade;
        $data["HR_CLASS"][TEXT]     = $hrClass;
        $data["EXEC_TIME"][NUMBER]  = "sysdate()";
        $data["BASE_DATE"][TEXT]    = str_replace("/", "-", $model->base_date);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "KAIKIN_EXEC_DAT");

        return $query;
    }
}
