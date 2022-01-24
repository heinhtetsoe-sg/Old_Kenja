<?php

require_once('for_php7.php');


class knje366bQuery extends Query {
    //年度取得
    function getYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR AS VALUE ";
        $query .= "   , T1.YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT T2 ON  T1.YEAR     = T2.YEAR ";
        $query .= "                         AND T1.GRADE    = T2.GRADE ";
        $query .= "                         AND T2.SCHOOL_KIND IN ('H') ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //MAX学期取得
    function getMaxSemester($model) {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " AND SEMESTER <> '9' ";

        return $query;
    }

    //業者情報
    function getCompany() {
        $query  = " SELECT ";
        $query .= "     T1.NAMESPARE1 AS VALUE ";
        $query .= "   , T1.NAMESPARE1 || ':' || T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'H313' ";

        return $query;
    }
    //模試情報
    function getBeneMock($model, $grade) {
        $compCd = substr($model->field["COMPANYCD"], -1);
        $gradeCd = substr($grade, -1);
        $mockCd = "1{$model->field["YEAR"]}{$compCd}{$gradeCd}%";

        $query  = " SELECT ";
        $query .= "     T1.MOCKCD AS VALUE ";
        $query .= "   , T1.MOCKCD || ':' || T1.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.COMPANYCD = '{$model->field["COMPANYCD"]}' ";
        $query .= "     AND T1.MOCKCD LIKE '{$mockCd}' ";


        return $query;
    }

    //既卒生有無チェック
    function checkGradCnt($model, $semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(T1.SCHREGNO) ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " AND NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT E1 ";
        $query .= "                 WHERE ";
        $query .= "                     E1.YEAR     = T1.YEAR ";
        $query .= "                 AND E1.SEMESTER = '".$semester."' ";
        $query .= "                 AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                 ) ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $semester, $kisotsu) {
        $query  = " SELECT ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= "   , T1.GRADE || '-' || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON  T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= "     AND T2.SCHOOL_KIND IN ('H') ";
        $query .= " INNER JOIN V_NAME_MST T3 ";
        $query .= "     ON  T1.YEAR = T3.YEAR ";
        $query .= "     AND T3.NAMECD1 = 'A023' ";
        $query .= "     AND T3.NAME1 IN ('H') ";
        $query .= "     AND T1.GRADE BETWEEN T3.NAMESPARE2 AND T3.NAMESPARE3 ";
        $query .= " WHERE T1.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "   AND T1.SEMESTER = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
        } else if ($kisotsu > 0) {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     '既卒生' AS LABEL ";
            $query .= "   , 'ZZ-ZZZ' AS VALUE ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象（合否・進路先）取得
    function getGouhi($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD1 || '-' || NAMECD2 AS VALUE ";
        $query .= "   , NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E005' ";

        $query .= " UNION ALL ";

        $query .= " SELECT ";
        $query .= "     NAMECD1 || '-' || NAMECD2 AS VALUE ";
        $query .= "   , NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E006' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種別（設置区分）取得
    function getKubun() {
        $query  = " SELECT ";
        $query .= "     NAMECD1 || '-' || NAMECD2 AS VALUE ";
        $query .= "   , NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 IN ('E012') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ
    function getNameMst($model, $namecd1, $name1) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= "   , NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        if ($name1) {
            $query .= " AND NAME1 = '{$name1}' ";
        }

        return $query;
    }

    //名称マスタ
    function getNameMst2($namecd1, $namespare = "") {
        $query  = " SELECT ";
        $query .= "     NAME1      AS LABEL ";
        $query .= "   , NAMECD2    AS VALUE ";
        $query .= "   , NAMESPARE2 AS NAMESPARE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namespare != ""){
            $query .= "     AND NAMESPARE2 = '".$namespare."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //NAMESPARE取得
    function getNamespare($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMESPARE2 IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     NAMESPARE2 ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE2 ";
        return $query;
    }

    //進学
    function getCsvQuery($model, $hrClassList, $decisionList, $planstat, $typeList) {
        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "       , T1.GRADE ";
        $query .= "       , T1.HR_CLASS ";
        $query .= "       , T1.ATTENDNO ";
        $query .= "       , S2.HR_NAME ";
        $query .= "       , S1.NAME ";
        $query .= "       , S1.NAME_KANA ";
        $query .= "       , S1.SEX ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT S2 ";
        $query .= "             ON S2.YEAR = T1.YEAR ";
        $query .= "             AND S2.SEMESTER = T1.SEMESTER ";
        $query .= "             AND S2.GRADE    = T1.GRADE ";
        $query .= "             AND S2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '{$model->field['YEAR']}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field['SEMESTER']}' ";
        $values = array();
        for ($i=0; $i < get_count($hrClassList); $i++) { 
            list($grade, $hrClass) = explode('-', $hrClassList[$i]);
            $values[] = " VALUES('{$grade}', '{$hrClass}') ";
        }
        $query .= "     AND (T1.GRADE, T1.HR_CLASS) IN (". implode(' UNION ', $values) .") ";

        $query .= "     UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "       , 'ZZ-' || VALUE(FISCALYEAR(CASE WHEN S2.GRD_DATE IS NOT NULL THEN S2.GRD_DATE ELSE S1.GRD_DATE END), '') || '-' || VALUE(S2.GRD_GRADE, '') AS GRADE ";
        $query .= "       , 'ZZZ-' || VALUE(S2.GRD_HR_CLASS,'') AS HR_CLASS ";
        $query .= "       , 'ZZZ-' || VALUE(S2.GRD_ATTENDNO,'') AS ATTENDNO ";
        $query .= "       , '既卒生' AS HR_NAME ";
        $query .= "       , S1.NAME ";
        $query .= "       , S1.NAME_KANA ";
        $query .= "       , S1.SEX ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN GRD_BASE_MST S2 ON S2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->field['YEAR']}' ";
        $query .= "     AND NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT E1 ";
        $query .= "                     WHERE ";
        $query .= "                         E1.YEAR     = T1.YEAR ";
        $query .= "                     AND E1.SEMESTER = '{$model->field['SEMESTER']}' ";
        $query .= "                     AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                 ) ";
        $query .= "     AND ('ZZ', 'ZZZ') IN (". implode(' UNION ', $values) .") ";
        $query .= " ) ";


        if ($model->field['OUT_DIV'] == '2') {
            $rank = array();
            $rank[] = array("TABLENAME" => "RANK1M_E", "COLNAME" => "AVG", "SEMESTER" => "1", "TESTKINDCD" => "01", "TESTITEMCD" => "01", "SCORE_DIV" => "08"); //１学期中間 評価
            $rank[] = array("TABLENAME" => "RANK1M_R", "COLNAME" => "AVG", "SEMESTER" => "1", "TESTKINDCD" => "01", "TESTITEMCD" => "01", "SCORE_DIV" => "09"); //１学期中間 評定
            $rank[] = array("TABLENAME" => "RANK1E_E", "COLNAME" => "AVG", "SEMESTER" => "1", "TESTKINDCD" => "02", "TESTITEMCD" => "01", "SCORE_DIV" => "08"); //１学期期末 評価
            $rank[] = array("TABLENAME" => "RANK1E_R", "COLNAME" => "AVG", "SEMESTER" => "1", "TESTKINDCD" => "02", "TESTITEMCD" => "01", "SCORE_DIV" => "09"); //１学期期末 評定

            $rank[] = array("TABLENAME" => "RANK2M_E", "COLNAME" => "AVG", "SEMESTER" => "2", "TESTKINDCD" => "01", "TESTITEMCD" => "01", "SCORE_DIV" => "08"); //２学期中間 評価
            $rank[] = array("TABLENAME" => "RANK2M_R", "COLNAME" => "AVG", "SEMESTER" => "2", "TESTKINDCD" => "01", "TESTITEMCD" => "01", "SCORE_DIV" => "09"); //２学期中間 評定
            $rank[] = array("TABLENAME" => "RANK2E_E", "COLNAME" => "AVG", "SEMESTER" => "2", "TESTKINDCD" => "02", "TESTITEMCD" => "01", "SCORE_DIV" => "08"); //２学期期末 評価
            $rank[] = array("TABLENAME" => "RANK2E_R", "COLNAME" => "AVG", "SEMESTER" => "2", "TESTKINDCD" => "02", "TESTITEMCD" => "01", "SCORE_DIV" => "09"); //２学期期末 評定

            $rank[] = array("TABLENAME" => "RANK3E_E", "COLNAME" => "AVG", "SEMESTER" => "3", "TESTKINDCD" => "02", "TESTITEMCD" => "01", "SCORE_DIV" => "08"); //学年末 評価
            $rank[] = array("TABLENAME" => "RANK3E_R", "COLNAME" => "AVG", "SEMESTER" => "3", "TESTKINDCD" => "02", "TESTITEMCD" => "01", "SCORE_DIV" => "09"); //学年末 評定

            $rank[] = array("TABLENAME" => "RANK9E_E", "COLNAME" => "AVG", "SEMESTER" => "9", "TESTKINDCD" => "99", "TESTITEMCD" => "00", "SCORE_DIV" => "08"); //総合成績
            $rank[] = array("TABLENAME" => "RANK9E_R", "COLNAME" => "GRADE_AVG_RANK", "SEMESTER" => "9", "TESTKINDCD" => "99", "TESTITEMCD" => "00", "SCORE_DIV" => "09"); //学年順位


            for ($i=0; $i < get_count($rank); $i++) { 
                $table = $rank[$i];

                $query .= ", {$table["TABLENAME"]} AS (";
                $query .= "     SELECT ";
                $query .= "         T1.SCHREGNO ";
                $query .= "       , T1.{$table["COLNAME"]} ";
                $query .= "     FROM ";
                $query .= "         RECORD_RANK_SDIV_DAT T1 ";
                $query .= "     WHERE ";
                $query .= "         T1.YEAR     = '{$model->field['YEAR']}' ";
                $query .= "     AND T1.SEMESTER = '{$table["SEMESTER"]}' ";
                $query .= "     AND T1.TESTKINDCD = '{$table["TESTKINDCD"]}' ";
                $query .= "     AND T1.TESTITEMCD = '{$table["TESTITEMCD"]}' ";
                $query .= "     AND T1.SCORE_DIV = '{$table["SCORE_DIV"]}' ";
                $query .= "     AND T1.CLASSCD = '99' ";
                $query .= "     AND T1.SCHOOL_KIND = 'H' ";
                $query .= "     AND T1.CURRICULUM_CD = '99' ";
                $query .= "     AND T1.SUBCLASSCD = '999999' ";
    
                $query .= " ) ";
            }

        }

        $query .= " SELECT ";
        $query .= "     T1.SEQ ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , I1.GRADE ";
        $query .= "   , I1.HR_CLASS ";
        $query .= "   , I1.ATTENDNO ";
        $query .= "   , I1.HR_NAME ";
        $query .= "   , I1.HR_NAME || '-' || I1.ATTENDNO HR_NAME_ATTENDNO ";
        $query .= "   , I1.NAME ";
        $query .= "   , T1.SENKOU_KIND ";
        $query .= "   , T1.STAT_CD ";
        $query .= "   , L1.SCHOOL_NAME_SHOW1 AS STAT_NAME ";
        $query .= "   , T1.SCHOOL_GROUP ";
        $query .= "   , E012.NAME1 AS SCHOOL_GROUP_NAME ";
        $query .= "   , T1.FACULTYCD ";
        $query .= "   , L2.FACULTYNAME ";
        $query .= "   , T1.DEPARTMENTCD ";
        $query .= "   , L3.DEPARTMENTNAME ";
        $query .= "   , T1.HOWTOEXAM ";
        $query .= "   , E002.NAME1 AS HOWTOEXAM_NAME ";
        $query .= "   , T1.DECISION ";
        $query .= "   , E005.NAME1 AS DECISION_NAME ";
        $query .= "   , T1.PLANSTAT ";
        $query .= "   , E006.NAME1 AS PLANSTAT_NAME ";
        $query .= "   , AFT_GRAD_D.REMARK9 AS EXAMNO ";

        if ($model->field['OUT_DIV'] == '2') {
            for ($i=0; $i < get_count($rank); $i++) {
                $table = $rank[$i];
                $query .= "   , {$table["TABLENAME"]}.{$table["COLNAME"]} AS {$table["TABLENAME"]} ";
            }
        }

        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG I1 ON I1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ";
        $query .= "         ON T1.YEAR = AFT_GRAD_D.YEAR ";
        $query .= "         AND T1.SEQ = AFT_GRAD_D.SEQ ";
        $query .= "         AND AFT_GRAD_D.DETAIL_SEQ = 1 ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST L2 ";
        $query .= "         ON L2.SCHOOL_CD = T1.STAT_CD ";
        $query .= "         AND L2.FACULTYCD = T1.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ";
        $query .= "         ON L3.SCHOOL_CD = T1.STAT_CD ";
        $query .= "         AND L3.FACULTYCD = T1.FACULTYCD ";
        $query .= "         AND L3.DEPARTMENTCD = T1.DEPARTMENTCD ";
        $query .= "     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ";
        $query .= "     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ";
        $query .= "     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ";
        $query .= "     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ";
        $query .= "     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = T1.SCHOOL_GROUP ";

        if ($model->field['OUT_DIV'] == '2') {
            for ($i=0; $i < get_count($rank); $i++) {
                $table = $rank[$i];
                $query .= "     LEFT JOIN {$table["TABLENAME"]} ON {$table["TABLENAME"]}.SCHREGNO = T1.SCHREGNO ";
            }

        }

        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$model->field['YEAR']}' ";
        $query .= "     AND T1.SENKOU_KIND  = '0' ";

        //対象（合否・進路状況）
        if (get_count($decisionList) > 0) {
            $values = array();
            for ($i=0; $i < get_count($decisionList); $i++) { 
                $values[] = " '{$decisionList[$i]}' ";
            }
            $query .= "     AND T1.DECISION IN (". implode(',', $values) .") ";
        }
        if (get_count($planstat) > 0) {
            $values = array();
            for ($i=0; $i < get_count($planstat); $i++) { 
                $values[] = " '{$planstat[$i]}' ";
            }
            $query .= "     AND T1.PLANSTAT IN (". implode(',', $values) .") ";
        }

        //種別（設置区分）
        if (get_count($typeList) > 0) {
            $values = array();
            for ($i=0; $i < get_count($typeList); $i++) { 
                list($nameCd1, $schoolGroup) = explode('-', $typeList[$i]);
                $values[] = " '{$schoolGroup}' ";
            }
            $query .= "     AND T1.SCHOOL_GROUP IN (". implode(',', $values) .") ";
        }

        $query .= " ORDER BY ";
        $query .= "     T1.STAT_CD ";
        $query .= "   , T1.FACULTYCD ";
        $query .= "   , T1.DEPARTMENTCD ";
        $query .= "   , I1.GRADE ";
        $query .= "   , I1.HR_CLASS ";
        $query .= "   , I1.ATTENDNO ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T1.SEQ ";

        return $query;
    }

    //模試科目最大数取得
    function getMockSubclassCnt($model, $grade) {

        $query  = " SELECT ";
        $query .= "     FIELD_CNT ";
        $query .= " FROM ";
        $query .= "   MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "   AND GRADE = '{$grade}' ";

        return $query;
    }

    //模試名称取得
    function getMockKyouzaiName($model, $kyouzaiCd) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     KYOUZAINAME ";
        $query .= " FROM ";
        $query .= "   MOCK_CSV_BENE_SCORE_HDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND KYOUZAICD = '{$kyouzaiCd}' ";
        $query .= "   AND MOCKCD = '{$model->field["MOCKCD"]}' ";

        return $query;
    }

    //模試情報取得
    function getMockRankRangeDat($model, $schregNo) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.MOCKCD ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T1.MOCK_SUBCLASS_CD ";
        $query .= "   , T2.SUBCLASS_ABBV MOCK_SUBCLASS_NAME ";
        $query .= "   , T1.SCORE ";
        $query .= "   , T1.GTZ ";
        $query .= " FROM ";
        $query .= "   MOCK_RANK_RANGE_DAT T1 ";
        $query .= "   LEFT JOIN MOCK_SUBCLASS_MST T2 ";
        $query .= "     ON T1.MOCK_SUBCLASS_CD = T2.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "   AND T1.RANK_DIV = '02' ";
        $query .= "   AND T1.MOCKDIV = '1' ";
        $query .= "   AND T1.RANK_RANGE = '2' ";
        $query .= "   AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= " ORDER BY MOCK_SUBCLASS_CD ";

        return $query;
    }

}
?>
