<?php

require_once('for_php7.php');

class knjl030eQuery extends Query {
    //入試年度取得
    function getEntExamYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model) {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '".$model->entexamyear."' ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($model, $namecd1, $default="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        if ($default) {
            $query .= "    ,NAMESPARE2 AS DEFAULT ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->entexamyear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //名称マスタよりNAMESPARE2取得(初期値を取得できていない場合)
    function getNameSpare2($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->entexamyear."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMESPARE2  = '1' ";

        return $query;
    }

    //名称マスタより取得(初期値を取得できていない場合)
    function getNameMstSet($model, $namecd1, $namespare2) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->entexamyear."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namespare2) {
            $query .= " AND NAMESPARE2 = '".$namespare2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMHALLCD, ";
        $query .= "     T1.EXAMHALLCD || '：' || VALUE(T1.EXAMHALL_NAME,'') AS EXAMHALL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->entexamyear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->testdiv."' AND ";
        $query .= "     T1.EXAM_TYPE    = '".$model->examhall_type."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMHALLCD ";

        return $query;
    }

    //データ取得 -- ENTEXAM_HALL_YDAT
    function getEntexamHallYdat($model, $examhallcd) {
        $query  = " SELECT ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     EXAMHALL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->entexamyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->examhall_type."' AND ";
        $query .= "     EXAMHALLCD      = '".$examhallcd."' ";

        return $query;
    }

    //存在チェック
    function getExists($model, $fields) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->entexamyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->examhall_type."' AND ";
        $query .= "     EXAMHALLCD      = '".$fields["EXAMHALLCD"]."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        //追加 -- ENTEXAM_HALL_YDAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->entexamyear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]        = $model->examhall_type;
        $data["EXAMHALLCD"][TEXT]       = $fields["EXAMHALLCD"];
        $data["EXAMHALL_NAME"][TEXT]    = $fields["EXAMHALL_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $db->query(Query::insertSQL($data, "ENTEXAM_HALL_YDAT"));

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        //更新 -- ENTEXAM_HALL_YDAT
        $data = array();
        $data["EXAMHALL_NAME"][TEXT]        = $fields["EXAMHALL_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$model->entexamyear."' ";
        $where .= "   AND APPLICANTDIV      = '".$model->applicantdiv."' ";
        $where .= "   AND TESTDIV           = '".$model->testdiv."' ";
        $where .= "   AND EXAM_TYPE         = '".$model->examhall_type."' ";
        $where .= "   AND EXAMHALLCD        = '".$fields["EXAMHALLCD"]."' ";

        $db->query(Query::updateSQL($data, "ENTEXAM_HALL_YDAT", $where));

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->entexamyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->examhall_type."' AND ";
        $query .= "     EXAMHALLCD      = '".$fields["EXAMHALLCD"]."' ";
        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }

    //ある年度にデータが存在するのかチェック
    function getExists2($table, $year, $namecd1="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        if ($table == "V_NAME_MST") {
            $query .= "     YEAR        = '".$year."' AND ";
            $query .= "     NAMECD1     = '".$namecd1."' ";
        } else {
            $query .= "     ENTEXAMYEAR = '".$year."' ";
        }

        return $query;
    }

    //COPY
    function getCopyQuery($db, $thisYear, $nextYear) {
        //ENTEXAM_HALL_YDAT
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     EXAMHALL_NAME, ";
        $query .= "     CAPA_CNT, ";
        $query .= "     S_RECEPTNO, ";
        $query .= "     E_RECEPTNO, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' AND ";
        $query .= "     APPLICANTDIV IN (SELECT NAMECD2 FROM V_NAME_MST WHERE YEAR = '".$nextYear."' AND NAMECD1 = 'L003') AND ";
        $query .= "     TESTDIV IN (SELECT NAMECD2 FROM V_NAME_MST WHERE YEAR = '".$nextYear."' AND NAMECD1 = 'L004') ";
        $db->query($query);

        return $query;
    }
}
?>
