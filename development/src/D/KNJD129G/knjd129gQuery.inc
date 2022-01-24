<?php

require_once('for_php7.php');


class knjd129gquery extends Query {

    //学期
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "     AND SCHOOL_KIND = 'J' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース
    function getCourse($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || ':' || L1.MAJORNAME || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN V_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.COURSECD = L1.COURSECD ";
        $query .= "           AND T1.MAJORCD = L1.MAJORCD ";
        $query .= "     INNER JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "           AND T1.COURSECODE = L2.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->schSeme}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //処理種別(成績)
    function getTest($model)
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "          AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ";
        $query .= "              = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
        $query .= "          AND T2.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "          AND T2.SCORE_DIV IS NOT NULL  ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD IN('01', '02') ";
        $query .= "     AND T1.SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目
    function getSubclassCd($model)
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名取得
    function getTestName($model, $testSubCnt) {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T2.SEMESTERNAME, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T2.EDATE, ";
        $query .= "     T3.TESTITEMABBV1 AS TESTITEMNAME, ";
        $query .= "     T3.SIDOU_INPUT, ";
        $query .= "     T3.SIDOU_INPUT_INF, ";
        $query .= "     T1.CONTROL_FLG ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST T2 ";
        $query .= "         ON  T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T3 ";
        $query .= "         ON  T3.YEAR         = T1.YEAR ";
        $query .= "         AND T3.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T3.TESTKINDCD   = T1.TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD   = T1.TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV    = T1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        //科目別設定がある場合は、科目別設定。それ以外は基本設定されたパーツのみ表示する。
        $subclasscd = (0 < $testSubCnt) ? $model->field["SUBCLASSCD"] : $model->subclassAll;
        $subclass_array = array();
        $subclass_array = explode("-", $subclasscd);
        $query .= "          AND T1.CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "          AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "          AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "          AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //科目別設定があるか
    function getTestSubCnt($model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
        $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
        $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
        $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     RD.ATTENDNO, ";
        $query .= "     RD.COURSECD, ";
        $query .= "     RD.MAJORCD, ";
        $query .= "     RD.COURSECODE, ";
        $query .= "     COURSE_GD.GROUP_CD, ";
        $query .= "     RH.HR_NAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     SUBM.SUBCLASSNAME, ";
        $query .= "     CASE WHEN SUPP.SCORE_FLG = '2' ";
        $query .= "          THEN SUPP.SCORE ";
        $query .= "          ELSE RANK_D.SCORE ";
        $query .= "     END AS SCORE, ";
        $query .= "     SUPP.SCORE_FLG ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT RANK_D ON T1.YEAR = RANK_D.YEAR ";
        $query .= "          AND T1.SEMESTER = RANK_D.SEMESTER ";
        $query .= "          AND T1.TESTKINDCD = RANK_D.TESTKINDCD ";
        $query .= "          AND T1.TESTITEMCD = RANK_D.TESTITEMCD ";
        $query .= "          AND T1.SCORE_DIV = RANK_D.SCORE_DIV ";
        $query .= "          AND T1.CLASSCD = RANK_D.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = RANK_D.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = RANK_D.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = RANK_D.SUBCLASSCD ";
        $query .= "          AND T1.SCHREGNO = RANK_D.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT RD ON T1.YEAR = RD.YEAR ";
        $query .= "           AND T1.SEMESTER = RD.SEMESTER ";
        $query .= "           AND T1.SCHREGNO = RD.SCHREGNO ";
        $query .= "           AND RD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "           AND RD.COURSECD || '-' || RD.MAJORCD || '-' || RD.COURSECODE = '{$model->field["COURSE"]}' ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT COURSE_GD ON RD.YEAR = COURSE_GD.YEAR ";
        $query .= "          AND RD.GRADE = COURSE_GD.GRADE ";
        $query .= "          AND RD.COURSECD = COURSE_GD.COURSECD ";
        $query .= "          AND RD.MAJORCD = COURSE_GD.MAJORCD ";
        $query .= "          AND RD.COURSECODE = COURSE_GD.COURSECODE ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT RH ON RD.YEAR = RH.YEAR ";
        $query .= "           AND RD.SEMESTER = RH.SEMESTER ";
        $query .= "           AND RD.GRADE = RH.GRADE ";
        $query .= "           AND RD.HR_CLASS = RH.HR_CLASS ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUBM ON T1.CLASSCD = SUBM.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = SUBM.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = SUBM.SUBCLASSCD ";
        $query .= "     LEFT JOIN SUPP_EXA_SDIV_DAT SUPP ON T1.YEAR = SUPP.YEAR ";
        $query .= "          AND T1.SEMESTER = SUPP.SEMESTER ";
        $query .= "          AND T1.TESTKINDCD = SUPP.TESTKINDCD ";
        $query .= "          AND T1.TESTITEMCD = SUPP.TESTITEMCD ";
        $query .= "          AND T1.SCORE_DIV = SUPP.SCORE_DIV ";
        $query .= "          AND T1.CLASSCD = SUPP.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = SUPP.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = SUPP.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = SUPP.SUBCLASSCD ";
        $query .= "          AND T1.SCHREGNO = SUPP.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->schSeme}' ";
        $query .= "     AND T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV = '{$model->field["TESTIK"]}' ";
        $query .= "     AND T1.VALUE_DI = '*' ";
        if ($model->field["SUBCLASS_CD"]) {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASS_CD"]}' ";
        }
        if (!$model->dispZumi) {
            $query .= "     AND VALUE(SUPP.SCORE_FLG, '9') != '2' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RD.HR_CLASS, ";
        $query .= "     RD.ATTENDNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        return $query;
    }

    //成績データ
    function getScore($year, $testcd, $model, $rowKey) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     '1' AS DIV, ";
        $query .= "     SCORE ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' AND ";
        $query .= "     SCHREGNO || '-' || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$rowKey}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '2' AS DIV, ";
        $query .= "     SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' AND ";
        $query .= "     SCHREGNO || '-' || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$rowKey}' ";
        $query .= " ORDER BY ";
        $query .= "     DIV ";
        return $query;
    }

    function getRecordRankSdiv($model, $schSub) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        $query .= "     SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE_DIV = '01' ";
        $query .= "     AND SCHREGNO || '-' || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$schSub}' ";

        return $query;
    }

    function getRecordAvgSdiv($model, $subclassCd, $groupCd) {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.AVG ";
        $query .= " FROM ";
        $query .= "     RECORD_AVERAGE_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCORE_DIV = '01' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$subclassCd}' ";
        $query .= "     AND T1.AVG_DIV = '5' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '0' || '{$groupCd}' || '0000' ";

        return $query;
    }

    /* 成績データ更新処理 */
    function update($model) {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //年度
        $year = CTRL_YEAR;
        list($testkindcd, $testitemcd, $score_div) = preg_split("/-/", $model->field["TESTIK"]);

        foreach ($model->data["KEY"] as $schSub) {
            $query  = " DELETE FROM ";
            $query .= "     SUPP_EXA_SDIV_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
            $query .= "     AND TESTKINDCD = '{$testkindcd}' ";
            $query .= "     AND TESTITEMCD = '{$testitemcd}' ";
            $query .= "     AND SCORE_DIV = '{$score_div}' ";
            $query .= "     AND SCHREGNO || '-' || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$schSub}' ";

            $db->query($query);

            if ($model->fields[$schSub]) {
                list($schregNo, $classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $schSub);
                $data = array();
                $data["YEAR"][TEXT]             = $year;
                $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                $data["TESTKINDCD"][TEXT]       = $testkindcd;
                $data["TESTITEMCD"][TEXT]       = $testitemcd;
                $data["SCORE_DIV"][TEXT]        = $score_div;
                $data["CLASSCD"][TEXT]          = $classCd;
                $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
                $data["SUBCLASSCD"][TEXT]       = $subclassCd;
                $data["SCHREGNO"][TEXT]         = $schregNo;
                $data["SCORE"][NUMBER]          = $model->fields[$schSub];
                $data["SCORE_FLG"][TEXT]        = "2";
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "SUPP_EXA_SDIV_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }

}
?>
