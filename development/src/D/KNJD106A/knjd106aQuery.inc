<?php

require_once('for_php7.php');

class knjd106aQuery extends Query {

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
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) 
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('H', 'J') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース一覧取得
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T2.COURSENAME, ";
        $query .= "     T3.MAJORNAME, ";
        $query .= "     T4.COURSECODENAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.MOCKCD AS TESTCD, ";
        $query .= "     T1.FOOTNOTE ";
        $query .= " FROM ";
        $query .= "     MOCK_DOCUMENT_DAT T1 ";
        $query .= " LEFT JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ";
        $query .= " LEFT JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD ";
        $query .= "      AND T3.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN COURSECODE_MST T4 ON T4.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.MOCKCD = '".$model->testcd ."' ";
        $query .= "     AND T1.GRADE      = '".$model->grade ."' ";
        $query .= "     AND T1.MOCK_SUBCLASS_CD = '".$model->subclasscd ."' ";

        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MOCK_DOCUMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD = '".$model->testcd ."'";
        $query .= "     AND GRADE = '".$model->grade ."' ";
        $query .= "     AND COURSECD = '".$model->coursecd ."' ";
        $query .= "     AND MAJORCD = '".$model->majorcd ."' ";
        $query .= "     AND COURSECODE = '".$model->coursecode."' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '".$model->subclasscd."' ";

        return $query;
    }

    //重複チェック
    function chkCode($model)
    {
        $db = Query::dbCheckOut();

        list($COURSECD, $MAJORCD, $COURSECODE) = preg_split("/-/", $model->field["COURSE"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MOCK_DOCUMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD = '".$model->testcd ."'";
        $query .= "     AND COURSECD = '".$COURSECD ."' ";
        $query .= "     AND MAJORCD = '".$MAJORCD ."' ";
        $query .= "     AND COURSECODE = '".$COURSECODE."' ";
        $query .= "     AND GRADE = '".$model->grade ."' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '".$model->subclasscd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //コースリスト取得
    function getCourseList($grade)
    {
        $query  = " WITH SCH_REGD AS ( ";
        $query .= "  SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= "  FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "  WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND";
        $query .= "     GRADE = '".$grade."' ";
        $query .= "  GROUP BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= "  ORDER BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '(' || T1.COURSECD || T1.MAJORCD || ') ' || T2.COURSENAME || T3.MAJORNAME ";
        $query .= "     || '(' || T1.COURSECODE || ') ' || T4.COURSECODENAME AS LABEL, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCH_REGD T1 ";
        $query .= " LEFT JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ";
        $query .= " LEFT JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD ";
        $query .= "      AND T3.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN COURSECODE_MST T4 ON T4.COURSECODE = T1.COURSECODE ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";

        return $query;
        
    }

    //前年度からのデータを取得
    function &getOldYear($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MOCK_DOCUMENT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".(CTRL_YEAR - 1)."' ";
        $query .= "     AND T1.MOCKCD = '".$model->testcd ."' ";
        $query .= "     AND T1.GRADE = '".$model->grade."' ";
        $query .= "     AND T1.MOCK_SUBCLASS_CD = '".$model->subclasscd."' ";
        $query .= "     AND NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         MOCK_DOCUMENT_DAT T2 ";
        $query .= "                     WHERE ";
        $query .= "                         T2.year = '".CTRL_YEAR."' ";
        $query .= "                         AND T1.MOCKCD = T2.MOCKCD ";
        $query .= "                         AND T1.GRADE = T2.GRADE ";
        $query .= "                         AND T1.COURSECD = T2.COURSECD ";
        $query .= "                         AND T1.MAJORCD = T2.MAJORCD ";
        $query .= "                         AND T1.COURSECODE = T2.COURSECODE ";
        $query .= "                         AND T1.MOCK_SUBCLASS_CD = T2.MOCK_SUBCLASS_CD ";
        $query .= "                    ) ";
        $query .= "     AND EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     COURSECODE_MST T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.COURSECODE = T2.COURSECODE ";
        $query .= "                ) ";

        return $query;
    }

    //前年度からのデータを今年度にINSERT
    function &copyInsert($Row)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["MOCKCD"][TEXT]           = $Row["MOCKCD"];
        $data["GRADE"][TEXT]            = $Row["GRADE"];
        $data["COURSECD"][TEXT]         = $Row["COURSECD"];
        $data["MAJORCD"][TEXT]          = $Row["MAJORCD"];
        $data["COURSECODE"][TEXT]       = $Row["COURSECODE"];
        $data["MOCK_SUBCLASS_CD"][TEXT] = $Row["MOCK_SUBCLASS_CD"];
        $data["FOOTNOTE"][TEXT]         = $Row["FOOTNOTE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "MOCK_DOCUMENT_DAT");
        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {

        if($model->cmd == "update"){
            knjd106aQuery::getDeleteQuery($model);
        }

        list($COURSECD, $MAJORCD, $COURSECODE) = preg_split("/-/", $model->field["COURSE"]);

        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["MOCKCD"][TEXT]           = $model->testcd;
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["COURSECD"][TEXT]         = $COURSECD;
        $data["MAJORCD"][TEXT]          = $MAJORCD;
        $data["COURSECODE"][TEXT]       = $COURSECODE;
        $data["MOCK_SUBCLASS_CD"][TEXT] = $model->subclasscd;
        $data["FOOTNOTE"][TEXT]         = $model->field["FOOTNOTE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "MOCK_DOCUMENT_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        list($COURSECD, $MAJORCD, $COURSECODE) = preg_split("/-/", $model->field["COURSE"]);

        $query  = " DELETE FROM MOCK_DOCUMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD = '".$model->testcd ."'";
        $query .= "     AND COURSECD = '".$COURSECD ."' ";
        $query .= "     AND MAJORCD = '".$MAJORCD ."' ";
        $query .= "     AND COURSECODE = '".$COURSECODE."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"] ."' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '".$model->subclasscd."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
