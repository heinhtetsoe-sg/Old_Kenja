<?php

require_once('for_php7.php');

class knjz020pQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
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
        $query .= "     ENTEXAMYEAR >= '".$model->year."' ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタよりNAMESPARE2取得(初期値を取得できていない場合)
    function getNameSpare2($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".$model->year."' ";
        $query .= "     AND NAMECD1    = '".$namecd1."' ";
        $query .= "     AND NAMESPARE2 = '1' ";
        
        return $query;
    }

    //名称マスタより取得(初期値を取得できていない場合)
    function getNameMstSet($model, $namecd1, $namespare2) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namespare2) {
            $query .= "     AND NAMESPARE2 = '".$namespare2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $name = ($model->applicantdiv == "1") ? "NAME1" : "NAME2";

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS TOTALCD, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T2.EXAMCOURSE_NAME AS TOTALCD_NAME, ";
        $query .= "     T2.EXAMCOURSE_NAME, ";
        $query .= "     T3.".$name." AS TESTSUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST T2 ";
        $query .= "          ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T2.COURSECD     = T1.COURSECD ";
        $query .= "         AND T2.MAJORCD      = T1.MAJORCD ";
        $query .= "         AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ";
        $query .= "          ON T3.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.NAMECD1      = 'L009' ";
        $query .= "         AND T3.NAMECD2      = T1.TESTSUBCLASSCD ";
        $query .= "         AND T3.".$name." IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV      = '".$model->testdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD, ";
        $query .= "     T1.TESTSUBCLASSCD ";

        return $query;
    }

    //受験コースカウント
    function getEntexamCourseCnt($model, $totalcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND COURSECD || MAJORCD || EXAMCOURSECD = '".$totalcd."' ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $testdiv, $totalcd, $testsubclasscd) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS TOTALCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '".$year."' ";
        $query .= "     AND APPLICANTDIV    = '".$applicantdiv."' ";
        $query .= "     AND TESTDIV         = '".$testdiv."' ";
        $query .= "     AND COURSECD || MAJORCD || EXAMCOURSECD = '".$totalcd."' ";
        $query .= "     AND TESTSUBCLASSCD  = '".$testsubclasscd."' ";

        return $query;
    }

    //受験コース取得
    function getEntExamCourse($model) {
        $query .= " SELECT ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";

        return $query;
    }

    //試験科目取得
    function getTestSubclass($model) {
        $name = ($model->applicantdiv == "1") ? "NAME1" : "NAME2";

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ".$name." AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'L009' ";
        $query .= "     AND ".$name." IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields) {
        $coursecd     = substr($fields["TOTALCD"], 0, 1);
        $majorcd      = substr($fields["TOTALCD"], 1, 3);
        $examcoursecd = substr($fields["TOTALCD"], 4, 4);

        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]          = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]              = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]             = $coursecd;
        $data["MAJORCD"][TEXT]              = $majorcd;
        $data["EXAMCOURSECD"][TEXT]         = $examcoursecd;
        $data["TESTSUBCLASSCD"][TEXT]       = $fields["TESTSUBCLASSCD"];
        $data["PERFECT"][NUMBER]            = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PERFECT_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields) {
        $coursecd     = substr($fields["TOTALCD"], 0, 1);
        $majorcd      = substr($fields["TOTALCD"], 1, 3);
        $examcoursecd = substr($fields["TOTALCD"], 4, 4);

        $db = Query::dbCheckOut();

        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]              = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]             = $coursecd;
        $data["MAJORCD"][TEXT]              = $majorcd;
        $data["EXAMCOURSECD"][TEXT]         = $examcoursecd;
        $data["TESTSUBCLASSCD"][TEXT]       = $fields["TESTSUBCLASSCD"];
        $data["PERFECT"][NUMBER]            = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND TESTDIV           = '".$fields["TESTDIV"]."' ";
        $where .= "   AND COURSECD || MAJORCD || EXAMCOURSECD = '".$fields["TOTALCD"]."'";
        $where .= "   AND TESTSUBCLASSCD    = '".$fields["TESTSUBCLASSCD"]."'";

        $query = Query::updateSQL($data, "ENTEXAM_PERFECT_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_PERFECT_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND TESTDIV         = '".$fields["TESTDIV"]."'";
        $query .= "         AND COURSECD || MAJORCD || EXAMCOURSECD = '".$fields["TOTALCD"]."'";
        $query .= "         AND TESTSUBCLASSCD  = '".$fields["TESTSUBCLASSCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //ある年度にデータが存在するのかチェック
    function getCheckPerfectMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     PERFECT, ";
        $query .= "     RATE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";

        return $query;
    }
}
?>
