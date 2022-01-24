<?php

require_once('for_php7.php');

class knjd658aQuery extends Query {
    //学年取得（権限チェック）
    function getGrade() {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "      TR_CD2 = '".STAFFCD."' OR ";
            $query .= "      TR_CD3 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校種別取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$grade."' ";

        return $query;
    }

    //１レコード取得
    function getRecordDocumentKindDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_DOCUMENT_KIND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER   = '{$model->semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$model->testcd}' AND ";
        $query .= "     GRADE      = '{$model->grade}' AND ";
        $query .= "     HR_CLASS   = '{$model->hr_class}' AND ";
        $query .= "     COURSECD   = '{$model->coursecd}' AND ";
        $query .= "     MAJORCD    = '{$model->majorcd}' AND ";
        $query .= "     COURSECODE = '{$model->coursecode}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD       = '{$model->classcd}' AND ";
            $query .= "     SCHOOL_KIND   = '{$model->school_kind}' AND ";
            $query .= "     CURRICULUM_CD = '{$model->curriculum_cd}' AND ";
        }
        $query .= "     SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     KIND_DIV   = '{$model->kind_div}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
    
        $db = Query::dbCheckOut();

        $query = knjd658aQuery::getDeleteQuery($model); //update時
        $db->query($query);

        $data["YEAR"][TEXT]       = CTRL_YEAR;
        $data["SEMESTER"][TEXT]   = $model->semester;
        $data["TESTKINDCD"][TEXT] = substr($model->testcd,0,2);
        $data["TESTITEMCD"][TEXT] = substr($model->testcd,2);
        $data["COURSECD"][TEXT]   = $model->coursecd;
        $data["MAJORCD"][TEXT]    = $model->majorcd;
        $data["COURSECODE"][TEXT] = $model->coursecode;
        $data["GRADE"][TEXT]      = $model->grade;
        $data["HR_CLASS"][TEXT]   = $model->hr_class;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $model->classcd;
            $data["SCHOOL_KIND"][TEXT]   = $model->school_kind;
            $data["CURRICULUM_CD"][TEXT] = $model->curriculum_cd;
        }
        $data["SUBCLASSCD"][TEXT] = $model->subclasscd;
        $data["KIND_DIV"][TEXT]   = $model->kind_div;
        $data["FOOTNOTE"][TEXT]   = $model->field["FOOTNOTE"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $query = Query::insertSQL($data, "RECORD_DOCUMENT_KIND_DAT");

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
    
        $query  = " DELETE FROM RECORD_DOCUMENT_KIND_DAT ";
        $query .= "      WHERE YEAR       = '" .CTRL_YEAR ."' AND ";
        $query .= "            SEMESTER   = '{$model->semester}' AND ";
        $query .= "            TESTKINDCD || TESTITEMCD = '{$model->testcd}' AND ";
        $query .= "            COURSECD   = '{$model->coursecd}' AND ";
        $query .= "            MAJORCD    = '{$model->majorcd}' AND ";
        $query .= "            COURSECODE = '{$model->coursecode}' AND ";
        $query .= "            GRADE      = '{$model->grade}' AND ";
        $query .= "            HR_CLASS   = '{$model->hr_class}' AND ";
        $query .= "            KIND_DIV   = '{$model->kind_div}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            CLASSCD       = '{$model->classcd}' AND ";
            $query .= "            SCHOOL_KIND   = '{$model->school_kind}' AND ";
            $query .= "            CURRICULUM_CD = '{$model->curriculum_cd}' AND ";
        }
        $query .= "            SUBCLASSCD = '{$model->subclasscd}' ";

        return $query;
    }

    //前年度コピー時、更新対象が何件あるかカウントする
    function getCopyCount($model) {
    
        $last_year = CTRL_YEAR - 1;
        $this_year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_DOCUMENT_KIND_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '{$last_year}' AND ";
        $query .= "     SEMESTER   = '{$model->semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$model->testcd}' AND ";
        $query .= "     GRADE      = '{$model->grade}' AND ";
        $query .= "     HR_CLASS   = '{$model->hr_class}' AND ";
        $query .= "     COURSECD   = '{$model->coursecd}' AND ";
        $query .= "     MAJORCD    = '{$model->majorcd}' AND ";
        $query .= "     COURSECODE = '{$model->coursecode}' AND ";
        $query .= "     KIND_DIV   = '{$model->kind_div}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            CLASSCD       = '{$model->classcd}' AND ";
            $query .= "            SCHOOL_KIND   = '{$model->school_kind}' AND ";
            $query .= "            CURRICULUM_CD = '{$model->curriculum_cd}' AND ";
        }
        $query .= "            SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     KIND_DIV   = '{$model->kind_div}' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     RECORD_DOCUMENT_KIND_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR       = '{$this_year}' AND ";
        $query .= "                     SEMESTER   = T1.SEMESTER AND ";
        $query .= "                     TESTKINDCD = T1.TESTKINDCD AND ";
        $query .= "                     TESTITEMCD = T1.TESTITEMCD AND ";
        $query .= "                     GRADE      = T1.GRADE AND ";
        $query .= "                     HR_CLASS   = T1.HR_CLASS AND ";
        $query .= "                     COURSECD   = T1.COURSECD AND ";
        $query .= "                     MAJORCD    = T1.MAJORCD AND ";
        $query .= "                     COURSECODE = T1.COURSECODE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     CLASSCD = T1.CLASSCD AND ";
            $query .= "                     SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "                     CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                     SUBCLASSCD = T1.SUBCLASSCD AND ";
        $query .= "                     KIND_DIV   = T1.KIND_DIV AND ";
        $query .= "                     VALUE(FOOTNOTE, '') <> '' ";
        $query .= "                 ) ";

        return $query;
    }

    //前年度コピー
    function copyInsert($model) {
    
        $last_year = CTRL_YEAR - 1;
        $this_year = CTRL_YEAR;

        $query  = " INSERT INTO RECORD_DOCUMENT_KIND_DAT ( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     KIND_DIV, ";
        $query .= "     FOOTNOTE, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$this_year}' AS YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     KIND_DIV, ";
        $query .= "     FOOTNOTE, ";
        $query .= "     '". STAFFCD ."' AS REGISTERCD, ";
        $query .= "     sysdate() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     RECORD_DOCUMENT_KIND_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '{$last_year}' AND ";
        $query .= "     SEMESTER   = '{$model->semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$model->testcd}' AND ";
        $query .= "     GRADE      = '{$model->grade}' AND ";
        $query .= "     HR_CLASS   = '{$model->hr_class}' AND ";
        $query .= "     COURSECD   = '{$model->coursecd}' AND ";
        $query .= "     MAJORCD    = '{$model->majorcd}' AND ";
        $query .= "     COURSECODE = '{$model->coursecode}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD         = '{$model->classcd}' AND ";
            $query .= "         SCHOOL_KIND     = '{$model->school_kind}' AND ";
            $query .= "         CURRICULUM_CD   = '{$model->curriculum_cd}' AND ";
        }
        $query .= "     SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     KIND_DIV   = '{$model->kind_div}' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     RECORD_DOCUMENT_KIND_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR       = '{$this_year}' AND ";
        $query .= "                     SEMESTER   = T1.SEMESTER AND ";
        $query .= "                     TESTKINDCD = T1.TESTKINDCD AND ";
        $query .= "                     TESTITEMCD = T1.TESTITEMCD AND ";
        $query .= "                     GRADE      = T1.GRADE AND ";
        $query .= "                     HR_CLASS   = T1.HR_CLASS AND ";
        $query .= "                     COURSECD   = T1.COURSECD AND ";
        $query .= "                     MAJORCD    = T1.MAJORCD AND ";
        $query .= "                     COURSECODE = T1.COURSECODE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     CLASSCD         = T1.CLASSCD AND ";
            $query .= "                     SCHOOL_KIND     = T1.SCHOOL_KIND AND ";
            $query .= "                     CURRICULUM_CD   = T1.CURRICULUM_CD AND ";
        }
        $query .= "                     SUBCLASSCD = T1.SUBCLASSCD AND ";
        $query .= "                     KIND_DIV   = T1.KIND_DIV AND ";
        $query .= "                     VALUE(FOOTNOTE, '') <> '' ";
        $query .= "                 ) ";

        return $query;
    }
}
?>
