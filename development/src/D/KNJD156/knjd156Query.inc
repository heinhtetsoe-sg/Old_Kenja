<?php

require_once('for_php7.php');

class knjd156Query extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getTest($model) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD <> '9900' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     CAST(CAST(GRADE AS SMALLINT) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' AND ";
        $query .= "     GRADE < '07' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

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

    //テスト種別一覧取得
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD AS TESTCD, ";
        $query .= "     L1.TESTITEMNAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.KIND_DIV, ";
        $query .= "     T1.FOOTNOTE ";
        $query .= " FROM ";
        $query .= "     RECORD_DOCUMENT_KIND_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW L1 ON  L1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                  AND L1.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                  AND L1.YEAR = T1.YEAR ";
        $query .= "                                  AND L1.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '{$model->semester}' AND ";
        $query .= "     T1.GRADE      = '{$model->grade}' AND ";
        $query .= "     T1.HR_CLASS   = '{$model->hr_class}' AND ";
        $query .= "     T1.COURSECD   = '{$model->coursecd}' AND ";
        $query .= "     T1.MAJORCD    = '{$model->majorcd}' AND ";
        $query .= "     T1.COURSECODE = '{$model->coursecode}' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD       = '{$model->classcd}' AND ";
            $query .= "     T1.SCHOOL_KIND   = '{$model->school_kind}' AND ";
            $query .= "     T1.CURRICULUM_CD = '{$model->curriculum_cd}' AND ";
        }
        $query .= "     T1.SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     T1.KIND_DIV   = '{$model->kind_div}' ";
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //１レコード取得
    function getRow($model) {
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
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD       = '{$model->classcd}' AND ";
            $query .= "     SCHOOL_KIND   = '{$model->school_kind}' AND ";
            $query .= "     CURRICULUM_CD = '{$model->curriculum_cd}' AND ";
        }
        $query .= "     SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     KIND_DIV   = '{$model->kind_div}' ";

        return $query;
    }

    //重複チェック
    function chkCODE($model) {
        $db = Query::dbCheckOut();

        list($COURSECD, $MAJORCD, $COURSECODE) = preg_split("/-/", $model->field["COURSE"]);

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
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD       = '{$model->classcd}' AND ";
            $query .= "     SCHOOL_KIND   = '{$model->school_kind}' AND ";
            $query .= "     CURRICULUM_CD = '{$model->curriculum_cd}' AND ";
        }
        $query .= "     SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     KIND_DIV   = '{$model->kind_div}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //前年度からのデータを取得
    function &getOldYear($model) {
        $query  = " SELECT * ";
        $query .= " FROM ";
        $query .= "     RECORD_DOCUMENT_KIND_DAT m ";
        $query .= " WHERE ";
        $query .= "     m.year      = '".(CTRL_YEAR-1)."' AND ";
        $query .= "     m.semester  = '".$model->semester."' AND ";
        $query .= "     m.testkindcd || m.testitemcd = '".$model->testcd ."' AND ";
        $query .= "     m.grade      = '".$model->grade."' AND ";
        $query .= "     m.hr_class   = '".$model->hr_class."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     m.classcd       = '".$model->classcd."' AND ";
            $query .= "     m.school_kind   = '".$model->school_kind."' AND ";
            $query .= "     m.curriculum_cd = '".$model->curriculum_cd."' AND ";
        }
        $query .= "     m.subclasscd = '".$model->subclasscd."' AND ";
        $query .= "     not exists (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     RECORD_DOCUMENT_KIND_DAT m3 ";
        $query .= "                 WHERE ";
        $query .= "                     m3.year      = '".CTRL_YEAR."' AND ";
        $query .= "                     m.semester   = m3.semester AND ";
        $query .= "                     m.grade      = m3.grade AND ";
        $query .= "                     m.hr_class   = m3.hr_class AND ";
        $query .= "                     m.testkindcd = m3.testkindcd AND ";
        $query .= "                     m.testitemcd = m3.testitemcd AND ";
        $query .= "                     m.coursecode = m3.coursecode AND ";
        $query .= "                     m.coursecd   = m3.coursecd AND ";
        $query .= "                     m.majorcd    = m3.majorcd AND ";
        $query .= "                     m.kind_div   = m3.kind_div AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     m.classcd       = m3.classcd AND ";
            $query .= "                     m.school_kind   = m3.school_kind AND ";
            $query .= "                     m.curriculum_cd = m3.curriculum_cd AND ";
        }
        $query .= "                     m.subclasscd = m3.subclasscd ";
        $query .= "                 ) ";

        return $query;
    }

    //前年度からのデータを今年度にINSERT
    function &copyInsert($model, $Row) {

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $Row["SEMESTER"];
        $data["TESTKINDCD"][TEXT]       = $Row["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]       = $Row["TESTITEMCD"];
        $data["GRADE"][TEXT]            = $Row["GRADE"];
        $data["HR_CLASS"][TEXT]         = $Row["HR_CLASS"];
        $data["COURSECODE"][TEXT]       = $Row["COURSECODE"];
        $data["COURSECD"][TEXT]         = $Row["COURSECD"];
        $data["MAJORCD"][TEXT]          = $Row["MAJORCD"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $Row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $Row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $Row["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]       = $Row["SUBCLASSCD"];
        $data["KIND_DIV"][TEXT]         = $Row["KIND_DIV"];
        $data["FOOTNOTE"][TEXT]         = $Row["FOOTNOTE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "RECORD_DOCUMENT_KIND_DAT");
        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        if($model->cmd == "update"){
            knjd156Query::getDeleteQuery($model); //update時
        }

        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["TESTKINDCD"][TEXT]       = substr($model->testcd,0,2);
        $data["TESTITEMCD"][TEXT]       = substr($model->testcd,2);
        $data["COURSECD"][TEXT]         = $model->coursecd;
        $data["MAJORCD"][TEXT]          = $model->majorcd;
        $data["COURSECODE"][TEXT]       = $model->coursecode;
        $data["GRADE"][TEXT]            = $model->grade;
        $data["HR_CLASS"][TEXT]         = $model->hr_class;
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $model->classcd;
            $data["SCHOOL_KIND"][TEXT]      = $model->school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $model->curriculum_cd;
        }
        $data["SUBCLASSCD"][TEXT]       = $model->subclasscd;
        $data["KIND_DIV"][TEXT]         = $model->kind_div;
        $data["FOOTNOTE"][TEXT]         = $model->field["FOOTNOTE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "RECORD_DOCUMENT_KIND_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

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
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            CLASSCD       = '{$model->classcd}' AND ";
            $query .= "            SCHOOL_KIND   = '{$model->school_kind}' AND ";
            $query .= "            CURRICULUM_CD = '{$model->curriculum_cd}' AND ";
        }
        $query .= "            SUBCLASSCD = '{$model->subclasscd}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
