<?php

require_once('for_php7.php');

class knjz233aQuery extends Query {

    //課程学科コース取得
    function getCourseMajor($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 || ' (' || T1.COURSECD || T1.MAJORCD || ') ' || T3.COURSENAME || T3.MAJORNAME || ";
        $query .= "     ' (' || T1.COURSECODE || ') ' || T4.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     V_COURSE_MAJOR_MST T3, ";
        $query .= "     V_COURSECODE_MST T4 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = T3.YEAR AND ";
        $query .= "     T1.YEAR         = T4.YEAR AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        $query .= "     T1.COURSECD     = T3.COURSECD AND ";
        $query .= "     T1.MAJORCD      = T3.MAJORCD AND ";
        $query .= "     T1.COURSECODE   = T4.COURSECODE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE";
        
        return $query;
    }

    //リスト取得
    function getList($model) {
        list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->leftField["GRADE_COURSE"]);

        $query .= " SELECT ";
        $query .= "     T1.SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME AS COMBINED_SUBCLASS_NAME, ";
        $query .= "     L1.SEQ, ";
        $query .= "     L1.ATTEND_SUBCLASSCD, ";
        $query .= "     L2.SUBCLASSNAME AS ATTEND_SUBCLASSCD_NAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD AS COMBINED_CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND AS COMBINED_SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD AS COMBINED_CURRICULUM_CD, ";
            $query .= "     L1.ATTEND_CLASSCD, ";
            $query .= "     L1.ATTEND_SCHOOL_KIND, ";
            $query .= "     L1.ATTEND_CURRICULUM_CD, ";
        }
        if ($model->Properties["weightingHyouki"] == "1") {
            $query .= "     DECIMAL(L1.WEIGHTING2,4,1) AS WEIGHTING ";
        } else {
            $query .= "     L1.WEIGHTING ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_WEIGHTING_COURSE_DAT L1 ON ";
        $query .= "         T1.YEAR         = L1.YEAR AND ";
        $query .= "         L1.FLG          = '".$model->leftField["FLG"]."' AND ";
        $query .= "         L1.GRADE        = '".$grade."' AND ";
        $query .= "         L1.COURSECD     = '".$coursecd."' AND ";
        $query .= "         L1.MAJORCD      = '".$majorcd."' AND ";
        $query .= "         L1.COURSECODE   = '".$coursecode."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD   = L1.COMBINED_CLASSCD AND ";
            $query .= "         T1.SCHOOL_KIND   = L1.COMBINED_SCHOOL_KIND AND ";
            $query .= "         T1.CURRICULUM_CD   = L1.COMBINED_CURRICULUM_CD AND ";
        }
        $query .= "         T1.SUBCLASSCD   = L1.COMBINED_SUBCLASSCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON ";
        $query .= "         L1.ATTEND_SUBCLASSCD = L2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND L1.ATTEND_CLASSCD   = L2.CLASSCD ";
            $query .= "     AND L1.ATTEND_SCHOOL_KIND   = L2.SCHOOL_KIND ";
            $query .= "     AND L1.ATTEND_CURRICULUM_CD   = L2.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.SEQ, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.ATTEND_CLASSCD, ";
            $query .= "         L1.ATTEND_SCHOOL_KIND, ";
            $query .= "         L1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     L1.ATTEND_SUBCLASSCD ";

        return $query;

    }

    //読替先科目数取得
    function getSubclassCnt($model, $subclasscd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
        list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->leftField["GRADE_COURSE"]);

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     FLG                 = '".$model->leftField["FLG"]."' AND ";
        $query .= "     GRADE               = '".$grade."' AND ";
        $query .= "     COURSECD            = '".$coursecd."' AND ";
        $query .= "     MAJORCD             = '".$majorcd."' AND ";
        $query .= "     COURSECODE          = '".$coursecode."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     COMBINED_CLASSCD        = '".$subclass_array[0]."' AND ";
            $query .= "     COMBINED_SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
            $query .= "     COMBINED_CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
            $query .= "     COMBINED_SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "     COMBINED_SUBCLASSCD = '".$subclasscd."' ";
        }
        
        return $query;
    }

    //ＳＥＱ数取得
    function getSeqCnt($model, $subclasscd, $seq) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
        list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->leftField["GRADE_COURSE"]);

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "     FLG                 = '".$model->leftField["FLG"]."' AND ";
        $query .= "     GRADE               = '".$grade."' AND ";
        $query .= "     COURSECD            = '".$coursecd."' AND ";
        $query .= "     MAJORCD             = '".$majorcd."' AND ";
        $query .= "     COURSECODE          = '".$coursecode."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     COMBINED_CLASSCD        = '".$subclass_array[0]."' AND ";
            $query .= "     COMBINED_SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
            $query .= "     COMBINED_CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
            $query .= "     COMBINED_SUBCLASSCD     = '".$subclass_array[3]."' AND";
        } else {
            $query .= "     COMBINED_SUBCLASSCD = '".$subclasscd."' AND ";
        }
        $query .= "     SEQ                 = '".$seq."' ";

        return $query;
    }

    //文京判定取得
    function checkBunkyo() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = 'bunkyo' ";

        return $query;
    }

    //読替先科目名称取得
    function getCombinedSubclass($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->combined_subclasscd);
        }
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD   = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD      = '".$subclass_array[3]."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->combined_subclasscd."' ";
        }

        return $query;
    }

    //教科名称取得
    function getClassName($classCd, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classCd);
        }
        $query  = " SELECT ";
        $query .= "     CLASSNAME ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD          = '".$class_array[0]."' ";
            $query .= "     AND SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     CLASSCD = '".$classCd."' ";
        }

        return $query;
    }

    //読替元科目取得
    function getAttendSubclass($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->combined_subclasscd);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME AS ATTEND_SUBCLASS_NAME, ";
        $query .= "     L1.COMBINED_SUBCLASSCD, ";
        $query .= "     L2.SUBCLASSNAME AS COMBINED_SUBCLASS_NAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     L1.COMBINED_CLASSCD, ";
            $query .= "     L1.COMBINED_SCHOOL_KIND, ";
            $query .= "     L1.COMBINED_CURRICULUM_CD, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD AS ATTEND_SUBCLASSCD, ";
        if($model->send_flg == "SEQ") {
            if ($model->Properties["weightingHyouki"] == "1") {
                $query .= "     DECIMAL(L1.WEIGHTING2,4,1) AS WEIGHTING ";
            } else {
                $query .= "     L1.WEIGHTING ";
            }
        } else {
            $query .= "     '' AS WEIGHTING ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_WEIGHTING_COURSE_DAT L1 ON ";
        $query .= "         T1.YEAR                 = L1.YEAR AND ";
        $query .= "         L1.FLG                  = '".$model->flg."' AND ";
        $query .= "         L1.GRADE                = '".$model->grade."' AND ";
        $query .= "         L1.COURSECD             = '".$model->coursecd."' AND ";
        $query .= "         L1.MAJORCD              = '".$model->majorcd."' AND ";
        $query .= "         L1.COURSECODE           = '".$model->coursecode."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD          = L1.ATTEND_CLASSCD AND ";
            $query .= "         T1.SCHOOL_KIND      = L1.ATTEND_SCHOOL_KIND AND ";
            $query .= "         T1.CURRICULUM_CD    = L1.ATTEND_CURRICULUM_CD AND ";
        }
        $query .= "         T1.SUBCLASSCD           = L1.ATTEND_SUBCLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.COMBINED_CLASSCD         = '{$subclass_array[0]}' AND ";
            $query .= "         L1.COMBINED_SCHOOL_KIND     = '{$subclass_array[1]}' AND ";
            $query .= "         L1.COMBINED_CURRICULUM_CD   = '{$subclass_array[2]}' AND ";
            $query .= "         L1.COMBINED_SUBCLASSCD      = '{$subclass_array[3]}' ";
        } else {
            $query .= "         L1.COMBINED_SUBCLASSCD  = '".$model->combined_subclasscd."' ";
        }
        if($model->send_flg == "SEQ") {
            $query .= "     AND L1.SEQ  = '".$model->seq."' ";
        }
        $query .= "     LEFT JOIN V_SUBCLASS_MST L2 ON ";
        $query .= "         L1.YEAR                 = L2.YEAR AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.COMBINED_CLASSCD  = L2.CLASSCD AND ";
            $query .= "         L1.COMBINED_SCHOOL_KIND  = L2.SCHOOL_KIND AND ";
            $query .= "         L1.COMBINED_CURRICULUM_CD  = L2.CURRICULUM_CD AND ";
        }
        $query .= "         L1.COMBINED_SUBCLASSCD  = L2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD         = '{$subclass_array[0]}' AND ";
            $query .= "     T1.SCHOOL_KIND     = '{$subclass_array[1]}' AND ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) = '".substr($model->combined_subclasscd, 0, 2)."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ('".$model->combined_subclasscd."') ";
        } else {
            $query .= "     T1.SUBCLASSCD NOT IN ('".$model->combined_subclasscd."') ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //存在チェック
    function checkExists($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->combined_subclasscd);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     FLG         = '".$model->flg."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     COURSECD    = '".$model->coursecd."' AND ";
        $query .= "     MAJORCD     = '".$model->majorcd."' AND ";
        $query .= "     COURSECODE  = '".$model->coursecode."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         COMBINED_CLASSCD         = '{$subclass_array[0]}' AND ";
            $query .= "         COMBINED_SCHOOL_KIND     = '{$subclass_array[1]}' AND ";
            $query .= "         COMBINED_CURRICULUM_CD   = '{$subclass_array[2]}' AND ";
            $query .= "         COMBINED_SUBCLASSCD      = '{$subclass_array[3]}' AND ";
        } else {
            $query .= "     COMBINED_SUBCLASSCD  = '".$model->combined_subclasscd."' AND ";
        }
        $query .= "     SEQ         = '".$model->field["SEQ"]."' ";

        return $query;
    }

    //重複チェック
    function checkDuplication($model, $subclasscd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->combined_subclasscd);
        }
        $query  = " WITH SUBCLASS_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEQ, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         COUNT(ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD) AS SUB_CNT ";
        } else {
            $query .= "         COUNT(ATTEND_SUBCLASSCD) AS SUB_CNT ";
        }
        $query .= "     FROM ";
        $query .= "         SUBCLASS_WEIGHTING_COURSE_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         FLG          = '".$model->flg."' AND ";
        $query .= "         GRADE        = '".$model->grade."' AND ";
        $query .= "         COURSECD     = '".$model->coursecd."' AND ";
        $query .= "         MAJORCD      = '".$model->majorcd."' AND ";
        $query .= "         COURSECODE   = '".$model->coursecode."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         COMBINED_CLASSCD        = '{$subclass_array[0]}' AND ";
            $query .= "         COMBINED_SCHOOL_KIND    = '{$subclass_array[1]}' AND ";
            $query .= "         COMBINED_CURRICULUM_CD  = '{$subclass_array[2]}' AND ";
            $query .= "         COMBINED_SUBCLASSCD     = '{$subclass_array[3]}' ";
        } else {
            $query .= "         COMBINED_SUBCLASSCD = '".$model->combined_subclasscd."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SEQ ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEQ, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         COUNT(DISTINCT T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || T2.ATTEND_SUBCLASSCD) AS CNT ";
        } else {
            $query .= "         COUNT(DISTINCT T2.ATTEND_SUBCLASSCD) AS CNT ";
        }
        $query .= "     FROM ";
        $query .= "         SUBCLASS_WEIGHTING_COURSE_DAT T1 ";
        $query .= "         LEFT JOIN SUBCLASS_WEIGHTING_COURSE_DAT T2 ON ";
        $query .= "             T1.YEAR         = T2.YEAR AND ";
        $query .= "             T1.FLG          = T2.FLG AND ";
        $query .= "             T1.GRADE        = T2.GRADE AND ";
        $query .= "             T1.COURSECD     = T2.COURSECD AND ";
        $query .= "             T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "             T1.COURSECODE   = T2.COURSECODE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T1.COMBINED_CLASSCD         = T2.COMBINED_CLASSCD AND ";
            $query .= "             T1.COMBINED_SCHOOL_KIND     = T2.COMBINED_SCHOOL_KIND AND ";
            $query .= "             T1.COMBINED_CURRICULUM_CD   = T2.COMBINED_CURRICULUM_CD AND ";
        }
        $query .= "             T1.COMBINED_SUBCLASSCD = T2.COMBINED_SUBCLASSCD AND ";
        $query .= "             T1.SEQ          = T2.SEQ AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || T2.ATTEND_SUBCLASSCD NOT IN ('".implode("','", $subclasscd)."') ";
        } else {
            $query .= "             T2.ATTEND_SUBCLASSCD NOT IN ('".implode("','", $subclasscd)."') ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T1.FLG          = '".$model->flg."' AND ";
        $query .= "         T1.GRADE        = '".$model->grade."' AND ";
        $query .= "         T1.COURSECD     = '".$model->coursecd."' AND ";
        $query .= "         T1.MAJORCD      = '".$model->majorcd."' AND ";
        $query .= "         T1.COURSECODE   = '".$model->coursecode."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.COMBINED_CLASSCD         = '{$subclass_array[0]}' AND ";
            $query .= "         T1.COMBINED_SCHOOL_KIND     = '{$subclass_array[1]}' AND ";
            $query .= "         T1.COMBINED_CURRICULUM_CD   = '{$subclass_array[2]}' AND ";
            $query .= "         T1.COMBINED_SUBCLASSCD      = '{$subclass_array[3]}' AND ";
        } else {
            $query .= "         T1.COMBINED_SUBCLASSCD = '".$model->combined_subclasscd."' AND ";
        }
        $query .= "         T1.SEQ IN ( SELECT SEQ FROM SUBCLASS_CNT WHERE SUB_CNT = ".get_count($subclasscd).") ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SEQ ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     CNT = 0 ";

        return $query;
    }

    //--- UPDATE 
    function &getInsertQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $combined_subclass_array = array();
            $combined_subclass_array = explode("-", $model->combined_subclasscd);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        foreach ($model->field["SUBCLASSCD"] as $key => $val) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $attend_subclass_array = array();
                $attend_subclass_array = explode("-", $model->field["SUBCLASSCD"][$key]);
            }
            if ($model->field["WEIGHTING"][$key] == "") {
                continue;
            }

            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["FLG"][TEXT]                  = $model->flg;
            $data["GRADE"][TEXT]                = $model->grade;
            $data["COURSECD"][TEXT]             = $model->coursecd;
            $data["MAJORCD"][TEXT]              = $model->majorcd;
            $data["COURSECODE"][TEXT]           = $model->coursecode;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["COMBINED_CLASSCD"][TEXT]         = $combined_subclass_array[0];
                $data["COMBINED_SCHOOL_KIND"][TEXT]     = $combined_subclass_array[1];
                $data["COMBINED_CURRICULUM_CD"][TEXT]   = $combined_subclass_array[2];
                $data["COMBINED_SUBCLASSCD"][TEXT]      = $combined_subclass_array[3];
            } else {
                $data["COMBINED_SUBCLASSCD"][TEXT]  = $model->combined_subclasscd;
            }
            $data["SEQ"][TEXT]                  = $model->field["SEQ"];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["ATTEND_CLASSCD"][TEXT]       = $attend_subclass_array[0];
                $data["ATTEND_SCHOOL_KIND"][TEXT]   = $attend_subclass_array[1];
                $data["ATTEND_CURRICULUM_CD"][TEXT] = $attend_subclass_array[2];
                $data["ATTEND_SUBCLASSCD"][TEXT]    = $attend_subclass_array[3];
            } else {
                $data["ATTEND_SUBCLASSCD"][TEXT]    = $model->field["SUBCLASSCD"][$key];
            }
            if ($model->Properties["weightingHyouki"] == "1") {
                $data["WEIGHTING2"][NUMBER]          = $model->field["WEIGHTING"][$key];
            } else {
                $data["WEIGHTING"][NUMBER]          = $model->field["WEIGHTING"][$key];
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $query = Query::insertSQL($data, "SUBCLASS_WEIGHTING_COURSE_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->combined_subclasscd);
        }
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     FLG         = '" .$model->flg."' AND ";
        $query .= "     GRADE       = '" .$model->grade."' AND ";
        $query .= "     COURSECD    = '" .$model->coursecd."' AND ";
        $query .= "     MAJORCD     = '" .$model->majorcd."' AND ";
        $query .= "     COURSECODE  = '" .$model->coursecode."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         COMBINED_CLASSCD        = '{$subclass_array[0]}' AND ";
            $query .= "         COMBINED_SCHOOL_KIND    = '{$subclass_array[1]}' AND ";
            $query .= "         COMBINED_CURRICULUM_CD  = '{$subclass_array[2]}' AND ";
            $query .= "         COMBINED_SUBCLASSCD         = '{$subclass_array[3]}' AND ";
        } else {
            $query .= "     COMBINED_SUBCLASSCD = '" .$model->combined_subclasscd."' AND ";
        }
        $query .= "     SEQ         = '" .$model->seq."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //前年度からのコピーの件数カウント
    function getCopyCountQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "      COUNT(*) ";
        $query .= "  FROM ";
        $query .= "      SUBCLASS_WEIGHTING_COURSE_DAT T0 ";
        $query .= "  WHERE ";
        $query .= "     YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " T0.COMBINED_SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " T0.COMBINED_SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     NOT EXISTS ( ";     //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM SUBCLASS_WEIGHTING_COURSE_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.FLG          = T0.FLG AND ";
        $query .= "                     T1.GRADE        = T0.GRADE AND ";
        $query .= "                     T1.COURSECD     = T0.COURSECD AND ";
        $query .= "                     T1.MAJORCD      = T0.MAJORCD AND ";
        $query .= "                     T1.COURSECODE   = T0.COURSECODE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T1.COMBINED_CLASSCD         = T0.COMBINED_CLASSCD AND ";
            $query .= "                     T1.COMBINED_SCHOOL_KIND     = T0.COMBINED_SCHOOL_KIND AND ";
            $query .= "                     T1.COMBINED_CURRICULUM_CD   = T0.COMBINED_CURRICULUM_CD AND ";
        }
        $query .= "                     T1.COMBINED_SUBCLASSCD  = T0.COMBINED_SUBCLASSCD AND ";
        $query .= "                     T1.SEQ          = T0.SEQ AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T1.ATTEND_CLASSCD       = T0.ATTEND_CLASSCD AND ";
            $query .= "                     T1.ATTEND_SCHOOL_KIND   = T0.ATTEND_SCHOOL_KIND AND ";
            $query .= "                     T1.ATTEND_CURRICULUM_CD = T0.ATTEND_CURRICULUM_CD AND ";
        }
        $query .= "                     T1.ATTEND_SUBCLASSCD    = T0.ATTEND_SUBCLASSCD ";
        $query .= "                 )  AND ";
/******************************************************************/
/* 今年度にない教科や課程、学科、コース、学年のものはコピーしない */
/******************************************************************/
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSE_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T2.COURSECD = T0.COURSECD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MAJOR_YDAT T3 ";
        $query .= "             WHERE ";
        $query .= "                 T3.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T3.COURSECD = T0.COURSECD AND ";
        $query .= "                 T3.MAJORCD  = T0.MAJORCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSECODE_YDAT T4 ";
        $query .= "             WHERE ";
        $query .= "                 T4.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                 T4.COURSECODE = T0.COURSECODE ";
        $query .= "             ) AND";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 V_SUBCLASS_MST T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T5.CLASSCD      = T0.COMBINED_CLASSCD AND ";
            $query .= "                 T5.SCHOOL_KIND  = T0.COMBINED_SCHOOL_KIND AND ";
            $query .= "                 T5.CURRICULUM_CD    = T0.COMBINED_CURRICULUM_CD AND ";
        }
        $query .= "                 T5.SUBCLASSCD = T0.COMBINED_SUBCLASSCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 V_SUBCLASS_MST T6 ";
        $query .= "             WHERE ";
        $query .= "                 T6.YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T6.CLASSCD      = T0.ATTEND_CLASSCD AND ";
            $query .= "                 T6.SCHOOL_KIND  = T0.ATTEND_SCHOOL_KIND AND ";
            $query .= "                 T6.CURRICULUM_CD    = T0.ATTEND_CURRICULUM_CD AND ";
        }
        $query .= "                 T6.SUBCLASSCD = T0.ATTEND_SUBCLASSCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_HDAT T7 ";
        $query .= "             WHERE ";
        $query .= "                 T7.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T7.GRADE    = T0.GRADE ";
        $query .= "             ) AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_WEIGHTING_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.FLG          = T0.FLG AND ";
        $query .= "                     T8.GRADE        = T0.GRADE AND ";
        $query .= "                     T8.COURSECD     = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD      = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE   = T0.COURSECODE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.COMBINED_CLASSCD         = T0.COMBINED_CLASSCD AND ";
            $query .= "                     T8.COMBINED_SCHOOL_KIND     = T0.COMBINED_SCHOOL_KIND AND ";
            $query .= "                     T8.COMBINED_CURRICULUM_CD   = T0.COMBINED_CURRICULUM_CD AND ";
        }
        $query .= "                     T8.COMBINED_SUBCLASSCD  = T0.COMBINED_SUBCLASSCD AND ";
        $query .= "                     T8.SEQ          = T0.SEQ AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.ATTEND_CLASSCD       = T0.ATTEND_CLASSCD AND ";
            $query .= "                     T8.ATTEND_SCHOOL_KIND   = T0.ATTEND_SCHOOL_KIND AND ";
            $query .= "                     T8.ATTEND_CURRICULUM_CD = T0.ATTEND_CURRICULUM_CD AND ";
        }
        $query .= "                     T8.ATTEND_SUBCLASSCD    = T0.ATTEND_SUBCLASSCD ";
        $query .= "                 ) ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO SUBCLASS_WEIGHTING_COURSE_DAT( ";
        $query .= "     YEAR, ";
        $query .= "     FLG, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     COMBINED_CLASSCD, ";
            $query .= "     COMBINED_SCHOOL_KIND, ";
            $query .= "     COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     COMBINED_SUBCLASSCD, ";
        $query .= "     SEQ, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     ATTEND_CLASSCD, ";
            $query .= "     ATTEND_SCHOOL_KIND, ";
            $query .= "     ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     ATTEND_SUBCLASSCD, ";
        $query .= "     WEIGHTING, ";
        $query .= "     WEIGHTING2, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "      '" . CTRL_YEAR ."', ";
        $query .= "      FLG, ";
        $query .= "      GRADE, ";
        $query .= "      COURSECD, ";
        $query .= "      MAJORCD, ";
        $query .= "      COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     COMBINED_CLASSCD, ";
            $query .= "     COMBINED_SCHOOL_KIND, ";
            $query .= "     COMBINED_CURRICULUM_CD, ";
        }
        $query .= "      COMBINED_SUBCLASSCD, ";
        $query .= "      SEQ, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     ATTEND_CLASSCD, ";
            $query .= "     ATTEND_SCHOOL_KIND, ";
            $query .= "     ATTEND_CURRICULUM_CD, ";
        }
        $query .= "      ATTEND_SUBCLASSCD, ";
        $query .= "      WEIGHTING, ";
        $query .= "      WEIGHTING2, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      SUBCLASS_WEIGHTING_COURSE_DAT T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " T0.COMBINED_SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " T0.COMBINED_SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     NOT EXISTS ( ";     //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM SUBCLASS_WEIGHTING_COURSE_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.FLG          = T0.FLG AND ";
        $query .= "                     T1.GRADE        = T0.GRADE AND ";
        $query .= "                     T1.COURSECD     = T0.COURSECD AND ";
        $query .= "                     T1.MAJORCD      = T0.MAJORCD AND ";
        $query .= "                     T1.COURSECODE   = T0.COURSECODE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T1.COMBINED_CLASSCD         = T0.COMBINED_CLASSCD AND ";
            $query .= "                     T1.COMBINED_SCHOOL_KIND     = T0.COMBINED_SCHOOL_KIND AND ";
            $query .= "                     T1.COMBINED_CURRICULUM_CD   = T0.COMBINED_CURRICULUM_CD AND ";
        }
        $query .= "                     T1.COMBINED_SUBCLASSCD  = T0.COMBINED_SUBCLASSCD AND ";
        $query .= "                     T1.SEQ          = T0.SEQ AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T1.ATTEND_CLASSCD       = T0.ATTEND_CLASSCD AND ";
            $query .= "                     T1.ATTEND_SCHOOL_KIND   = T0.ATTEND_SCHOOL_KIND AND ";
            $query .= "                     T1.ATTEND_CURRICULUM_CD = T0.ATTEND_CURRICULUM_CD AND ";
        }
        $query .= "                     T1.ATTEND_SUBCLASSCD    = T0.ATTEND_SUBCLASSCD ";
        $query .= "                 )  AND ";
/******************************************************************/
/* 今年度にない教科や課程、学科、コース、学年のものはコピーしない */
/******************************************************************/
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSE_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T2.COURSECD = T0.COURSECD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MAJOR_YDAT T3 ";
        $query .= "             WHERE ";
        $query .= "                 T3.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T3.COURSECD = T0.COURSECD AND ";
        $query .= "                 T3.MAJORCD  = T0.MAJORCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSECODE_YDAT T4 ";
        $query .= "             WHERE ";
        $query .= "                 T4.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                 T4.COURSECODE = T0.COURSECODE ";
        $query .= "             ) AND";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 V_SUBCLASS_MST T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T5.CLASSCD      = T0.COMBINED_CLASSCD AND ";
            $query .= "                     T5.SCHOOL_KIND  = T0.COMBINED_SCHOOL_KIND AND ";
            $query .= "                     T5.CURRICULUM_CD    = T0.COMBINED_CURRICULUM_CD AND ";
        }
        $query .= "                 T5.SUBCLASSCD = T0.COMBINED_SUBCLASSCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 V_SUBCLASS_MST T6 ";
        $query .= "             WHERE ";
        $query .= "                 T6.YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T6.CLASSCD      = T0.ATTEND_CLASSCD AND ";
            $query .= "                     T6.SCHOOL_KIND  = T0.ATTEND_SCHOOL_KIND AND ";
            $query .= "                     T6.CURRICULUM_CD    = T0.ATTEND_CURRICULUM_CD AND ";
        }
        $query .= "                 T6.SUBCLASSCD = T0.ATTEND_SUBCLASSCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_HDAT T7 ";
        $query .= "             WHERE ";
        $query .= "                 T7.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T7.GRADE    = T0.GRADE ";
        $query .= "             ) AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_WEIGHTING_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.FLG          = T0.FLG AND ";
        $query .= "                     T8.GRADE        = T0.GRADE AND ";
        $query .= "                     T8.COURSECD     = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD      = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE   = T0.COURSECODE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.COMBINED_CLASSCD         = T0.COMBINED_CLASSCD AND ";
            $query .= "                     T8.COMBINED_SCHOOL_KIND     = T0.COMBINED_SCHOOL_KIND AND ";
            $query .= "                     T8.COMBINED_CURRICULUM_CD   = T0.COMBINED_CURRICULUM_CD AND ";
        }
        $query .= "                     T8.COMBINED_SUBCLASSCD  = T0.COMBINED_SUBCLASSCD AND ";
        $query .= "                     T8.SEQ          = T0.SEQ AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.ATTEND_CLASSCD       = T0.ATTEND_CLASSCD AND ";
            $query .= "                     T8.ATTEND_SCHOOL_KIND   = T0.ATTEND_SCHOOL_KIND AND ";
            $query .= "                     T8.ATTEND_CURRICULUM_CD = T0.ATTEND_CURRICULUM_CD AND ";
        }
        $query .= "                     T8.ATTEND_SUBCLASSCD    = T0.ATTEND_SUBCLASSCD ";
        $query .= "                 ) ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }
}
?>
