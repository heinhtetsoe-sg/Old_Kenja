<?php

require_once('for_php7.php');

class knjl034fQuery extends Query {
    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_EXEMPTION_MST ";
        $query .= " WHERE ";
        $query .= "     DIV     = '0' AND ";
        $query .= "     KIND_CD = '1' ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model) {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_EXEMPTION_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '{$model->year}' AND ";
        $query .= "     DIV     = '0' AND ";
        $query .= "     KIND_CD = '1' ";

        return $query;
    }

    //データ取得
    function selectQuery($year) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS APPLI_NAME, ";
        $query .= "     CASE WHEN T1.APPLICANTDIV = '1' THEN N2.NAME1 ELSE N2.NAME2 END AS EXEMPTION_NAME, ";
        $query .= "     T2.ITEM_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_EXEMPTION_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "                  ON N1.YEAR    = T1.ENTEXAMYEAR  ";
        $query .= "                 AND N1.NAMECD1 = 'L003' ";
        $query .= "                 AND N1.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "                  ON N2.YEAR    = T1.ENTEXAMYEAR  ";
        $query .= "                 AND N2.NAMECD1 = 'L036' ";
        $query .= "                 AND N2.NAMECD2 = T1.EXEMPTION_CD ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_ITEM_MST T2 ";
        $query .= "                  ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                 AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                 AND T2.DIV = T1.DIV ";
        $query .= "                 AND T2.ITEM_CD = T1.ITEM_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     T1.DIV          = '0' AND ";
        $query .= "     T1.KIND_CD      = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     CASE WHEN T1.APPLICANTDIV = '1' THEN N2.NAMESPARE1 ELSE N2.NAMESPARE2 END, ";
        $query .= "     T1.EXEMPTION_CD, ";
        $query .= "     T1.ITEM_CD ";

        return $query;
    }

    //件数取得
    function getDataCnt($year, $cd = "") {
        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        if ($cd == "EXEMPTION_CD") {
            $query .= "     T1.EXEMPTION_CD, ";
        }
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_EXEMPTION_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     T1.DIV          = '0' AND ";
        $query .= "     T1.KIND_CD      = '1' ";
        $query .= " GROUP BY ";
        $query .= "     T1.APPLICANTDIV ";
        if ($cd == "EXEMPTION_CD") {
            $query .= "     ,T1.EXEMPTION_CD ";
        }

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

    //免除コード取得
    function getExemptionCd($year, $applicantdiv) {
        $div = ($applicantdiv == "1") ? "1" : "2";

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME{$div} AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$year}' AND ";
        $query .= "     NAMECD1 = 'L036' AND ";
        $query .= "     NAME{$div} IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE{$div}, ";
        $query .= "     VALUE ";

        return $query;
    }

    //費目コード取得
    function getItemCd($year, $applicantdiv) {
        $query  = " SELECT ";
        $query .= "     ITEM_CD AS VALUE, ";
        $query .= "     ITEM_CD || ':' || ITEM_NAME AS LABEL, ";
        $query .= "     ITEM_MONEY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$year}' AND ";
        $query .= "     APPLICANTDIV    = '{$applicantdiv}' AND ";
        $query .= "     DIV             = '0' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $item_cd, $exemption_cd) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_EXEMPTION_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$year}' AND ";
        $query .= "     APPLICANTDIV    = '{$applicantdiv}' AND ";
        $query .= "     DIV             = '0' AND ";
        $query .= "     ITEM_CD         = '{$item_cd}' AND ";
        $query .= "     KIND_CD         = '1' AND ";
        $query .= "     EXEMPTION_CD    = '{$exemption_cd}' ";

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
        $data["KIND_CD"][TEXT]          = "1";
        $data["EXEMPTION_CD"][TEXT]     = $fields["EXEMPTION_CD"];
        $data["EXEMPTION_MONEY"][NUMBER]= $fields["EXEMPTION_MONEY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PAYMENT_EXEMPTION_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["EXEMPTION_MONEY"][NUMBER]= $fields["EXEMPTION_MONEY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '{$fields["ENTEXAMYEAR"]}' ";
        $where .= "   AND APPLICANTDIV  = '{$fields["APPLICANTDIV"]}' ";
        $where .= "   AND DIV           = '0' ";
        $where .= "   AND ITEM_CD       = '{$fields["ITEM_CD"]}' ";
        $where .= "   AND KIND_CD       = '1' ";
        $where .= "   AND EXEMPTION_CD  = '{$fields["EXEMPTION_CD"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_PAYMENT_EXEMPTION_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_PAYMENT_EXEMPTION_MST ";
        $query .= "       WHERE ENTEXAMYEAR  = '{$fields["ENTEXAMYEAR"] }' ";
        $query .= "         AND APPLICANTDIV = '{$fields["APPLICANTDIV"]}' ";
        $query .= "         AND DIV          = '0' ";
        $query .= "         AND ITEM_CD      = '{$fields["ITEM_CD"]}' ";
        $query .= "         AND KIND_CD      = '1' ";
        $query .= "         AND EXEMPTION_CD = '{$fields["EXEMPTION_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //ある年度にデータが存在するのかチェック
    function getCheckCopySql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_EXEMPTION_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' AND ";
        $query .= "     KIND_CD     = '1' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_PAYMENT_EXEMPTION_MST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     DIV, ";
        $query .= "     ITEM_CD, ";
        $query .= "     KIND_CD, ";
        $query .= "     EXEMPTION_CD, ";
        $query .= "     EXEMPTION_MONEY, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     DIV, ";
        $query .= "     ITEM_CD, ";
        $query .= "     KIND_CD, ";
        $query .= "     EXEMPTION_CD, ";
        $query .= "     EXEMPTION_MONEY, ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_PAYMENT_EXEMPTION_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$thisYear}' AND ";
        $query .= "     KIND_CD     = '1' ";

        return $query;
    }
}
?>
