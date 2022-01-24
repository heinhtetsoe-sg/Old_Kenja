<?php
class knjd219kQuery extends Query {

    //学期コンボ
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種コンボ
    function getSchoolKind($model, $year, $ncd1, $ncd2="") {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$ncd1}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年コンボ
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースコンボ
    function getCoursecode($model) {
        $query  = " SELECT ";
        $query .= "     COURSECODE || ':' || COURSECODENAME AS LABEL, ";
        $query .= "     COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND COURSECODE IN ( ";
        $query .= "         SELECT ";
        $query .= "             COURSECODE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$model->year}' ";
        $query .= "             AND SEMESTER = '{$model->field["SEMESTER_SCH"]}' ";
        if ($model->field["GRADE"] !== $model->field["GRADE_ALL"]) {
            $query .= "             AND GRADE = '{$model->field["GRADE"]}' ";
        } else {
            $query .= "             AND GRADE IN ( ";
            $query .= "                 SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR = '{$model->year}' ";
            $query .= "                     AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "             ) ";
        }
        $query .= "         GROUP BY ";
        $query .= "             COURSECODE ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    function getSubclasscd($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             CREDIT_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$model->year}' ";
        $query .= "             AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        if ($model->field["GRADE"] != $model->field["GRADE_ALL"]) {
            $query .= "             AND GRADE = '{$model->field["GRADE"]}' ";
        } else {
            $query .= "             AND GRADE IN ( ";
            $query .= "                 SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR = '{$model->year}' ";
            $query .= "                     AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "             ) ";
        }
        if ($model->field["COURSECODE"] != $model->field["COURSECODE_ALL"]) {
            $query .= "             AND COURSECODE = '{$model->field["COURSECODE"]}' ";
        } else {
            $query .= "             AND COURSECODE IN ( ";
            $query .= "                 SELECT ";
            $query .= "                     COURSECODE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_DAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR = '{$model->year}' ";
            $query .= "                     AND SEMESTER = '{$model->field["SEMESTER_SCH"]}' ";
            if ($model->field["GRADE"] != $model->field["GRADE_ALL"]) {
                $query .= "                     AND GRADE = '{$model->field["GRADE"]}' ";
            } else {
                $query .= "                     AND GRADE IN ( ";
                $query .= "                         SELECT ";
                $query .= "                             GRADE ";
                $query .= "                         FROM ";
                $query .= "                             SCHREG_REGD_GDAT ";
                $query .= "                         WHERE ";
                $query .= "                             YEAR = '{$model->year}' ";
                $query .= "                             AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
                $query .= "                     ) ";
            }
            $query .= "                 GROUP BY ";
            $query .= "                     COURSECODE ";
            $query .= "             ) ";
        }
        $query .= "         GROUP BY ";
        $query .= "             CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //算出先になる成績
    function getTestcd($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER || '-' || TESTKINDCD || TESTITEMCD || '-' || SCORE_DIV || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$model->year}' ";
        $query .= "     AND SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //履歴一覧
    function getListRireki($model) {
        $query  = " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     SM.SEMESTERNAME, ";
        $query .= "     NM.ABBV1 AS SCHOOL_KIND_NAME, ";
        $query .= "     CASE WHEN T1.GRADE = '{$model->field["GRADE_ALL"]}' THEN '全て' ELSE RG.GRADE_NAME1 END AS GRADE_NAME, ";
        $query .= "     CASE WHEN T1.COURSECODE = '{$model->field["COURSECODE_ALL"]}' THEN '全て' ELSE CM.COURSECODENAME END AS COURSECODENAME, ";
        $query .= "     CASE WHEN T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD_ALL"]}' THEN '全て' ELSE SB.SUBCLASSNAME END AS SUBCLASSNAME, ";
        $query .= "     TM.TESTITEMNAME AS TESTNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_EXEC_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST SM ON SM.YEAR = T1.YEAR AND SM.SEMESTER = T1.SEMESTER ";
        $query .= "     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'A023' AND NM.NAME1 = T1.SCHOOL_KIND ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT RG ON RG.YEAR = T1.YEAR AND RG.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN COURSECODE_MST CM ON CM.COURSECODE = T1.COURSECODE ";
        $query .= "     LEFT JOIN SUBCLASS_MST SB ON SB.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                              AND SB.CLASSCD = T1.CLASSCD ";
        $query .= "                              AND SB.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                              AND SB.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TM ON TM.YEAR = T1.YEAR ";
        $query .= "                                                AND TM.SEMESTER = T1.SEMESTER ";
        $query .= "                                                AND TM.TESTKINDCD = T1.COMBINED_TESTKINDCD ";
        $query .= "                                                AND TM.TESTITEMCD = T1.COMBINED_TESTITEMCD ";
        $query .= "                                                AND TM.SCORE_DIV = T1.COMBINED_SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.SEMESTER || '-' || T1.COMBINED_TESTKINDCD || '-' || T1.COMBINED_TESTITEMCD || '-' || T1.COMBINED_SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC ";
        return $query;
    }

    //実行履歴
    function executeRireki($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knjd219kQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjd219kQuery::getInsertRireki($calcDate, $calcTime, $model);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //実行日付・時間を取得
    function getCalcDateTime() {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }

    //実行履歴データ・追加
    function getInsertRireki($calcDate, $calcTime, $model) {
        $test_array = array();
        $test_array = explode("-", $model->field["TESTCD"]);
        $subArray = array();
        $subArray = explode("-", $model->field["SUBCLASSCD"]);

        $data = array();
        $data["CALC_DATE"][TEXT]            = $calcDate;
        $data["CALC_TIME"][TEXT]            = $calcTime;
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $test_array[0];
        $data["COMBINED_TESTKINDCD"][TEXT]  = $test_array[1];
        $data["COMBINED_TESTITEMCD"][TEXT]  = $test_array[2];
        $data["COMBINED_SCORE_DIV"][TEXT]   = $test_array[3];
        $data["GRADE"][TEXT]                = $model->field["GRADE"];
        $data["COURSECODE"][TEXT]           = $model->field["COURSECODE"];
        $data["CLASSCD"][TEXT]              = $subArray[0];
        $data["SCHOOL_KIND"][TEXT]          = $subArray[1];
        $data["CURRICULUM_CD"][TEXT]        = $subArray[2];
        $data["SUBCLASSCD"][TEXT]           = $subArray[3];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "SUBCLASS_SEMVAL_PERCENT_EXEC_SDIV_DAT");

        return $query;
    }

    //学期成績自動算出
    function getCalcSemValSql($model) {
        $query  = "";
        $query .= " WITH T_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T1.YEAR AND G1.GRADE = T1.GRADE ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$model->year}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER_SCH"]}' ";
        $query .= "         AND G1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        if ($model->field["GRADE"] != $model->field["GRADE_ALL"]) {
            $query .= "         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["COURSECODE"] != $model->field["COURSECODE_ALL"]) {
            $query .= "         AND T1.COURSECODE = '{$model->field["COURSECODE"]}' ";
        }
        $query .= " ) ";
        $query .= " , T_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         S1.SCHREGNO, ";
        $query .= "         S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "         S1.SEMESTER || '-' || S1.TESTKINDCD || '-' || S1.TESTITEMCD || '-' || S1.SCORE_DIV AS ATTEND_TESTCD, ";
        if ($model->Properties["KNJD219K_OtherScorePattern"]=="1") {
            $query .= "     CASE WHEN S1.VALUE_DI = '*' THEN S2.SCORE ";
            $query .= "          ELSE S1.SCORE END AS SCORE ";
        } else {
            $query .= "         S1.SCORE ";
        }
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT S1 ";
        $query .= "         INNER JOIN T_REGD T1 ON T1.SCHREGNO = S1.SCHREGNO ";
        if ($model->Properties["KNJD219K_OtherScorePattern"]=="1") {
            $query .= "     LEFT JOIN SUPP_EXA_SDIV_DAT S2 ";
            $query .= "       ON S2.YEAR =  S1.YEAR ";
            $query .= "      AND S2.SEMESTER = S1.SEMESTER ";
            $query .= "      AND S2.TESTKINDCD = S1.TESTKINDCD ";
            $query .= "      AND S2.TESTITEMCD = S1.TESTITEMCD ";
            $query .= "      AND S2.SCORE_DIV = S1.SCORE_DIV ";
            $query .= "      AND S2.CLASSCD = S1.CLASSCD ";
            $query .= "      AND S2.SCHOOL_KIND = S1.SCHOOL_KIND ";
            $query .= "      AND S2.CURRICULUM_CD = S1.CURRICULUM_CD ";
            $query .= "      AND S2.SUBCLASSCD = S1.SUBCLASSCD ";
            $query .= "      AND S2.SCHREGNO = S1.SCHREGNO ";
        }
        $query .= "     WHERE ";
        $query .= "             S1.YEAR     = '{$model->year}' ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "         AND S1.SEMESTER < '{$model->field["SEMESTER"]}' ";
        } else {
            $query .= "         AND S1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        }
        $query .= "         AND S1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        if ($model->Properties["KNJD219K_OtherScorePattern"]=="1") {
	        $query .= "         AND (S1.SCORE IS NOT NULL OR S1.VALUE_DI = '*') ";
        } else {
	        $query .= "         AND S1.SCORE IS NOT NULL ";
        }
        if ($model->field["SUBCLASSCD"] != $model->field["SUBCLASSCD_ALL"]) {
            $query .= "         AND S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " ) ";
        $query .= " , T_PERCENT AS ( ";
        $query .= "     SELECT ";
        $query .= "         P1.CLASSCD || '-' || P1.SCHOOL_KIND || '-' || P1.CURRICULUM_CD || '-' || P1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "         P1.GRADE, ";
        $query .= "         P1.COURSECODE, ";
        $query .= "         P1.ATTEND_SEMESTER || '-' || P1.ATTEND_TESTKINDCD || '-' || P1.ATTEND_TESTITEMCD || '-' || P1.ATTEND_SCORE_DIV AS ATTEND_TESTCD, ";
        $query .= "         P1.PERCENT ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_SEMVAL_PERCENT_SDIV_DAT P1 ";
        $query .= "     WHERE ";
        $query .= "             P1.YEAR = '{$model->year}' ";
        $query .= "         AND P1.SEMESTER || '-' || P1.COMBINED_TESTKINDCD || '-' || P1.COMBINED_TESTITEMCD || '-' || P1.COMBINED_SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "         AND P1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " ) ";
        $query .= " , T_SUBGC AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECODE ";
        $query .= "     FROM ";
        $query .= "         T_SCORE T1 ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECODE ";
        $query .= " ) ";
        $query .= " , T_PERCENT_SUBGC AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         '1' AS FLG ";
        $query .= "     FROM ";
        $query .= "         T_PERCENT T1 ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECODE ";
        $query .= " ) ";
        $query .= " , T_SUBGC_MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECODE, ";

        $query .= "         CASE WHEN P4.FLG = '1' THEN P4.SUBCLASSCD ";
        $query .= "              WHEN P3.FLG = '1' THEN P3.SUBCLASSCD ";
        $query .= "              WHEN P2.FLG = '1' THEN P2.SUBCLASSCD ";
        $query .= "              WHEN P1.FLG = '1' THEN P1.SUBCLASSCD ";

        $query .= "              WHEN J4.FLG = '1' THEN J4.SUBCLASSCD ";
        $query .= "              WHEN J3.FLG = '1' THEN J3.SUBCLASSCD ";
        $query .= "              WHEN J2.FLG = '1' THEN J2.SUBCLASSCD ";
        $query .= "              WHEN J1.FLG = '1' THEN J1.SUBCLASSCD ";
        $query .= "         END AS P_SUBCLASSCD, ";

        $query .= "         CASE WHEN P4.FLG = '1' THEN P4.GRADE ";
        $query .= "              WHEN P3.FLG = '1' THEN P3.GRADE ";
        $query .= "              WHEN P2.FLG = '1' THEN P2.GRADE ";
        $query .= "              WHEN P1.FLG = '1' THEN P1.GRADE ";

        $query .= "              WHEN J4.FLG = '1' THEN J4.GRADE ";
        $query .= "              WHEN J3.FLG = '1' THEN J3.GRADE ";
        $query .= "              WHEN J2.FLG = '1' THEN J2.GRADE ";
        $query .= "              WHEN J1.FLG = '1' THEN J1.GRADE ";
        $query .= "         END AS P_GRADE, ";

        $query .= "         CASE WHEN P4.FLG = '1' THEN P4.COURSECODE ";
        $query .= "              WHEN P3.FLG = '1' THEN P3.COURSECODE ";
        $query .= "              WHEN P2.FLG = '1' THEN P2.COURSECODE ";
        $query .= "              WHEN P1.FLG = '1' THEN P1.COURSECODE ";

        $query .= "              WHEN J4.FLG = '1' THEN J4.COURSECODE ";
        $query .= "              WHEN J3.FLG = '1' THEN J3.COURSECODE ";
        $query .= "              WHEN J2.FLG = '1' THEN J2.COURSECODE ";
        $query .= "              WHEN J1.FLG = '1' THEN J1.COURSECODE ";
        $query .= "         END AS P_COURSECODE ";
        $query .= "     FROM ";
        $query .= "         T_SUBGC T1 ";

        $query .= "         LEFT JOIN T_PERCENT_SUBGC P1 ON P1.SUBCLASSCD = T1.SUBCLASSCD AND P1.GRADE = '{$model->field["GRADE_ALL"]}' AND P1.COURSECODE = '{$model->field["COURSECODE_ALL"]}' ";
        $query .= "         LEFT JOIN T_PERCENT_SUBGC P2 ON P2.SUBCLASSCD = T1.SUBCLASSCD AND P2.GRADE = '{$model->field["GRADE_ALL"]}' AND P2.COURSECODE = T1.COURSECODE ";
        $query .= "         LEFT JOIN T_PERCENT_SUBGC P3 ON P3.SUBCLASSCD = T1.SUBCLASSCD AND P3.GRADE = T1.GRADE AND P3.COURSECODE = '{$model->field["COURSECODE_ALL"]}' ";
        $query .= "         LEFT JOIN T_PERCENT_SUBGC P4 ON P4.SUBCLASSCD = T1.SUBCLASSCD AND P4.GRADE = T1.GRADE AND P4.COURSECODE = T1.COURSECODE ";

        $query .= "         LEFT JOIN T_PERCENT_SUBGC J1 ON J1.SUBCLASSCD = '{$model->field["SUBCLASSCD_ALL"]}' AND J1.GRADE = '{$model->field["GRADE_ALL"]}' AND J1.COURSECODE = '{$model->field["COURSECODE_ALL"]}' ";
        $query .= "         LEFT JOIN T_PERCENT_SUBGC J2 ON J2.SUBCLASSCD = '{$model->field["SUBCLASSCD_ALL"]}' AND J2.GRADE = '{$model->field["GRADE_ALL"]}' AND J2.COURSECODE = T1.COURSECODE ";
        $query .= "         LEFT JOIN T_PERCENT_SUBGC J3 ON J3.SUBCLASSCD = '{$model->field["SUBCLASSCD_ALL"]}' AND J3.GRADE = T1.GRADE AND J3.COURSECODE = '{$model->field["COURSECODE_ALL"]}' ";
        $query .= "         LEFT JOIN T_PERCENT_SUBGC J4 ON J4.SUBCLASSCD = '{$model->field["SUBCLASSCD_ALL"]}' AND J4.GRADE = T1.GRADE AND J4.COURSECODE = T1.COURSECODE ";
        $query .= " ) ";
        $query .= " , T_MAIN2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.P_SUBCLASSCD, ";
        $query .= "         T1.P_GRADE, ";
        $query .= "         T1.P_COURSECODE, ";
        $query .= "         P1.ATTEND_TESTCD, ";
        $query .= "         P1.PERCENT, ";
        $query .= "         S1.SCHREGNO, ";
        $query .= "         S1.COURSECD, ";
        $query .= "         S1.MAJORCD, ";
        $query .= "         S1.SCORE, ";
        $query .= "         S1.SCORE * P1.PERCENT AS SCORE_PERCENT ";
        $query .= "     FROM ";
        $query .= "         T_SUBGC_MAIN T1 ";
        $query .= "         LEFT JOIN T_PERCENT P1 ON P1.SUBCLASSCD = T1.P_SUBCLASSCD AND P1.GRADE = T1.P_GRADE AND P1.COURSECODE = T1.P_COURSECODE ";
        $query .= "         LEFT JOIN T_SCORE S1 ON S1.SUBCLASSCD = T1.SUBCLASSCD AND S1.GRADE = T1.GRADE AND S1.COURSECODE = T1.COURSECODE AND S1.ATTEND_TESTCD = P1.ATTEND_TESTCD ";
        $query .= " ) ";

        $query .= " , T_MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         FLOAT(T1.SCORE) * T1.PERCENT AS SCORE_PERCENT ";
        $query .= "     FROM ";
        $query .= "         T_MAIN2 T1 ";
        if ($model->Properties["KNJD219J_SeisekiSanshutsuPattern"] == "1") {
            $query .= "     WHERE ";
            $query .= "         T1.ATTEND_TESTCD NOT LIKE '%01' ";
            $query .= "     UNION ALL ";
            $query .= "     SELECT ";
            $query .= "         T1.SUBCLASSCD, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.COURSECD, ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.COURSECODE, ";
            $query .= "         AVG(FLOAT(T1.SCORE)) * SUM(T1.PERCENT) AS SCORE_PERCENT ";
            $query .= "     FROM ";
            $query .= "         T_MAIN2 T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.ATTEND_TESTCD LIKE '%01' ";
            $query .= "     GROUP BY ";
            $query .= "         T1.SUBCLASSCD, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.COURSECD, ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.COURSECODE ";
        }
        $query .= " ) ";

        list($testSeme, $testKind, $testItem, $testSdiv) = explode("-", $model->field["TESTCD"]);
        if (($model->Properties["knjd219kUseAssessCourseMst"] == "1" && $testSeme == "9" && $model->field["TESTCD"] != "9-99-00-08")
            ||
            ($model->Properties["knjd219kUseAssessCourseMst"] == "2" && $model->field["TESTCD"] != "9-99-00-08")
        ) {
            $assesscd = $model->field["TESTCD"] == "9-99-00-09" ? "3" : "2";
            $query .= " SELECT ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.ASSESSLEVEL AS SCORE ";
            $query .= " FROM ";
            $query .= "     (SELECT ";
            $query .= "         T1.SUBCLASSCD, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.COURSECD, ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.COURSECODE, ";
            $query .= "         SMALLINT(ROUND(FLOAT(SUM(T1.SCORE_PERCENT))/100,0)) AS SCORE ";
            $query .= "     FROM ";
            $query .= "         T_MAIN T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.SCORE_PERCENT IS NOT NULL ";
            $query .= "     GROUP BY ";
            $query .= "         T1.SUBCLASSCD, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.COURSECD, ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.COURSECODE ";
            $query .= "     ) T1 ";
            $query .= "     LEFT JOIN ASSESS_COURSE_MST T2 ON T2.ASSESSCD = '".$assesscd."' ";
            $query .= "         AND T2.COURSECD = T1.COURSECD ";
            $query .= "         AND T2.MAJORCD = T1.MAJORCD ";
            $query .= "         AND T2.COURSECODE = T1.COURSECODE ";
            $query .= "         AND T1.SCORE BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ";
            $query .= " WHERE ";
            $query .= "     T2.ASSESSLEVEL IS NOT NULL ";
            $query .= " ORDER BY ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SCHREGNO ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SMALLINT(ROUND(FLOAT(SUM(T1.SCORE_PERCENT))/100,0)) AS SCORE ";
            $query .= " FROM ";
            $query .= "     T_MAIN T1 ";
            $query .= " WHERE ";
            $query .= "     T1.SCORE_PERCENT IS NOT NULL ";
            $query .= " GROUP BY ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SCHREGNO ";
            $query .= " ORDER BY ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SCHREGNO ";
        }

        return $query;
    }

    //成績算出
    function insRecordScore($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjd219kQuery::getCalcSemValSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subcd = $row["SUBCLASSCD"];
            $schno = $row["SCHREGNO"];
            $score = $row["SCORE"];

            $query = knjd219kQuery::getDeleteRecordDat($model, $model->field["TESTCD"], $subcd, $schno);
            $db->query($query);
            $query = knjd219kQuery::getInsertRecordDat($model, $model->field["TESTCD"], $subcd, $schno, $score);
            $db->query($query);

            if ($model->Properties["KNJD219J_SeisekiSanshutsuPattern"] == "1") {
                $query = knjd219kQuery::getDeleteRecordDat($model, $model->field["TESTCD9"], $subcd, $schno);
                $db->query($query);
                $query = knjd219kQuery::getInsertRecordDat($model, $model->field["TESTCD9"], $subcd, $schno, $score);
                $db->query($query);
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //RECORD_SCORE_DATのレコードを削除。
    function getDeleteRecordDat($model, $testcd, $subcd, $schno) {
        $test_array = array();
        $test_array = explode("-", $testcd);
        $subclass_array = array();
        $subclass_array = explode("-", $subcd);

        $query  = "";
        $query .= " DELETE FROM RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR            = '{$model->year}' ";
        $query .= "     AND SEMESTER        = '".$test_array[0]."' ";
        $query .= "     AND TESTKINDCD      = '".$test_array[1]."' ";
        $query .= "     AND TESTITEMCD      = '".$test_array[2]."' ";
        $query .= "     AND SCORE_DIV       = '".$test_array[3]."' ";
        $query .= "     AND CLASSCD         = '".$subclass_array[0]."' ";
        $query .= "     AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
        $query .= "     AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
        $query .= "     AND SUBCLASSCD      = '".$subclass_array[3]."' ";
        $query .= "     AND SCHREGNO        = '{$schno}' ";
        return $query;
    }

    //RECORD_SCORE_DATのレコードを追加
    function getInsertRecordDat($model, $testcd, $subcd, $schno, $score) {
        $test_array = array();
        $test_array = explode("-", $testcd);
        $subclass_array = array();
        $subclass_array = explode("-", $subcd);

        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["SEMESTER"][TEXT]         = $test_array[0];
        $data["TESTKINDCD"][TEXT]       = $test_array[1];
        $data["TESTITEMCD"][TEXT]       = $test_array[2];
        $data["SCORE_DIV"][TEXT]        = $test_array[3];
        $data["CLASSCD"][TEXT]          = $subclass_array[0];
        $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
        $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
        $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
        $data["SCHREGNO"][TEXT]         = $schno;
        $data["SCORE"][NUMBER]          = $score;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        return $query;
    }
}
?>
