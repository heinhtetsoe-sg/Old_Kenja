<?php

require_once('for_php7.php');

class knjz020bQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
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
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
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
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMESPARE2 = '1' ";
        
        return $query;
    }

    //名称マスタより取得(初期値を取得できていない場合)
    function getNameMstSet($model, $namecd1, $namespare2) {
        $query .= " SELECT ";
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
    function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.EXAMCOURSE_NAME, ";
        if($model->applicantdiv == "1") {
            $query .= "     T3.NAME1 AS TESTSUBCLASSNAME ";
        } else {
            $query .= "     T3.NAME2 AS TESTSUBCLASSNAME ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST T2 ON ";
        $query .= "         T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND ";
        $query .= "         T2.APPLICANTDIV = T1.APPLICANTDIV AND ";
        $query .= "         T2.TESTDIV = T1.TESTDIV AND ";
        $query .= "         T2.COURSECD = T1.COURSECD AND ";
        $query .= "         T2.MAJORCD = T1.MAJORCD AND ";
        $query .= "         T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON ";
        $query .= "         T3.YEAR = T1.ENTEXAMYEAR AND ";
        $query .= "         T3.NAMECD1 = 'L009' AND ";
        $query .= "         T3.NAMECD2 = T1.TESTSUBCLASSCD AND ";
        if($model->applicantdiv == "1") {
            $query .= "         T3.NAME1 IS NOT NULL ";
        } else {
            $query .= "         T3.NAME2 IS NOT NULL ";
        }
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

    //１レコード取得
    function getRow($year, $applicantdiv, $testdiv, $totalcd, $testsubclasscd)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$testdiv."' AND ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD = '".$totalcd."' AND ";
        $query .= "     TESTSUBCLASSCD  = '".$testsubclasscd."' ";

        return $query;
    }

    //試験科目取得
    function getTestSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'L009' AND ";
        if ($model->testdiv === '1') {
            $query .= "     NAMESPARE2 = '1' AND ";
        } else {
            $query .= "     NAMESPARE3 = '1' AND ";
        }
        $query .= "     NAME1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]          = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]              = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]             = sprintf("%01d", '0');
        $data["MAJORCD"][TEXT]              = sprintf("%03d", '0');
        $data["EXAMCOURSECD"][TEXT]         = sprintf("%04d", '0');
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
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]              = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]             = sprintf("%01d", '0');
        $data["MAJORCD"][TEXT]              = sprintf("%03d", '0');
        $data["EXAMCOURSECD"][TEXT]         = sprintf("%04d", '0');
        $data["TESTSUBCLASSCD"][TEXT]       = $fields["TESTSUBCLASSCD"];
        $data["PERFECT"][NUMBER]            = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        
        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND TESTDIV           = '".$fields["TESTDIV"]."'";
        $where .= "   AND COURSECD          = '0'";
        $where .= "   AND MAJORCD           = '000'";
        $where .= "   AND EXAMCOURSECD      = '0000'";
        $where .= "   AND TESTSUBCLASSCD    = '".$fields["TESTSUBCLASSCD"]."'";

        $query = Query::updateSQL($data, "ENTEXAM_PERFECT_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_PERFECT_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND TESTDIV         = '".$fields["TESTDIV"]."'";
        $query .= "         AND COURSECD        = '0'";
        $query .= "         AND MAJORCD         = '000'";
        $query .= "         AND EXAMCOURSECD    = '0000'";
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
