<?php

require_once('for_php7.php');

class knjz020qQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= "     INNER JOIN V_NAME_MST N1 ";
        $query .= "                  ON T1.ENTEXAMYEAR  = N1.YEAR  ";
        $query .= "                 AND T1.APPLICANTDIV = N1.NAMECD2 ";
        $query .= "                 AND N1.NAMECD1      = 'L003' ";
        $query .= "                 AND N1.NAMESPARE3   = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ENTEXAMYEAR DESC ";

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
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタよりNAMESPARE2取得(初期値を取得できていない場合)
    function getNameSpare2($model, $namecd1) {
        $query .= " SELECT ";
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
        $query .= " SELECT ";
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
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS TOTALCD, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T2.EXAMCOURSE_NAME AS TOTALCD_NAME, ";
        $query .= "     T2.EXAMCOURSE_NAME, ";
        if (SCHOOLKIND == "P") {
            $query .= "     T3.NAME3 AS TESTSUBCLASSNAME ";
        } else if (SCHOOLKIND == "J") {
            $query .= "     T3.NAME2 AS TESTSUBCLASSNAME ";
        } else {
            $query .= "     T3.NAME1 AS TESTSUBCLASSNAME ";
        }
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
        if (SCHOOLKIND == "P") {
            $query .= "         AND T3.NAME3 IS NOT NULL ";
        } else if (SCHOOLKIND == "J") {
            $query .= "         AND T3.NAME2 IS NOT NULL ";
        } else {
            $query .= "         AND T3.NAME1 IS NOT NULL ";
        }
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

    //試験科目取得(小学入試)
    function getTestSubclassP($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME3 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'L009' ";
        $query .= "     AND NAME3 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //試験科目取得(中学入試)
    function getTestSubclassJ($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME2 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'L009' ";
        $query .= "     AND NAME2 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験科目取得
    function getTestSubclass($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'L009' ";
        $query .= "     AND NAME1 IS NOT NULL ";
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

    //次年度に名称マスタ「L003」が存在するのかチェック
    function getCheckNameMstL003Sql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$year}' ";
        $query .= "     AND NAMECD1    = 'L003' ";
        $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";

        return $query;
    }

    //ある年度にデータが存在するのかチェック
    function getCheckPerfectMstSql($year, $applicantdiv) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear, $applicantdiv) {
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
        $query .= "         ENTEXAMYEAR  = '".$thisYear."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";

        return $query;
    }
}
?>
