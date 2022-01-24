<?php

require_once('for_php7.php');

class knjd157dQuery extends Query
{

    //学期取得
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得（権限チェック）
    public function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('J','H') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別
    public function getTest($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別(CSVファイルの名前に使用)
    public function getTestName($model)
    {
        $query  = " SELECT ";
        $query .= "        T1.TESTITEMNAME ";
        $query .= "      , T1.SCORE_DIV ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";

        return $query;
    }

    public function getHrClass($model, $semester)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    public function isShowCoursename($semester)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     CASE WHEN COUNT(DISTINCT T1.COURSECD) > 1";
        $query .= "          THEN 1 ";
        $query .= "          ELSE 0 ";
        $query .= "          END AS SHOW_COURSENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";

        return $query;
    }

    //選択クラス内で警告点以下の科目を取得
    public function getKeikokuSubclass($model, $select)
    {
        $query .= "WITH SUB_MIN_SCORE AS ( ";
        $query .= "        SELECT RSD.YEAR ";
        $query .= "             , RSD.SEMESTER ";
        $query .= "             , SRD.GRADE ";
        $query .= "             , SRD.HR_CLASS ";
        $query .= "             , RSD.TESTKINDCD ";
        $query .= "             , RSD.TESTITEMCD ";
        $query .= "             , RSD.SCORE_DIV ";
        $query .= "             , RSD.CURRICULUM_CD ";
        $query .= "             , RSD.CHAIRCD ";
        $query .= "             , RSD.SUBCLASSCD ";
        $query .= "             , RSD.CLASSCD ";
        $query .= "             , RSD.SCHOOL_KIND ";
        $query .= "             , SRD.COURSECD ";
        $query .= "             , SRD.MAJORCD ";
        $query .= "             , SRD.COURSECODE ";
        $query .= "             , MIN(SCORE) AS SCORE ";
        $query .= "          FROM RECORD_SCORE_DAT RSD ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT SRD ";
        $query .= "            ON RSD.YEAR        = SRD.YEAR ";
        $query .= "           AND RSD.SEMESTER    = SRD.SEMESTER ";
        $query .= "           AND RSD.SCHREGNO    = SRD.SCHREGNO ";
        $query .= "         WHERE RSD.YEAR            = '".CTRL_YEAR."' ";
        $query .= "           AND RSD.SEMESTER        = '".$model->field["SEMESTER"]."' ";
        $query .= "           AND    RSD.TESTKINDCD ";
        $query .= "               || RSD.TESTITEMCD ";
        $query .= "               || RSD.SCORE_DIV    = '".$model->field["TESTKINDCD"]."' ";
        $query .= "           AND RSD.SCHREGNO IN ";
        $query .= "               ( ";
        $query .= "                SELECT SCHREGNO ";
        $query .= "                  FROM SCHREG_REGD_DAT ";
        $query .= "                 WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "                   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                   AND GRADE || HR_CLASS IN ( ";
        $query .= "'".implode("','", $select)."')";
        $query .= "               ) ";
        $query .= "      GROUP BY RSD.YEAR ";
        $query .= "             , RSD.SEMESTER ";
        $query .= "             , SRD.GRADE ";
        $query .= "             , SRD.HR_CLASS ";
        $query .= "             , RSD.TESTKINDCD ";
        $query .= "             , RSD.TESTITEMCD ";
        $query .= "             , RSD.SCORE_DIV ";
        $query .= "             , RSD.CURRICULUM_CD ";
        $query .= "             , RSD.CHAIRCD ";
        $query .= "             , RSD.SUBCLASSCD ";
        $query .= "             , RSD.CLASSCD ";
        $query .= "             , RSD.SCHOOL_KIND ";
        $query .= "             , SRD.COURSECD ";
        $query .= "             , SRD.MAJORCD ";
        $query .= "             , SRD.COURSECODE ";
        $query .= ") ";
        $query .= "SELECT DISTINCT SUB_MIN_SCORE.CHAIRCD ";
        $query .= "     , SUB_MIN_SCORE.SUBCLASSCD ";
        $query .= "     , SUB.SUBCLASSABBV ";
        $query .= "  FROM SUB_MIN_SCORE ";
        $query .= "LEFT JOIN CHAIR_DETAIL_DAT CDD ";
        $query .= "       ON CDD.YEAR            = SUB_MIN_SCORE.YEAR ";
        $query .= "      AND CDD.SEMESTER        = SUB_MIN_SCORE.SEMESTER ";
        $query .= "      AND CDD.CHAIRCD         = SUB_MIN_SCORE.CHAIRCD ";
        $query .= "      AND CDD.SEQ             = '003' ";
        $query .= "LEFT JOIN SUBCLASS_MST SUB ";
        $query .= "       ON SUB.SUBCLASSCD      = SUB_MIN_SCORE.SUBCLASSCD ";
        $query .= "      AND SUB.CURRICULUM_CD   = SUB_MIN_SCORE.CURRICULUM_CD ";
        $query .= "LEFT JOIN SCHOOL_DETAIL_DAT SDD ";
        $query .= "       ON SDD.YEAR            = SUB_MIN_SCORE.YEAR ";
        $query .= "      AND SDD.SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "      AND SDD.SCHOOL_SEQ      = '009' ";
        $query .= "LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ";
        $query .= "       ON RASD.YEAR           = SUB_MIN_SCORE.YEAR ";
        $query .= "      AND RASD.SEMESTER       = SUB_MIN_SCORE.SEMESTER ";
        $query .= "      AND RASD.TESTKINDCD     = SUB_MIN_SCORE.TESTKINDCD ";
        $query .= "      AND RASD.TESTITEMCD     = SUB_MIN_SCORE.TESTITEMCD ";
        $query .= "      AND RASD.SCORE_DIV      = SUB_MIN_SCORE.SCORE_DIV ";
        $query .= "      AND RASD.CLASSCD        = SUB_MIN_SCORE.CLASSCD ";
        $query .= "      AND RASD.SCHOOL_KIND    = SUB_MIN_SCORE.SCHOOL_KIND ";
        $query .= "      AND RASD.CURRICULUM_CD  = SUB_MIN_SCORE.CURRICULUM_CD ";
        $query .= "      AND RASD.SUBCLASSCD     = SUB_MIN_SCORE.SUBCLASSCD ";
        $query .= "      AND RASD.AVG_DIV        = '6' ";
        $query .= "      AND RASD.GRADE          = SUB_MIN_SCORE.GRADE ";
        $query .= "      AND RASD.HR_CLASS       = SUB_MIN_SCORE.HR_CLASS ";
        $query .= "      AND RASD.COURSECD       = SUB_MIN_SCORE.COURSECD ";
        $query .= "      AND RASD.MAJORCD        = SUB_MIN_SCORE.MAJORCD ";
        $query .= "      AND RASD.COURSECODE     = SUB_MIN_SCORE.COURSECODE ";
        $query .= "    WHERE CASE SDD.SCHOOL_REMARK1 ";
        $query .= "               WHEN '1' THEN SUB_MIN_SCORE.SCORE <= CDD.REMARK1 ";
        $query .= "               WHEN '2' THEN SUB_MIN_SCORE.SCORE <= RASD.AVG * CDD.REMARK2 / CDD.REMARK3 ";
        $query .= "               ELSE( CASE WHEN RASD.SCORE_DIV = '09' THEN SUB_MIN_SCORE.SCORE <= 1 ";  //評定の場合、警告点は1
        $query .= "                          ELSE SUB_MIN_SCORE.SCORE <= (SELECT ASSESSHIGH FROM ASSESS_MST WHERE ASSESSCD = '2' AND ASSESSLEVEL = '1') ";
        $query .= "                     END) ";
        $query .= "          END ";
        $query .= "      AND SUB.SUBCLASSCD IS NOT NULL ";
        $query .= " ORDER BY SUB_MIN_SCORE.SUBCLASSCD ";

        return $query;
    }

    //生徒情報(欠点講座数等含む)を取得
    public function getStudentDate($model, $select)
    {
        $query .= "WITH MIJUKEN AS ( ";
        $query .= "              SELECT RSD.SCHREGNO ";
        $query .= "                   , COUNT(*) AS MIJUKEN_COUNT ";
        $query .= "                FROM RECORD_SCORE_DAT RSD ";
        $query .= "               WHERE RSD.YEAR             = '".CTRL_YEAR."' ";
        $query .= "                 AND RSD.SEMESTER         = '".$model->field["SEMESTER"]."' ";
        $query .= "                 AND    RSD.TESTKINDCD ";
        $query .= "                     || RSD.TESTITEMCD ";
        $query .= "                     || RSD.SCORE_DIV     = '".$model->field["TESTKINDCD"]."' ";
        $query .= "                 AND RSD.VALUE_DI         = '*' ";
        $query .= "            GROUP BY RSD.SCHREGNO ";
        $query .= "), KETTEN_KOUZA AS ( ";
        $query .= "              SELECT RSD.SCHREGNO ";
        $query .= "                   , COUNT(*) AS KETTEN_COUNT ";
        $query .= "                FROM RECORD_SCORE_DAT RSD ";
        $query .= "           LEFT JOIN CHAIR_DETAIL_DAT CDD ";
        $query .= "                  ON CDD.YEAR             = RSD.YEAR ";
        $query .= "                 AND CDD.SEMESTER         = RSD.SEMESTER ";
        $query .= "                 AND CDD.CHAIRCD          = RSD.CHAIRCD ";
        $query .= "                 AND CDD.SEQ              = '003' ";
        $query .= "           LEFT JOIN SCHREG_REGD_DAT SRD ";
        $query .= "                  ON SRD.YEAR             = RSD.YEAR ";
        $query .= "                 AND SRD.SEMESTER         = RSD.SEMESTER ";
        $query .= "                 AND SRD.SCHREGNO         = RSD.SCHREGNO ";
        $query .= "           LEFT JOIN SCHOOL_DETAIL_DAT SDD ";
        $query .= "                  ON SDD.YEAR            = SRD.YEAR ";
        $query .= "                 AND SDD.SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "                 AND SDD.SCHOOL_SEQ      = '009' ";
        $query .= "           LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ";
        $query .= "                  ON RASD.YEAR           = SRD.YEAR ";
        $query .= "                 AND RASD.SEMESTER       = SRD.SEMESTER ";
        $query .= "                 AND RASD.TESTKINDCD     = RSD.TESTKINDCD ";
        $query .= "                 AND RASD.TESTITEMCD     = RSD.TESTITEMCD ";
        $query .= "                 AND RASD.SCORE_DIV      = RSD.SCORE_DIV ";
        $query .= "                 AND RASD.CLASSCD        = RSD.CLASSCD ";
        $query .= "                 AND RASD.SCHOOL_KIND    = RSD.SCHOOL_KIND ";
        $query .= "                 AND RASD.CURRICULUM_CD  = RSD.CURRICULUM_CD ";
        $query .= "                 AND RASD.SUBCLASSCD     = RSD.SUBCLASSCD ";
        $query .= "                 AND RASD.AVG_DIV        = '6' ";
        $query .= "                 AND RASD.GRADE          = SRD.GRADE ";
        $query .= "                 AND RASD.HR_CLASS       = SRD.HR_CLASS ";
        $query .= "                 AND RASD.COURSECD       = SRD.COURSECD ";
        $query .= "                 AND RASD.MAJORCD        = SRD.MAJORCD ";
        $query .= "                 AND RASD.COURSECODE     = SRD.COURSECODE ";
        $query .= "               WHERE RSD.YEAR             = '".CTRL_YEAR."' ";
        $query .= "                 AND RSD.SEMESTER         = '".$model->field["SEMESTER"]."' ";
        $query .= "                 AND    RSD.TESTKINDCD ";
        $query .= "                     || RSD.TESTITEMCD ";
        $query .= "                     || RSD.SCORE_DIV     = '".$model->field["TESTKINDCD"]."' ";
        $query .= "                 AND CASE SDD.SCHOOL_REMARK1 ";
        $query .= "                     WHEN '1' THEN RSD.SCORE <= CDD.REMARK1 ";
        $query .= "                     WHEN '2' THEN RSD.SCORE <= RASD.AVG * CDD.REMARK2 / CDD.REMARK3 ";
        $query .= "                     ELSE( CASE WHEN RSD.SCORE_DIV = '09' THEN RSD.SCORE <= 1  ";  //評定の場合、警告点は1
        $query .= "                                ELSE RSD.SCORE <= (SELECT ASSESSHIGH FROM ASSESS_MST WHERE ASSESSCD = '2' AND ASSESSLEVEL = '1')  ";
        $query .= "                           END) ";
        $query .= "                     END ";
        $query .= "            GROUP BY RSD.SCHREGNO ";
        $query .= "), KETTEN_CREDITS AS ( ";
        $query .= "               SELECT RSD.SCHREGNO ";
        $query .= "                    , SUM(CRM.CREDITS) AS CREDITS_TOTAL ";
        $query .= "                 FROM RECORD_SCORE_DAT RSD ";
        $query .= "            LEFT JOIN CHAIR_DETAIL_DAT CDD ";
        $query .= "                   ON CDD.YEAR            = RSD.YEAR ";
        $query .= "                  AND CDD.SEMESTER        = RSD.SEMESTER ";
        $query .= "                  AND CDD.CHAIRCD         = RSD.CHAIRCD ";
        $query .= "                  AND CDD.SEQ             = '003' ";
        $query .= "            LEFT JOIN SCHREG_REGD_DAT SRD ";
        $query .= "                   ON SRD.SCHREGNO        = RSD.SCHREGNO ";
        $query .= "                  AND SRD.YEAR            = RSD.YEAR ";
        $query .= "                  AND SRD.SEMESTER = RSD.SEMESTER ";
        $query .= "            LEFT JOIN CREDIT_MST CRM ";
        $query .= "                   ON CRM.YEAR            = RSD.YEAR ";
        $query .= "                  AND CRM.COURSECD        = SRD.COURSECD ";
        $query .= "                  AND CRM.MAJORCD         = SRD.MAJORCD ";
        $query .= "                  AND CRM.GRADE           = SRD.GRADE ";
        $query .= "                  AND CRM.COURSECODE      = SRD.COURSECODE ";
        $query .= "                  AND CRM.CLASSCD         = RSD.CLASSCD ";
        $query .= "                  AND CRM.SCHOOL_KIND     = RSD.SCHOOL_KIND ";
        $query .= "                  AND CRM.CURRICULUM_CD   = RSD.CURRICULUM_CD ";
        $query .= "                  AND CRM.SUBCLASSCD      = RSD.SUBCLASSCD ";
        $query .= "            LEFT JOIN SCHOOL_DETAIL_DAT SDD ";
        $query .= "                   ON SDD.YEAR            = SRD.YEAR ";
        $query .= "                  AND SDD.SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "                  AND SDD.SCHOOL_SEQ      = '009' ";
        $query .= "            LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ";
        $query .= "                   ON RASD.YEAR           = SRD.YEAR ";
        $query .= "                  AND RASD.SEMESTER       = SRD.SEMESTER ";
        $query .= "                  AND RASD.TESTKINDCD     = RSD.TESTKINDCD ";
        $query .= "                  AND RASD.TESTITEMCD     = RSD.TESTITEMCD ";
        $query .= "                  AND RASD.SCORE_DIV      = RSD.SCORE_DIV ";
        $query .= "                  AND RASD.CLASSCD        = RSD.CLASSCD ";
        $query .= "                  AND RASD.SCHOOL_KIND    = RSD.SCHOOL_KIND ";
        $query .= "                  AND RASD.CURRICULUM_CD  = RSD.CURRICULUM_CD ";
        $query .= "                  AND RASD.SUBCLASSCD     = RSD.SUBCLASSCD ";
        $query .= "                  AND RASD.AVG_DIV        = '6' ";
        $query .= "                  AND RASD.GRADE          = SRD.GRADE ";
        $query .= "                  AND RASD.HR_CLASS       = SRD.HR_CLASS ";
        $query .= "                  AND RASD.COURSECD       = SRD.COURSECD ";
        $query .= "                  AND RASD.MAJORCD        = SRD.MAJORCD ";
        $query .= "                  AND RASD.COURSECODE     = SRD.COURSECODE ";
        $query .= "                WHERE RSD.YEAR            = '".CTRL_YEAR."' ";
        $query .= "                  AND RSD.SEMESTER        = '".$model->field["SEMESTER"]."' ";
        $query .= "                  AND    RSD.TESTKINDCD ";
        $query .= "                      || RSD.TESTITEMCD ";
        $query .= "                      || RSD.SCORE_DIV    = '".$model->field["TESTKINDCD"]."' ";
        $query .= "                  AND CASE SDD.SCHOOL_REMARK1 ";
        $query .= "                      WHEN '1' THEN RSD.SCORE <= CDD.REMARK1 ";
        $query .= "                      WHEN '2' THEN RSD.SCORE <= RASD.AVG * CDD.REMARK2 / CDD.REMARK3 ";
        $query .= "                      ELSE( CASE WHEN RSD.SCORE_DIV = '09' THEN RSD.SCORE <= 1  ";  //評定の場合、警告点は1
        $query .= "                                 ELSE RSD.SCORE <= (SELECT ASSESSHIGH FROM ASSESS_MST WHERE ASSESSCD = '2' AND ASSESSLEVEL = '1')  ";
        $query .= "                            END) ";
        $query .= "                      END ";
        $query .= "             GROUP BY RSD.SCHREGNO ";
        $query .= ") ";
        $query .= "   SELECT SRH.GRADE ";
        $query .= "        , SRH.HR_CLASS_NAME1 ";
        $query .= "        , SRD.ATTENDNO ";
        $query .= "        , SBM.NAME ";
        $query .= "        , SBM.SCHREGNO ";
        $query .= "        , MIJUKEN.MIJUKEN_COUNT ";
        $query .= "        , KETTEN_KOUZA.KETTEN_COUNT ";
        $query .= "        , KETTEN_CREDITS.CREDITS_TOTAL ";
        $query .= "     FROM SCHREG_REGD_DAT SRD ";
        $query .= "LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .= "       ON SRH.YEAR                        = SRD.YEAR ";
        $query .= "      AND SRH.SEMESTER                    = SRD.SEMESTER ";
        $query .= "      AND SRH.GRADE                       = SRD.GRADE ";
        $query .= "      AND SRH.HR_CLASS                    = SRD.HR_CLASS ";
        $query .= "LEFT JOIN SCHREG_BASE_MST SBM ";
        $query .= "       ON SBM.SCHREGNO                    = SRD.SCHREGNO ";
        $query .= "LEFT JOIN MIJUKEN ";
        $query .= "       ON MIJUKEN.SCHREGNO                = SRD.SCHREGNO ";
        $query .= "LEFT JOIN KETTEN_KOUZA ";
        $query .= "       ON KETTEN_KOUZA.SCHREGNO           = SRD.SCHREGNO ";
        $query .= "LEFT JOIN KETTEN_CREDITS ";
        $query .= "       ON KETTEN_CREDITS.SCHREGNO         = SRD.SCHREGNO ";
        $query .= "    WHERE SRD.YEAR                        = '".CTRL_YEAR."' ";
        $query .= "      AND SRD.SEMESTER                    = '".$model->field["SEMESTER"]."' ";
        $query .= "      AND KETTEN_KOUZA.KETTEN_COUNT IS NOT NULL ";
        $query .= "      AND SRD.GRADE || SRD.HR_CLASS IN ( ";
        $query .= "'".implode("','", $select)."')";
        $query .= " ORDER BY SRH.GRADE ";
        $query .= "        , SRH.HR_CLASS_NAME1 ";
        $query .= "        , SRD.ATTENDNO ";

        return $query;
    }

    //生徒の科目ごとの点数を取得
    public function getSubclassScore($model, $select)
    {
        $query  = "   SELECT SRD.SCHREGNO ";
        $query .= "        , RSD.SUBCLASSCD ";
        $query .= "        , RSD.SCORE ";
        $query .= "     FROM SCHREG_REGD_DAT  SRD ";
        $query .= "LEFT JOIN RECORD_SCORE_DAT RSD ";
        $query .= "       ON RSD.YEAR             = SRD.YEAR ";
        $query .= "      AND RSD.SEMESTER         = SRD.SEMESTER ";
        $query .= "      AND RSD.SCHREGNO         = SRD.SCHREGNO ";
        $query .= "    WHERE SRD.YEAR             = '".CTRL_YEAR."' ";
        $query .= "      AND SRD.SEMESTER         = '".$model->field["SEMESTER"]."' ";
        $query .= "      AND    RSD.TESTKINDCD  ";
        $query .= "          || RSD.TESTITEMCD  ";
        $query .= "          || RSD.SCORE_DIV     = '".$model->field["TESTKINDCD"]."' ";
        $query .= "      AND SRD.GRADE || SRD.HR_CLASS IN ( ";
        $query .= "'".implode("','", $select)."')";
        $query .= " ORDER BY SRD.SCHREGNO ";
        $query .= "        , RSD.SUBCLASSCD ";

        return $query;
    }
}
