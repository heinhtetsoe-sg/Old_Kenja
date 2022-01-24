<?php

require_once('for_php7.php');

class knja611vQuery extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //学期取得
    function getSemester($semester="") {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($semester) {
            $query .= " AND SEMESTER = '".$semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getTestItem($model, $div="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.TESTITEMNAME AS LABEL, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     {$model->tableName} T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["GAKKI"]."' ";
        $query .= "     AND T1.JYORETSU_FLG = '1' ";    //1:序列対象
        if ($div == "TESTNAME") {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "   AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "   AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["GAKKI"] != "9") {
            $query .= " AND T1.SEMESTER = '".$model->field["GAKKI"]."' ";
        } else {
            $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND '".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3, T1.SUBTR_CD1, T1.SUBTR_CD2, T1.SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSelectSubClass($model) {
        $semester = ($model->field["GAKKI"] == "9") ? CTRL_SEMESTER : $model->field["GAKKI"];

        $query  = " WITH SCH_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".$semester."' AND ";
        $query .= "         GRADE || HR_CLASS IN ('".implode($model->selectdata,"','")."') ";
        $query .= " ), SUBCLASS_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         RECORD_RANK_SDIV_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".$model->field["GAKKI"]."' AND ";
        $query .= "         TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTKINDCD"]."' AND ";
        $query .= "         EXISTS (SELECT 'X' FROM SCH_T S1 WHERE T1.SCHREGNO = S1.SCHREGNO) ";
        if ($model->field["KESSEKI_FLG"] == '1') {
            $query .= "     UNION ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD ";
            $query .= "     FROM ";
            $query .= "         RECORD_SCORE_DAT T1 ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "         SEMESTER    = '".$model->field["GAKKI"]."' AND ";
            $query .= "         TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTKINDCD"]."' AND ";
            $query .= "         EXISTS (SELECT 'X' FROM SCH_T S1 WHERE T1.SCHREGNO = S1.SCHREGNO) AND ";
            $query .= "         VALUE_DI    = '*' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_T T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST T2 ";
        $query .= "          ON T1.CLASSCD          = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //CVS作成用のQUERY（テスト毎）
    function selectCsvQuery($model) {
        $semester = ($model->field["GAKKI"] == "9") ? CTRL_SEMESTER : $model->field["GAKKI"];
        $testcd = $model->field["GAKKI"].$model->field["TESTKINDCD"];

        //欠席者「*」
        $kessekiQuery = "";
        if ($model->field["KESSEKI_FLG"] == '1') {
            //「*」を印字する
            $kessekiQuery = " (CASE WHEN L1.SCORE IS NOT NULL THEN L1.SCORE WHEN L2.VALUE_DI = '*' THEN 7777 ELSE NULL END) ";
        } else {
            //「*」を印字しない
            $kessekiQuery = " L1.SCORE ";
        }

        $query  = " WITH SCH_T AS ( ";
        $query .= "     SELECT ";
        $query .= "        T1.*, ";
        $query .= "        T2.NAME_SHOW, ";
        $query .= "        T3.SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS IN ('".implode($model->selectdata,"','")."') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     M1.YEAR, ";
        $query .= "     M1.SCHREGNO, ";
        $query .= "     M1.GRADE, ";
        $query .= "     M1.HR_CLASS, ";
        $query .= "     M1.ATTENDNO, ";
        $query .= "     M1.NAME_SHOW, ";
        for ($i=0; $i < get_count($model->opt_subclass); $i++) {
            $query .= "     SUM(CASE M2.CLASSCD || '-' || M2.SCHOOL_KIND || '-' || M2.CURRICULUM_CD || '-' || M2.SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN ".$kessekiQuery." ELSE NULL END) AS SCORE{$i}, ";
            $query .= "     SUM(CASE M2.CLASSCD || '-' || M2.SCHOOL_KIND || '-' || M2.CURRICULUM_CD || '-' || M2.SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN DECIMAL(ROUND(L3.AVG * 10, 0) / 10, 5, 1) ELSE NULL END) AS AVG{$i}, ";
            $query .= "     SUM(CASE M2.CLASSCD || '-' || M2.SCHOOL_KIND || '-' || M2.CURRICULUM_CD || '-' || M2.SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN L1.COURSE_AVG_RANK ELSE NULL END) AS RANK{$i}, ";
            $query .= "     SUM(CASE M2.CLASSCD || '-' || M2.SCHOOL_KIND || '-' || M2.CURRICULUM_CD || '-' || M2.SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN L3.COUNT ELSE NULL END) AS CNT{$i}, ";
        }
        $query .= "     DECIMAL(ROUND(R1.AVG * 10, 0) / 10, 5, 1) AS COURSE_AVG, ";
        $query .= "     R1.COURSE_AVG_RANK AS COURSE_RANK, ";
        $query .= "     R2.COUNT AS COURSE_CNT ";
        $query .= " FROM ";
        $query .= "     SCH_T M1 ";
        $query .= "     INNER JOIN V_SUBCLASS_MST M2 ";
        $query .= "          ON M1.YEAR = M2.YEAR ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT L1 ";
        $query .= "          ON L1.YEAR             = M2.YEAR ";
        $query .= "         AND L1.CLASSCD          = M2.CLASSCD ";
        $query .= "         AND L1.SCHOOL_KIND      = M2.SCHOOL_KIND ";
        $query .= "         AND L1.CURRICULUM_CD    = M2.CURRICULUM_CD ";
        $query .= "         AND L1.SUBCLASSCD       = M2.SUBCLASSCD ";
        $query .= "         AND L1.SCHREGNO         = M1.SCHREGNO ";
        $query .= "         AND L1.SEMESTER || L1.TESTKINDCD || L1.TESTITEMCD || L1.SCORE_DIV = '".$testcd."' ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT L2 ";
        $query .= "          ON L2.YEAR             = M2.YEAR ";
        $query .= "         AND L2.CLASSCD          = M2.CLASSCD ";
        $query .= "         AND L2.SCHOOL_KIND      = M2.SCHOOL_KIND ";
        $query .= "         AND L2.CURRICULUM_CD    = M2.CURRICULUM_CD ";
        $query .= "         AND L2.SUBCLASSCD       = M2.SUBCLASSCD ";
        $query .= "         AND L2.SCHREGNO         = M1.SCHREGNO ";
        $query .= "         AND L2.SEMESTER || L2.TESTKINDCD || L2.TESTITEMCD || L2.SCORE_DIV = '".$testcd."' ";
        $query .= "     LEFT JOIN RECORD_AVERAGE_SDIV_DAT L3 ";
        $query .= "          ON L3.YEAR             = M2.YEAR ";
        $query .= "         AND L3.CLASSCD          = M2.CLASSCD ";
        $query .= "         AND L3.SCHOOL_KIND      = M2.SCHOOL_KIND ";
        $query .= "         AND L3.CURRICULUM_CD    = M2.CURRICULUM_CD ";
        $query .= "         AND L3.SUBCLASSCD       = M2.SUBCLASSCD ";
        $query .= "         AND L3.SEMESTER || L3.TESTKINDCD || L3.TESTITEMCD || L3.SCORE_DIV = '".$testcd."' ";
        $query .= "         AND L3.AVG_DIV          = '3' ";
        $query .= "         AND L3.GRADE            = M1.GRADE ";
        $query .= "         AND L3.HR_CLASS         = '000' ";
        $query .= "         AND L3.COURSECD         = M1.COURSECD ";
        $query .= "         AND L3.MAJORCD          = M1.MAJORCD ";
        $query .= "         AND L3.COURSECODE       = M1.COURSECODE ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT R1 ";
        $query .= "          ON R1.YEAR             = M2.YEAR ";
        $query .= "         AND R1.CLASSCD          = '99' ";
        $query .= "         AND R1.SCHOOL_KIND      = M1.SCHOOL_KIND ";
        $query .= "         AND R1.CURRICULUM_CD    = '99' ";
        $query .= "         AND R1.SUBCLASSCD       = '999999' ";
        $query .= "         AND R1.SCHREGNO         = M1.SCHREGNO ";
        $query .= "         AND R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '".$testcd."' ";
        $query .= "     LEFT JOIN RECORD_AVERAGE_SDIV_DAT R2 ";
        $query .= "          ON R2.YEAR             = M2.YEAR ";
        $query .= "         AND R2.CLASSCD          = '99' ";
        $query .= "         AND R2.SCHOOL_KIND      = M1.SCHOOL_KIND ";
        $query .= "         AND R2.CURRICULUM_CD    = '99' ";
        $query .= "         AND R2.SUBCLASSCD       = '999999' ";
        $query .= "         AND R2.SEMESTER || R2.TESTKINDCD || R2.TESTITEMCD || R2.SCORE_DIV = '".$testcd."' ";
        $query .= "         AND R2.AVG_DIV          = '3' ";
        $query .= "         AND R2.GRADE            = M1.GRADE ";
        $query .= "         AND R2.HR_CLASS         = '000' ";
        $query .= "         AND R2.COURSECD         = M1.COURSECD ";
        $query .= "         AND R2.MAJORCD          = M1.MAJORCD ";
        $query .= "         AND R2.COURSECODE       = M1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     M2.CLASSCD || '-' || M2.SCHOOL_KIND || '-' || M2.CURRICULUM_CD || '-' || M2.SUBCLASSCD IN ('".implode($model->opt_subclass,"','")."') ";
        $query .= " GROUP BY ";
        $query .= "     M1.YEAR, ";
        $query .= "     M1.SCHREGNO, ";
        $query .= "     M1.GRADE, ";
        $query .= "     M1.HR_CLASS, ";
        $query .= "     M1.ATTENDNO, ";
        $query .= "     M1.NAME_SHOW, ";
        $query .= "     R1.AVG, ";
        $query .= "     R1.COURSE_AVG_RANK, ";
        $query .= "     R2.COUNT ";
        $query .= " ORDER BY ";
        $query .= "     M1.GRADE, ";
        $query .= "     M1.HR_CLASS, ";
        $query .= "     M1.ATTENDNO ";

        return $query;
    }
}
?>
