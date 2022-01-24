<?php

require_once('for_php7.php');

class knjd128qQuery extends Query {

    //学期
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科
    function getCourseMajor($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     TEST_GCM.COURSECD || TEST_GCM.MAJORCD AS VALUE, ";
        $query .= "     VALUE(VC.COURSENAME, '') || VALUE(VM.MAJORNAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV TEST_GCM ";
        $query .= "     LEFT JOIN V_MAJOR_MST VM ON TEST_GCM.YEAR = VM.YEAR ";
        $query .= "          AND TEST_GCM.COURSECD = VM.COURSECD ";
        $query .= "          AND TEST_GCM.MAJORCD = VM.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSE_MST VC ON TEST_GCM.YEAR = VC.YEAR ";
        $query .= "          AND TEST_GCM.COURSECD = VC.COURSECD ";
        $query .= " WHERE ";
        $query .= "     TEST_GCM.YEAR = '{$model->year}' ";
        $query .= "     AND TEST_GCM.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND TEST_GCM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND TEST_GCM.SEMESTER = '{$model->semester}' ";
        $query .= "     AND TEST_GCM.SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト
    function getTest($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     TEST_GCM.TESTKINDCD || '-' || TEST_GCM.TESTKINDCD || '-' || TEST_GCM.SCORE_DIV AS VALUE, ";
        $query .= "     TEST_GCM.TESTKINDCD || TEST_GCM.TESTITEMCD || TEST_GCM.SCORE_DIV || '：' || TEST_GCM.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV TEST_GCM ";
        $query .= " WHERE ";
        $query .= "     TEST_GCM.YEAR = '{$model->year}' ";
        $query .= "     AND TEST_GCM.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND TEST_GCM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND TEST_GCM.SEMESTER = '{$model->semester}' ";
        $query .= "     AND TEST_GCM.SCORE_DIV = '01' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND TEST_GCM.COURSECD || TEST_GCM.MAJORCD = '{$model->courseMajor}' ";
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
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目
    function getSubclass($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || SUB_M.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || SUB_M.SUBCLASSCD AS VALUE, ";
        $query .= "     SUB_M.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_SDIV_DAT CHKFIN ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR_D ON CHKFIN.YEAR = CHAIR_D.YEAR ";
        $query .= "           AND CHKFIN.SEMESTER = CHAIR_D.SEMESTER ";
        $query .= "           AND CHKFIN.CHAIRCD = CHAIR_D.CHAIRCD ";
        $query .= "     INNER JOIN SUBCLASS_MST SUB_M ON CHAIR_D.CLASSCD = SUB_M.CLASSCD ";
        $query .= "           AND CHAIR_D.SCHOOL_KIND = SUB_M.SCHOOL_KIND ";
        $query .= "           AND CHAIR_D.CURRICULUM_CD = SUB_M.CURRICULUM_CD ";
        $query .= "           AND CHAIR_D.SUBCLASSCD = SUB_M.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     CHKFIN.YEAR = '{$model->year}' ";
        $query .= "     AND CHKFIN.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHKFIN.TESTKINDCD || '-' || CHKFIN.TESTITEMCD || '-' || CHKFIN.RECORD_DIV = '{$model->testCd}' ";
        $query .= "     AND CHKFIN.CHAIRCD IN (SELECT ";
        $query .= "                                I1.CHAIRCD ";
        $query .= "                            FROM ";
        $query .= "                                SCHREG_REGD_DAT REGD, ";
        $query .= "                                CHAIR_STD_DAT I1 ";
        $query .= "                            WHERE ";
        $query .= "                                REGD.YEAR = '{$model->year}' ";
        $query .= "                                AND REGD.SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "                                AND REGD.COURSECD || REGD.MAJORCD = '{$model->courseMajor}' ";
        }
        $query .= "                                AND REGD.YEAR = I1.YEAR ";
        $query .= "                                AND REGD.SEMESTER = I1.SEMESTER ";
        $query .= "                                AND REGD.SCHREGNO = I1.SCHREGNO ";
        $query .= "                           ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //得点
    function getScore($model, $schregNo) {
        $query  = " SELECT ";
        $query .= "     SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     SCORE.SCORE, ";
        $query .= "     SCORE.VALUE_DI, ";
        $query .= "     CASE WHEN PERFECT01.PASS_SCORE IS NOT NULL  ";
        $query .= "          THEN PERFECT01.PASS_SCORE ";
        $query .= "          ELSE CASE WHEN PERFECT02.PASS_SCORE IS NOT NULL  ";
        $query .= "               THEN PERFECT02.PASS_SCORE ";
        $query .= "               ELSE CASE WHEN PERFECT03.PASS_SCORE IS NOT NULL  ";
        $query .= "                         THEN PERFECT03.PASS_SCORE ";
        $query .= "                         ELSE PERFECT04.PASS_SCORE ";
        $query .= "                    END   ";
        $query .= "          END   ";
        $query .= "     END AS PASS_SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT SCORE ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON SCORE.YEAR = REGD.YEAR ";
        $query .= "           AND SCORE.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND SCORE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN PERFECT_RECORD_SDIV_DAT PERFECT01 ON SCORE.YEAR = PERFECT01.YEAR ";
        $query .= "           AND SCORE.SEMESTER = PERFECT01.SEMESTER ";
        $query .= "           AND SCORE.TESTKINDCD = PERFECT01.TESTKINDCD ";
        $query .= "           AND SCORE.TESTITEMCD = PERFECT01.TESTITEMCD ";
        $query .= "           AND PERFECT01.SCORE_DIV = '01' ";
        $query .= "           AND SCORE.CLASSCD = PERFECT01.CLASSCD ";
        $query .= "           AND SCORE.SCHOOL_KIND = PERFECT01.SCHOOL_KIND ";
        $query .= "           AND SCORE.CURRICULUM_CD = PERFECT01.CURRICULUM_CD ";
        $query .= "           AND SCORE.SUBCLASSCD = PERFECT01.SUBCLASSCD ";
        $query .= "           AND PERFECT01.DIV = '01' ";
        $query .= "     LEFT JOIN PERFECT_RECORD_SDIV_DAT PERFECT02 ON SCORE.YEAR = PERFECT02.YEAR ";
        $query .= "           AND SCORE.SEMESTER = PERFECT02.SEMESTER ";
        $query .= "           AND SCORE.TESTKINDCD = PERFECT02.TESTKINDCD ";
        $query .= "           AND SCORE.TESTITEMCD = PERFECT02.TESTITEMCD ";
        $query .= "           AND PERFECT02.SCORE_DIV = '01' ";
        $query .= "           AND SCORE.CLASSCD = PERFECT02.CLASSCD ";
        $query .= "           AND SCORE.SCHOOL_KIND = PERFECT02.SCHOOL_KIND ";
        $query .= "           AND SCORE.CURRICULUM_CD = PERFECT02.CURRICULUM_CD ";
        $query .= "           AND SCORE.SUBCLASSCD = PERFECT02.SUBCLASSCD ";
        $query .= "           AND PERFECT02.DIV = '02' ";
        $query .= "           AND PERFECT02.GRADE = '{$model->grade}' ";
        $query .= "           AND PERFECT02.COURSECD || PERFECT02.MAJORCD = '0000' ";
        $query .= "           AND PERFECT02.COURSECODE = '0000' ";
        $query .= "     LEFT JOIN PERFECT_RECORD_SDIV_DAT PERFECT03 ON SCORE.YEAR = PERFECT03.YEAR ";
        $query .= "           AND SCORE.SEMESTER = PERFECT03.SEMESTER ";
        $query .= "           AND SCORE.TESTKINDCD = PERFECT03.TESTKINDCD ";
        $query .= "           AND SCORE.TESTITEMCD = PERFECT03.TESTITEMCD ";
        $query .= "           AND PERFECT03.SCORE_DIV = '01' ";
        $query .= "           AND SCORE.CLASSCD = PERFECT03.CLASSCD ";
        $query .= "           AND SCORE.SCHOOL_KIND = PERFECT03.SCHOOL_KIND ";
        $query .= "           AND SCORE.CURRICULUM_CD = PERFECT03.CURRICULUM_CD ";
        $query .= "           AND SCORE.SUBCLASSCD = PERFECT03.SUBCLASSCD ";
        $query .= "           AND PERFECT03.DIV = '03' ";
        $query .= "           AND REGD.GRADE = PERFECT03.GRADE ";
        $query .= "           AND REGD.COURSECD = PERFECT03.COURSECD ";
        $query .= "           AND REGD.MAJORCD = PERFECT03.MAJORCD ";
        $query .= "           AND REGD.COURSECODE = PERFECT03.COURSECODE ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT GROUP_CD ON REGD.YEAR = GROUP_CD.YEAR ";
        $query .= "           AND REGD.GRADE = GROUP_CD.GRADE ";
        $query .= "           AND REGD.COURSECD = GROUP_CD.COURSECD ";
        $query .= "           AND REGD.MAJORCD = GROUP_CD.MAJORCD ";
        $query .= "           AND REGD.COURSECODE = GROUP_CD.COURSECODE ";
        $query .= "     LEFT JOIN PERFECT_RECORD_SDIV_DAT PERFECT04 ON SCORE.YEAR = PERFECT04.YEAR ";
        $query .= "           AND SCORE.SEMESTER = PERFECT04.SEMESTER ";
        $query .= "           AND SCORE.TESTKINDCD = PERFECT04.TESTKINDCD ";
        $query .= "           AND SCORE.TESTITEMCD = PERFECT04.TESTITEMCD ";
        $query .= "           AND PERFECT04.SCORE_DIV = '01' ";
        $query .= "           AND SCORE.CLASSCD = PERFECT04.CLASSCD ";
        $query .= "           AND SCORE.SCHOOL_KIND = PERFECT04.SCHOOL_KIND ";
        $query .= "           AND SCORE.CURRICULUM_CD = PERFECT04.CURRICULUM_CD ";
        $query .= "           AND SCORE.SUBCLASSCD = PERFECT04.SUBCLASSCD ";
        $query .= "           AND PERFECT04.DIV = '04' ";
        $query .= "           AND PERFECT04.GRADE = '{$model->grade}' ";
        $query .= "           AND PERFECT04.COURSECD = '0' ";
        $query .= "           AND PERFECT04.MAJORCD = GROUP_CD.GROUP_CD ";
        $query .= "           AND PERFECT04.COURSECODE = '0000' ";
        $query .= " WHERE ";
        $query .= "     SCORE.YEAR = '{$model->year}' ";
        $query .= "     AND SCORE.SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV = '{$model->testCd}' ";
        $query .= "     AND SCORE.SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    //追試
    function getSupp($model, $schregNo) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     SCORE, ";
        $query .= "     SCORE_DI ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND TESTKINDCD || '-' || TESTITEMCD  || '-' || SCORE_DIV = '{$model->testCd}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    //一覧
    function getSchregList($model) {
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ";
        $query .= "          AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND HDAT.GRADE = REGD.GRADE ";
        $query .= "          AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '{$model->year}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->hr_class}' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND REGD.COURSECD || REGD.MAJORCD = '{$model->courseMajor}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //生徒情報
    function getSchregInfo($model) {
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ";
        $query .= "          AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND HDAT.GRADE = REGD.GRADE ";
        $query .= "          AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '{$model->year}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND REGD.SCHREGNO = '{$model->sendSchregNo}' ";

        return $query;
    }

    //生徒情報
    function getUpdateQuery($db, $model, $updateData) {
        list($testkindCd, $testitemCd, $scoreDiv) = preg_split("/-/", $model->testCd);
        $query  = " DELETE FROM ";
        $query .= "     SUPP_EXA_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND TESTKINDCD = '{$testkindCd}' ";
        $query .= "     AND TESTITEMCD = '{$testitemCd}' ";
        $query .= "     AND SCORE_DIV = '{$scoreDiv}' ";
        $query .= "     AND CLASSCD = '{$updateData["CLASSCD"]}' ";
        $query .= "     AND SCHOOL_KIND = '{$updateData["SCHOOL_KIND"]}' ";
        $query .= "     AND CURRICULUM_CD = '{$updateData["CURRICULUM_CD"]}' ";
        $query .= "     AND SUBCLASSCD = '{$updateData["SUBCLASSCD"]}' ";
        $query .= "     AND SCHREGNO = '{$updateData["SCHREGNO"]}' ";
        $db->query($query);

        if (strlen($updateData["SCORE"]) > 0 || strlen($updateData["SCORE_DI"]) > 0) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->year;
            $data["SEMESTER"][TEXT]         = $model->semester;
            $data["TESTKINDCD"][TEXT]       = $testkindCd;
            $data["TESTITEMCD"][TEXT]       = $testitemCd;
            $data["SCORE_DIV"][TEXT]        = "01";
            $data["CLASSCD"][TEXT]          = $updateData["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $updateData["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $updateData["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]       = $updateData["SUBCLASSCD"];
            $data["SCHREGNO"][TEXT]         = $updateData["SCHREGNO"];
            $data["SCORE"][NUMBER]          = $updateData["SCORE"];
            $data["SCORE_PASS"][NUMBER]     = $updateData["SCORE_PASS"];
            $data["SCORE_PASS_FLG"][TEXT]   = $updateData["SCORE_PASS_FLG"];
            $data["SCORE_FLG"][TEXT]        = $updateData["SCORE_FLG"];
            $data["SCORE_DI"][TEXT]         = $updateData["SCORE_DI"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "SUPP_EXA_SDIV_DAT");
            $db->query($query);
        }
    }
}
?>
