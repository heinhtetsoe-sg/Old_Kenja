<?php

require_once('for_php7.php');

class knjd104bQuery extends Query {


    //学期取得
    function getSemester() 
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    SEMESTERNAME ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER <> '9' ";

        return $query;
        
    }


    //リストの最初表らわした時(デフォルト)のcouse_pk
    function getFirst_TestKey($semester) 
    {
        $db = Query::dbCheckOut();

        $query  = knjd104bQuery::getTestName($semester);
        $query .= " fetch first 1 rows only";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
        
    }


    //テスト項目取得
    function getTestName($semester)
    {
        $query  = "SELECT ";
        $query .= "    TESTKINDCD, ";
        $query .= "    TESTITEMCD, ";
        $query .= "    TESTITEMNAME ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "    YEAR = '" .CTRL_YEAR. "' AND ";
        $query .= "    SEMESTER = '" .$semester. "' AND ";
        $query .= "    TESTKINDCD <> '99' ";

        return $query;

    }


    //リストの最初表らわした時(デフォルト)のcouse_pk
    function getFirst_CouseKey($semester) 
    {
        $db = Query::dbCheckOut();

        $query  = knjd104bQuery::getCouseName($semester);
        $query .= " fetch first 1 rows only";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
        
    }

    //コース名コンボ取得
   function getCouseName($semester)
   {
        $query  = " SELECT DISTINCT ";
        $query .= "     T4.COURSECD, ";
        $query .= "     T4.MAJORCD, ";
        $query .= "     T4.COURSECODE, ";
        $query .= "     T4.GRADE, ";
        $query .= "     T4.YEAR, ";
        $query .= "     T4.SEMESTER, ";
        $query .= "     T2.COURSENAME, ";
        $query .= "     T3.MAJORNAME, ";
        $query .= "     T1.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST T1, ";
        $query .= "     COURSE_MST T2, ";
        $query .= "     MAJOR_MST T3, ";
        $query .= "     SCHREG_REGD_DAT T4 ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR       ='".CTRL_YEAR."' AND ";
        $query .= "     T4.SEMESTER   ='".$semester."' AND ";
        $query .= "     T1.COURSECODE = T4.COURSECODE AND ";
        $query .= "     T2.COURSECD   = T4.COURSECD AND ";
        $query .= "     T3.COURSECD   = T4.COURSECD AND ";
        $query .= "     T4.MAJORCD    = T3.MAJORCD ";
        $query .= " ORDER BY ";
        $query .= "     T4.GRADE, ";
        $query .= "     T4.COURSECD, ";
        $query .= "     T4.MAJORCD, ";
        $query .= "     T4.COURSECODE ";
        return $query;

    }

    //コース一覧取得
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.SUBCLASSNAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.FOOTNOTE ";
        $query .= " FROM ";
        $query .= "     RECORD_DOCUMENT_DAT T1, ";
        $query .= "     SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER   = '".$model->semester."' AND ";
        $query .= "     T1.TESTKINDCD = '".$model->testkindcd."' AND ";
        $query .= "     T1.TESTITEMCD = '".$model->testitemcd."' AND ";
        $query .= "     T1.COURSECD   = '".$model->coursecd ."' AND ";
        $query .= "     T1.COURSECODE = '".$model->coursecode ."' AND ";
        $query .= "     T1.MAJORCD    = '".$model->majorcd ."' AND ";
        $query .= "     T1.GRADE      = '".$model->grade ."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.CLASSCD       = T2.CLASSCD AND ";
            $query .= " T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= " T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD ";
        return $query;
    }
    
    //１レコード取得
    function getRow($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if (($model->cmd == 'edit' || $model->cmd == 'update' || $model->cmd == 'add' || $model->cmd == 'delete') && strlen($model->field["SUBCLASSCD"]) > 6) {
                $model->field["SUBCLASSCD"] = substr($model->field["SUBCLASSCD"],7,6);
            }
        }
        $db = Query::dbCheckOut();
        $query  = " SELECT * FROM record_document_dat ";
        $query .= "  WHERE coursecode = '".$model->coursecode."' AND ";
        $query .= "        coursecd   = '".$model->coursecd ."' AND ";
        $query .= "        majorcd    = '".$model->majorcd ."' AND ";
        $query .= "        grade      = '".$model->grade ."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        classcd        = '".$model->field["CLASSCD"]."' AND ";
            $query .= "        school_kind    = '".$model->field["SCHOOL_KIND"]."' AND ";
            $query .= "        curriculum_cd  = '".$model->field["CURRICULUM_CD"]."' AND ";
            $query .= "        subclasscd     = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "        subclasscd = '".$model->subclasscd."' AND ";
        }
        $query .= "        year       = '".CTRL_YEAR."' AND ";
        $query .= "        semester   = '".$model->semester ."' AND ";
        $query .= "        testkindcd = '".$model->testkindcd ."' AND ";
        $query .= "        testitemcd = '".$model->testitemcd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //重複チェック
    function chkCODE($model)
    {
        $db = Query::dbCheckOut();
        //教育課程対応
        if (($model->Properties["useCurriculumcd"] == '1') && (strlen($model->field["SUBCLASSCD"]) > 6)) {
            $subclasscd = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        
        $query  = " SELECT * FROM record_document_dat ";
        $query .= "  WHERE coursecode = '".$model->coursecode."' AND ";
        $query .= "        coursecd   = '".$model->coursecd ."' AND ";
        $query .= "        majorcd    = '".$model->majorcd ."' AND ";
        $query .= "        grade      = '".$model->grade ."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        classcd          = '".$model->field["CLASSCD"]."' AND ";
            $query .= "        school_kind      = '".$model->field["SCHOOL_KIND"]."' AND ";
            $query .= "        curriculum_cd    = '".$model->field["CURRICULUM_CD"]."' AND ";
            $query .= "        subclasscd = '".$subclasscd."' AND ";
        } else {
            $query .= "        subclasscd = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "        year       = '".CTRL_YEAR."' AND ";
        $query .= "        semester   = '".$model->semester ."' AND ";
        $query .= "        testkindcd = '".$model->testkindcd ."' AND ";
        $query .= "        testitemcd = '".$model->testitemcd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //科目取得
    function getSubclass($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR ='".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //前年度からのデータを取得
    function &getOldYear($model)
    {
        $query  = "SELECT * ";
        $query .= " FROM ";
        $query .= "     record_document_dat m ";
        $query .= " WHERE ";
        $query .= "     m.year = '".(CTRL_YEAR-1)."' AND ";
        $query .= "     m.semester = '".$model->semester."' AND ";
        $query .= "     m.coursecode = '".$model->coursecode."' AND ";
        $query .= "     not exists (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     RECORD_DOCUMENT_DAT m3 ";
        $query .= "                 WHERE ";
        $query .= "                     m3.year = '".CTRL_YEAR."' AND ";
        $query .= "                     m.semester = m3.semester AND ";
        $query .= "                     m.testkindcd = m3.testkindcd AND ";
        $query .= "                     m.testitemcd = m3.testitemcd AND ";
        $query .= "                     m.coursecode = m3.coursecode AND ";
        $query .= "                     m.coursecd = m3.coursecd AND ";
        $query .= "                     m.majorcd = m3.majorcd AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 m.CLASSCD = m3.CLASSCD AND ";
            $query .= "                 m.SCHOOL_KIND = m3.SCHOOL_KIND AND ";
            $query .= "                 m.CURRICULUM_CD = m3.CURRICULUM_CD AND ";        
        }
        $query .= "                     m.subclasscd = m3.subclasscd ";
        $query .= "                 ) ";
        $query .= " AND  exists (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 coursecode_mst m2 ";
        $query .= "             WHERE ";
        $query .= "                 m.coursecode = m2.coursecode ";
        $query .= "             ) ";
        $query .= " AND exists (SELECT ";
        $query .= "                'X' ";
        $query .= "             FROM ";
        $query .= "                 v_subclass_mst m4 ";
        $query .= "             WHERE ";
        $query .= "                 m4.year = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 m.CLASSCD = m4.CLASSCD AND ";
            $query .= "                 m.SCHOOL_KIND = m4.SCHOOL_KIND AND ";
            $query .= "                 m.CURRICULUM_CD = m4.CURRICULUM_CD AND ";        
        }
        $query .= "                 m.subclasscd = m4.subclasscd ";
        $query .= "             ) ";

        return $query;
    }

    //前年度からのデータを今年度にINSERT
    function &copyInsert($model, $Row)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $Row["SEMESTER"];
        $data["TESTKINDCD"][TEXT]       = $Row["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]       = $Row["TESTITEMCD"];
        $data["GRADE"][TEXT]            = $Row["GRADE"];
        $data["COURSECODE"][TEXT]       = $Row["COURSECODE"];
        $data["COURSECD"][TEXT]         = $Row["COURSECD"];
        $data["MAJORCD"][TEXT]          = $Row["MAJORCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $Row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]    = $Row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]  = $Row["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]       = $Row["SUBCLASSCD"];
        $data["FOOTNOTE"][TEXT]         = $Row["FOOTNOTE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "RECORD_DOCUMENT_DAT");
        return $query;
    }
    //INSERT
    function &getInsertQuery($model)
    {
        //教育課程対応
        if (($model->Properties["useCurriculumcd"] == '1') && (strlen($model->field["SUBCLASSCD"]) > 6)) {
            $subclasscd = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        if($model->cmd == "update"){
            knjd104bQuery::getDeleteQuery($model); //update時
        }
        
        $db = Query::dbCheckOut();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["TESTKINDCD"][TEXT]       = $model->testkindcd;
        $data["TESTITEMCD"][TEXT]       = $model->testitemcd;
        $data["COURSECODE"][TEXT]       = $model->coursecode;
        $data["COURSECD"][TEXT]         = $model->coursecd;
        $data["MAJORCD"][TEXT]          = $model->majorcd;
        $data["GRADE"][TEXT]            = $model->grade;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $model->field["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]    = $model->field["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]  = $model->field["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]     = $subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["FOOTNOTE"][TEXT]         = $model->field["FOOTNOTE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "RECORD_DOCUMENT_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        //教育課程対応
        if (($model->Properties["useCurriculumcd"] == '1') && (strlen($model->field["SUBCLASSCD"]) > 6)) {
            $subclasscd = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "DELETE FROM record_document_dat ";
        $query .= "      WHERE year       = '" .CTRL_YEAR ."' AND ";
        $query .= "            semester   = '" .$model->semester ."' AND ";
        $query .= "            testkindcd = '" .$model->testkindcd ."' AND ";
        $query .= "            testitemcd = '" .$model->testitemcd ."' AND ";
        $query .= "            coursecd   = '" .$model->coursecd ."' AND ";
        $query .= "            majorcd    = '" .$model->majorcd ."' AND ";
        $query .= "            grade      = '" .$model->grade ."' AND ";
        $query .= "            coursecode = '" .$model->coursecode ."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             classcd          = '".$model->field["CLASSCD"]."' AND ";
            $query .= "             school_kind      = '".$model->field["SCHOOL_KIND"]."' AND ";
            $query .= "             curriculum_cd    = '".$model->field["CURRICULUM_CD"]."' AND ";
            $query .= "             subclasscd       = '".$subclasscd."' ";
        } else {
            $query .= "        subclasscd = '" .$model->field["SUBCLASSCD"] ."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
