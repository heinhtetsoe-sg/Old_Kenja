<?php

require_once('for_php7.php');

class knjz020gQuery extends Query {
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
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
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
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMESPARE2 = '1' ";

        return $query;
    }

    //名称マスタより取得(初期値を取得できていない場合)
    function getNameMstSet($model, $namecd1, $namespare2) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namespare2) {
            $query .= " AND NAMESPARE2 = '".$namespare2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS TOTALCD, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || L2.COURSENAME || L2.MAJORNAME AS TOTALCD_NAME, ";
        $query .= "     T1.EXAMCOURSECD || ':' || VALUE(L1.EXAMCOURSE_NAME,'') AS EXAMCOURSE_NAME, ";
        $query .= "     L3.NAME1 AS TESTSUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ON ";
        $query .= "         L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND ";
        $query .= "         L1.APPLICANTDIV = T1.APPLICANTDIV AND ";
        $query .= "         L1.TESTDIV      = T1.TESTDIV AND ";
        $query .= "         L1.COURSECD     = T1.COURSECD AND ";
        $query .= "         L1.MAJORCD      = T1.MAJORCD AND ";
        $query .= "         L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L2 ON ";
        $query .= "         L2.YEAR         = T1.ENTEXAMYEAR AND ";
        $query .= "         L2.COURSECD     = T1.COURSECD AND ";
        $query .= "         L2.MAJORCD      = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_NAME_MST L3 ON ";
        $query .= "         L3.YEAR         = T1.ENTEXAMYEAR AND ";
        $query .= "         L3.NAMECD1      = 'L009' AND ";
        $query .= "         L3.NAMECD2      = T1.TESTSUBCLASSCD AND ";
        $query .= "         L3.NAME1 IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->testdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD, ";
        $query .= "     T1.TESTSUBCLASSCD ";

        return $query;
    }

    //課程学科・受験コース件数取得
    function getDataCnt($model, $flg) {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS TOTALCD, ";
        if ($flg == "examcoursecd") {
            $query .= "     EXAMCOURSECD, ";
        }
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV      = '".$model->testdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     COURSECD || MAJORCD ";
        if ($flg == "examcoursecd") {
            $query .= "    ,EXAMCOURSECD ";
        }

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $testdiv, $totalcd, $examcoursecd, $testsubclasscd) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS TOTALCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$testdiv."' AND ";
        $query .= "     COURSECD || MAJORCD = '".$totalcd."' AND ";
        $query .= "     EXAMCOURSECD    = '".$examcoursecd."' AND ";
        $query .= "     TESTSUBCLASSCD  = '".$testsubclasscd."' ";

        return $query;
    }

    //課程学科取得
    function getTotalcd($model) {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSECD || MAJORCD || ':' || COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験コース取得
    function getEntExamCourse($model, $totalcd) {
        $query  = " SELECT ";
        $query .= "     EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     COURSECD || MAJORCD = '{$totalcd}' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMCOURSECD ";

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
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'L009' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $coursecd = substr($fields["TOTALCD"], 0, 1);
        $majorcd  = substr($fields["TOTALCD"], 1, 3);

        $db = Query::dbCheckOut();

        $data = array();
        if ($model->cmd == "add") {
            $data["ENTEXAMYEAR"][TEXT]          = $fields["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]              = $fields["TESTDIV"];
            $data["COURSECD"][TEXT]             = $coursecd;
            $data["MAJORCD"][TEXT]              = $majorcd;
            $data["EXAMCOURSECD"][TEXT]         = $fields["EXAMCOURSECD"];
            $data["TESTSUBCLASSCD"][TEXT]       = $fields["TESTSUBCLASSCD"];
        }
        $data["PERFECT"][NUMBER]            = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($model->cmd == "update") {
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR         = '".$fields["ENTEXAMYEAR"]."' AND ";
            $where .= "     APPLICANTDIV        = '".$fields["APPLICANTDIV"]."' AND ";
            $where .= "     TESTDIV             = '".$fields["TESTDIV"]."' AND ";
            $where .= "     COURSECD || MAJORCD = '".$fields["TOTALCD"]."' AND ";
            $where .= "     EXAMCOURSECD        = '".$fields["EXAMCOURSECD"]."' AND ";
            $where .= "     TESTSUBCLASSCD      = '".$fields["TESTSUBCLASSCD"]."' ";

            $query = Query::updateSQL($data, "ENTEXAM_PERFECT_MST", $where);
        } else {
            $query = Query::insertSQL($data, "ENTEXAM_PERFECT_MST");
        }

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."' AND ";
        $query .= "     APPLICANTDIV    = '".$fields["APPLICANTDIV"]."' AND";
        $query .= "     TESTDIV         = '".$fields["TESTDIV"]."' AND ";
        $query .= "     COURSECD || MAJORCD = '".$fields["TOTALCD"]."' AND ";
        $query .= "     EXAMCOURSECD    = '".$fields["EXAMCOURSECD"]."' AND ";
        $query .= "     TESTSUBCLASSCD  = '".$fields["TESTSUBCLASSCD"]."'";

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
