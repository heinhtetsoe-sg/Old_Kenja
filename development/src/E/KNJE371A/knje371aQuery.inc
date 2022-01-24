<?php
class knje371aQuery extends Query
{
    //テスト種別
    public function getTest($model)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(T2.SEMESTERNAME,'') || ':' || VALUE(T1.TESTITEMNAME,'') AS LABEL, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ";
        $query .= "             ON T2.YEAR     = T1.YEAR ";
        $query .= "            AND T2.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR      = '".$model->year."' ";
        $query .= " AND T1.SEMESTER  <> '3' ";
        $query .= " AND T1.SCORE_DIV = '09' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //CSV出力データ
    public function selectQueryCsv($model)
    {
        $hopeWhere = ""; //条件句にて使用
        $query .= " WITH SCHNO_A AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         REGD.YEAR, ";
        $query .= "         REGD.SEMESTER, ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         REGD.COURSECD, ";
        $query .= "         REGD.MAJORCD, ";
        $query .= "         REGD.COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     = '".$model->year."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SEMESTER, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     HOPE.SCHOOL_CD1 || HOPE.FACULTYCD1 || HOPE.DEPARTMENTCD1 AS HOPE1, ";
        $query .= "     HOPE.SCHOOL_CD2 || HOPE.FACULTYCD2 || HOPE.DEPARTMENTCD2 AS HOPE2, ";
        for ($i=3; $i<=$model->hopeCnt; $i++) {
            $table = "HOPE_DT".$i;
            $hope = "HOPE".$i;
            $query .= "     ".$table.".SCHOOL_CD || ".$table.".FACULTYCD || ".$table.".DEPARTMENTCD AS ".$hope.", ";
            $hopeWhere .= ",".$table.".SCHOOL_CD"; //条件句にて使用
        }
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     REGD.COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHNO_A REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "            ON REGDG.YEAR  = REGD.YEAR ";
        $query .= "           AND REGDG.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "            ON REGDH.YEAR     = REGD.YEAR ";
        $query .= "           AND REGDH.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND REGDH.GRADE    = REGD.GRADE ";
        $query .= "           AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "            ON BASE.SCHREGNO = REGD.SCHREGNO ";
        //進路希望コード(変換前)
        $query .= "     LEFT JOIN COURSE_HOPE_DAT HOPE ";
        $query .= "            ON HOPE.ENTRYDATE = (SELECT MAX(ENTRYDATE) FROM COURSE_HOPE_DAT L_HOPE WHERE L_HOPE.SEQ = HOPE.SEQ AND L_HOPE.SCHREGNO = HOPE.SCHREGNO) ";
        $query .= "           AND HOPE.SEQ       = '1' "; //進学
        $query .= "           AND HOPE.SCHREGNO  = REGD.SCHREGNO ";
        for ($i=3; $i<=$model->hopeCnt; $i++) {
            $table = "HOPE_DT".$i;
            $query .= "     LEFT JOIN COURSE_HOPE_DETAIL_DAT ".$table." ";
            $query .= "            ON ".$table.".ENTRYDATE = HOPE.ENTRYDATE ";
            $query .= "           AND ".$table.".SEQ       = HOPE.SEQ ";
            $query .= "           AND ".$table.".SCHREGNO  = HOPE.SCHREGNO ";
            $query .= "           AND ".$table.".HOPE_NUM  = '".$i."' ";
        }
        $query .= " WHERE ";
        $query .= "     '00003602' IN (HOPE.SCHOOL_CD1, HOPE.SCHOOL_CD2 ".$hopeWhere.")"; //第1希望～第8希望の中に、「00003602」が含まれているレコードが対象
        $query .= "  OR '00003603' IN (HOPE.SCHOOL_CD1, HOPE.SCHOOL_CD2 ".$hopeWhere.")"; //第1希望～第8希望の中に、「00003603」が含まれているレコードが対象
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO ";

        return $query;
    }

    //RECORD_RANK_SDIV_DAT の取得
    public function getRecorRankSdiv($model, $schregno)
    {
        $query .= " WITH SCHNO_A AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         REGD.YEAR, ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         GDAT.GRADE_CD, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         REGD.COURSECD, ";
        $query .= "         REGD.MAJORCD, ";
        $query .= "         REGD.COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "                 ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "                AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     <= '".$model->year."' "; //過年度含む
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= " ) , SCHNO AS( ";
        $query .= "     SELECT DISTINCT "; //3年分取得
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         SCHNO_A T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) "; //同一学年の最大年度を取得
        $query .= " ) , RECORD AS( ";
        $query .= "     SELECT DISTINCT "; //SUBCLASS_MSTに存在する教科
        $query .= "         R1.* ";
        $query .= "     FROM ";
        $query .= "         SCHNO REGD ";
        $query .= "         INNER JOIN RECORD_RANK_SDIV_DAT R1 ";
        $query .= "                ON R1.YEAR      = REGD.YEAR ";
        $query .= "               AND R1.SEMESTER  <> '3' ";
        $query .= "               AND R1.SCORE_DIV IN ('08','09') ";
        $query .= "               AND R1.SCHREGNO  = REGD.SCHREGNO ";
        $query .= "         INNER JOIN SUBCLASS_MST SUBCLASS ";
        $query .= "                ON SUBCLASS.CLASSCD       = R1.CLASSCD ";
        $query .= "               AND SUBCLASS.SCHOOL_KIND   = R1.SCHOOL_KIND ";
        $query .= "               AND SUBCLASS.CURRICULUM_CD = R1.CURRICULUM_CD ";
        $query .= "               AND SUBCLASS.SUBCLASSCD    = R1.SUBCLASSCD ";
        $query .= "     UNION ";
        $query .= "     SELECT DISTINCT "; //ALL9教科(SUBCLASSCD = '999999')
        $query .= "         R1.* ";
        $query .= "     FROM ";
        $query .= "         SCHNO REGD ";
        $query .= "         INNER JOIN RECORD_RANK_SDIV_DAT R1 ";
        $query .= "                ON R1.YEAR      = REGD.YEAR ";
        $query .= "               AND R1.SEMESTER  <> '3' ";
        $query .= "               AND R1.SCORE_DIV IN ('08','09') ";
        $query .= "               AND R1.SUBCLASSCD = '999999' ";
        $query .= "               AND R1.SCHREGNO  = REGD.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV AS TEST_CD, ";
        $query .= "     R1.CLASSCD, ";
        $query .= "     CASE WHEN R1.SUBCLASSCD = '999999' ";
        $query .= "          THEN '999999' ";
        $query .= "          ELSE R1.CLASSCD || '-' || R1.SCHOOL_KIND || '-' || R1.CURRICULUM_CD || '-' || R1.SUBCLASSCD ";
        $query .= "     END AS SUBCLASSCD, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE_CD, ";
        $query .= "     R1.SCORE, ";
        $query .= "     CASE WHEN R1.SUBCLASSCD = '999999' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE C1.CREDITS ";
        $query .= "      END AS CREDITS ";
        $query .= " FROM ";
        $query .= "     SCHNO REGD ";
        $query .= "     INNER JOIN RECORD R1 ";
        $query .= "            ON R1.YEAR       = REGD.YEAR ";
        $query .= "           AND R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV IN ('9990009', '9990008', '".$model->field["TEST_CD"]."') ";
        $query .= "           AND R1.SCHREGNO   = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN CREDIT_MST C1 ";
        $query .= "            ON C1.YEAR          = REGD.YEAR ";
        $query .= "           AND C1.COURSECD      = REGD.COURSECD ";
        $query .= "           AND C1.MAJORCD       = REGD.MAJORCD ";
        $query .= "           AND C1.GRADE         = REGD.GRADE ";
        $query .= "           AND C1.COURSECODE    = REGD.COURSECODE ";
        $query .= "           AND C1.CLASSCD       = R1.CLASSCD ";
        $query .= "           AND C1.SCHOOL_KIND   = R1.SCHOOL_KIND ";
        $query .= "           AND C1.CURRICULUM_CD = R1.CURRICULUM_CD ";
        $query .= "           AND C1.SUBCLASSCD    = R1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     REGD.YEAR DESC, ";
        $query .= "     TEST_CD ";

        return $query;
    }

    //SCHREG_QUALIFIED_HOBBY_DAT の取得
    public function getSchregQualifiedHobbyDat($year, $schregno, $qualifiedCd, $field)
    {
        $query .= " SELECT ";
        if ($field == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.".$field." ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR          = '".$year."' ";
        $query .= " AND T1.SCHREGNO      = '".$schregno."' ";
        $query .= " AND T1.QUALIFIED_CD  = '".$qualifiedCd."' ";
        $query .= " AND T1.SEQ           = (SELECT MAX(T2.SEQ) AS SEQ ";
        $query .= "                           FROM SCHREG_QUALIFIED_HOBBY_DAT T2 ";
        $query .= "                          WHERE T2.YEAR         = T1.YEAR ";
        $query .= "                            AND T2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "                            AND T2.QUALIFIED_CD = T1.QUALIFIED_CD) ";
        return $query;
    }

    //COLLEGE_RECOMMEND_CONDITION_MAIN_DAT の取得
    public function getCollegeRecommendConditionMainDat($row, $schoolCd, $facultycd, $departmentcd)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_RECOMMEND_CONDITION_MAIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".$row["YEAR"]."' ";
        $query .= " AND SCHOOL_CD    = '".$schoolCd."' ";
        $query .= " AND FACULTYCD    = '".$facultycd."' ";
        $query .= " AND DEPARTMENTCD = '".$departmentcd."' ";
        return $query;
    }

    //COLLEGE_RECOMMEND_CONDITION_COURSE_DAT の取得
    public function getCollegeRecommendConditionCourseDat($row, $schoolCd, $facultycd, $departmentcd)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_RECOMMEND_CONDITION_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".$row["YEAR"]."' ";
        $query .= " AND SCHOOL_CD    = '".$schoolCd."' ";
        $query .= " AND FACULTYCD    = '".$facultycd."' ";
        $query .= " AND DEPARTMENTCD = '".$departmentcd."' ";
        $query .= " AND COURSECD     = '".$row["COURSECD"]."' ";
        $query .= " AND MAJORCD      = '".$row["MAJORCD"]."' ";
        $query .= " AND COURSECODE   = '".$row["COURSECODE"]."' ";
        return $query;
    }

    //COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT の取得
    public function getCollegeRecommendConditionSubclassDat($row, $schoolCd, $facultycd, $departmentcd)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR          = '".$row["YEAR"]."' ";
        $query .= " AND SCHOOL_CD     = '".$schoolCd."' ";
        $query .= " AND FACULTYCD     = '".$facultycd."' ";
        $query .= " AND DEPARTMENTCD  = '".$departmentcd."' ";
        return $query;
    }

    //COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT の取得
    public function getCollegeRecommendConditionQualifiedDat($row, $schoolCd, $facultycd, $departmentcd)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR          = '".$row["YEAR"]."' ";
        $query .= " AND SCHOOL_CD     = '".$schoolCd."' ";
        $query .= " AND FACULTYCD     = '".$facultycd."' ";
        $query .= " AND DEPARTMENTCD  = '".$departmentcd."' ";
        // $query .= " AND QUALIFIED_CD  = '".$row["QUALIFIED_CD"]."' ";
        return $query;
    }

    //推薦条件クリア(履修科目要件) の取得
    public function getHopePassSubclass($year, $schregno, $Row)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD ";
        $query .= " FROM  ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ";
        $query .= "             ON T2.YEAR          = T1.YEAR ";
        $query .= "            AND T2.SEMESTER      = T1.SEMESTER ";
        $query .= "            AND T2.CHAIRCD       = T1.CHAIRCD ";
        $query .= "            AND T2.CLASSCD       = '".$Row["CLASSCD"]."' ";
        $query .= "            AND T2.SCHOOL_KIND   = '".$Row["SCHOOL_KIND"]."' ";
        $query .= "            AND T2.CURRICULUM_CD = '".$Row["CURRICULUM_CD"]."' ";
        $query .= "            AND T2.SUBCLASSCD    = '".$Row["SUBCLASSCD"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$year."' ";
        $query .= " AND T1.SCHREGNO = '".$schregno."' ";
        return $query;
    }

    //推薦条件クリア(資格要件) の取得
    public function getHopePassQualified($row, $Row)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".$row["YEAR"]."' ";
        $query .= " AND SCHREGNO     = '".$row["SCHREGNO"]."' ";
        $query .= " AND QUALIFIED_CD = '".$Row["QUALIFIED_CD"]."' ";
        $query .= " AND REGDDATE     >= '".$Row["VALID_S_DATE"]."' ";
        $query .= " AND RANK         = '".$Row["CONDITION_RANK"]."' ";
        $query .= " AND SCORE        >= '".$Row["CONDITION_SCORE"]."' ";
        return $query;
    }

    //高1～高3の年度を取得
    public function getSchregYear($model, $schregno)
    {
        $query .= " WITH SCHNO_A AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         REGD.YEAR, ";
        $query .= "         GDAT.GRADE_CD ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "                 ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "                AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     <= '".$model->year."' "; //過年度含む
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= " )";
        $query .= " SELECT DISTINCT "; //3年分取得
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHNO_A T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) "; //同一学年の最大年度を取得
        return $query;
    }

    //大学4桁コードの取得
    public function getConvertHopeCd($hopeCd)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E070' ";
        $query .= " AND NAME1   = '$hopeCd' ";
        return $query;
    }
}
