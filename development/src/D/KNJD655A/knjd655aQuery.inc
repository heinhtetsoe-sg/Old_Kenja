<?php

require_once('for_php7.php');

class knjd655aQuery extends Query {
    //学期取得
    function getSemester() {
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

    //テスト種別取得
    function getTest($model) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($model) {
        //学年末は今学期とする
        $semester = $model->semester == "9" ? CTRL_SEMESTER : $model->semester;

        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$semester}' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."') ";
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
        //教育課程対応
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '{$model->classcd}' AND ";
            $query .= "     T1.SCHOOL_KIND      = '{$model->school_kind}' AND ";
            $query .= "     T1.CURRICULUM_CD    = '{$model->curriculum_cd}' AND ";
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD          = '{$model->classcd}' AND ";
            $query .= "     SCHOOL_KIND      = '{$model->school_kind}' AND ";
            $query .= "     CURRICULUM_CD    = '{$model->curriculum_cd}' AND ";
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD          = '{$model->classcd}' AND ";
            $query .= "     SCHOOL_KIND      = '{$model->school_kind}' AND ";
            $query .= "     CURRICULUM_CD    = '{$model->curriculum_cd}' AND ";
        }
        $query .= "     SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     KIND_DIV   = '{$model->kind_div}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($model) {
        if($model->cmd == "update"){
            knjd655aQuery::getDeleteQuery($model); //update時
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
        //教育課程対応
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD          = '{$model->classcd}' AND ";
            $query .= "         SCHOOL_KIND      = '{$model->school_kind}' AND ";
            $query .= "         CURRICULUM_CD    = '{$model->curriculum_cd}' AND ";
        }
        $query .= "            SUBCLASSCD = '{$model->subclasscd}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
