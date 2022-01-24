<?php

require_once('for_php7.php');


// kanji=漢字

class knjd210cquery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($seme)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$seme."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別
    function getTest($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新用
    function getTestData($model)
    {
        $fieldName = "SCORE";
        if (substr($model->field["TESTKIND"], 0, 2) == "99" && $model->field["SEMESTER"] != "9") {
            $fieldName = "VALUE";
        }
        $query  = " WITH TAISYOU AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.COMBINED_CLASSCD, ";
            $query .= " T1.COMBINED_SCHOOL_KIND, ";
            $query .= " T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.ATTEND_CLASSCD, ";
            $query .= " T1.ATTEND_SCHOOL_KIND, ";
            $query .= " T1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        $query .= "     T1.WEIGHTING ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_GRADES_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ), ATTEND_COMB AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.COMBINED_CLASSCD, ";
            $query .= " T1.COMBINED_SCHOOL_KIND, ";
            $query .= " T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.ATTEND_CLASSCD, ";
            $query .= " T1.ATTEND_SCHOOL_KIND, ";
            $query .= " T1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        $query .= "     T1.WEIGHTING ";
        $query .= " FROM ";
        $query .= "     TAISYOU T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS(SELECT ";
        $query .= "                'X' ";
        $query .= "            FROM ";
        $query .= "                TAISYOU T2 ";
        $query .= "            WHERE ";
        $query .= "                T2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            AND T2.COMBINED_CLASSCD = T1.ATTEND_CLASSCD ";
            $query .= "            AND T2.COMBINED_SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ";
            $query .= "            AND T2.COMBINED_CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "                AND T2.COMBINED_SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "           ) ";
        $query .= " ), ATTEND_ONLY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.COMBINED_CLASSCD, ";
            $query .= " T1.COMBINED_SCHOOL_KIND, ";
            $query .= " T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.ATTEND_CLASSCD, ";
            $query .= " T1.ATTEND_SCHOOL_KIND, ";
            $query .= " T1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        $query .= "     T1.WEIGHTING ";
        $query .= " FROM ";
        $query .= "     TAISYOU T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                    'X' ";
        $query .= "                FROM ";
        $query .= "                    ATTEND_COMB T2 ";
        $query .= "                WHERE ";
        $query .= "                    T2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                AND T2.COMBINED_CLASSCD = T1.COMBINED_CLASSCD ";
            $query .= "                AND T2.COMBINED_SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
            $query .= "                AND T2.COMBINED_CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        }
        $query .= "                    AND T2.COMBINED_SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= "               ) ";
        $query .= " ), ATTEND_ONLY_SUM AS ( ";
        $query .= " SELECT ";
        $query .= "     '1' AS ORDER_CD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     REGD.SCHREGNO, ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     '9' AS SEMESTER, ";
        } else {
            $query .= "     REGD.SEMESTER, ";
        }
        $query .= "     L1.TESTKINDCD, ";
        $query .= "     L1.TESTITEMCD, ";
        $query .= "     L1.SCORE_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.COMBINED_CLASSCD, ";
            $query .= " T1.COMBINED_SCHOOL_KIND, ";
            $query .= " T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     SUM(CASE WHEN L1.{$fieldName} IS NULL THEN 1 ELSE 0 END) AS NULL_CNT, ";
        $query .= "     SUM(FLOAT(L1.{$fieldName}) / 100 * T1.WEIGHTING) AS VAL, ";
        $query .= "     SUM(L1.COMP_CREDIT) AS COMP_CREDIT, ";
        $query .= "     SUM(L1.GET_CREDIT) AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     ATTEND_ONLY T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ";
        $query .= "          AND REGD.SEMESTER = '".$model->seme."' ";
        $query .= "          AND T1.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND L1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "          AND L1.TESTKINDCD || L1.TESTITEMCD = '".$model->field["TESTKIND"]."' ";
        if (substr($model->field["TESTKIND"], 0, 2) == "99") {
            $query .= "          AND L1.SCORE_DIV = '00' ";
        } else {
            $query .= "          AND L1.SCORE_DIV = '01' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      AND T1.ATTEND_CLASSCD = L1.CLASSCD ";
            $query .= "      AND T1.ATTEND_SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "      AND T1.ATTEND_CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= "          AND T1.ATTEND_SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= "          AND REGD.SCHREGNO = L1.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     REGD.SCHREGNO, ";
        if ($model->field["SEMESTER"] != "9") {
            $query .= "     REGD.SEMESTER, ";
        }
        $query .= "     L1.TESTKINDCD, ";
        $query .= "     L1.TESTITEMCD, ";
        $query .= "     L1.SCORE_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.COMBINED_CLASSCD, ";
            $query .= " T1.COMBINED_SCHOOL_KIND, ";
            $query .= " T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        $query .= " ), COMB_ONLY_SUM AS ( ";
        $query .= " SELECT ";
        $query .= "     '2' AS ORDER_CD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     REGD.SCHREGNO, ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     '9' AS SEMESTER, ";
        } else {
            $query .= "     REGD.SEMESTER, ";
        }
        $query .= "     L1.TESTKINDCD, ";
        $query .= "     L1.TESTITEMCD, ";
        $query .= "     L1.SCORE_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.COMBINED_CLASSCD, ";
            $query .= " T1.COMBINED_SCHOOL_KIND, ";
            $query .= " T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     SUM(CASE WHEN L1.{$fieldName} IS NULL THEN 1 ELSE 0 END) AS NULL_CNT, ";
        $query .= "     SUM(FLOAT(CASE WHEN L2.COMBINED_SUBCLASSCD IS NOT NULL ";
        $query .= "                    THEN L2.VAL ";
        $query .= "                    ELSE L1.{$fieldName} ";
        $query .= "                    END ";
        $query .= "              ) / 100 * T1.WEIGHTING) AS VAL, ";
        $query .= "     SUM(CASE WHEN L2.COMBINED_SUBCLASSCD IS NOT NULL ";
        $query .= "              THEN L2.COMP_CREDIT ";
        $query .= "              ELSE L1.COMP_CREDIT ";
        $query .= "              END) AS COMP_CREDIT, ";
        $query .= "     SUM(CASE WHEN L2.COMBINED_SUBCLASSCD IS NOT NULL ";
        $query .= "              THEN L2.GET_CREDIT ";
        $query .= "              ELSE L1.GET_CREDIT ";
        $query .= "              END) AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     TAISYOU T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ";
        $query .= "          AND REGD.SEMESTER = '".$model->seme."' ";
        $query .= "          AND T1.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND L1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "          AND L1.TESTKINDCD || L1.TESTITEMCD = '".$model->field["TESTKIND"]."' ";
        if (substr($model->field["TESTKIND"], 0, 2) == "99") {
            $query .= "          AND L1.SCORE_DIV = '00' ";
        } else {
            $query .= "          AND L1.SCORE_DIV = '01' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      AND T1.ATTEND_CLASSCD = L1.CLASSCD ";
            $query .= "      AND T1.ATTEND_SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "      AND T1.ATTEND_CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= "          AND T1.ATTEND_SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= "          AND REGD.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN ATTEND_ONLY_SUM L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.GRADE = L2.GRADE ";
        $query .= "          AND L1.SCHREGNO = L2.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      AND T1.ATTEND_CLASSCD = L2.COMBINED_CLASSCD ";
            $query .= "      AND T1.ATTEND_SCHOOL_KIND = L2.COMBINED_SCHOOL_KIND ";
            $query .= "      AND T1.ATTEND_CURRICULUM_CD = L2.COMBINED_CURRICULUM_CD ";
        }
        $query .= "          AND T1.ATTEND_SUBCLASSCD = L2.COMBINED_SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                    'X' ";
        $query .= "                FROM ";
        $query .= "                    ATTEND_ONLY T2 ";
        $query .= "                WHERE ";
        $query .= "                    T2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                AND T2.COMBINED_CLASSCD = T1.COMBINED_CLASSCD ";
            $query .= "                AND T2.COMBINED_SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
            $query .= "                AND T2.COMBINED_CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        }
        $query .= "                    AND T2.COMBINED_SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                AND T2.ATTEND_CLASSCD = T1.ATTEND_CLASSCD ";
            $query .= "                AND T2.ATTEND_SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ";
            $query .= "                AND T2.ATTEND_CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "                    AND T2.ATTEND_SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "               ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     REGD.SCHREGNO, ";
        if ($model->field["SEMESTER"] != "9") {
            $query .= "     REGD.SEMESTER, ";
        }
        $query .= "     L1.TESTKINDCD, ";
        $query .= "     L1.TESTITEMCD, ";
        $query .= "     L1.SCORE_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.COMBINED_CLASSCD, ";
            $query .= " T1.COMBINED_SCHOOL_KIND, ";
            $query .= " T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     ORDER_CD, ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     SCHREGNO, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " COMBINED_CLASSCD, ";
            $query .= " COMBINED_SCHOOL_KIND, ";
            $query .= " COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     COMBINED_SUBCLASSCD, ";
        $query .= "     CASE WHEN NULL_CNT = 0 ";
        $query .= "          THEN SMALLINT(ROUND(FLOAT(DECIMAL(VAL,5,1)),0)) ";
        $query .= "          ELSE CAST(NULL AS SMALLINT) ";
        $query .= "     END AS VAL, ";
        $query .= "     COMP_CREDIT, ";
        $query .= "     GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     ATTEND_ONLY_SUM ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     ORDER_CD, ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     SCHREGNO, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " COMBINED_CLASSCD, ";
            $query .= " COMBINED_SCHOOL_KIND, ";
            $query .= " COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     COMBINED_SUBCLASSCD, ";
        $query .= "     CASE WHEN NULL_CNT = 0 ";
        $query .= "          THEN SMALLINT(ROUND(FLOAT(DECIMAL(VAL,5,1)),0)) ";
        $query .= "          ELSE CAST(NULL AS SMALLINT) ";
        $query .= "     END AS VAL, ";
        $query .= "     COMP_CREDIT, ";
        $query .= "     GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     COMB_ONLY_SUM ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ORDER_CD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " COMBINED_CLASSCD, ";
            $query .= " COMBINED_SCHOOL_KIND, ";
            $query .= " COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     COMBINED_SUBCLASSCD ";

        return $query;
    }

    //更新用・・・未使用
    function getGradTestData($model)
    {
        $fieldName = "SCORE";
        if (substr($model->field["TESTKIND"], 0, 2) == "99") {
            $fieldName = "VALUE";
        }
        $query .= " WITH TAISYOU AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        $query .= "     T1.WEIGHTING ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_GRADES_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ), COMB_ONLY AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     TAISYOU T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                T1.COMBINED_SUBCLASSCD ";
        $query .= "            FROM ";
        $query .= "                TAISYOU T2 ";
        $query .= "            WHERE ";
        $query .= "                T1.GRADE = T2.GRADE ";
        $query .= "                AND T1.COMBINED_SUBCLASSCD = T2.ATTEND_SUBCLASSCD ";
        $query .= "           ) ";
        $query .= " ), T_MAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     L1.TESTKINDCD, ";
        $query .= "     L1.TESTITEMCD, ";
        $query .= "     L1.SCORE_DIV, ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     ROUND(ROUND(AVG(FLOAT(L1.{$fieldName})) * 10, 0) / 10, 0) AS VAL ";
        $query .= " FROM ";
        $query .= "     COMB_ONLY T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ";
        $query .= "          AND REGD.SEMESTER = '".$model->seme."' ";
        $query .= "          AND T1.GRADE = REGD.GRADE ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND L1.SEMESTER < '".$model->field["SEMESTER"]."' ";
        $query .= "          AND L1.TESTKINDCD || L1.TESTITEMCD = '".$model->field["TESTKIND"]."' ";
        if (substr($model->field["TESTKIND"], 0, 2) == "99") {
            $query .= "          AND L1.SCORE_DIV = '00' ";
        } else {
            $query .= "          AND L1.SCORE_DIV = '01' ";
        }
        $query .= "          AND T1.COMBINED_SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= "          AND REGD.SCHREGNO = L1.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     L1.TESTKINDCD, ";
        $query .= "     L1.TESTITEMCD, ";
        $query .= "     L1.SCORE_DIV, ";
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T1.VAL AS SCORE, ";
        $query .= "     L1.ASSESSLEVEL AS VALUE ";
        $query .= " FROM ";
        $query .= "     T_MAIN T1 ";
        $query .= "     LEFT JOIN ASSESS_MST L1 ON L1.ASSESSCD = '3' ";
        $query .= "          AND T1.VAL BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ";

        return $query;
    }

    //更新処理
    function insertExe($model)
    {
        $fieldName = "SCORE";
        if (substr($model->field["TESTKIND"], 0, 2) == "99") {
            $fieldName = "VALUE";
        }
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

//        if ($model->field["SEMESTER"] == "9") {
//            $query = knjd210cQuery::getGradTestData($model);
//        } else {
            $query = knjd210cQuery::getTestData($model);
//        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if (!$row["SCORE_DIV"]) {
                if (substr($model->field["TESTKIND"], 0, 2) == "99") {
                    $row["SCORE_DIV"] = "00";
                } else {
                    $row["SCORE_DIV"] = "01";
                }
            }
            if (!$row["TESTKINDCD"]) {
                $row["TESTKINDCD"] = substr($model->field["TESTKIND"], 0, 2);
                $row["TESTITEMCD"] = substr($model->field["TESTKIND"], 2);
            }


            $data = array();
            if ($model->field["SEMESTER"] == "9") {
                $data["SCORE"][NUMBER]          = $row["VAL"];
                //評定・履修単位・修得単位
                $gradValue = $db->getOne(knjd210cQuery::getAssessMst($row["VAL"]));
                $data["VALUE"][NUMBER]          = $gradValue;
                $data["COMP_CREDIT"][NUMBER]    = $row["COMP_CREDIT"];
                $data["GET_CREDIT"][NUMBER]     = ($gradValue == 1 && strlen($row["GET_CREDIT"])) ? "0" : $row["GET_CREDIT"];
            } else {
                $data[$fieldName][NUMBER]   = $row["VAL"];
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
            //条件クエリー
            $where = knjd210cQuery::getWhereQuery($row, $model);
            //存在クエリー
            $query = knjd210cQuery::getExistsQuery($where);
            if (0 < $db->getOne($query)) {
                //更新
                $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
                $db->query($query);
            } else {
                $data["YEAR"][TEXT]         = $row["YEAR"];
                $data["SEMESTER"][TEXT]     = $row["SEMESTER"];
                $data["TESTKINDCD"][TEXT]   = $row["TESTKINDCD"];
                $data["TESTITEMCD"][TEXT]   = $row["TESTITEMCD"];
                $data["SCORE_DIV"][TEXT]    = $row["SCORE_DIV"];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]       = $row["COMBINED_CLASSCD"];
                    $data["SCHOOL_KIND"][TEXT]   = $row["COMBINED_SCHOOL_KIND"];
                    $data["CURRICULUM_CD"][TEXT] = $row["COMBINED_CURRICULUM_CD"];
                }
                $data["SUBCLASSCD"][TEXT]   = $row["COMBINED_SUBCLASSCD"];
                $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
                //追加
                $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
                $db->query($query);
            }
        }

        //DB切断
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //条件
    function getWhereQuery($row, $model)
    {
        $where  = " WHERE ";
        $where .= "     YEAR = '".$row["YEAR"]."' ";
        $where .= "     AND SEMESTER = '".$row["SEMESTER"]."' ";
        $where .= "     AND TESTKINDCD || TESTITEMCD = '".$row["TESTKINDCD"].$row["TESTITEMCD"]."' ";
        $where .= "     AND SCORE_DIV = '".$row["SCORE_DIV"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= " AND CLASSCD = '".$row["COMBINED_CLASSCD"]."' ";
            $where .= " AND SCHOOL_KIND = '".$row["COMBINED_SCHOOL_KIND"]."' ";
            $where .= " AND CURRICULUM_CD = '".$row["COMBINED_CURRICULUM_CD"]."' ";
        }
        $where .= "     AND SUBCLASSCD = '".$row["COMBINED_SUBCLASSCD"]."' ";
        $where .= "     AND SCHREGNO = '".$row["SCHREGNO"]."' ";

        return $where;
    }

    //存在
    function getExistsQuery($where)
    {
        $query = " SELECT COUNT(*) FROM RECORD_SCORE_DAT ".$where;

        return $query;
    }

    //評定
    function getAssessMst($score)
    {
        if (!strlen($score)) $score = "-1";

        $query  = " SELECT ASSESSLEVEL FROM ASSESS_MST ";
        $query .= " WHERE ASSESSCD = '3' AND {$score} BETWEEN ASSESSLOW AND ASSESSHIGH ";

        return $query;
    }

}
?>