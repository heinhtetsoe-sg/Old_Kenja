<?php

require_once('for_php7.php');

class knjl361cQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery()
    {
        $query  = "SELECT DISTINCT entexamyear ";
        $query .= "  FROM ENTEXAM_COMMODITY_MST ";
        $query .= " ORDER BY entexamyear DESC";
        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM ENTEXAM_COMMODITY_MST ";
        $query .= " WHERE ENTEXAMYEAR >= '{$model->year}'";
        return $query;
    }

    //データ取得
    function selectQuery($year)
    {
        $query .= "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_COMMODITY_MST  ";
        $query .= "WHERE ";
        $query .= "    ENTEXAMYEAR = '{$year}' ";
        $query .= "ORDER BY ";
        $query .= "    ITEMCD ";

        return $query;
    }

    //１レコード取得
    function getRow($year,$itemcd)
    {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COMMODITY_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$year}' AND";
        $query .= "     ITEMCD       = '{$itemcd}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["ITEMCD"][TEXT]           = $fields["ITEMCD"];
        $data["ITEMNAME"][TEXT]         = $fields["ITEMNAME"];
        $data["MONEY_BOY"][NUMBER]      = $fields["MONEY_BOY"];
        $data["MONEY_GIRL"][NUMBER]     = $fields["MONEY_GIRL"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_COMMODITY_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ITEMCD"][TEXT]           = $fields["ITEMCD"];
        $data["ITEMNAME"][TEXT]         = $fields["ITEMNAME"];
        $data["MONEY_BOY"][NUMBER]      = $fields["MONEY_BOY"];
        $data["MONEY_GIRL"][NUMBER]     = $fields["MONEY_GIRL"];
        
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        
        $where  = " WHERE entexamyear   = '{$fields["ENTEXAMYEAR"]}'";
        $where .= "   AND itemcd        = '{$fields["ITEMCD"]}'";

        $query = Query::updateSQL($data, "ENTEXAM_COMMODITY_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_COMMODITY_MST ";
        $query .= "       WHERE entexamyear  = '{$fields["ENTEXAMYEAR"] }'";
        $query .= "         AND itemcd = '{$fields["ITEMCD"]}'";
        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }

    //ある年度にCOMMODITY_MSTが存在するのかチェックする。
    function getCheckCommodityMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COMMODITY_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //今年度のレコードを次年度としてインサート
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_COMMODITY_MST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     ITEMCD, ";
        $query .= "     ITEMNAME, ";
        $query .= "     MONEY_BOY, ";
        $query .= "     MONEY_GIRL, ";
        $query .= "     '" . STAFFCD . "', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_COMMODITY_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }
}
?>
