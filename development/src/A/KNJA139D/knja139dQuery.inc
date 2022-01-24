<?php

require_once('for_php7.php');

class knja139dquery extends Query {
    //学期取得
    function getSemester($semester) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($semester != "") {
            $query .= " AND SEMESTER = '{$semester}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND GRADE IN (  SELECT ";
            $query .= "                         GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_HDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                        (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "                         TR_CD2 = '".STAFFCD."' OR ";
            $query .= "                         TR_CD3 = '".STAFFCD."')) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //種別
    function getA042($div = "") {
        $query  = " SELECT ";
        if ($div == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A042' ";
        if ($div != "cnt") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //データ数
    function getDataCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND GRADE       = '{$model->field["GRADE"]}' ";
        $query .= "     AND DATA_DIV    = '{$model->field["DATA_DIV"]}' ";

        return $query;
    }

    //所見取得
    function getHtrainremarkTempDat($model, $pattern_cd, $flg) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND GRADE       = '{$model->field["GRADE"]}' ";
        $query .= "     AND DATA_DIV    = '{$model->field["DATA_DIV"]}' ";
        $query .= "     AND PATTERN_CD  =  {$pattern_cd} ";

        return $query;
    }

    //学期コース毎定型文取得
    function getHtrainremarkTempSemesCourseDat($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' ";

        return $query;
    }

    //コース取得(在籍)
    function getCouse($model) {

        $query  = " WITH REGD_COURSE AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       YEAR ";
        $query .= "     , GRADE ";
        $query .= "     , COURSECD ";
        $query .= "     , MAJORCD ";
        $query .= "     , COURSECODE ";
        $query .= "   FROM SCHREG_REGD_DAT ";
        $query .= "   WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.GRADE ";

        $query .= "   , T2.COURSECD || '-' || T2.MAJORCD || '-' || T3.COURSECODE  VALUE ";
        $query .= "   , '(' || T2.COURSECD || T2.MAJORCD || ') ' || T2.COURSENAME || T2.MAJORNAME || ' (' || T3.COURSECODE || ') ' || T3.COURSECODENAME LABEL ";

        $query .= " FROM ";
        $query .= "     REGD_COURSE T1 ";
        $query .= " INNER JOIN V_COURSE_MAJOR_MST T2 ";
        $query .= "   ON T2.YEAR = T1.YEAR ";
        $query .= "   AND T2.COURSECD = T1.COURSECD ";
        $query .= "   AND T2.MAJORCD = T1.MAJORCD ";
        $query .= " INNER JOIN V_COURSECODE_MST T3 ";
        $query .= "   ON T3.YEAR = T1.YEAR ";
        $query .= "   AND T3.COURSECODE = T1.COURSECODE ";

        $query .= " WHERE ";
        $query .= "       T1.YEAR  = '".CTRL_YEAR."' ";
        $query .= "   AND T1.GRADE = '{$model->field["GRADE"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "   AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   YEAR ";
        $query .= "   , GRADE ";
        $query .= "   , VALUE ";

        return $query;
    }

    //コース取得
    function getSubclass($model, $classCd) {

        $query  = " WITH REGD_SUBCLASS AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       CHAIR.YEAR ";
        $query .= "     , CHAIR.CLASSCD ";
        $query .= "     , CHAIR.SCHOOL_KIND ";
        $query .= "     , CHAIR.CURRICULUM_CD ";
        $query .= "     , CHAIR.SUBCLASSCD ";
        $query .= "   FROM SCHREG_REGD_DAT REGD ";
        $query .= "   INNER JOIN CHAIR_STD_DAT STD ";
        $query .= "     ON STD.YEAR     = REGD.YEAR ";
        $query .= "    AND STD.SEMESTER = REGD.SEMESTER ";
        $query .= "    AND STD.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   INNER JOIN CHAIR_DAT CHAIR ";
        $query .= "     ON CHAIR.YEAR     = STD.YEAR ";
        $query .= "    AND CHAIR.SEMESTER = STD.SEMESTER ";
        $query .= "    AND CHAIR.CHAIRCD = STD.CHAIRCD ";

        $query .= "   WHERE REGD.YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD VALUE ";
        $query .= "   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T1.SUBCLASSNAME LABEL ";

        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " INNER JOIN REGD_SUBCLASS T2 ";
        $query .= "   ON T2.YEAR          = T1.YEAR ";
        $query .= "  AND T2.CLASSCD       = T1.CLASSCD ";
        $query .= "  AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "  AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "  AND T2.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR  = '".CTRL_YEAR."' ";
        if ($classCd != "") {
            $query .= "   AND T1.CLASSCD = '{$classCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期コース毎定型文取得
    function getHtrainremarkTempDataCheck($model) {

        $query  = " SELECT ";
        $query .= "   T1.* ";
        $query .= "   , T2.COURSENAME ";
        $query .= "   , T2.MAJORNAME ";
        $query .= "   , T3.COURSECODENAME ";
        $query .= "   , T4.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "   HTRAINREMARK_TEMP_SEMES_COURSE_DAT T1 ";
        $query .= "   INNER JOIN V_COURSE_MAJOR_MST T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.COURSECD = T1.COURSECD ";
        $query .= "     AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "   INNER JOIN V_COURSECODE_MST T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.COURSECODE = T1.COURSECODE ";
        $query .= "   INNER JOIN V_SUBCLASS_MST T4 ";
        $query .= "     ON T4.YEAR = T1.YEAR ";
        $query .= "     AND T4.CLASSCD = T1.CLASSCD ";
        $query .= "     AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //更新処理
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' ";

        $db->query($query);

        for ($i = 0; $i < get_count($model->pattern_cd); $i++) {

                //データセット
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["GRADE"][TEXT]        = $model->field["GRADE"];
                $data["DATA_DIV"][TEXT]     = $model->field["DATA_DIV"];
                $data["PATTERN_CD"][NUMBER] = $model->pattern_cd[$i];
                $data["PATTERN_NAME"][TEXT] = $model->pattern_cd[$i];
                $data["REMARK"][TEXT]       = $model->fields[$i]["REMARK"];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                //追加
                $query = Query::insertSQL($data, "HTRAINREMARK_TEMP_SEMES_DAT");
                $db->query($query);
        }

        //コース科目毎定型文登録
        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' ";
        $db->query($query);

        for ($i=0; $i < get_count($model->field["COURSE_SELECTED"]); $i++) { 
            list($courseCd, $majorCd, $courseCode) = explode("-", $model->field["COURSE_SELECTED"][$i]);

            for ($subclassIndex=0; $subclassIndex < get_count($model->field["SUBCLASS_SELECTED"]); $subclassIndex++) { 
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->field["SUBCLASS_SELECTED"][$subclassIndex]);

                //データセット
                $data = array();
                $data["YEAR"][TEXT]          = CTRL_YEAR;
                $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
                $data["GRADE"][TEXT]         = $model->field["GRADE"];
                $data["DATA_DIV"][TEXT]      = $model->field["DATA_DIV"];

                $data["COURSECD"][TEXT]      = $courseCd;
                $data["MAJORCD"][TEXT]       = $majorCd;
                $data["COURSECODE"][TEXT]    = $courseCode;
                $data["CLASSCD"][TEXT]       = $classCd;
                $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
                $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
                $data["SUBCLASSCD"][TEXT]    = $subclassCd;

                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                //追加
                $query = Query::insertSQL($data, "HTRAINREMARK_TEMP_SEMES_COURSE_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    //削除
    function getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //データ取得 -- HTRAINREMARK_TEMP_SEMES_DAT
    function checkHtrainremarkTempDat($model, $year, $flg) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND GRADE IN (  SELECT ";
        $query .= "                     GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                 AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                 AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "              ) ";

        return $query;
    }

    //事前チェック -- SCHREG_REGD_GDAT
    function checkSchregRegdGdat() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //コピー -- HTRAINREMARK_TEMP_SEMES_DAT
    function &copyUpdateQuery($Row) {
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $Row["SEMESTER"];
        $data["GRADE"][TEXT]            = $Row["GRADE"];
        $data["DATA_DIV"][TEXT]         = $Row["DATA_DIV"];
        $data["PATTERN_CD"][NUMBER]     = $Row["PATTERN_CD"];
        $data["REMARK"][TEXT]           = $Row["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "HTRAINREMARK_TEMP_SEMES_DAT");
        return $query;
    }
}
?>
