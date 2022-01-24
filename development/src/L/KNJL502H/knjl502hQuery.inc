<?php

require_once('for_php7.php');

class knjl502hQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " WITH T_ALL (VALUE, LABEL) AS ( ";
        $query .= "      VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      VALUE, ";
        $query .= "      LABEL ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR AS VALUE, ";
        $query .= "     ENTEXAMYEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($model, $namecd1, $nameCd2="") {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //リスト取得
    function selectQuery($model, $examType="") {
        $query  = " SELECT ";
        if ($examType !== "") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     MAIN.*, ";
            $query .= "     L009.NAME1 AS SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST MAIN ";
        $query .= "     LEFT JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST SUBM ON MAIN.ENTEXAMYEAR  = SUBM.ENTEXAMYEAR ";
        $query .= "                                                 AND MAIN.APPLICANTDIV = SUBM.APPLICANTDIV ";
        $query .= "                                                 AND MAIN.EXAM_TYPE    = SUBM.EXAM_TYPE ";
        $query .= "     LEFT JOIN V_NAME_MST L009 ON L009.YEAR    = MAIN.ENTEXAMYEAR ";
        $query .= "                              AND L009.NAMECD1 = 'L009' ";
        $query .= "                              AND L009.NAMECD2 = SUBM.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         MAIN.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND MAIN.APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($examType !== "") {
            $query .= "     AND MAIN.EXAM_TYPE = '".$examType."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     MAIN.EXAM_TYPE, ";
            $query .= "     SUBCLASSCD ";
        }

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $examType) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$examType."' ";

        return $query;
    }

    //科目取得
    function getRow2($year, $applicantdiv, $examType) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$examType."' ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]          = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["EXAM_TYPE"][TEXT]            = $fields["EXAM_TYPE"];
        $data["EXAMTYPE_NAME"][TEXT]        = $fields["EXAMTYPE_NAME"];
        $data["EXAMTYPE_NAME_ABBV"][TEXT]   = $fields["EXAMTYPE_NAME_ABBV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_EXAMTYPE_MST");
        $db->query($query);

        // ENTEXAM_EXAMTYPE_SUBCLASS_MST
        knjl502hQuery::getDelInsEntexamExamtypeSubclassMst($db, $fields, $model);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data["EXAMTYPE_NAME"][TEXT]        = $fields["EXAMTYPE_NAME"];
        $data["EXAMTYPE_NAME_ABBV"][TEXT]   = $fields["EXAMTYPE_NAME_ABBV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND EXAM_TYPE         = '".$fields["EXAM_TYPE"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_EXAMTYPE_MST", $where);
        $db->query($query);

        // ENTEXAM_EXAMTYPE_SUBCLASS_MST
        knjl502hQuery::getDelInsEntexamExamtypeSubclassMst($db, $fields, $model);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_EXAMTYPE_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND EXAM_TYPE       = '".$fields["EXAM_TYPE"]."'";
        $db->query($query);

        $query  = " DELETE FROM ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND EXAM_TYPE       = '".$fields["EXAM_TYPE"]."'";
        $db->query($query);

        Query::dbCheckIn($db);

        return $result;
    }

    //ENTEXAM_EXAMTYPE_SUBCLASS_MST
    function &getDelInsEntexamExamtypeSubclassMst($db, $fields, $model) {
        $query  = " DELETE FROM ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND EXAM_TYPE       = '".$fields["EXAM_TYPE"]."'";
        $db->query($query);

        foreach ($model->subClassArr as $key => $name1) {
            if ($fields[$key] == "") continue;

            $data["ENTEXAMYEAR"][TEXT]          = $fields["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
            $data["EXAM_TYPE"][TEXT]            = $fields["EXAM_TYPE"];
            $data["SUBCLASSCD"][TEXT]           = $fields[$key];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_EXAMTYPE_SUBCLASS_MST");
            $db->query($query);
        }
        return;
    }

    /********************/
    /* 前年度コピー処理 */
    /********************/
    //ある年度のデータが存在するかチェック
    function CheckData($year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }
    //ある年度にデータが存在するのかチェック
    function getCheckPerfectMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     EXAMTYPE_NAME, ";
        $query .= "     EXAMTYPE_NAME_ABBV, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     EXAMTYPE_NAME, ";
        $query .= "     EXAMTYPE_NAME_ABBV, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";
        $db->query($query);

        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
