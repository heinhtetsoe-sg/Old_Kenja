<?php

require_once('for_php7.php');

class knjl033gQuery extends Query {
    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     DIV = '0' ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model) {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '{$model->year}' AND ";
        $query .= "     DIV = '0' ";

        return $query;
    }

    //データ取得
    function selectQuery($year) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T3.NAME1 AS APPLI_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_ITEM_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ";
        $query .= "                  ON T1.ENTEXAMYEAR  = T3.YEAR  ";
        $query .= "                 AND T1.APPLICANTDIV = T3.NAMECD2 ";
        $query .= "                 AND T3.NAMECD1      = 'L003' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     T1.DIV          = '0' ";
        $query .= " ORDER BY ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.ITEM_CD ";

        return $query;
    }

    //件数取得
    function getDataCnt($year) {
        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_ITEM_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     T1.DIV          = '0' ";
        $query .= " GROUP BY ";
        $query .= "     T1.APPLICANTDIV ";

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

    //１レコード取得
    function getRow($year, $applicantdiv, $item_cd) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_ITEM_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$year}' AND ";
        $query .= "     APPLICANTDIV    = '{$applicantdiv}' AND ";
        $query .= "     DIV             = '0' AND ";
        $query .= "     ITEM_CD         = '{$item_cd}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["DIV"][TEXT]              = "0";
        $data["ITEM_CD"][TEXT]          = $fields["ITEM_CD"];
        $data["ITEM_NAME"][TEXT]        = $fields["ITEM_NAME"];
        $data["ITEM_MONEY"][NUMBER]     = $fields["ITEM_MONEY"];
        $data["REMARK1"][TEXT]          = $fields["REMARK1"];
        $data["REMARK3"][TEXT]          = $fields["REMARK3"];
        $data["REMARK4"][TEXT]          = $fields["REMARK4"];
        $data["REMARK5"][TEXT]          = $fields["REMARK5"];
        $data["REMARK6"][TEXT]          = $fields["ITEM_ABBV"];
        $data["REMARK7"][TEXT]          = $fields["REMARK7"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PAYMENT_ITEM_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["DIV"][TEXT]              = "0";
        $data["ITEM_CD"][TEXT]          = $fields["ITEM_CD"];
        $data["ITEM_NAME"][TEXT]        = $fields["ITEM_NAME"];
        $data["ITEM_MONEY"][NUMBER]     = $fields["ITEM_MONEY"];
        $data["REMARK1"][TEXT]          = $fields["REMARK1"];
        $data["REMARK3"][TEXT]          = $fields["REMARK3"];
        $data["REMARK4"][TEXT]          = $fields["REMARK4"];
        $data["REMARK5"][TEXT]          = $fields["REMARK5"];
        $data["REMARK6"][TEXT]          = $fields["ITEM_ABBV"];
        $data["REMARK7"][TEXT]          = $fields["REMARK7"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '{$fields["ENTEXAMYEAR"]}' ";
        $where .= "   AND APPLICANTDIV  = '{$fields["APPLICANTDIV"]}' ";
        $where .= "   AND DIV           = '0' ";
        $where .= "   AND ITEM_CD       = '{$fields["ITEM_CD"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_PAYMENT_ITEM_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= "       WHERE ENTEXAMYEAR  = '{$fields["ENTEXAMYEAR"] }' ";
        $query .= "         AND APPLICANTDIV = '{$fields["APPLICANTDIV"]}' ";
        $query .= "         AND DIV          = '0' ";
        $query .= "         AND ITEM_CD      = '{$fields["ITEM_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //ある年度にデータが存在するのかチェック
    function getCheckCourseMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     DIV, ";
        $query .= "     ITEM_CD, ";
        $query .= "     ITEM_NAME, ";
        $query .= "     ITEM_MONEY, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK3, ";
        $query .= "     REMARK4, ";
        $query .= "     REMARK5, ";
        $query .= "     REMARK6, ";
        $query .= "     REMARK7, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     DIV, ";
        $query .= "     ITEM_CD, ";
        $query .= "     ITEM_NAME, ";
        $query .= "     ITEM_MONEY, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK3, ";
        $query .= "     REMARK4, ";
        $query .= "     REMARK5, ";
        $query .= "     REMARK6, ";
        $query .= "     REMARK7, ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }
}
?>
