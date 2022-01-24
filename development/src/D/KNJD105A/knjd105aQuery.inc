<?php

require_once('for_php7.php');

class knjd105aQuery extends Query {


    //学期取得
    function getSemester() 
    {
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
    function getTest($model) 
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' AND ";
        $query .= "     TESTKINDCD = '01' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) 
    {
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
        $query .= "     T2.COURSENAME, ";
        $query .= "     T3.MAJORNAME, ";
        $query .= "     T4.COURSECODENAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD AS TESTCD, ";
        $query .= "     T1.FOOTNOTE ";
        $query .= " FROM ";
        $query .= "     RECORD_DOCUMENT_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST T2 ";
        $query .= " ON ";
        $query .= "     T2.COURSECD = T1.COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST T3 ";
        $query .= " ON ";
        $query .= "     T3.COURSECD = T1.COURSECD AND ";
        $query .= "     T3.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST T4 ";
        $query .= " ON ";
        $query .= "     T4.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE      = '".$model->grade ."' AND ";
        $query .= "     T1.SEMESTER   = '".$model->semester ."'  AND ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '".$model->testcd ."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD        = '".$model->classcd."' AND ";
            $query .= "     SCHOOL_KIND    = '".$model->school_kind."' AND ";
            $query .= "     CURRICULUM_CD  = '".$model->curriculum_cd."' AND ";
        }
        $query .= "     T1.SUBCLASSCD = '".$model->subclasscd ."' ";
        
        return $query;
    }
    
    //１レコード取得
    function getRow($model)
    {
        $query  = " SELECT * FROM record_document_dat ";
        $query .= "  WHERE coursecode = '".$model->coursecode."' AND ";
        $query .= "        coursecd   = '".$model->coursecd ."' AND ";
        $query .= "        majorcd    = '".$model->majorcd ."' AND ";
        $query .= "        grade      = '".$model->grade ."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD        = '".$model->classcd."' AND ";
            $query .= "     SCHOOL_KIND    = '".$model->school_kind."' AND ";
            $query .= "     CURRICULUM_CD  = '".$model->curriculum_cd."' AND ";
        }
        $query .= "        subclasscd = '".$model->subclasscd."' AND ";
        $query .= "        year       = '".CTRL_YEAR."' AND ";
        $query .= "        semester   = '".$model->semester ."' AND ";
        $query .= "        testkindcd || testitemcd = '".$model->testcd ."'";

        return $query;
    }

    //重複チェック
    function chkCODE($model)
    {
        $db = Query::dbCheckOut();
        
        list($COURSECD, $MAJORCD, $COURSECODE) = preg_split("/-/", $model->field["COURSE"]);
        
        $query  = " SELECT * FROM record_document_dat ";
        $query .= "  WHERE coursecode = '".$COURSECODE."' AND ";
        $query .= "        coursecd   = '".$COURSECD ."' AND ";
        $query .= "        majorcd    = '".$MAJORCD ."' AND ";
        $query .= "        grade      = '".$model->grade ."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     classcd        = '".$model->classcd."' AND ";
            $query .= "     school_kind    = '".$model->school_kind."' AND ";
            $query .= "     curriculum_cd  = '".$model->curriculum_cd."' AND ";
        }
        $query .= "        subclasscd = '".$model->subclasscd."' AND ";
        $query .= "        year       = '".CTRL_YEAR."' AND ";
        $query .= "        semester   = '".$model->semester ."' AND ";
        $query .= "        testkindcd || testitemcd = '".$model->testcd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //コースリスト取得
    function getCourseList() 
    {

        $query  = " WITH SCH_REGD AS ( ";
        $query .= "  SELECT ";
        $query .= "       COURSECD ";
        $query .= "      ,MAJORCD ";
        $query .= "      ,COURSECODE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "  GROUP BY ";
        $query .= "       COURSECD ";
        $query .= "      ,MAJORCD ";
        $query .= "      ,COURSECODE ";
        $query .= "  ORDER BY ";
        $query .= "       COURSECD ";
        $query .= "      ,MAJORCD ";
        $query .= "      ,COURSECODE ";
        $query .= " ) ";
        
        $query .= " SELECT ";
        $query .= "      T1.COURSECD ";
        $query .= "     ,T1.MAJORCD ";
        $query .= "     ,T1.COURSECODE ";
        $query .= "     ,T2.COURSENAME ";
        $query .= "     ,T3.MAJORNAME ";
        $query .= "     ,T4.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     SCH_REGD T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST T2 ";
        $query .= " ON ";
        $query .= "     T2.COURSECD = T1.COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST T3 ";
        $query .= " ON ";
        $query .= "     T3.COURSECD = T1.COURSECD AND ";
        $query .= "     T3.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST T4 ";
        $query .= " ON ";
        $query .= "     T4.COURSECODE = T1.COURSECODE ";
        $query .= " ORDER BY ";
        $query .= "      T1.COURSECD ";
        $query .= "     ,T1.MAJORCD ";
        $query .= "     ,T1.COURSECODE ";

        return $query;
        
    }

    //前年度からのデータを取得
    function &getOldYear($model)
    {
        $query  = " SELECT * ";
        $query .= " FROM ";
        $query .= "     record_document_dat m ";
        $query .= " WHERE ";
        $query .= "     m.year = '".(CTRL_YEAR-1)."' AND ";
        $query .= "     m.semester = '".$model->semester."' AND ";
        $query .= "     m.grade = '".$model->grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    m.classcd       = '".$model->classcd."' AND ";
            $query .= "    m.school_kind   = '".$model->school_kind."' AND ";
            $query .= "    m.curriculum_cd = '".$model->curriculum_cd."' AND ";
        }
        $query .= "     m.subclasscd = '".$model->subclasscd."' AND ";
        $query .= "     m.testkindcd || m.testitemcd = '".$model->testcd ."' AND ";
        $query .= "     not exists (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     RECORD_DOCUMENT_DAT m3 ";
        $query .= "                 WHERE ";
        $query .= "                     m3.year = '".CTRL_YEAR."' AND ";
        $query .= "                     m.semester = m3.semester AND ";
        $query .= "                     m.grade = m3.grade AND ";
        $query .= "                     m.testkindcd = m3.testkindcd AND ";
        $query .= "                     m.testitemcd = m3.testitemcd AND ";
        $query .= "                     m.coursecode = m3.coursecode AND ";
        $query .= "                     m.coursecd = m3.coursecd AND ";
        $query .= "                     m.majorcd = m3.majorcd AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                   m.classcd       = m3.classcd AND ";
            $query .= "                   m.school_kind   = m3.school_kind AND ";
            $query .= "                   m.curriculum_cd = m3.curriculum_cd AND ";
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
        $query .= " AND  exists (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT m4 ";
        $query .= "             WHERE ";
        $query .= "                 m4.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                 m4.SEMESTER = m.SEMESTER AND ";
        $query .= "                 m4.COURSECODE = m.COURSECODE AND ";
        $query .= "                 m4.MAJORCD = m.MAJORCD AND ";
        $query .= "                 m4.COURSECODE = m.COURSECODE ";
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
            $data["CLASSCD"][TEXT]              = $Row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]          = $Row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]        = $Row["CURRICULUM_CD"];
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

        if($model->cmd == "update"){
            knjd105aQuery::getDeleteQuery($model); //update時
        }
        
        list($COURSECD, $MAJORCD, $COURSECODE) = preg_split("/-/", $model->field["COURSE"]);
        
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["TESTKINDCD"][TEXT]       = substr($model->testcd,0,2);
        $data["TESTITEMCD"][TEXT]       = substr($model->testcd,2);
        $data["COURSECODE"][TEXT]       = $COURSECODE;
        $data["COURSECD"][TEXT]         = $COURSECD;
        $data["MAJORCD"][TEXT]          = $MAJORCD;
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]              = $model->classcd;
            $data["SCHOOL_KIND"][TEXT]          = $model->school_kind;
            $data["CURRICULUM_CD"][TEXT]        = $model->curriculum_cd;
        }        
        $data["SUBCLASSCD"][TEXT]       = $model->subclasscd;
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

        list($COURSECD, $MAJORCD, $COURSECODE) = preg_split("/-/", $model->field["COURSE"]);

        $query  = " DELETE FROM record_document_dat ";
        $query .= "      WHERE year       = '" .CTRL_YEAR ."' AND ";
        $query .= "            semester   = '" .$model->semester ."' AND ";
        $query .= "            testkindcd || testitemcd = '" .$model->testcd ."' AND ";
        $query .= "            coursecd   = '" .$COURSECD ."' AND ";
        $query .= "            majorcd    = '" .$MAJORCD ."' AND ";
        $query .= "            coursecode = '" .$COURSECODE ."' AND ";
        $query .= "            grade      = '" .$model->field["GRADE"] ."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         classcd        = '".$model->classcd."' AND ";
            $query .= "         school_kind    = '".$model->school_kind."' AND ";
            $query .= "         curriculum_cd  = '".$model->curriculum_cd."' AND ";
        }
        $query .= "            subclasscd = '" .$model->subclasscd ."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
