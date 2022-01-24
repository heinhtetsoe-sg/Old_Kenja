<?php

require_once('for_php7.php');

class knjh537aQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ";
        $query .= "          ON T1.YEAR     = T2.YEAR ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実力区分取得
    function getProficiencyDiv($semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCYDIV AS VALUE, ";
        $query .= "     T1.PROFICIENCYDIV || ':' || T2.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T1.YEAR    = T2.YEAR ";
        $query .= "                            AND T2.NAMECD1 = 'H508' ";
        $query .= "                            AND T1.PROFICIENCYDIV = T2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' ";

        return $query;
    }

    //実力コード取得
    function getProficiencyCd($semester, $p_div) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST T2 ON T1.PROFICIENCYDIV   = T2.PROFICIENCYDIV ";
        $query .= "                                 AND T1.PROFICIENCYCD    = T2.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.PROFICIENCYDIV = '".$p_div."'  ";

        return $query;
    }

    //考査科目設定取得
    function getProficiencySubclassYdatList($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T2.GRADE_NAME1, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS COURSE, ";
        if ($model->div == "1") {
            $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || ':' || T3.MAJORNAME || T4.COURSECODENAME AS COURSE_NAME, ";
        } else {
            $query .= "     T1.MAJORCD || ':' || T5.GROUP_NAME AS COURSE_NAME, ";
        }
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD || ':' || T6.SUBCLASS_NAME AS PROFICIENCY_SUBCLASS_NAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T7.SUBCLASSNAME AS SUBCLASSNAME ";
        } else {
            $query .= "     T1.SUBCLASSCD || ':' || T7.SUBCLASSNAME AS SUBCLASSNAME ";
        }
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_YDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR  = T2.YEAR ";
        $query .= "                                  AND T1.GRADE = T2.GRADE ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T3 ON T1.YEAR      = T3.YEAR ";
        $query .= "                                    AND T1.COURSECD  = T3.COURSECD ";
        $query .= "                                    AND T1.MAJORCD   = T3.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T4 ON T1.YEAR        = T4.YEAR ";
        $query .= "                                  AND T1.COURSECODE  = T4.COURSECODE ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_HDAT T5 ON T1.YEAR       = T5.YEAR ";
        $query .= "                                   AND T1.GRADE      = T5.GRADE ";
        $query .= "                                   AND T1.MAJORCD    = T5.GROUP_CD ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_MST T6 ON T1.PROFICIENCY_SUBCLASS_CD = T6.PROFICIENCY_SUBCLASS_CD ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST T7 ON T1.YEAR = T7.YEAR ";
        $query .= "                                AND T1.SUBCLASSCD = T7.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T1.CLASSCD = T7.CLASSCD ";
            $query .= "                             AND T1.SCHOOL_KIND = T7.SCHOOL_KIND ";
            $query .= "                             AND T1.CURRICULUM_CD = T7.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER         = '".$model->semester."' AND ";
        $query .= "     T1.PROFICIENCYDIV   = '".$model->proficiencydiv."' AND ";
        $query .= "     T1.PROFICIENCYCD    = '".$model->proficiencycd."' AND ";
        if ($model->div == "1") {
            $query .= "     T1.DIV = '03' ";
        } else {
            $query .= "     T1.DIV = '04' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     COURSE, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //科目数取得
    function getSubclassCnt($model, $row) {
        $query  = " WITH MAIN AS ( ";
        $query .= knjh537aQuery::getProficiencySubclassYdatList($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     GRADE   = '".$row["GRADE"]."' AND ";
        $query .= "     COURSE  = '".$row["COURSE"]."' AND ";
        $query .= "     PROFICIENCY_SUBCLASS_CD = '".$row["PROFICIENCY_SUBCLASS_CD"]."' ";

        return $query;
    }

    //実力科目による考査科目設定取得
    function getProficiencySubclassYdat($model) {

        $div = ($model->div == "1") ? "03" : "04";

        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || COURSECODE AS COURSE ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER        = '".$model->semester."' AND ";
        $query .= "     PROFICIENCYDIV  = '".$model->proficiencydiv."' AND ";
        $query .= "     PROFICIENCYCD   = '".$model->proficiencycd."' AND ";
        $query .= "     DIV             = '".$div."' AND ";
        $query .= "     GRADE           = '".$model->grade."' AND ";
        $query .= "     PROFICIENCY_SUBCLASS_CD = '".$model->proficiency_subclass_cd."' AND ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || COURSECODE = '".$model->course."' ";

        return $query;
    }

    //実力科目取得
    function getProficiencySubclassCd() {
        $query  = " SELECT DISTINCT ";
        $query .= "     PROFICIENCY_SUBCLASS_CD AS VALUE, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD || ':' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コース取得
    function getCourseMajor($model, $grade) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || ':' || T2.MAJORNAME || T3.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T2 ON T1.YEAR      = T2.YEAR ";
        $query .= "                                    AND T1.COURSECD  = T2.COURSECD ";
        $query .= "                                    AND T1.MAJORCD   = T2.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T3 ON T1.YEAR        = T3.YEAR ";
        $query .= "                                  AND T1.COURSECODE  = T3.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GRADE    = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースグループ取得
    function getCourseGroup($grade) {
        $query  = " SELECT DISTINCT ";
        $query .= "     '0-' || GROUP_CD || '-0000' AS VALUE, ";
        $query .= "     GROUP_CD || ':' || GROUP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ一覧取得
    function selectQuery($model, $grade, $course, $p_subclass) {

        $div = ($model->div == "1") ? "03" : "04";

        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS JOINCD, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.SUBCLASSCD AS JOINCD, ";
            $query .= "     T1.SUBCLASSCD || ': ' || T1.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_YDAT T2 ON ";
        $query .= "             T2.YEAR             = T1.YEAR AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T2.CLASSCD = T1.CLASSCD AND ";
            $query .= "             T2.SCHOOL_KIND = T1.SCHOOL_KIND AND  ";
            $query .= "             T2.CURRICULUM_CD = T1.CURRICULUM_CD AND  ";
        }
        $query .= "             T2.SUBCLASSCD       = T1.SUBCLASSCD AND ";
        $query .= "             T2.SEMESTER         = '".$model->semester."' AND ";
        $query .= "             T2.PROFICIENCYDIV   = '".$model->proficiencydiv."' AND ";
        $query .= "             T2.PROFICIENCYCD    = '".$model->proficiencycd."' AND ";
        $query .= "             T2.DIV              = '".$div."' AND ";
        $query .= "             T2.GRADE            = '".$grade."' AND ";
        $query .= "             T2.PROFICIENCY_SUBCLASS_CD = '".$p_subclass."' AND ";
        $query .= "             T2.COURSECD || '-' || T2.MAJORCD || '-' || T2.COURSECODE = '".$course."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //PROFICIENCY_SUBCLASS_YDAT追加処理
    function &insertProficiencySubclassYdatQuery(&$db, $selectdata, $model) {
        $data = array();
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $db->query(knjh537aQuery::insertProficiencySubclassYdat($model, $selectdata[$i]));
        }
        return;
    }

    //PROFICIENCY_SUBCLASS_YDAT追加
    function insertProficiencySubclassYdat($model, $selectdata) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $selectdata);
        }
        $div = ($model->div == "1") ? "03" : "04";
        list($coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->field["COURSE"]);

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["PROFICIENCYDIV"][TEXT]   = $model->proficiencydiv;
        $data["PROFICIENCYCD"][TEXT]    = $model->proficiencycd;
        $data["PROFICIENCY_SUBCLASS_CD"][TEXT] = $model->field["PROFICIENCY_SUBCLASS_CD"];
        $data["DIV"][TEXT]              = $div;
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["COURSECD"][TEXT]         = $coursecd;
        $data["MAJORCD"][TEXT]          = $majorcd;
        $data["COURSECODE"][TEXT]       = $coursecode;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]       = $selectdata;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "PROFICIENCY_SUBCLASS_YDAT");

        return $query;
    }

    //PROFICIENCY_SUBCLASS_YDAT削除
    function deleteProficiencySubclassYdat($model) {

        $div = ($model->div == "1") ? "03" : "04";

        $query  = " DELETE FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_YDAT ";
        $query .= " WHERE ";
        $query .="      YEAR                = '".CTRL_YEAR."' AND ";
        $query .="      SEMESTER            = '".$model->semester."' AND ";
        $query .="      PROFICIENCYDIV      = '".$model->proficiencydiv."' AND ";
        $query .="      PROFICIENCYCD       = '".$model->proficiencycd."' AND ";
        $query .="      DIV                 = '".$div."' AND ";
        $query .="      GRADE               = '".$model->field["GRADE"]."' AND ";
        $query .="      PROFICIENCY_SUBCLASS_CD = '".$model->field["PROFICIENCY_SUBCLASS_CD"]."' AND ";
        $query .="      COURSECD || '-' || MAJORCD || '-' || COURSECODE = '".$model->field["COURSE"]."' ";

        return $query;
    }
}
?>
