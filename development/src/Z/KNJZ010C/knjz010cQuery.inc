<?php

require_once('for_php7.php');

class knjz010cQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery()
    {
        $query  = "SELECT DISTINCT entexamyear ";
        $query .= "  FROM entexam_course_mst ";
        $query .= " ORDER BY entexamyear DESC";
        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM ENTEXAM_COURSE_MST ";
        $query .= " WHERE ENTEXAMYEAR >= '{$model->year}'";
        return $query;
    }

    //データ取得
    function selectQuery($year)
    {
        $query .= "SELECT ";
        $query .= "    T1.*, ";
        $query .= "    T3.NAME1, ";
        $query .= "    T4.ABBV1, ";
        $query .= "    T2.COURSENAME, ";
        $query .= "    T2.MAJORNAME ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_COURSE_MST T1  ";
        $query .= "    LEFT OUTER JOIN V_COURSE_MAJOR_MST T2  ";
        $query .= "                 ON T1.ENTEXAMYEAR = T2.YEAR  ";
        $query .= "                    AND T1.COURSECD = T2.COURSECD AND T1.MAJORCD = T2.MAJORCD ";
        $query .= "    LEFT OUTER JOIN V_NAME_MST T3  ";
        $query .= "                 ON T1.ENTEXAMYEAR = T3.YEAR  ";
        $query .= "                    AND T1.APPLICANTDIV = T3.NAMECD2 AND T3.NAMECD1 = 'L003' ";
        $query .= "    LEFT OUTER JOIN V_NAME_MST T4  ";
        $query .= "                 ON T1.ENTEXAMYEAR = T4.YEAR  ";
        $query .= "                    AND T1.TESTDIV = T4.NAMECD2 AND T4.NAMECD1 = 'L004' ";
        $query .= "WHERE ";
        $query .= "    T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "ORDER BY ";
        $query .= "    T1.APPLICANTDIV, ";
        $query .= "    T1.TESTDIV, ";
        $query .= "    T1.COURSECD, ";
        $query .= "    T1.MAJORCD, ";
        $query .= "    T1.EXAMCOURSECD ";

        return $query;
    }

    //入試制度
    function selectApplicantdiv($year)
    {
        $query  = "SELECT ";
        $query .= "    NAMECD2 AS VALUE, ";
        $query .= "    NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        $query .= "    NAMECD1 = 'L003' ";
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    function selectSchooldiv($year) {
        $query .= "SELECT ";
        $query .= "    NAMECD2 AS VALUE, ";
        $query .= "    NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        $query .= "    NAMECD1 = 'L004' ";

        return $query;
    }



    //課程学科
    function selectTotalcd($year)
    {
        $query  = "SELECT ";
        $query .= "    COURSECD || MAJORCD || ':' || COURSENAME || MAJORNAME AS LABEL, ";
        $query .= "    COURSECD || MAJORCD AS VALUE ";
        $query .= "FROM ";
        $query .= "    V_COURSE_MAJOR_MST  ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' ";
        $query .= "ORDER BY ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD ";

        return $query;
    }

    //右フレームの入力値チェックのhiddenを作成する為のクエリ
    function getChecker($year) {
        $query  = "SELECT ";
        $query .= "    APPLICANTDIV || TESTDIV AS CHECKER ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_COURSE_MST ";
        $query .= "WHERE ";
        $query .= "    ENTEXAMYEAR  = '{$year}' ";
        $query .= "ORDER BY ";
        $query .= "    ENTEXAMYEAR, ";
        $query .= "    APPLICANTDIV, ";
        $query .= "    TESTDIV ";

        return $query;
    }

    //１レコード取得
    function getRow($year,$examcoursecd,$applicantdiv,$testdiv,$coursecd,$majorcd)
    {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$year}' AND";
        $query .= "     APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     TESTDIV      = '{$testdiv}' AND ";
        $query .= "     COURSECD     = '{$coursecd}' AND ";
        $query .= "     MAJORCD      = '{$majorcd}' AND ";
        $query .= "     EXAMCOURSECD = '{$examcoursecd}'";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $fields["EXAMCOURSECD"];
        $data["EXAMCOURSE_NAME"][TEXT]  = $fields["EXAMCOURSE_NAME"];
        $data["EXAMCOURSE_MARK"][TEXT]  = $fields["EXAMCOURSE_MARK"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_COURSE_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $fields["EXAMCOURSECD"];
        $data["EXAMCOURSE_NAME"][TEXT]  = $fields["EXAMCOURSE_NAME"];
        $data["EXAMCOURSE_MARK"][TEXT]  = $fields["EXAMCOURSE_MARK"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        
        $where  = " WHERE entexamyear   = '{$fields["ENTEXAMYEAR"]}'";
        $where .= "   AND applicantdiv  = '{$fields["APPLICANTDIV"]}'";
        $where .= "   AND testdiv       = '{$fields["TESTDIV"]}'";
        $where .= "   AND coursecd      = '{$fields["COURSECD"]}'";
        $where .= "   AND majorcd       = '{$fields["MAJORCD"]}'";
        $where .= "   AND examcoursecd  = '{$fields["EXAMCOURSECD"]}'";

        $query = Query::updateSQL($data, "ENTEXAM_COURSE_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM entexam_course_mst ";
        $query .= "       WHERE entexamyear  = '{$fields["ENTEXAMYEAR"] }'";
        $query .= "         AND applicantdiv = '{$fields["APPLICANTDIV"]}'";
        $query .= "         AND testdiv      = '{$fields["TESTDIV"]}'";
        $query .= "         AND coursecd     = '{$fields["COURSECD"]}'";
        $query .= "         AND majorcd      = '{$fields["MAJORCD"]}'";
        $query .= "         AND examcoursecd = '{$fields["EXAMCOURSECD"]}'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }



    //ある年度にコースマスタが存在するのかチェックする。
    function getCheckCourseMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //今年度のレコードを次年度としてインサート
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     EXAMCOURSE_NAME, ";
        $query .= "     EXAMCOURSE_MARK, ";
        $query .= "     CAPACITY, ";
        $query .= "     '" . STAFFCD . "', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }
}
?>
