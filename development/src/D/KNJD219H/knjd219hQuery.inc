<?php

require_once('for_php7.php');

class knjd219hQuery extends Query {

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
        $query .= "     AND TESTITEMCD = '00' ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     AND SCORE_DIV  = '09' ";
        } else {
            $query .= "     AND SCORE_DIV  = '08' ";
        }
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
        $calcRow = $db->getRow(knjd219hQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjd219hQuery::getInsertRireki($calcDate, $calcTime, $model);
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
        //在籍データ
        $query .= " WITH T_REGD AS ( ";
        $query .= "     SELECT ";
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
        //成績データ
        $query .= " , T_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        $query .= "         S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD AS SUBCLASSCD, ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "         AVG(FLOAT(S1.SCORE)) AS SEMSCORE ";
        } else {
            $query .= "         AVG(FLOAT(CASE WHEN S1.SCORE_DIV = '01' THEN S1.SCORE ELSE NULL END)) AS SOTEN, ";
            $query .= "         AVG(FLOAT(CASE WHEN S1.SCORE_DIV = '02' THEN S1.SCORE ELSE NULL END)) AS HEIJOU ";
        }
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT S1 ";
        $query .= "     WHERE ";
        $query .= "             S1.YEAR = '{$model->year}' ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "         AND S1.SEMESTER  != '{$model->field["SEMESTER"]}' ";
            $query .= "         AND S1.TESTKINDCD = '99' ";
            $query .= "         AND S1.TESTITEMCD = '00' ";
            $query .= "         AND S1.SCORE_DIV  = '08' ";
        } else {
            $query .= "         AND S1.SEMESTER   = '{$model->field["SEMESTER"]}' ";
            $query .= "         AND S1.SCORE_DIV  IN ('01','02') ";
        }
        $query .= "         AND S1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "         AND S1.SCORE IS NOT NULL ";
        if ($model->field["SUBCLASSCD"] != $model->field["SUBCLASSCD_ALL"]) {
            $query .= "         AND S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         S1.SCHREGNO, ";
        $query .= "         S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     S1.SUBCLASSCD, ";
        $query .= "     S1.SCHREGNO, ";
        if ($model->field["SEMESTER"] == "9") {
            //学年成績
            $query .= "     SMALLINT(ROUND(S1.SEMSCORE,0)) AS SCORE ";
        } else {
            //各学期成績
            $query .= "     SMALLINT(ROUND(VALUE(S1.SOTEN,0) * 0.8 + VALUE(S1.HEIJOU,0),0)) AS SCORE ";
        }
        $query .= " FROM ";
        $query .= "     T_SCORE S1 ";
        $query .= "     INNER JOIN T_REGD T1 ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     S1.SUBCLASSCD, ";
        $query .= "     S1.SCHREGNO ";

        return $query;
    }

    //成績算出
    function insRecordScore($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjd219hQuery::getCalcSemValSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field["SEMESTER"] != "9") {
                $testcd = $model->field["TESTCD"];
                $subcd = $row["SUBCLASSCD"];
                $schno = $row["SCHREGNO"];
                $score = $row["SCORE"];
                //各学期成績
                $query = knjd219hQuery::getDeleteRecordDat($model, $testcd, $subcd, $schno);
                $db->query($query);
                $query = knjd219hQuery::getInsertRecordDat($model, $testcd, $subcd, $schno, $score);
                $db->query($query);
            } else if ($model->field["TESTCD"] == "9-99-00-09") {
                $testcd08 = "9-99-00-08";
                $testcd09 = "9-99-00-09";
                $subcd = $row["SUBCLASSCD"];
                $schno = $row["SCHREGNO"];
                $score08 = $row["SCORE"];
                $score09 = "";
                //学年評定は学年成績を５段階に換算し取得
                if (strlen($score08)) {
                    $query = knjd219hQuery::getAssessLevel($model, $subcd, $schno, $score08);
                    $score09 = $db->getOne($query);
                }
                //学年成績
                $query = knjd219hQuery::getDeleteRecordDat($model, $testcd08, $subcd, $schno);
                $db->query($query);
                $query = knjd219hQuery::getInsertRecordDat($model, $testcd08, $subcd, $schno, $score08);
                $db->query($query);
                //学年評定
                $query = knjd219hQuery::getDeleteRecordDat($model, $testcd09, $subcd, $schno);
                $db->query($query);
                $query = knjd219hQuery::getInsertRecordDat($model, $testcd09, $subcd, $schno, $score09);
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

    //学年成績を５段階に換算し取得
    function getAssessLevel($model, $subcd, $schno, $score) {
        $subclass_array = array();
        $subclass_array = explode("-", $subcd);

        $query  = "";
        //相対評定マスタ
        $query .= "WITH OTHER_CNT AS ( ";
        if ($model->Properties["useAssessSubclassMst"] == "1") {
            $query .= "    SELECT ";
            $query .= "        YEAR, ";
            $query .= "        GRADE, ";
            $query .= "        COURSECD, ";
            $query .= "        MAJORCD, ";
            $query .= "        COURSECODE, ";
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
            $query .= "        SUBCLASSCD, ";
            $query .= "        COUNT(*) AS CNT ";
            $query .= "    FROM ";
            $query .= "        ASSESS_SUBCLASS_MST ";
            $query .= "    WHERE ";
            $query .= "        YEAR = '{$model->year}' ";
            $query .= "    GROUP BY ";
            $query .= "        YEAR, ";
            $query .= "        GRADE, ";
            $query .= "        COURSECD, ";
            $query .= "        MAJORCD, ";
            $query .= "        COURSECODE, ";
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
            $query .= "        SUBCLASSCD ";
        } else if ($model->Properties["useAssessCourseMst"] == "1") {
            $query .= "    SELECT ";
            $query .= "        ASSESSCD, ";
            $query .= "        COURSECD, ";
            $query .= "        MAJORCD, ";
            $query .= "        COURSECODE, ";
            $query .= "        COUNT(*) AS CNT ";
            $query .= "    FROM ";
            $query .= "        ASSESS_COURSE_MST ";
            $query .= "    WHERE ";
            $query .= "        ASSESSCD = '3' ";
            $query .= "    GROUP BY ";
            $query .= "        ASSESSCD, ";
            $query .= "        COURSECD, ";
            $query .= "        MAJORCD, ";
            $query .= "        COURSECODE ";
        } else {
            $query .= "    SELECT ";
            $query .= "        ASSESSCD, ";
            $query .= "        GRADE, ";
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
            $query .= "        SUBCLASSCD, ";
            $query .= "        COUNT(*) AS CNT ";
            $query .= "    FROM ";
            $query .= "        RELATIVEASSESS_MST ";
            $query .= "    WHERE ";
            $query .= "        ASSESSCD = '3' ";
            $query .= "    GROUP BY ";
            $query .= "        ASSESSCD, ";
            $query .= "        GRADE, ";
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
            $query .= "        SUBCLASSCD ";
        }
        $query .= "    ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     CASE WHEN R2.CNT IS NOT NULL AND 0 < R2.CNT THEN R1.ASSESSLEVEL ELSE L1.ASSESSLEVEL END AS ASSESSLEVEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ASSESS_MST L1 ON L1.ASSESSCD = '3' ";
        $query .= "         AND {$score} BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ";
        if ($model->Properties["useAssessSubclassMst"] == "1") {
            $query .= "     LEFT JOIN ASSESS_SUBCLASS_MST R1 ON R1.YEAR = '{$model->year}' ";
            $query .= "         AND R1.GRADE            = T1.GRADE ";
            $query .= "         AND R1.COURSECD         = T1.COURSECD ";
            $query .= "         AND R1.MAJORCD          = T1.MAJORCD ";
            $query .= "         AND R1.COURSECODE       = T1.COURSECODE ";
            $query .= "         AND R1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND R1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND R1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND R1.SUBCLASSCD       = '".$subclass_array[3]."' ";
            $query .= "         AND {$score} BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
        } else if ($model->Properties["useAssessCourseMst"] == "1") {
            $query .= "     LEFT JOIN ASSESS_COURSE_MST R1 ON R1.ASSESSCD = '3' ";
            $query .= "         AND R1.COURSECD         = T1.COURSECD ";
            $query .= "         AND R1.MAJORCD          = T1.MAJORCD ";
            $query .= "         AND R1.COURSECODE       = T1.COURSECODE ";
            $query .= "         AND {$score} BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
        } else {
            $query .= "     LEFT JOIN RELATIVEASSESS_MST R1 ON R1.ASSESSCD = '3' ";
            $query .= "         AND R1.GRADE            = T1.GRADE ";
            $query .= "         AND R1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND R1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND R1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND R1.SUBCLASSCD       = '".$subclass_array[3]."' ";
            $query .= "         AND {$score} BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
        }
        if ($model->Properties["useAssessSubclassMst"] == "1") {
            $query .= "     LEFT JOIN OTHER_CNT R2 ON R2.YEAR = '{$model->year}' ";
            $query .= "         AND R2.GRADE            = T1.GRADE ";
            $query .= "         AND R2.COURSECD         = T1.COURSECD ";
            $query .= "         AND R2.MAJORCD          = T1.MAJORCD ";
            $query .= "         AND R2.COURSECODE       = T1.COURSECODE ";
            $query .= "         AND R2.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND R2.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND R2.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND R2.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else if ($model->Properties["useAssessCourseMst"] == "1") {
            $query .= "     LEFT JOIN OTHER_CNT R2 ON R2.ASSESSCD = '3' ";
            $query .= "         AND R2.COURSECD         = T1.COURSECD ";
            $query .= "         AND R2.MAJORCD          = T1.MAJORCD ";
            $query .= "         AND R2.COURSECODE       = T1.COURSECODE ";
        } else {
            $query .= "     LEFT JOIN OTHER_CNT R2 ON R2.ASSESSCD = '3' ";
            $query .= "         AND R2.GRADE            = T1.GRADE ";
            $query .= "         AND R2.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND R2.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND R2.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND R2.SUBCLASSCD       = '".$subclass_array[3]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER_SCH"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$schno}' ";

        return $query;
    }
}
?>
