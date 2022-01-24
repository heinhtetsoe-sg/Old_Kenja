<?php

require_once('for_php7.php');

class knjb1215Query extends Query {

    //年度取得
    function getYearData() {

        $query  = " WITH EXE_YEAR (LABEL, VALUE) AS ( ";
        $query .= "     VALUES (".CTRL_YEAR.",".CTRL_YEAR."), (".(CTRL_YEAR + 1).",".(CTRL_YEAR + 1).") ";
        $query .= " ) ";

        $query .= " SELECT * FROM EXE_YEAR ";

        return $query;
    }

    //コース
    function getCourse($model) {

        $query .= " SELECT DISTINCT ";
        $query .= "     CREDIT.COURSECD || '-' || CREDIT.MAJORCD || '-' || CREDIT.COURSECODE AS VALUE, ";
        $query .= "     CREDIT.COURSECD || CREDIT.MAJORCD || CREDIT.COURSECODE || ':' || COURSE.COURSENAME || MAJOR.MAJORNAME || COURSECODE.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST CREDIT ";
        $query .= "     INNER JOIN COURSE_MST COURSE ON CREDIT.COURSECD = COURSE.COURSECD ";
        $query .= "     INNER JOIN MAJOR_MST MAJOR ON CREDIT.COURSECD = MAJOR.COURSECD ";
        $query .= "           AND CREDIT.MAJORCD = MAJOR.MAJORCD ";
        $query .= "     INNER JOIN COURSECODE_MST COURSECODE ON CREDIT.COURSECODE = COURSECODE.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     CREDIT.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧取得
    function getList($model) {

        $query  = " SELECT ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     ( ";
        $query .= "      SELECT ";
        $query .= "          COUNT(T2.TEXTBOOKCD) ";
        $query .= "      FROM ";
        $query .= "          SUBCLASS_TEXTBOOK_COURSE_DAT T2 ";
        $query .= "      WHERE ";
        $query .= "         T2.YEAR                 = T1.YEAR ";
        $query .= "         AND T2.CLASSCD          = T1.CLASSCD ";
        $query .= "         AND T2.SCHOOL_KIND      = T1.SCHOOL_KIND ";
        $query .= "         AND T2.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        $query .= "         AND T2.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= "         AND T2.COURSECD || '-' || T2.MAJORCD || '-' || T2.COURSECODE = '".$model->leftCourse."' ";
        $query .= "     ) AS TEXT_CNT ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST CREDIT, ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     CREDIT.YEAR = '".$model->year."' AND ";
        $query .= "     CREDIT.COURSECD || '-' || CREDIT.MAJORCD || '-' || CREDIT.COURSECODE = '".$model->leftCourse."' AND ";
        $query .= "     CREDIT.YEAR = T1.YEAR AND ";
        $query .= "     CREDIT.CLASSCD = T1.CLASSCD AND ";
        $query .= "     CREDIT.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        $query .= "     CREDIT.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        $query .= "     CREDIT.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //科目名取得
    function getSubclassName($model) {

        $query  = " SELECT ";
        $query .= "      CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '　' || SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "      SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD         = '".$model->classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->subclasscd."' ";

        return $query;
    }

    //１レコード取得
    function getRow($model) {

        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T2.TEXTBOOKCD, ";
        $query .= "     T2.TEXTBOOKNAME, ";
        $query .= "     T2.TEXTBOOKMS, ";
        $query .= "     T2.TEXTBOOKPRICE, ";
        $query .= "     COMPANY.ISSUECOMPANYABBV, ";
        $query .= "     T3.TEXTBOOKCD AS TEXT_CHECK, ";
        $query .= "     T3.NOT_DEFAULT ";
        $query .= " FROM ";
        $query .= "     TEXTBOOK_YDAT T1 ";
        $query .= "     LEFT JOIN TEXTBOOK_MST T2 ON T2.TEXTBOOKCD = T1.TEXTBOOKCD ";
        $query .= "     LEFT JOIN ISSUECOMPANY_MST COMPANY ON COMPANY.ISSUECOMPANYCD = T2.ISSUECOMPANYCD ";
        $query .= "     LEFT JOIN SUBCLASS_TEXTBOOK_COURSE_DAT T3 ON T3.YEAR           = T1.YEAR ";
        $query .= "                                       AND T3.TEXTBOOKCD     = T1.TEXTBOOKCD ";
        $query .= "                                       AND T3.CLASSCD        = '".$model->classcd."' ";
        $query .= "                                       AND T3.SCHOOL_KIND    = '".$model->school_kind."' ";
        $query .= "                                       AND T3.CURRICULUM_CD  = '".$model->curriculum_cd."' ";
        $query .= "                                       AND T3.SUBCLASSCD     = '".$model->subclasscd."' ";
        $query .= "                                       AND T3.COURSECD || '-' || T3.MAJORCD || '-' || T3.COURSECODE = '".$model->leftCourse."' ";
        $query .= " WHERE ";
        $query .= "          T1.YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "      T3.TEXTBOOKCD, ";
        $query .= "      T2.TEXTBOOKCD ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $textbookcd, $notDefault) {
        $db = Query::dbCheckOut();
        list($course, $major, $courseCode) = explode("-", $model->leftCourse);

        $data["YEAR"][TEXT]             = $model->year;
        $data["CLASSCD"][TEXT]          = $model->classcd;
        $data["SCHOOL_KIND"][TEXT]      = $model->school_kind;
        $data["CURRICULUM_CD"][TEXT]    = $model->curriculum_cd;
        $data["SUBCLASSCD"][TEXT]       = $model->subclasscd;
        $data["COURSECD"][TEXT]         = $course;
        $data["MAJORCD"][TEXT]          = $major;
        $data["COURSECODE"][TEXT]       = $courseCode;
        $data["TEXTBOOKCD"][TEXT]       = $textbookcd;
        $data["NOT_DEFAULT"][TEXT]      = $notDefault;

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SUBCLASS_TEXTBOOK_COURSE_DAT");

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_TEXTBOOK_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     CLASSCD         = '".$model->classcd ."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->school_kind ."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd ."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->subclasscd ."' AND ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || COURSECODE = '".$model->leftCourse."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //データ件数チェック
    function countRecordCount($tableName, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //次年度作成
    function getCopyQuery($this_year, $next_year) {

        $query  = " INSERT INTO ";
        $query .= "     SUBCLASS_TEXTBOOK_COURSE_DAT ";
        $query .= " SELECT ";
        $query .= "     '{$next_year}', ";
        $query .= "     T0.CLASSCD, ";
        $query .= "     T0.SCHOOL_KIND, ";
        $query .= "     T0.CURRICULUM_CD, ";
        $query .= "     T0.SUBCLASSCD, ";
        $query .= "     T0.COURSECD, ";
        $query .= "     T0.MAJORCD, ";
        $query .= "     T0.COURSECODE, ";
        $query .= "     T0.TEXTBOOKCD, ";
        $query .= "     T0.NOT_DEFAULT, ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_TEXTBOOK_COURSE_DAT T0 ";
        $query .= " WHERE ";
        $query .= "     T0.YEAR = '{$this_year}' AND ";
        $query .= "     T0.SCHOOL_KIND = 'H' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.CLASSCD          = T0.CLASSCD AND ";
        $query .= "                 T2.SCHOOL_KIND      = T0.SCHOOL_KIND AND ";
        $query .= "                 T2.CURRICULUM_CD    = T0.CURRICULUM_CD AND ";
        $query .= "                 T2.SUBCLASSCD       = T0.SUBCLASSCD AND ";
        $query .= "                 T2.YEAR             = '{$next_year}' ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 TEXTBOOK_YDAT T1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.TEXTBOOKCD    = T0.TEXTBOOKCD AND ";
        $query .= "                 T1.YEAR          = '{$next_year}' ";
        $query .= "             ) ";

        return $query;
    }
}
?>
