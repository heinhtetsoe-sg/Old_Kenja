<?php

require_once('for_php7.php');

class knjp987Query extends Query {
    /**********/
    /* 左画面 */
    /**********/
    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     LTAX.*, ";
        $query .= "     P008.NAME1 ";
        $query .= " FROM ";
        $query .= "     LEVY_TAX_MST LTAX ";
        $query .= "     LEFT JOIN NAME_MST P008 ON P008.NAMECD1 = 'P008' ";
        $query .= "                            AND P008.NAMECD2 = LTAX.TAX_CD ";
        $query .= " WHERE ";
        $query .= "     LTAX.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     LTAX.TAX_CD, ";
        $query .= "     LTAX.DATE_FROM ";

        return $query;
    }

    /**********/
    /* 左画面 */
    /**********/
    //１レコード取得
    function getRow($model, $taxCd, $dateFrom) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     LEVY_TAX_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$model->year}' ";
        $query .= "     AND TAX_CD     = '{$taxCd}' ";
        $query .= "     AND DATE_FROM  = '{$dateFrom}' ";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //コード：名称取得
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 as VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 as LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'P008' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]         = $fields["YEAR"];
        $data["TAX_CD"][TEXT]       = $fields["TAX_CD"];
        $data["DATE_FROM"][DATE]    = $fields["DATE_FROM"];
        $data["DATE_TO"][DATE]      = $fields["DATE_TO"];
        $data["TAX_VALUE"][TEXT]    = $fields["TAX_VALUE"];
        $data["TAX_SUMMARY"][TEXT]  = $fields["TAX_SUMMARY"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_TAX_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["DATE_TO"][DATE]      = $fields["DATE_TO"];
        $data["TAX_VALUE"][TEXT]    = $fields["TAX_VALUE"];
        $data["TAX_SUMMARY"][TEXT]  = $fields["TAX_SUMMARY"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR      = '".$fields["YEAR"]."' ";
        $where .= " AND TAX_CD    = '".$fields["TAX_CD"]."'";
        $where .= " AND DATE_FROM = '".$fields["DATE_FROM"]."'";

        $query = Query::updateSQL($data, "LEVY_TAX_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM LEVY_TAX_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '".$fields["YEAR"]."' ";
        $query .= " AND TAX_CD    = '".$fields["TAX_CD"]."'";
        $query .= " AND DATE_FROM = '".$fields["DATE_FROM"]."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
