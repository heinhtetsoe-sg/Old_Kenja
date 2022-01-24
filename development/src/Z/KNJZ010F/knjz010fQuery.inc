<?php

require_once('for_php7.php');

class knjz010fQuery extends Query {
    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model) {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '{$model->year}' AND ";
        $query .= "     TESTDIV = '1' ";

        return $query;
    }

    //データ取得
    function selectQuery($year) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T3.NAME1 AS APPLI_NAME, ";
        $query .= "     T2.COURSENAME, ";
        $query .= "     T2.MAJORNAME, ";
        $query .= "     T4.COURSENAME AS ENTER_COURSENAME, ";
        $query .= "     T4.MAJORNAME AS ENTER_MAJORNAME, ";
        $query .= "     T5.COURSECODENAME AS ENTER_COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T2 ";
        $query .= "                  ON T1.ENTEXAMYEAR  = T2.YEAR ";
        $query .= "                 AND T1.COURSECD     = T2.COURSECD ";
        $query .= "                 AND T1.MAJORCD      = T2.MAJORCD ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ";
        $query .= "                  ON T1.ENTEXAMYEAR  = T3.YEAR  ";
        $query .= "                 AND T1.APPLICANTDIV = T3.NAMECD2 ";
        $query .= "                 AND T3.NAMECD1      = 'L003' ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T4 ";
        $query .= "                  ON T1.ENTEXAMYEAR      = T4.YEAR ";
        $query .= "                 AND T1.ENTER_COURSECD   = T4.COURSECD ";
        $query .= "                 AND T1.ENTER_MAJORCD    = T4.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T5 ";
        $query .= "                  ON T1.ENTEXAMYEAR      = T5.YEAR ";
        $query .= "                 AND T1.ENTER_COURSECODE = T5.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     T1.TESTDIV      = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD ";

        return $query;
    }

    //入試制度取得
    function getApplicantdiv($year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 AS DEFAULT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$year}' AND ";
        $query .= "     NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科
    function getTotalcd($year, $flg="") {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSECD || MAJORCD || ':' || COURSENAME || MAJORNAME AS LABEL ";
        if ($flg) {
            $query .= "    ,'1' AS DEFAULT ";
        }
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース
    function getCourceCode($year) {
        $query  = " SELECT ";
        $query .= "     COURSECODE || ':' || COURSECODENAME AS LABEL, ";
        $query .= "     COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $examcoursecd, $applicantdiv, $coursecd, $majorcd) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     COURSECD || MAJORCD AS TOTALCD, ";
        $query .= "     ENTER_COURSECD || ENTER_MAJORCD AS ENTER_TOTALCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$year}' AND ";
        $query .= "     APPLICANTDIV    = '{$applicantdiv}' AND ";
        $query .= "     TESTDIV         = '1' AND ";
        $query .= "     COURSECD        = '{$coursecd}' AND ";
        $query .= "     MAJORCD         = '{$majorcd}' AND ";
        $query .= "     EXAMCOURSECD    = '{$examcoursecd}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = "1";
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $fields["EXAMCOURSECD"];
        $data["EXAMCOURSE_NAME"][TEXT]  = $fields["EXAMCOURSE_NAME"];
        $data["EXAMCOURSE_ABBV"][TEXT]  = $fields["EXAMCOURSE_ABBV"];
        $data["EXAMCOURSE_MARK"][TEXT]  = $fields["EXAMCOURSE_MARK"];
        $data["ENTER_COURSECD"][TEXT]   = $fields["ENTER_COURSECD"];
        $data["ENTER_MAJORCD"][TEXT]    = $fields["ENTER_MAJORCD"];
        $data["ENTER_COURSECODE"][TEXT] = $fields["ENTER_COURSECODE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_COURSE_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = "1";
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $fields["EXAMCOURSECD"];
        $data["EXAMCOURSE_NAME"][TEXT]  = $fields["EXAMCOURSE_NAME"];
        $data["EXAMCOURSE_ABBV"][TEXT]  = $fields["EXAMCOURSE_ABBV"];
        $data["EXAMCOURSE_MARK"][TEXT]  = $fields["EXAMCOURSE_MARK"];
        $data["ENTER_COURSECD"][TEXT]   = $fields["ENTER_COURSECD"];
        $data["ENTER_MAJORCD"][TEXT]    = $fields["ENTER_MAJORCD"];
        $data["ENTER_COURSECODE"][TEXT] = $fields["ENTER_COURSECODE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE entexamyear   = '{$fields["ENTEXAMYEAR"]}' ";
        $where .= "   AND applicantdiv  = '{$fields["APPLICANTDIV"]}' ";
        $where .= "   AND testdiv       = '1' ";
        $where .= "   AND coursecd      = '{$fields["COURSECD"]}' ";
        $where .= "   AND majorcd       = '{$fields["MAJORCD"]}' ";
        $where .= "   AND examcoursecd  = '{$fields["EXAMCOURSECD"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_COURSE_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM entexam_course_mst ";
        $query .= "       WHERE entexamyear  = '{$fields["ENTEXAMYEAR"] }' ";
        $query .= "         AND applicantdiv = '{$fields["APPLICANTDIV"]}' ";
        $query .= "         AND testdiv      = '1' ";
        $query .= "         AND coursecd     = '{$fields["COURSECD"]}' ";
        $query .= "         AND majorcd      = '{$fields["MAJORCD"]}' ";
        $query .= "         AND examcoursecd = '{$fields["EXAMCOURSECD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }

    //ある年度にデータが存在するのかチェック
    function getCheckCourseMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //COPY
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
        $query .= "     EXAMCOURSE_ABBV, ";
        $query .= "     EXAMCOURSE_MARK, ";
        $query .= "     CAPACITY, ";
        $query .= "     ENTER_COURSECD, ";
        $query .= "     ENTER_MAJORCD, ";
        $query .= "     ENTER_COURSECODE, ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }
}
?>
