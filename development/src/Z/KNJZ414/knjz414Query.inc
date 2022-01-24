<?php

require_once('for_php7.php');

class knjz414Query extends Query {
    //リスト取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     T1.INDUSTRY_LCD, ";
        $query .= "     T1.INDUSTRY_LNAME, ";
        $query .= "     T2.INDUSTRY_MCD, ";
        $query .= "     T2.INDUSTRY_MNAME ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_L_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     INDUSTRY_M_MST T2 ON T2.INDUSTRY_LCD = T1.INDUSTRY_LCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.INDUSTRY_LCD, ";
        $query .= "     T2.INDUSTRY_MCD ";

        return $query;
    }

    //産業種別マスタ(1レコード取得)
    function getIndustryMst($industry_lcd, $industry_mcd) {
        $query  = " SELECT ";
        $query .= "     T1.INDUSTRY_LCD, ";
        $query .= "     T1.INDUSTRY_LNAME, ";
        $query .= "     T2.INDUSTRY_MCD, ";
        $query .= "     T2.INDUSTRY_MNAME, ";
        $query .= "     T2.NO_OUTPUT ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_L_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     INDUSTRY_M_MST T2 ON T2.INDUSTRY_LCD = T1.INDUSTRY_LCD ";
        $query .= "                      AND T2.INDUSTRY_MCD = '".$industry_mcd."' ";
        $query .= " WHERE ";
        $query .= "     T1.INDUSTRY_LCD = '".$industry_lcd."' ";

        return $query;
    }

    //産業種別マスタ（大分類）存在チェック
    function chkIndustryL($industry_lcd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_L_MST ";
        $query .= " WHERE ";
        $query .= "     INDUSTRY_LCD = '".$industry_lcd."' ";

        return $query;
    }

    //産業種別マスタ（中分類）存在チェック
    function chkIndustryM($industry_lcd, $industry_mcd="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_M_MST ";
        $query .= " WHERE ";
        $query .= "     INDUSTRY_LCD = '".$industry_lcd."' ";
        if($industry_mcd){
            $query .= "     AND INDUSTRY_MCD = '".$industry_mcd."' ";
        }

        return $query;
    }

    //INDUSTRY_L_MST
    function &getUpdateLQuery($fields) {
        $db = Query::dbCheckOut();

        $cnt = get_count($db->getCol(knjz414Query::chkIndustryL($fields["INDUSTRY_LCD"])));
        $insert = ($cnt == 0) ? "insert" : "update";

        $data["INDUSTRY_LCD"][TEXT]     = $fields["INDUSTRY_LCD"];
        $data["INDUSTRY_LNAME"][TEXT]   = $fields["INDUSTRY_LNAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        if($insert == "insert"){
            $db->query(Query::insertSQL($data, "INDUSTRY_L_MST"));
        } else {
            $where  = " WHERE ";
            $where .= "     INDUSTRY_LCD = '".$fields["INDUSTRY_LCD"]."'  ";

            $db->query(Query::updateSQL($data, "INDUSTRY_L_MST", $where));
        }

        Query::dbCheckIn($db);
        return;
    }

    //INDUSTRY_M_MST
    function &getUpdateMQuery($fields) {
        $db = Query::dbCheckOut();

        $cnt = get_count($db->getCol(knjz414Query::chkIndustryM($fields["INDUSTRY_LCD"], $fields["INDUSTRY_MCD"])));
        $insert = ($cnt == 0) ? "insert" : "update";

        $data["INDUSTRY_LCD"][TEXT]     = $fields["INDUSTRY_LCD"];
        $data["INDUSTRY_MCD"][TEXT]     = $fields["INDUSTRY_MCD"];
        $data["INDUSTRY_MNAME"][TEXT]   = $fields["INDUSTRY_MNAME"];
        $data["NO_OUTPUT"][TEXT]        = $fields["NO_OUTPUT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        if($insert == "insert"){
            $db->query(Query::insertSQL($data, "INDUSTRY_M_MST"));
        } else {
            $where  = " WHERE ";
            $where .= "     INDUSTRY_LCD = '".$fields["INDUSTRY_LCD"]."' AND ";
            $where .= "     INDUSTRY_MCD = '".$fields["INDUSTRY_MCD"]."'  ";

            $db->query(Query::updateSQL($data, "INDUSTRY_M_MST", $where));
        }

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = get_count($db->getCol(knjz414Query::chkIndustryM($fields["INDUSTRY_LCD"])));
        if($cnt == 1){
            $query  = " DELETE FROM INDUSTRY_L_MST ";
            $query .= " WHERE ";
            $query .= "     INDUSTRY_LCD = '".$fields["INDUSTRY_LCD"]."' ";

            $db->query($query);
        }

        $query  = " DELETE FROM INDUSTRY_M_MST ";
        $query .= " WHERE ";
        $query .= "     INDUSTRY_LCD = '".$fields["INDUSTRY_LCD"]."' AND ";
        $query .= "     INDUSTRY_MCD = '".$fields["INDUSTRY_MCD"]."'";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
