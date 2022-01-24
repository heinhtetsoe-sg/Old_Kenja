<?php

require_once('for_php7.php');

class knjz215Query extends Query {

    //リスト
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GROUP_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.GROUP_NAME, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     L1.COURSENAME || L1.MAJORNAME || L2.COURSECODENAME AS COURSE_NAME, ";
        $query .= "     T1.GROUP_DIV || T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS KEY, ";
        $query .= "     L3.SUBCLASSCD, ";
        $query .= "     L4.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD = L1.COURSECD || L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= "     LEFT JOIN REC_SUBCLASS_GROUP_DAT L3 ON T1.YEAR = L3.YEAR ";
        $query .= "          AND T1.GROUP_DIV = L3.GROUP_DIV ";
        $query .= "          AND T1.GRADE = L3.GRADE ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = L3.COURSECD || L3.MAJORCD || L3.COURSECODE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND L3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "          AND L3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST L4 ON L3.SUBCLASSCD = L4.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L3.CLASSCD         = L4.CLASSCD ";
            $query .= "          AND L3.SCHOOL_KIND     = L4.SCHOOL_KIND ";
            $query .= "          AND L3.CURRICULUM_CD   = L4.CURRICULUM_CD ";
        }
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "              ON T1.YEAR     = G1.YEAR ";
        $query .= "             AND T1.GRADE    = G1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->leftGroupDiv) {
            $query .= "     AND T1.GROUP_DIV = '".$model->leftGroupDiv."' ";
        }
        if ($model->leftGrade) {
            $query .= "     AND T1.GRADE = '".$model->leftGrade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GROUP_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L3.CLASSCD, ";
            $query .= "     L3.SCHOOL_KIND, ";
            $query .= "     L3.CURRICULUM_CD, ";
        }
        $query .= "     L3.SUBCLASSCD ";

        return $query;
    }

    //科目数取得
    function getSubclassCnt($field, $div = "", $model)
    {
        $query  = " SELECT ";
        if ($div == "SELECT") {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD";
            } else {
                $query .= "     SUBCLASSCD ";
            }
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUP_DIV = '".$field["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$field["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$field["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$field["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$field["COURSECODE"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //１レコード取得
    function getSelectData($field)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUP_DIV = '".$field["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$field["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$field["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$field["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$field["COURSECODE"]."' ";

        return $query;
    }

    //科目数
    function getGroupDiv()
    {
        $query  = " SELECT ";
        $query .= "     GROUP_DIV AS VALUE, ";
        $query .= "     GROUP_DIV AS LABEL ";
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     GROUP_DIV ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     G1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "              ON T1.YEAR     = G1.YEAR ";
        $query .= "             AND T1.GRADE    = G1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程
    function getCourse($model, $grade, $groupDiv, $key)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || ':' || T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE AS VALUE, ";
        $query .= "     G1.GRADE_NAME1 || ' ' || T1.GROUP_DIV || '科 ' || L1.COURSENAME || '' || L1.MAJORNAME || ' ' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD = L1.COURSECD ";
        $query .= "          AND T1.MAJORCD = L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON T1.YEAR = G1.YEAR ";
        $query .= "          AND T1.GRADE = G1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "          AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GROUP_DIV = '".$groupDiv."' ";
        if ($key) {
            $query .= "     AND T1.GRADE || ':' || T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE = '".$key."' ";
        } else if ($grade) {
            $query .= "     AND T1.GRADE = '".$grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科
    function getClass($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND || '-' || CLASSCD AS SORTVAL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || ':' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SORTVAL ";

        return $query;
    }

    //科目
    function getSubclass($classCd, $key, $model)
    {
        //教育課程対応
        if (($model->Properties["useCurriculumcd"] == '1') && (strlen($key) > 6)) {
            $subclass_array = array();
            $subclass_array = explode("-", $key);
            $class_array = array();
            $class_array = explode("-", $classCd);
        } else if (($model->Properties["useCurriculumcd"] == '1') && $classCd) {
            $class_array = array();
            $class_array = explode("-", $classCd);
        }
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND || '-' || CLASSCD || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SORTVAL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($key) {
            //教育課程対応
            if (($model->Properties["useCurriculumcd"] == '1') && (strlen($key) > 6)) {
                $query .= "     AND CLASSCD         = '".$subclass_array[0]."' ";
                $query .= "     AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
                $query .= "     AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
                $query .= "     AND SUBCLASSCD      = '".$subclass_array[3]."' ";
            } else {
                $query .= "     AND SUBCLASSCD = '".$key."' ";
            }
        } else if ($classCd) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND substr(SUBCLASSCD, 1, 2) = '".$class_array[0]."' ";
                $query .= "     AND SCHOOL_KIND = '".$class_array[1]."' ";
            } else {
                $query .= "     AND substr(SUBCLASSCD, 1, 2) = '".$classCd."' ";
            }
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SORTVAL ";

        return $query;
    }

    //教科
    function getCopyList($model, $yearDiv = "NOW")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_DAT ";
        $query .= " WHERE ";
        if ($yearDiv == "NOW") {
            $query .= "     YEAR = '".CTRL_YEAR."' ";
        } else {
            $query .= "     YEAR = '".(CTRL_YEAR - 1)."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //--- COPY
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();

        $result = $db->query(knjz215Query::getCopyList($model, "LAST"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();

            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["GROUP_DIV"][TEXT]    = $row["GROUP_DIV"];
            $data["GRADE"][TEXT]        = $row["GRADE"];
            $data["COURSECD"][TEXT]     = $row["COURSECD"];
            $data["MAJORCD"][TEXT]      = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]   = $row["COURSECODE"];
            $data["CLASSCD"][TEXT]      = substr($row["SUBCLASSCD"], 0, 2);
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["SCHOOL_KIND"][TEXT]    = $row["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]  = $row["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]   = $row["SUBCLASSCD"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "REC_SUBCLASS_GROUP_DAT");
            $db->query($query);
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }

    //--- INSERT
    function &getInsertQuery($fields, $subclass, $model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["GROUP_DIV"][TEXT]    = $fields["GROUP_DIV"];
        $data["GRADE"][TEXT]        = $fields["GRADE"];
        $data["COURSECD"][TEXT]     = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]      = $fields["MAJORCD"];
        $data["COURSECODE"][TEXT]   = $fields["COURSECODE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        foreach ($subclass as $subKey => $subVal) {
            $data["CLASSCD"][TEXT]   = substr($subVal, 0, 2);
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["SCHOOL_KIND"][TEXT]    = substr($subVal, 3, 1);
                $data["CURRICULUM_CD"][TEXT]  = substr($subVal, 5, 1);
                $data["SUBCLASSCD"][TEXT]     = substr($subVal, 7, 6);
            } else {
                $data["SUBCLASSCD"][TEXT]   = $subVal;
            }
            $query = Query::insertSQL($data, "REC_SUBCLASS_GROUP_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     REC_SUBCLASS_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUP_DIV = '".$fields["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$fields["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$fields["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$fields["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$fields["COURSECODE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
