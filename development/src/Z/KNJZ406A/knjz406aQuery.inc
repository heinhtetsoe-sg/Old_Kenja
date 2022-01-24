<?php

require_once('for_php7.php');

class knjz406aQuery extends Query {
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
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1      = '".STAFFCD."' ";
            $query .= "          OR TR_CD2      = '".STAFFCD."' ";
            $query .= "          OR TR_CD3      = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1   = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2   = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3   = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
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
        list($grade, $hr_class) = explode('-', $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1, ";
        $query .= "     JVIEWNAME_GRADE_YDAT T2, ";
        $query .= "     V_SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR         = T3.YEAR AND ";
        $query .= "     T2.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        $query .= "     T1.GRADE        = '".$grade."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "     T1.CLASSCD      = T3.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "     T1.SCHOOL_KIND  = T3.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
            $query .= "     T1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD   = T2.SUBCLASSCD AND ";
        $query .= "     T1.SUBCLASSCD   = T3.SUBCLASSCD AND";
        $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) <= '91' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //観点一覧取得
    function getViewList($model) {
        list($grade, $hr_class) = explode('-', $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     T1.VIEWCD AS VALUE, ";
        $query .= "     T1.VIEWCD || ':' || VALUE(T1.VIEWABBV,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT S1 ";
        $query .= "              ON S1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "             AND S1.GRADE        = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND S1.CLASSCD      = T1.CLASSCD ";
            $query .= "             AND S1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "             AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND S1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "             AND S1.VIEWCD       = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト単元データ取得
    function getUnitTestDat($model) {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     SORT, ";
        $query .= "     UNIT_L_NAME, ";
        $query .= "     UNIT_TEST_DATE ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV    = '2' AND ";
        $query .= "     GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SORT, ";
        $query .= "     SEQ ";

        return $query;
    }

    //データ取得
    function getUnitTestInputseqDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }

        return $query;
    }

    //更新処理
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list ($grade, $hr_class) = explode('-', $model->field["GRADE_HR_CLASS"]);

        if ($model->Properties["useCurriculumcd"] == '1') {
            list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->field["SUBCLASSCD"]);
        } else {
            $subclasscd = $model->field["SUBCLASSCD"];
        }

        foreach ($model->field["SEQ"] as $key => $seq) {
            //更新対象の単元のみ処理
            if ($model->setData[$seq]["CHECK"]) {

                //削除
                $query  = " DELETE FROM ";
                $query .= "     UNIT_TEST_INPUTSEQ_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
                $query .= "     GRADE           = '".$grade."' AND ";
                $query .= "     HR_CLASS        = '".$hr_class."' AND ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     CLASSCD         = '".$classcd."' AND ";
                    $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
                    $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
                }
                $query .= "     SUBCLASSCD      = '".$subclasscd."' AND ";
                $query .= "     SEQ             =  ".$seq." ";

                $db->query($query);

                //追加
                foreach ($model->viewList as $viewcd) {
                    $data = array();
                    $data["YEAR"][TEXT]                 = CTRL_YEAR;
                    $data["GRADE"][TEXT]                = $grade;
                    $data["HR_CLASS"][TEXT]             = $hr_class;
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data["CLASSCD"][TEXT]              = $classcd;
                        $data["SCHOOL_KIND"][TEXT]          = $school_kind;
                        $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
                    }
                    $data["SUBCLASSCD"][TEXT]           = $subclasscd;
                    $data["SEQ"][NUMBER]                = $seq;
                    $data["VIEWCD"][TEXT]               = $viewcd;
                    $data["VIEWFLG"][TEXT]              = ($model->setData[$seq]["VIEWFLG_".$viewcd]) ? "1" : "";
                    $data["UNIT_ASSESSHIGH"][NUMBER]    = $model->setData[$seq]["UNIT_ASSESSHIGH_".$viewcd];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][FUNC]              = "sysdate()";

                    $query = Query::insertSQL($data, "UNIT_TEST_INPUTSEQ_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //データ存在チェック -- UNIT_TEST_DAT
    function checkUnitTestDat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     DATA_DIV    = '2' ";

        return $query;
    }

    //データ存在チェック -- JVIEWNAME_GRADE_YDAT
    function checkJviewnameGradeYdat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //データ存在チェック -- UNIT_TEST_INPUTSEQ_DAT
    function checkUnitTestInpuseqDat($model, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(T1.VIEWCD) AS CNT ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT S1 ";
        $query .= "              ON T1.YEAR         = S1.YEAR ";
        $query .= "             AND T1.GRADE        = S1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T1.CLASSCD      = S1.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND  = S1.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD = S1.CURRICULUM_CD ";
        }
        $query .= "             AND T1.SUBCLASSCD   = S1.SUBCLASSCD ";
        $query .= "             AND T1.VIEWCD       = S1.VIEWCD ";
        $query .= "     INNER JOIN UNIT_TEST_DAT S2 ";
        $query .= "              ON T1.YEAR         = S2.YEAR ";
        $query .= "             AND S2.DATA_DIV     = '2' ";
        $query .= "             AND T1.GRADE        = S2.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T1.CLASSCD      = S2.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND  = S2.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD = S2.CURRICULUM_CD ";
        }
        $query .= "             AND T1.SUBCLASSCD   = S2.SUBCLASSCD ";
        $query .= "             AND T1.SEQ          = S2.SEQ ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) = '91' ";

        return $query;
    }

    //コピー
    function &getCopyQuery($db, $model) {
        $pre_year   = CTRL_YEAR - 1;
        $this_year  = CTRL_YEAR;

        $query  = " INSERT INTO UNIT_TEST_INPUTSEQ_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$this_year."' AS YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWFLG, ";
        $query .= "     T1.UNIT_ASSESSHIGH, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT S1 ";
        $query .= "              ON S1.YEAR         = '".$this_year."' ";
        $query .= "             AND T1.GRADE        = S1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T1.CLASSCD      = S1.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND  = S1.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD = S1.CURRICULUM_CD ";
        }
        $query .= "             AND T1.SUBCLASSCD   = S1.SUBCLASSCD ";
        $query .= "             AND T1.VIEWCD       = S1.VIEWCD ";
        $query .= "     INNER JOIN UNIT_TEST_DAT S2 ";
        $query .= "              ON S2.YEAR         = '".$this_year."' ";
        $query .= "             AND S2.DATA_DIV     = '2' ";
        $query .= "             AND T1.GRADE        = S2.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T1.CLASSCD      = S2.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND  = S2.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD = S2.CURRICULUM_CD ";
        }
        $query .= "             AND T1.SUBCLASSCD   = S2.SUBCLASSCD ";
        $query .= "             AND T1.SEQ          = S2.SEQ ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$pre_year."' AND ";
        $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) = '91' ";

        $db->query($query);

        return;
    }
}
?>
