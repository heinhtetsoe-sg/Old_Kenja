<?php

require_once('for_php7.php');

class knjd126lQuery extends Query {
    //年組取得
    function getGradeHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "              ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "              ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";

        return $query;
    }

    function selectSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     REGD_G.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT REGD_G ";
        $query .= " WHERE ";
        $query .= "     REGD_G.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_G.GRADE = '{$grade}' ";

        return $query;
    }

    //科目取得
    function getSubclass($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD   = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND L1.CLASSCD      = T1.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.UNIT_ASSESSHIGH > 1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座取得
    function getChair($model) {
        //教育課程対応
        $query  = " WITH SCH_T AS (";
        $query .= knjd126lQuery::getSchList($model, "CHAIR");
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T1.YEAR       = T3.YEAR ";
        $query .= "     AND T1.SEMESTER   = T3.SEMESTER ";
        $query .= "     AND T1.CHAIRCD    = T3.CHAIRCD ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "     AND T2.SCHREGNO IN (SELECT SCH_T.SCHREGNO FROM SCH_T) ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "       AND T3.STAFFCD = '".STAFFCD."' ";
        }
        //教育課程対応
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト単元取得
    function getUnitTestDat($model, $seq="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEQ AS VALUE, ";
        $query .= "     T1.UNIT_L_NAME AS LABEL, ";
        $query .= "     T1.SORT, ";
        $query .= "     T1.UNIT_TEST_DATE ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT T1 ";
        $query .= "     INNER JOIN UNIT_TEST_INPUTSEQ_DAT S1 ";
        $query .= "              ON S1.YEAR         = T1.YEAR ";
        $query .= "             AND S1.GRADE        = T1.GRADE ";
        $query .= "             AND S1.HR_CLASS     = T1.HR_CLASS ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND S1.CLASSCD      = T1.CLASSCD ";
            $query .= "             AND S1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "             AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND S1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "             AND S1.SEQ          = T1.SEQ ";
        $query .= "             AND S1.UNIT_ASSESSHIGH > 1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.DATA_DIV = '2' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($seq) {
            $query .= " AND T1.SEQ = ".$seq." ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SORT, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //観点一覧取得
    function getViewList($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     L1.VIEWABBV, ";
        $query .= "     T1.UNIT_ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST L1 ";
        $query .= "              ON L1.GRADE        = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND L1.CLASSCD      = T1.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND L1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "             AND L1.VIEWCD       = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     RTRIM(CHAR(T1.SEQ))  = '".$model->field["SEQ"]."' AND ";
        $query .= "     T1.UNIT_ASSESSHIGH > 1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //生徒一覧取得
    function getSchList($model, $div = "") {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "              ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        if ($div != "CHAIR") {
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STD_DAT CHAIR_STD ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = CHAIR_STD.YEAR ";
            $query .= "             AND CHAIR_STD.CHAIRCD = '{$model->field["CHAIRCD"]}' ";
            $query .= "             AND T1.SCHREGNO = CHAIR_STD.SCHREGNO ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //成績データ取得
    function getScoreData($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.VIEWCD, ";
        $query .= "     L1.SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN UNIT_TEST_SCORE_DAT L1 ";
        $query .= "              ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "             AND L1.YEAR     = T1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "             AND L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "             AND RTRIM(CHAR(L1.SEQ)) = '".$model->field["SEQ"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.VIEWCD ";

        return $query;
    }

    //更新処理
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->Properties["useCurriculumcd"] == '1') {
            list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->field["SUBCLASSCD"]);
        } else {
            $subclasscd = $model->field["SUBCLASSCD"];
        }

        $total_score = 0;
        foreach ($model->field["SCHREGNO"] as $key => $schregno) {
            //削除 -- UNIT_TEST_SCORE_DAT
            $query  = " DELETE FROM ";
            $query .= "     UNIT_TEST_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $query .= "     SCHREGNO           = '".$schregno."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD         = '".$classcd."' AND ";
                $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
                $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
            }
            $query .= "     SUBCLASSCD      = '".$subclasscd."' AND ";
            $query .= "     SEQ             =  ".$model->field["SEQ"]." ";

            $db->query($query);

            //追加 -- UNIT_TEST_SCORE_DAT
            foreach ($model->viewList as $viewcd) {
                $data = array();
                $data["YEAR"][TEXT]                 = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]                = $schregno;
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]              = $classcd;
                    $data["SCHOOL_KIND"][TEXT]          = $school_kind;
                    $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
                }
                $data["SUBCLASSCD"][TEXT]           = $subclasscd;
                $data["SEQ"][NUMBER]                = $model->field["SEQ"];
                $data["VIEWCD"][TEXT]               = $viewcd;
                $data["SCORE"][NUMBER]              = $model->setData[$schregno]["SCORE_".$viewcd];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $query = Query::insertSQL($data, "UNIT_TEST_SCORE_DAT");
                $db->query($query);

                if (strlen($model->setData[$schregno]["SCORE_".$viewcd])) $total_score += $model->setData[$schregno]["SCORE_".$viewcd];
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
