<?php

require_once('for_php7.php');


class knjd219sQuery extends Query {

    //処理学期
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";
        return $query;
    }

    //処理学年
    function getGrade() {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //処理種別(成績)
    function getName($model)
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || SEM_M.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST SEM_M ";
        $query .= "         ON  SEM_M.YEAR = T1.YEAR ";
        $query .= "         AND SEM_M.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "          AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ";
        $query .= "              = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
        $query .= "          AND T2.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "          AND T2.SCORE_DIV IS NOT NULL  ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.SCORE_DIV = '08' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //処理学年
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        return $query;
    }

    //科目
    function getSubclassCd($model, $div = "ALL")
    {
        if ($div == "ALL") {
            $query  = " WITH ALL_T(VALUE, LABEL) AS ( ";
            $query .= "     VALUES('00-{$model->schoolKind}-0-000000', '全 て') ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     VALUE, ";
            $query .= "     LABEL ";
            $query .= " FROM ";
            $query .= "     ALL_T ";
            $query .= " UNION ";
        }
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        //制限付き権限
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 || '-' ||  FIELD3 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '1050' ";
            $query .= "             ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /* 実行 */
    function ExecuteQuery(&$model)
    {
        $model->TANTAI = "1";
        $model->RUI_SEME = "2";
        $model->RUI_YEAR = "3";
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //コースグループ取得
        $groupArray = array();
        $query = knjd219sQuery::getCourseGroup($model);
        $result = $db->query($query);
        while ($rowCourse = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $groupArray[] = $rowCourse["GROUP_CD"];
        }

        //科目取得
        $subclassArray = array();
        if (substr($model->subclassCd, 0, 2) != "00") {
                $subclassArray[] = $model->subclassCd;
        } else {
            $query = knjd219sQuery::getSubclassCd($model, "NOT ALL");
            $result = $db->query($query);
            while ($rowSubclass = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclassArray[] = $rowSubclass["VALUE"];
            }
        }

        foreach ($subclassArray as $key => $subVal) {
            $subclassKey = explode("-", $subVal);
            $model->subclass["CLASSCD"]          = $subclassKey[0];
            $model->subclass["SCHOOL_KIND"]      = $subclassKey[1];
            $model->subclass["CURRICULUM_CD"]    = $subclassKey[2];
            $model->subclass["SUBCLASSCD"]       = $subclassKey[3];

            foreach ($groupArray as $key => $groupCd) {
                if ($model->test["TESTKINDCD"] == "99") {
                    knjd219sQuery::update99Exe($db, $model, $model->TANTAI, $groupCd);
                    knjd219sQuery::update99Exe($db, $model, $model->RUI_SEME, $groupCd);
                    knjd219sQuery::update99Exe($db, $model, $model->RUI_YEAR, $groupCd);
                } else {
                    knjd219sQuery::updateExe($db, $model, $model->TANTAI, $groupCd);
                    knjd219sQuery::updateExe($db, $model, $model->RUI_SEME, $groupCd);
                    knjd219sQuery::updateExe($db, $model, $model->RUI_YEAR, $groupCd);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    function updateExe($db, $model, $RUISEKI_DIV, $groupCd) {
        //ADMIN_CONTROL_TESTITEM_DAT登録科目かどうか
        $query = knjd219sQuery::getTestSubclassCnt($model, $groupCd);
        $testSubclassCnt = $db->getOne($query);
        if ($testSubclassCnt > 0) {
            $query = knjd219sQuery::getTestCnt($model, $model->subclass["CLASSCD"], $model->subclass["SCHOOL_KIND"], $model->subclass["CURRICULUM_CD"], $model->subclass["SUBCLASSCD"], $groupCd, $RUISEKI_DIV);
        } else {
            $query = knjd219sQuery::getTestCnt($model, "00", $model->schoolKind, "00", "000000", $groupCd, $RUISEKI_DIV);
        }

        $testCnt = $db->getOne($query);
        if ($testCnt == 0) {
            return;
        }

        $query = knjd219sQuery::getPerfect($model, $groupCd, $RUISEKI_DIV);
        $perfect = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($perfect["CNT"] == 0 && $testCnt == 0) {
            return;
        }

        if ($perfect["CNT"] != $testCnt) {
            $perfect["SUM_PERFECT"] = $perfect["SUM_PERFECT"] + (($testCnt - $perfect["CNT"]) * 100);
        }
        $perfect50Per = $perfect["SUM_PERFECT"] * 0.5;

        $query = knjd219sQuery::getAvg($model, $groupCd, $RUISEKI_DIV, "GET_ONE");
        $exeAvgVal = $db->getOne($query);
        if (strlen($exeAvgVal) == 0) {
            return;
        }

        $query = knjd219sQuery::getAvg($model, $groupCd, $RUISEKI_DIV);
        $avgVal = $db->getOne($query);
        $setLine = 0;
        if ($avgVal >= $perfect50Per) {
            $setLine = $perfect["SUM_PERFECT"] * 0.3;
        } else {
            $setLine = floor($avgVal / 2);
        }

        if ($RUISEKI_DIV == $model->RUI_YEAR) {
            $query = knjd219sQuery::getRuikeiData($model, $RUISEKI_DIV, $groupCd);
            $setLinePlus = $db->getOne($query);
            $setLine += $setLinePlus;
        }

        $query = knjd219sQuery::getDeleteQuery($model, $groupCd, $RUISEKI_DIV);
        $db->query($query);

        $query = knjd219sQuery::getInserQuery($model, $groupCd, $RUISEKI_DIV, $setLine);
        $db->query($query);

    }

    function getRuikeiData($model, $RUISEKI_DIV, $groupCd) {
        $setSemeTest = $model->test["TESTKINDCD"] == "99" ? $model->semester."0101" : $model->semester.$model->test["TESTKINDCD"].$model->test["TESTITEMCD"];
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     MAX(T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD) AS MAX_KEY, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.RUISEKI_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.PASS_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";
        $query .= " FROM ";
        $query .= "     PASS_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($RUISEKI_DIV == $model->RUI_SEME) {
            $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        }
        if ($RUISEKI_DIV == $model->RUI_YEAR) {
            $query .= "     AND T1.SEMESTER < '{$model->semester}' ";
        }
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD < '{$setSemeTest}' ";
        $query .= "     AND T1.TESTKINDCD IN ('01', '02') ";
        $query .= "     AND T1.SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        $query .= "     AND T1.RUISEKI_DIV = '{$RUISEKI_DIV}' ";
        $query .= "     AND T1.MAJORCD = '{$groupCd}' ";
        $query .= "     AND T1.CLASSCD = '{$model->subclass["CLASSCD"]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->subclass["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$model->subclass["CURRICULUM_CD"]}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$model->subclass["SUBCLASSCD"]}' ";
        $query .= "     AND T1.PASS_DIV = '5' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.RUISEKI_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.PASS_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCORE_LINE ";
        $query .= " FROM ";
        $query .= "     PASS_SCORE_DAT T1, ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = MAIN_T.YEAR ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = MAIN_T.MAX_KEY ";
        $query .= "     AND T1.SCORE_DIV = MAIN_T.SCORE_DIV ";
        $query .= "     AND T1.RUISEKI_DIV = MAIN_T.RUISEKI_DIV ";
        $query .= "     AND T1.CLASSCD = MAIN_T.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = MAIN_T.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = MAIN_T.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = MAIN_T.SUBCLASSCD ";
        $query .= "     AND T1.PASS_DIV = MAIN_T.PASS_DIV ";
        $query .= "     AND T1.GRADE = MAIN_T.GRADE ";
        $query .= "     AND T1.HR_CLASS = MAIN_T.HR_CLASS ";
        $query .= "     AND T1.COURSECD = MAIN_T.COURSECD ";
        $query .= "     AND T1.MAJORCD = MAIN_T.MAJORCD ";
        $query .= "     AND T1.COURSECODE = MAIN_T.COURSECODE ";

        return $query;
    }

    function update99Exe($db, $model, $RUISEKI_DIV, $groupCd) {
        $query = knjd219sQuery::getDeleteQuery($model, $groupCd, $RUISEKI_DIV);
        $db->query($query);

        $setSemeTest = $model->semester.$model->test["TESTKINDCD"].$model->test["TESTITEMCD"];
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     MAX(T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD) AS MAX_KEY, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.RUISEKI_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.PASS_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";
        $query .= " FROM ";
        $query .= "     PASS_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD < '{$setSemeTest}' ";
        if ($model->semester == "9") {
            $query .= "     AND T1.TESTKINDCD = '99' ";
            $query .= "     AND T1.TESTITEMCD = '00' ";
            $query .= "     AND T1.SCORE_DIV = '08' ";
        } else {
            $query .= "     AND T1.TESTKINDCD IN ('01', '02') ";
            $query .= "     AND T1.SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        }
        $query .= "     AND T1.RUISEKI_DIV = '{$RUISEKI_DIV}' ";
        $query .= "     AND T1.CLASSCD = '{$model->subclass["CLASSCD"]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->subclass["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$model->subclass["CURRICULUM_CD"]}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$model->subclass["SUBCLASSCD"]}' ";
        $query .= "     AND T1.PASS_DIV = '5' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= "     AND T1.HR_CLASS = '000' ";
        $query .= "     AND T1.COURSECD = '0' ";
        $query .= "     AND T1.MAJORCD = '".$groupCd."' ";
        $query .= "     AND T1.COURSECODE = '0000' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.RUISEKI_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.PASS_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCORE_LINE ";
        $query .= " FROM ";
        $query .= "     PASS_SCORE_DAT T1, ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = MAIN_T.YEAR ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = MAIN_T.MAX_KEY ";
        $query .= "     AND T1.SCORE_DIV = MAIN_T.SCORE_DIV ";
        $query .= "     AND T1.RUISEKI_DIV = MAIN_T.RUISEKI_DIV ";
        $query .= "     AND T1.CLASSCD = MAIN_T.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = MAIN_T.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = MAIN_T.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = MAIN_T.SUBCLASSCD ";
        $query .= "     AND T1.PASS_DIV = MAIN_T.PASS_DIV ";
        $query .= "     AND T1.GRADE = MAIN_T.GRADE ";
        $query .= "     AND T1.HR_CLASS = MAIN_T.HR_CLASS ";
        $query .= "     AND T1.COURSECD = MAIN_T.COURSECD ";
        $query .= "     AND T1.MAJORCD = MAIN_T.MAJORCD ";
        $query .= "     AND T1.COURSECODE = MAIN_T.COURSECODE ";

        $setScoreLine = $db->getOne($query);

        if (strlen($setScoreLine) == 0) {
            return;
        }

        $query = knjd219sQuery::getInserQuery($model, $groupCd, $RUISEKI_DIV, $setScoreLine);
        $db->query($query);

    }

    //コースグループ取得
    function getCourseGroup($model) {
        $query  = " SELECT ";
        $query .= "     GROUP_CD ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     GROUP_CD ";

        return $query;
    }

    //科目のテスト設定あり
    function getTestSubclassCnt($model, $groupCd) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= "     AND T1.CLASSCD = '{$model->subclass["CLASSCD"]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->subclass["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$model->subclass["CURRICULUM_CD"]}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$model->subclass["SUBCLASSCD"]}' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= "         IN (SELECT ";
        $query .= "                 I1.COURSECD || I1.MAJORCD || I1.COURSECODE ";
        $query .= "             FROM ";
        $query .= "                 COURSE_GROUP_CD_DAT I1 ";
        $query .= "             WHERE ";
        $query .= "                 I1.YEAR    = '".CTRL_YEAR."' ";
        $query .= "                 AND I1.GRADE   = '{$model->grade}' ";
        $query .= "                 AND I1.GROUP_CD   = '{$groupCd}' ";
        $query .= "         ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";

        return $query;
    }

    //テスト数
    function getTestCnt($model, $classCd, $sKind, $curriculum, $subclassCd, $groupCd, $RUISEKI_DIV) {
        $setSemeTest = $model->semester.$model->test["TESTKINDCD"].$model->test["TESTITEMCD"];

        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($RUISEKI_DIV != $model->TANTAI) {
            $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        }
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= "         IN (SELECT ";
        $query .= "                 I1.COURSECD || I1.MAJORCD || I1.COURSECODE ";
        $query .= "             FROM ";
        $query .= "                 COURSE_GROUP_CD_DAT I1 ";
        $query .= "             WHERE ";
        $query .= "                 I1.YEAR    = '".CTRL_YEAR."' ";
        $query .= "                 AND I1.GRADE   = '{$model->grade}' ";
        $query .= "                 AND I1.GROUP_CD   = '{$groupCd}' ";
        $query .= "         ) ";
        $query .= "     AND T1.CLASSCD = '{$classCd}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$sKind}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$curriculum}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$subclassCd}' ";
        if ($RUISEKI_DIV == $model->TANTAI) {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '{$setSemeTest}' ";
        } else {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '{$setSemeTest}' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";

        return $query;
    }

    //満点取得
    function getPerfect($model, $groupCd, $RUISEKI_DIV) {
        $setSemeTest = $model->semester.$model->test["TESTKINDCD"].$model->test["TESTITEMCD"];
        $query  = " SELECT ";
        $query .= "     SUM(T1.PERFECT) AS SUM_PERFECT, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($RUISEKI_DIV == $model->RUI_SEME) {
            $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        }
        if ($RUISEKI_DIV == $model->TANTAI) {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '{$setSemeTest}' ";
        } else {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '{$setSemeTest}' ";
        }
        $query .= "     AND T1.TESTKINDCD IN ('01', '02') ";
        $query .= "     AND T1.SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        $query .= "     AND T1.CLASSCD = '{$model->subclass["CLASSCD"]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->subclass["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$model->subclass["CURRICULUM_CD"]}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$model->subclass["SUBCLASSCD"]}' ";
        $query .= "     AND T1.GRADE = CASE WHEN T1.DIV = '01' THEN '00' ELSE '{$model->grade}' END ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ";
        $query .= "         CASE WHEN T1.DIV IN ('01','02') ";
        $query .= "              THEN '00000000' ";
        $query .= "              ELSE '0' || '{$groupCd}' || '0000' END ";

        return $query;
    }

    //平均取得
    function getAvg($model, $groupCd, $RUISEKI_DIV, $div = "") {
        $setSemeTest = $model->semester.$model->test["TESTKINDCD"].$model->test["TESTITEMCD"];
        $query  = " SELECT ";
        $query .= "     SUM(T1.AVG) AS SUM_AVG ";
        $query .= " FROM ";
        $query .= "     RECORD_AVERAGE_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($div == "GET_ONE") {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '{$setSemeTest}' ";
        } else {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '{$setSemeTest}' ";
        }
        if ($RUISEKI_DIV != $model->TANTAI) {
            $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        }
        if ($RUISEKI_DIV == $model->TANTAI) {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '{$setSemeTest}' ";
        } else {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '{$setSemeTest}' ";
        }
        $query .= "     AND T1.TESTKINDCD IN ('01', '02') ";
        $query .= "     AND T1.SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        $query .= "     AND T1.CLASSCD = '{$model->subclass["CLASSCD"]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->subclass["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$model->subclass["CURRICULUM_CD"]}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$model->subclass["SUBCLASSCD"]}' ";
        $query .= "     AND T1.AVG_DIV = '5' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '0' || '{$groupCd}' || '0000' ";

        return $query;
    }

    //削除
    function getDeleteQuery($model, $groupCd, $RUISEKI_DIV) {
        $query  = " DELETE FROM ";
        $query .= "     PASS_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        $query .= "     AND RUISEKI_DIV = '{$RUISEKI_DIV}' ";
        $query .= "     AND CLASSCD = '{$model->subclass["CLASSCD"]}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->subclass["SCHOOL_KIND"]}' ";
        $query .= "     AND CURRICULUM_CD = '{$model->subclass["CURRICULUM_CD"]}' ";
        $query .= "     AND SUBCLASSCD = '{$model->subclass["SUBCLASSCD"]}' ";
        $query .= "     AND PASS_DIV = '5' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        $query .= "     AND HR_CLASS = '000' ";
        $query .= "     AND COURSECD = '0' ";
        $query .= "     AND MAJORCD = '".$groupCd."' ";
        $query .= "     AND COURSECODE = '0000' ";

        return $query;
    }

    //新規
    function getInserQuery($model, $groupCd, $RUISEKI_DIV, $setLine) {
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["TESTKINDCD"][TEXT]       = $model->test["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]       = $model->test["TESTITEMCD"];
        $data["SCORE_DIV"][TEXT]        = $model->test["SCORE_DIV"];
        $data["RUISEKI_DIV"][TEXT]      = $RUISEKI_DIV;
        $data["CLASSCD"][TEXT]          = $model->subclass["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]      = $model->subclass["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]    = $model->subclass["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]       = $model->subclass["SUBCLASSCD"];
        $data["PASS_DIV"][TEXT]         = "5";
        $data["GRADE"][TEXT]            = $model->grade;
        $data["HR_CLASS"][TEXT]         = "000";
        $data["COURSECD"][TEXT]         = "0";
        $data["MAJORCD"][TEXT]          = $groupCd;
        $data["COURSECODE"][TEXT]       = "0000";
        $data["SCORE_LINE"][NUMBER]     = $setLine;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "PASS_SCORE_DAT");

        return $query;
    }

}
?>
