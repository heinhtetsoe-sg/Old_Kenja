<?php
class knja352query extends Query
{

    //校種取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度取得
    public function getYearHist($model)
    {
        $query .= "   SELECT DISTINCT ";
        $query .= "       YEAR AS LABEL,";
        $query .= "       YEAR AS VALUE";
        $query .= "   FROM ";
        $query .= "       SEMESTER_MST ";
        $query .= "   WHERE ";
        $query .= "       YEAR <= '{$model->year}' ";
        $query .= "   ORDER BY ";
        $query .= "       YEAR DESC ";

        return $query;
    }

    //CSV用SQL
    public function selectCsvQuery($model)
    {
        //新入生、在校生
        if ($model->field["OUTPUT_DIV"] != "3") {
            $query .= "   WITH MAIN AS ( ";
            $query .= "       SELECT ";
            $query .= "           REGD.YEAR, ";
            $query .= "           REGD.SEMESTER, ";
            $query .= "           REGD.SCHREGNO ";
            $query .= "       FROM ";
            $query .= "           SCHREG_REGD_DAT REGD ";
            $query .= "           INNER JOIN SCHREG_REGD_GDAT GDAT ";
            $query .= "               ON GDAT.YEAR      = REGD.YEAR ";
            $query .= "               AND GDAT.GRADE    = REGD.GRADE ";
            $query .= "           INNER JOIN SCHREG_BASE_MST BASE ";
            $query .= "               ON BASE.SCHREGNO  = REGD.SCHREGNO ";
            $query .= "       WHERE ";
            $query .= "           REGD.YEAR             = '{$model->year}' ";
            $query .= "           AND REGD.SEMESTER     = '{$model->semester}' ";
            $query .= "           AND GDAT.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
            if ($model->field["OUTPUT_DIV"] == "1") { //新入生
                $query .= "           AND GDAT.GRADE_CD = '01' ";
            }
            $query .= "   ) ";
            $query .= "   SELECT ";
            $query .= "       REGD.SCHREGNO, ";
            $query .= "       REGD.ATTENDNO, ";
            $query .= "       REGD.GRADE, ";
            $query .= "       BASE.NAME, ";
            $query .= "       BASE.SEX, ";
            $query .= "       BASE.NAME_KANA, ";
            $query .= "       BASE.NAME_ENG, ";
            $query .= "       BASE.BIRTHDAY, ";
            $query .= "       BASE.ENT_DATE, ";
            $query .= "       BASE.GRD_DIV, ";
            $query .= "       BASE.GRD_DATE, ";
            $query .= "       BASE.EMERGENCYTELNO, ";
            $query .= "       FISCALYEAR(BASE.GRD_DATE) AS GRD_YEAR, ";
            $query .= "       BASE.FINSCHOOLCD, ";
            $query .= "       GDAT.SCHOOL_KIND, ";
            $query .= "       GDAT.GRADE_CD, ";
            $query .= "       HDAT.HR_CLASS_NAME1, ";
            $query .= "       BDET.BASE_REMARK1 AS EXAMNO, ";
            $query .= "       ADDRESS.ZIPCD, ";
            $query .= "       ADDRESS.ADDR1, ";
            $query .= "       ADDRESS.ADDR2, ";
            $query .= "       ADDRESS.TELNO, ";
            $query .= "       GUARD.GUARD_NAME, ";
            $query .= "       GUARD.GUARD_KANA, ";
            $query .= "       GADDRESS.GUARD_TELNO, ";
            $query .= "       GADDRESS.GUARD_ZIPCD, ";
            $query .= "       GADDRESS.GUARD_ADDR1, ";
            $query .= "       GADDRESS.GUARD_ADDR2, ";
            $query .= "       TRANSFER1.TRANSFERCD ";
            $query .= "   FROM ";
            $query .= "       MAIN ";
            $query .= "       INNER JOIN SCHREG_REGD_DAT REGD ";
            $query .= "           ON REGD.SCHREGNO  = MAIN.SCHREGNO ";
            $query .= "           AND REGD.YEAR     = MAIN.YEAR ";
            $query .= "           AND REGD.SEMESTER = MAIN.SEMESTER ";
            $query .= "       INNER JOIN SCHREG_BASE_MST BASE ";
            $query .= "           ON BASE.SCHREGNO  = REGD.SCHREGNO ";
            $query .= "       INNER JOIN SCHREG_REGD_GDAT GDAT ";
            $query .= "           ON GDAT.YEAR      = REGD.YEAR ";
            $query .= "           AND GDAT.GRADE    = REGD.GRADE ";
            $query .= "       INNER JOIN SCHREG_REGD_HDAT HDAT ";
            $query .= "           ON HDAT.YEAR      = REGD.YEAR ";
            $query .= "           AND HDAT.SEMESTER = REGD.SEMESTER ";
            $query .= "           AND HDAT.GRADE    = REGD.GRADE ";
            $query .= "           AND HDAT.GRADE    = REGD.GRADE ";
            $query .= "           AND HDAT.HR_CLASS = REGD.HR_CLASS ";
            $query .= "       LEFT JOIN SCHREG_BASE_DETAIL_MST BDET ";
            $query .= "           ON BDET.SCHREGNO  = BASE.SCHREGNO ";
            $query .= "           AND BDET.BASE_SEQ = '003' ";
            $query .= "       LEFT JOIN ( ";
            $query .= "           SELECT ";
            $query .= "               T1.* ";
            $query .= "           FROM ";
            $query .= "               SCHREG_ADDRESS_DAT T1 ";
            $query .= "               INNER JOIN ( ";
            $query .= "                   SELECT ";
            $query .= "                       SCHREGNO, ";
            $query .= "                       MAX(ISSUEDATE) AS MAX_ISSUEDATE ";
            $query .= "                   FROM ";
            $query .= "                       SCHREG_ADDRESS_DAT ";
            $query .= "                   WHERE ";
            $query .= "                       '{$model->ctrlDate}' BETWEEN ISSUEDATE AND EXPIREDATE ";
            $query .= "                   GROUP BY ";
            $query .= "                       SCHREGNO ";
            $query .= "               ) T2 ";
            $query .= "                    ON T2.SCHREGNO       = T1.SCHREGNO ";
            $query .= "                   AND T2.MAX_ISSUEDATE  = T1.ISSUEDATE ";
            $query .= "       ) ADDRESS ";
            $query .= "           ON ADDRESS.SCHREGNO = REGD.SCHREGNO ";
            $query .= "       LEFT JOIN GUARDIAN_DAT GUARD ";
            $query .= "           ON GUARD.SCHREGNO = REGD.SCHREGNO ";
            $query .= "       LEFT JOIN ( ";
            $query .= "           SELECT ";
            $query .= "               T1.* ";
            $query .= "           FROM ";
            $query .= "               GUARDIAN_ADDRESS_DAT T1 ";
            $query .= "               INNER JOIN ( ";
            $query .= "                   SELECT ";
            $query .= "                       SCHREGNO, ";
            $query .= "                       MAX(ISSUEDATE) AS MAX_ISSUEDATE ";
            $query .= "                   FROM ";
            $query .= "                       GUARDIAN_ADDRESS_DAT ";
            $query .= "                   WHERE ";
            $query .= "                       '{$model->ctrlDate}' BETWEEN ISSUEDATE AND EXPIREDATE ";
            $query .= "                   GROUP BY ";
            $query .= "                       SCHREGNO ";
            $query .= "               ) T2 ";
            $query .= "                    ON T2.SCHREGNO       = T1.SCHREGNO ";
            $query .= "                   AND T2.MAX_ISSUEDATE  = T1.ISSUEDATE ";
            $query .= "       ) GADDRESS ";
            $query .= "           ON GADDRESS.SCHREGNO = REGD.SCHREGNO ";
            $query .= "       LEFT JOIN ( ";
            $query .= "           SELECT ";
            $query .= "               SCHREGNO, ";
            $query .= "               TRANSFERCD, ";
            $query .= "               MAX(TRANSFER_SDATE) AS MAX_TRANSFER_SDATE, ";
            $query .= "               ROW_NUMBER() OVER(PARTITION BY SCHREGNO ORDER BY CASE WHEN TRANSFERCD = '2' THEN 1 WHEN TRANSFERCD = '1' THEN 2 WHEN TRANSFERCD = '3' THEN 3 ELSE 4 END) AS PRIORITY ";
            $query .= "           FROM ";
            $query .= "               SCHREG_TRANSFER_DAT ";
            $query .= "           WHERE ";
            $query .= "               '{$model->ctrlDate}' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
            $query .= "           GROUP BY ";
            $query .= "               SCHREGNO, ";
            $query .= "               TRANSFERCD ";
            $query .= "       ) TRANSFER1 ";
            $query .= "           ON TRANSFER1.SCHREGNO             = REGD.SCHREGNO ";
            $query .= "           AND TRANSFER1.PRIORITY            = 1 "; //同期間内で最も優先度の高い異動区分のレコードを取得 (2:休学 > 1:留学 > 3:退学 の優先度順)
    
            $query .= "   ORDER BY ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO ";
        } else {
            $query .= "   WITH MAIN AS ( ";
            $query .= "       SELECT ";
            $query .= "           REGD.YEAR, ";
            $query .= "           max(REGD.SEMESTER) as SEMESTER, ";
            $query .= "           REGD.SCHREGNO ";
            $query .= "       FROM ";
            $query .= "           GRD_REGD_DAT REGD ";
            $query .= "           INNER JOIN SCHREG_REGD_GDAT GDAT ";
            $query .= "               ON GDAT.YEAR = REGD.YEAR ";
            $query .= "               AND GDAT.GRADE = REGD.GRADE ";
            $query .= "           INNER JOIN GRD_BASE_MST BASE ";
            $query .= "               ON BASE.SCHREGNO = REGD.SCHREGNO ";
            $query .= "       WHERE ";
            $query .= "           REGD.YEAR             = '{$model->field["GRD_YEAR"]}' ";
            $query .= "           AND GDAT.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "       GROUP BY ";
            $query .= "           REGD.YEAR, ";
            $query .= "           REGD.SCHREGNO ";
            $query .= "   ) ";
            $query .= "   SELECT ";
            $query .= "       REGD.SCHREGNO, ";
            $query .= "       REGD.ATTENDNO, ";
            $query .= "       REGD.GRADE, ";
            $query .= "       BASE.NAME, ";
            $query .= "       BASE.SEX, ";
            $query .= "       BASE.NAME_KANA, ";
            $query .= "       BASE.NAME_ENG, ";
            $query .= "       BASE.BIRTHDAY, ";
            $query .= "       BASE.ENT_DATE, ";
            $query .= "       BASE.GRD_DIV, ";
            $query .= "       BASE.GRD_DATE, ";
            $query .= "       BASE.CUR_EMERGENCYTELNO AS EMERGENCYTELNO, ";
            $query .= "       FISCALYEAR(BASE.GRD_DATE) AS GRD_YEAR, ";
            $query .= "       BASE.FINSCHOOLCD, ";
            $query .= "       GDAT.SCHOOL_KIND, ";
            $query .= "       GDAT.GRADE_CD, ";
            $query .= "       HDAT.HR_CLASS_NAME1, ";
            $query .= "       ADDRESS.ZIPCD, ";
            $query .= "       ADDRESS.ADDR1, ";
            $query .= "       ADDRESS.ADDR2, ";
            $query .= "       ADDRESS.TELNO, ";
            $query .= "       GUARD.GUARD_NAME, ";
            $query .= "       GUARD.GUARD_KANA, ";
            $query .= "       GADDRESS.GUARD_TELNO, ";
            $query .= "       GADDRESS.GUARD_ZIPCD, ";
            $query .= "       GADDRESS.GUARD_ADDR1, ";
            $query .= "       GADDRESS.GUARD_ADDR2, ";
            $query .= "       TRANSFER1.TRANSFERCD, ";
            $query .= "       BASE.GRD_DIV ";
            $query .= "   FROM ";
            $query .= "       MAIN ";
            $query .= "       INNER JOIN GRD_REGD_DAT REGD ";
            $query .= "           ON REGD.SCHREGNO = MAIN.SCHREGNO ";
            $query .= "           AND REGD.YEAR = MAIN.YEAR ";
            $query .= "           AND REGD.SEMESTER = MAIN.SEMESTER ";
            $query .= "       INNER JOIN GRD_BASE_MST BASE ";
            $query .= "           ON BASE.SCHREGNO = REGD.SCHREGNO ";
            $query .= "       INNER JOIN SCHREG_REGD_GDAT GDAT ";
            $query .= "           ON GDAT.YEAR = REGD.YEAR ";
            $query .= "           AND GDAT.GRADE = REGD.GRADE ";
            $query .= "       INNER JOIN GRD_REGD_HDAT HDAT ";
            $query .= "           ON HDAT.YEAR = REGD.YEAR ";
            $query .= "           AND HDAT.SEMESTER = REGD.SEMESTER ";
            $query .= "           AND HDAT.GRADE = REGD.GRADE ";
            $query .= "           AND HDAT.GRADE = REGD.GRADE ";
            $query .= "           AND HDAT.HR_CLASS = REGD.HR_CLASS ";
            $query .= "       LEFT JOIN ( ";
            $query .= "           SELECT ";
            $query .= "               T1.* ";
            $query .= "           FROM ";
            $query .= "               GRD_ADDRESS_DAT T1 ";
            $query .= "               INNER JOIN ( ";
            $query .= "                   SELECT ";
            $query .= "                       SCHREGNO, ";
            $query .= "                       MAX(ISSUEDATE) AS MAX_ISSUEDATE ";
            $query .= "                   FROM ";
            $query .= "                       GRD_ADDRESS_DAT ";
            $query .= "                   GROUP BY ";
            $query .= "                       SCHREGNO ";
            $query .= "               ) T2 ";
            $query .= "                   ON T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                   AND T2.MAX_ISSUEDATE = T1.ISSUEDATE ";
            $query .= "       ) ADDRESS ";
            $query .= "           ON ADDRESS.SCHREGNO = REGD.SCHREGNO ";
            $query .= "       LEFT JOIN GRD_GUARDIAN_DAT GUARD ";
            $query .= "           ON GUARD.SCHREGNO = REGD.SCHREGNO ";
            $query .= "       LEFT JOIN ( ";
            $query .= "           SELECT ";
            $query .= "               T1.* ";
            $query .= "           FROM ";
            $query .= "               GRD_GUARDIAN_ADDRESS_DAT T1 ";
            $query .= "               INNER JOIN ( ";
            $query .= "                   SELECT ";
            $query .= "                       SCHREGNO, ";
            $query .= "                       MAX(ISSUEDATE) AS MAX_ISSUEDATE ";
            $query .= "                   FROM ";
            $query .= "                       GRD_GUARDIAN_ADDRESS_DAT ";
            $query .= "                   GROUP BY ";
            $query .= "                       SCHREGNO ";
            $query .= "               ) T2 ";
            $query .= "                   ON T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                   AND T2.MAX_ISSUEDATE = T1.ISSUEDATE ";
            $query .= "       ) GADDRESS ";
            $query .= "           ON GADDRESS.SCHREGNO = REGD.SCHREGNO ";
            $query .= "       LEFT JOIN ( ";
            $query .= "           SELECT ";
            $query .= "               SCHREGNO, ";
            $query .= "               TRANSFERCD, ";
            $query .= "               MAX(TRANSFER_SDATE) AS MAX_TRANSFER_SDATE, ";
            $query .= "               ROW_NUMBER() OVER ( ";
            $query .= "                   PARTITION BY ";
            $query .= "                       SCHREGNO ";
            $query .= "                   ORDER BY ";
            $query .= "                       CASE ";
            $query .= "                           WHEN TRANSFERCD = '2' ";
            $query .= "                               THEN 1 ";
            $query .= "                           WHEN TRANSFERCD = '1' ";
            $query .= "                               THEN 2 ";
            $query .= "                           WHEN TRANSFERCD = '3' ";
            $query .= "                               THEN 3 ";
            $query .= "                           ELSE 4 ";
            $query .= "                           END ";
            $query .= "               ) AS PRIORITY ";
            $query .= "           FROM ";
            $query .= "               GRD_TRANSFER_DAT ";
            $query .= "           GROUP BY ";
            $query .= "               SCHREGNO, ";
            $query .= "               TRANSFERCD ";
            $query .= "       ) TRANSFER1 ";
            $query .= "           ON TRANSFER1.SCHREGNO = REGD.SCHREGNO ";
            $query .= "           AND TRANSFER1.PRIORITY = 1 ";

            $query .= "   ORDER BY ";
            $query .= "       REGD.GRADE, ";
            $query .= "       REGD.HR_CLASS, ";
            $query .= "       REGD.ATTENDNO ";
        }

        return $query;
    }


    public function selectExamCsvQuery($model)
    {
        $enterYear = $model->year + 1;

        $query  = "   SELECT ";
        $query .= "       BD026.REMARK1 AS SCHREGNO, ";
        $query .= "       BASE.NAME, ";
        $query .= "       BASE.NAME_KANA, ";
        $query .= "       BASE.SEX, ";
        $query .= "       BASE.BIRTHDAY, ";
        $query .= "       BASE.EXAMNO ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD026 ";
        $query .= "           ON BASE.ENTEXAMYEAR = BD026.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = BD026.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO = BD026.EXAMNO ";
        $query .= "           AND BD026.SEQ = '026' ";
        $query .= "   WHERE ";
        $query .= "       BASE.ENTEXAMYEAR          = '{$enterYear}' ";
        $query .= "       AND BASE.APPLICANTDIV     = '{$model->applicantdiv}' ";

        return $query;
    }
}
