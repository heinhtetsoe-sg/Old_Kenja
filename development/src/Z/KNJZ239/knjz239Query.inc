<?php

require_once('for_php7.php');

class knjz239Query extends Query {
    //処理年度
    function getExeYear() {
        $query  = " WITH EXE_YEAR (VALUE, LABEL) AS ( ";
        $query .= "     VALUES (".CTRL_YEAR.", '".CTRL_YEAR."年度'), (".(CTRL_YEAR + 1).", '".(CTRL_YEAR + 1)."年度')";
        $query .= " ) ";

        $query .=" SELECT ";
        $query .="     * ";
        $query .=" FROM ";
        $query .="     EXE_YEAR ";
        $query .=" ORDER BY ";
        $query .="     VALUE DESC ";

        return $query;
    }

    //左フレーム 学年コンボ
    function getSchregRegdGdat($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //課程学科
    function getCourseMajor($model) {
        $query  = " SELECT ";
        $query .= "     COURSECD || '-' || MAJORCD || ':' || VALUE(COURSENAME, '') || VALUE(MAJORNAME, '') AS LABEL, ";
        $query .= "     COURSECD || '-' || MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース
    function getCourseCode($model) {
        $query  = " SELECT ";
        $query .= "     COURSECODE || ':' || COURSECODENAME AS LABEL, ";
        $query .= "     COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //教科コンボ
    function getClassCd($model) {
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || ' ' || L1.CLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "     T1.CLASSCD || ' ' || L1.CLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CREDIT_MST L0 ON L0.YEAR = T1.YEAR ";
        $query .= "                  AND L0.CLASSCD = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                AND L0.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     CLASS_MST L1 ON  L1.CLASSCD = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "              AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " AND L0.GRADE   = '{$model->field["GRADE"]}' ";
        $query .= " AND L0.COURSECD || '-' || L0.MAJORCD = '{$model->field["COURSEMAJOR"]}' ";
        $query .= " AND L0.COURSECODE = '{$model->field["COURSECODE"]}' ";
        //選択科目
        $query .= " AND L0.REQUIRE_FLG = '3' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストtoリスト
    function getSubclass($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || CASE WHEN L0.REQUIRE_FLG = '3' THEN '選' ELSE '　' END || ':' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     T1.SUBCLASSCD || ':' || CASE WHEN L0.REQUIRE_FLG = '3' THEN '選' ELSE '　' END || ':' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CREDIT_MST L0 ON L0.YEAR = T1.YEAR ";
        $query .= "                  AND L0.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                AND L0.CLASSCD = T1.CLASSCD ";
            $query .= "                AND L0.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                AND L0.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                AND L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->field["CLASSCD"] != "" && $model->field["CLASSCD"] !== 'all') {
                $query .= " AND T1.CLASSCD          = '".$class_array[0]."' ";
                $query .= " AND T1.SCHOOL_KIND      = '".$class_array[1]."' ";
            }
        } else {
            if (is_numeric($model->field["CLASSCD"])) {
                $query .= " AND SUBSTR(T1.SUBCLASSCD, 1, 2) = '{$model->field["CLASSCD"]}' ";
            }
        }
        //選択科目
        $query .= " AND L0.YEAR    = '".$model->year."' ";
        $query .= " AND L0.GRADE   = '{$model->field["GRADE"]}' ";
        $query .= " AND L0.COURSECD || '-' || L0.MAJORCD = '{$model->field["COURSEMAJOR"]}' ";
        $query .= " AND L0.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= " AND L0.REQUIRE_FLG = '3' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    function getSubclass2($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND GROUPCD = '{$model->groupcd}' ";
        $query .= "     AND GRADE   = '{$model->grade}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$model->courseMajor}' ";
        $query .= "     AND COURSECODE = '{$model->courseCode}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }
    function getSubclass3($model, $subclasscd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教育課程用
            $classcd           = substr($subclasscd, 0, 2);
            $school_kind       = substr($subclasscd, 3, 1);
            $curriculum_cd     = substr($subclasscd, 5, 1);
            $substr_subclasscd = substr($subclasscd, 7, 6);
        }
    
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || CASE WHEN L0.REQUIRE_FLG = '3' THEN '選' ELSE '　' END || ':' || T1.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     T1.SUBCLASSCD || ':' || CASE WHEN L0.REQUIRE_FLG = '3' THEN '選' ELSE '　' END || ':' || T1.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CREDIT_MST L0 ON L0.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     AND L0.YEAR    = '".$model->year."' ";
        $query .= "     AND L0.GRADE   = '{$model->field["GRADE"]}' ";
        $query .= "     AND L0.COURSECD || '-' || L0.MAJORCD = '{$model->field["COURSEMAJOR"]}' ";
        $query .= "     AND L0.COURSECODE = '{$model->field["COURSECODE"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                AND L0.CLASSCD = T1.CLASSCD ";
            $query .= "                AND L0.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                AND L0.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD         = '{$classcd}' ";
            $query .= " AND     T1.SCHOOL_KIND     = '{$school_kind}' ";
            $query .= " AND     T1.CURRICULUM_CD   = '{$curriculum_cd}' ";
            $query .= " AND     T1.SUBCLASSCD      = '{$substr_subclasscd}' ";
        } else {
            $query .= "         T1.SUBCLASSCD = '{$subclasscd}' ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }

    //左フレームのリスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.ABBV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L4.COURSENAME || L5.MAJORNAME || L6.COURSECODENAME AS COURSE_NAME, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD AS COURSEMAJOR, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.CREDITS, ";
        $query .= "     T1.JOUGEN, ";
        $query .= "     T1.KAGEN, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.CLASSCD, ";
            $query .= "     L1.SCHOOL_KIND, ";
            $query .= "     L1.CURRICULUM_CD, ";
        }         
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     L2.SUBCLASSNAME, ";
        $query .= "     L3.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT L1 ON  L1.YEAR    = T1.YEAR ";
        $query .= "                                 AND L1.GRADE   = T1.GRADE ";
        $query .= "                                 AND L1.GROUPCD = T1.GROUPCD ";
        $query .= "                                 AND L1.COURSECD = T1.COURSECD ";
        $query .= "                                 AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "                                 AND L1.COURSECODE = T1.COURSECODE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L2 ON  L2.SUBCLASSCD = L1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND  L2.CLASSCD         = L1.CLASSCD ";
            $query .= "     AND  L2.SCHOOL_KIND     = L1.SCHOOL_KIND ";
            $query .= "     AND  L2.CURRICULUM_CD   = L1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_GDAT L3 ON  L3.YEAR    = T1.YEAR ";
        $query .= "                         AND L3.GRADE   = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND L3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND L3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L4 ON  L4.COURSECD    = T1.COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST L5 ON  L5.COURSECD    = T1.COURSECD ";
        $query .= "                      AND L5.MAJORCD   = T1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST L6 ON  L6.COURSECODE    = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$model->year."' ";
        if ($model->field["GRADE"] != 'all' && $model->field["GRADE"] != '' ) {
            $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["GROUP_SELECT"] == "1") {
            $query .= "     AND T1.GROUPCD < '600' ";
        } else {
            $query .= "     AND T1.GROUPCD >= '600' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     COURSEMAJOR, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GROUPCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.CLASSCD, ";
            $query .= "     L1.SCHOOL_KIND, ";
            $query .= "     L1.CURRICULUM_CD, ";
        }         
        $query .= "     L1.SUBCLASSCD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getGroupCnt($model, $groupcd, $grade, $courseMajor, $courseCode) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND GROUPCD = '{$groupcd}' ";
        $query .= "     AND GRADE   = '{$grade}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$courseMajor}' ";
        $query .= "     AND COURSECODE = '{$courseCode}' ";

        return $query;
    }

    //SUBCLASS_COMP_SELECT_MST から１レコード取得
    function getSubclassCompSelectMst($year, $grade, $groupcd, $courseMajor, $courseCode) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD || '-' || MAJORCD AS COURSEMAJOR, ";
        $query .= "     COURSECODE, ";
        $query .= "     GROUPCD, ";
        $query .= "     NAME, ";
        $query .= "     ABBV, ";
        $query .= "     CREDITS, ";
        $query .= "     JOUGEN, ";
        $query .= "     KAGEN ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND GRADE   = '{$grade}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$courseMajor}' ";
        $query .= "     AND COURSECODE = '{$courseCode}' ";
        $query .= "     AND GROUPCD = '{$groupcd}' ";

        return $query;
    }

    //更新の時にレコードが存在するのかチェックする
    function getSubclassCompSelectDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND GRADE   = '{$model->field["GRADE"]}' ";
        $query .= "     AND GROUPCD = '{$model->field["GROUPCD"]}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$model->field["COURSEMAJOR"]}' ";
        $query .= "     AND COURSECODE = '{$model->field["COURSECODE"]}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }

    //前年度コピーの時のチェック
    function cntSubclassCompSelectDat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //削除時のチェック(履修選択科目登録データカウント)
    function countSubclassStdSelectDat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->year."' ";
        $query .= "     AND GROUPCD  = '{$model->groupcd}' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    //SUBCLASS_COMP_SELECT_MST を更新
    function &getInsertMstQuery($model) {
        $db = Query::dbCheckOut();

        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSEMAJOR"]);
        $data["YEAR"][TEXT]       = $model->year;
        $data["GRADE"][TEXT]      = $model->field["GRADE"];
        $data["COURSECD"][TEXT]   = $coursecd;
        $data["MAJORCD"][TEXT]    = $majorcd;
        $data["COURSECODE"][TEXT] = $model->field["COURSECODE"];
        $data["GROUPCD"][TEXT]    = $model->field["GROUPCD"];
        $data["NAME"][TEXT]       = $model->field["NAME"];
        $data["ABBV"][TEXT]       = $model->field["ABBV"];
        $data["JOUGEN"][NUMBER]   = $model->field["JOUGEN"];
        $data["KAGEN"][NUMBER]    = $model->field["KAGEN"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $query = Query::insertSQL($data, "SUBCLASS_COMP_SELECT_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //SUBCLASS_COMP_SELECT_DAT を更新
    function &getInsertDatQuery($model, $subclasscd) {
        $db = Query::dbCheckOut();

        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSEMAJOR"]);
        $data["YEAR"][TEXT]       = $model->year;
        $data["GRADE"][TEXT]      = $model->field["GRADE"];
        $data["COURSECD"][TEXT]   = $coursecd;
        $data["MAJORCD"][TEXT]    = $majorcd;
        $data["COURSECODE"][TEXT] = $model->field["COURSECODE"];
        $data["GROUPCD"][TEXT]    = $model->field["GROUPCD"];
        $data["CLASSCD"][TEXT]    = substr($subclasscd, 0, 2);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]    = substr($subclasscd, 3, 1);
            $data["CURRICULUM_CD"][TEXT]  = substr($subclasscd, 5, 1);
            $data["SUBCLASSCD"][TEXT]     = substr($subclasscd, 7, 6);
        } else {
            $data["SUBCLASSCD"][TEXT] = $subclasscd;
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $query = Query::insertSQL($data, "SUBCLASS_COMP_SELECT_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    //SUBCLASS_COMP_SELECT_MST を更新（グループコード"600"以上）
    function &getUpdateMstQuery($model) {
        $db = Query::dbCheckOut();

        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSEMAJOR"]);
        $data = array();
        $data["NAME"][TEXT]       = $model->field["NAME"];
        $data["ABBV"][TEXT]       = $model->field["ABBV"];
        $data["JOUGEN"][NUMBER]   = $model->field["JOUGEN"];
        $data["KAGEN"][NUMBER]    = $model->field["KAGEN"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR        = '".$model->year."' AND ";
        $where .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
        $where .= "     COURSECD    = '".$coursecd."' AND ";
        $where .= "     MAJORCD     = '".$majorcd."' AND ";
        $where .= "     COURSECODE  = '".$model->field["COURSECODE"]."' AND ";
        $where .= "     GROUPCD     = '".$model->field["GROUPCD"]."' ";

        $query = Query::updateSQL($data, "SUBCLASS_COMP_SELECT_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    /* dat */
    function &getDeleteDatQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND GRADE   = '{$model->field["GRADE"]}' ";
        $query .= "     AND GROUPCD = '{$model->field["GROUPCD"]}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$model->field["COURSEMAJOR"]}' ";
        $query .= "     AND COURSECODE = '{$model->field["COURSECODE"]}' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    /* mst */
    function &getDeleteMstQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND GRADE   = '{$model->field["GRADE"]}' ";
        $query .= "     AND GROUPCD = '{$model->field["GROUPCD"]}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$model->field["COURSEMAJOR"]}' ";
        $query .= "     AND COURSECODE = '{$model->field["COURSECODE"]}' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    /********/
    /* COPY */
    /********/
    function getInsertCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $fromYear = $model->year - 1;
        $toYear   = $model->year;

        $query  = " INSERT INTO SUBCLASS_COMP_SELECT_DAT ";
        $query .= " SELECT ";
        $query .= "      '{$toYear}', ";
        $query .= "      GRADE, ";
        $query .= "      COURSECD, ";
        $query .= "      MAJORCD, ";
        $query .= "      COURSECODE, ";
        $query .= "      GROUPCD, ";
        $query .= "      CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      SCHOOL_KIND, ";
            $query .= "      CURRICULUM_CD, ";
        }
        $query .= "      SUBCLASSCD, ";
        $query .= "      registercd, ";
        $query .= "      SYSDATE()";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$fromYear}' ";

        $db->query($query);

        $query  = " INSERT INTO SUBCLASS_COMP_SELECT_MST ";
        $query .= " SELECT ";
        $query .= "      '{$toYear}', ";
        $query .= "      GRADE, ";
        $query .= "      COURSECD, ";
        $query .= "      MAJORCD, ";
        $query .= "      COURSECODE, ";
        $query .= "      GROUPCD, ";
        $query .= "      NAME, ";
        $query .= "      ABBV, ";
        $query .= "      CREDITS, ";
        $query .= "      JOUGEN, ";
        $query .= "      KAGEN, ";
        $query .= "      registercd, ";
        $query .= "      SYSDATE()";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$fromYear}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
