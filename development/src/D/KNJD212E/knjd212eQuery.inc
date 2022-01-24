<?php

require_once('for_php7.php');


class knjd212eQuery extends Query {

    //校種
    function getSchoolKind() {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";
        return $query;
    }

    //考査種別
    function getTestcd($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND TESTKINDCD IN ('01','02') ";
        $query .= "     AND SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //見込点算出比率
    function getMikomiRateQuery($model) {
        $query  = " SELECT ";
        $query .= "     MIKOMI_RATE ";
        $query .= " FROM ";
        $query .= "     MIKOMI_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        return $query;
    }

    /* 実行 */
    function ExecuteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjd212eQuery::getMikomiScoreQuery($model);
        knjd212eQuery::insertRows($model, $db, $query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    // 挿入
    function insertRows($model, $db, $query) {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasscd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $schregno = $row["SCHREGNO"];
            $score = $row["MIKOMI_SCORE"];

            $query = knjd212eQuery::getDeleteQuery($model, $subclasscd, $schregno);
            $db->query($query);
            if (strlen($score)) {
                $query = knjd212eQuery::getInsertQuery($model, $subclasscd, $schregno, $score);
                $db->query($query);
            }
        }
        $result->free();
    }

    //
    function getInsertQuery($model, $subclasscd, $schregno, $score) {
        list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $subclasscd);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->test["SEMESTER"];
        $data["TESTKINDCD"][TEXT]       = $model->test["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]       = $model->test["TESTITEMCD"];
        $data["SCORE_DIV"][TEXT]        = $model->test["SCORE_DIV"];
        $data["CLASSCD"][TEXT]          = $classcd;
        $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
        $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
        $data["SUBCLASSCD"][TEXT]       = $subclasscd;
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["SCORE"][NUMBER]          = round($score);
        $data["SCORE_FLG"][TEXT]        = "2"; //2:見込点入力
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "SUPP_EXA_SDIV_DAT");

        return $query;
    }

    //削除
    function getDeleteQuery($model, $subclasscd, $schregno) {
        list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $subclasscd);

        $query  = " DELETE FROM ";
        $query .= "     SUPP_EXA_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->test["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$model->test["TESTKINDCD"]}' ";
        $query .= "     AND TESTITEMCD = '{$model->test["TESTITEMCD"]}' ";
        $query .= "     AND SCORE_DIV = '{$model->test["SCORE_DIV"]}' ";
        $query .= "     AND CLASSCD = '{$classcd}'  ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}'  ";
        $query .= "     AND CURRICULUM_CD = '{$curriculumCd}'  ";
        $query .= "     AND SUBCLASSCD = '{$subclasscd}'  ";
        $query .= "     AND SCHREGNO = '{$schregno}'  ";

        return $query;
    }

    //VALUE_DIが欠席者(*)または公欠者(**)のデータが対象
    function getKessekiQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.VALUE_DI, "; //欠席(*)または公欠(**)
        $query .= "     GAVG.AVG AS GRADE_AVG "; //欠席した学年平均
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "          ON REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND REGD.YEAR = T1.YEAR ";
        $query .= "         AND REGD.SEMESTER = T1.SEMESTER ";
        $query .= "         AND REGD.GRADE = '{$model->grade}' ";
        $query .= "     LEFT JOIN RECORD_AVERAGE_SDIV_DAT GAVG ";
        $query .= "          ON GAVG.YEAR = T1.YEAR ";
        $query .= "         AND GAVG.SEMESTER = T1.SEMESTER ";
        $query .= "         AND GAVG.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND GAVG.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND GAVG.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "         AND GAVG.CLASSCD = T1.CLASSCD ";
        $query .= "         AND GAVG.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         AND GAVG.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND GAVG.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND GAVG.AVG_DIV = '1' ";
        $query .= "         AND GAVG.GRADE = REGD.GRADE ";
        $query .= "         AND GAVG.HR_CLASS = '000' ";
        $query .= "         AND GAVG.COURSECD = '0' ";
        $query .= "         AND GAVG.MAJORCD= '000' ";
        $query .= "         AND GAVG.COURSECODE = '0000' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV = '{$model->testcd}' ";
        $query .= "     AND T1.SCORE IS NULL ";
        $query .= "     AND T1.VALUE_DI IN ('*','**') ";

        return $query;
    }

    //見込点算出
    function getMikomiScoreQuery($model) {
        $query  = " WITH KESSEKI AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.VALUE_DI, ";
        $query .= "         FLOAT(GAVG.AVG) AS AVG ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "              ON REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND REGD.YEAR = T1.YEAR ";
        $query .= "             AND REGD.SEMESTER = T1.SEMESTER ";
        $query .= "             AND REGD.GRADE = '{$model->grade}' ";
        $query .= "         LEFT JOIN RECORD_AVERAGE_SDIV_DAT GAVG ";
        $query .= "              ON GAVG.YEAR = T1.YEAR ";
        $query .= "             AND GAVG.SEMESTER = T1.SEMESTER ";
        $query .= "             AND GAVG.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "             AND GAVG.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "             AND GAVG.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "             AND GAVG.CLASSCD = T1.CLASSCD ";
        $query .= "             AND GAVG.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND GAVG.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND GAVG.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND GAVG.AVG_DIV = '1' ";
        $query .= "             AND GAVG.GRADE = REGD.GRADE ";
        $query .= "             AND GAVG.HR_CLASS = '000' ";
        $query .= "             AND GAVG.COURSECD = '0' ";
        $query .= "             AND GAVG.MAJORCD= '000' ";
        $query .= "             AND GAVG.COURSECODE = '0000' ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV = '{$model->testcd}' ";
        $query .= "         AND T1.SCORE IS NULL ";
        $query .= "         AND T1.VALUE_DI IN ('*','**') ";
        $query .= " ), SHUSSEKI AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         AVG(CASE WHEN T1.SEMESTER = '{$model->test["SEMESTER"]}' THEN FLOAT(T1.SCORE) END) AS SCORE1, ";
        $query .= "         AVG(CASE WHEN T1.SEMESTER = '{$model->test["SEMESTER"]}' THEN FLOAT(GAVG.AVG) END) AS AVG1, ";
        $query .= "         AVG(CASE WHEN T1.SEMESTER <> '9' THEN FLOAT(T1.SCORE) END) AS SCORE2, ";
        $query .= "         AVG(CASE WHEN T1.SEMESTER <> '9' THEN FLOAT(GAVG.AVG) END) AS AVG2 ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "              ON REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND REGD.YEAR = T1.YEAR ";
        $query .= "             AND REGD.SEMESTER = T1.SEMESTER ";
        $query .= "             AND REGD.GRADE = '{$model->grade}' ";
        $query .= "         LEFT JOIN RECORD_AVERAGE_SDIV_DAT GAVG ";
        $query .= "              ON GAVG.YEAR = T1.YEAR ";
        $query .= "             AND GAVG.SEMESTER = T1.SEMESTER ";
        $query .= "             AND GAVG.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "             AND GAVG.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "             AND GAVG.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "             AND GAVG.CLASSCD = T1.CLASSCD ";
        $query .= "             AND GAVG.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND GAVG.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND GAVG.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND GAVG.AVG_DIV = '1' ";
        $query .= "             AND GAVG.GRADE = REGD.GRADE ";
        $query .= "             AND GAVG.HR_CLASS = '000' ";
        $query .= "             AND GAVG.COURSECD = '0' ";
        $query .= "             AND GAVG.MAJORCD= '000' ";
        $query .= "             AND GAVG.COURSECODE = '0000' ";
        $query .= "         INNER JOIN KESSEKI KS ";
        $query .= "              ON KS.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND KS.CLASSCD = T1.CLASSCD ";
        $query .= "             AND KS.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND KS.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND KS.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV <> '{$model->testcd}' ";
        $query .= "         AND T1.TESTKINDCD IN ('01','02') ";
        $query .= "         AND T1.SCORE_DIV = '01' ";
        $query .= "         AND T1.SCORE IS NOT NULL ";
        $query .= "         AND T1.VALUE_DI IS NULL ";
        $query .= "     GROUP BY ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     KS.CLASSCD, ";
        $query .= "     KS.SCHOOL_KIND, ";
        $query .= "     KS.CURRICULUM_CD, ";
        $query .= "     KS.SUBCLASSCD, ";
        $query .= "     KS.SCHREGNO, ";
        $query .= "     CASE WHEN SS.SCORE1 IS NOT NULL AND KS.VALUE_DI = '*' ";
        $query .= "          THEN SS.SCORE1 * (KS.AVG / SS.AVG1) * (FLOAT({$model->mikomiRate}) / 100) ";
        $query .= "          WHEN SS.SCORE1 IS NOT NULL AND KS.VALUE_DI = '**' ";
        $query .= "          THEN SS.SCORE1 * (KS.AVG / SS.AVG1) ";
        $query .= "          WHEN SS.SCORE2 IS NOT NULL AND KS.VALUE_DI = '*' ";
        $query .= "          THEN SS.SCORE2 * (KS.AVG / SS.AVG2) * (FLOAT({$model->mikomiRate}) / 100) ";
        $query .= "          WHEN SS.SCORE2 IS NOT NULL AND KS.VALUE_DI = '**' ";
        $query .= "          THEN SS.SCORE2 * (KS.AVG / SS.AVG2) ";
        $query .= "          ELSE NULL ";
        $query .= "     END AS MIKOMI_SCORE ";
        $query .= " FROM ";
        $query .= "     KESSEKI KS ";
        $query .= "     LEFT JOIN SHUSSEKI SS ";
        $query .= "          ON SS.SCHREGNO = KS.SCHREGNO ";
        $query .= "         AND SS.CLASSCD = KS.CLASSCD ";
        $query .= "         AND SS.SCHOOL_KIND = KS.SCHOOL_KIND ";
        $query .= "         AND SS.CURRICULUM_CD = KS.CURRICULUM_CD ";
        $query .= "         AND SS.SUBCLASSCD = KS.SUBCLASSCD ";

        return $query;
    }

}
?>
