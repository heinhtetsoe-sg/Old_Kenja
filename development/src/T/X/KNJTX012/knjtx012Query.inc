<?php

require_once('for_php7.php');

class knjtx012Query extends Query {

    //銀行コンボ用データ取得---NO001
    function selectBankQuery()
    {
        $query  = "  SELECT DISTINCT BANKCD, BANKNAME ";
        $query .= "  FROM ";
        $query .= "      BANK_MST ";
        $query .= "  ORDER BY BANKCD";
        return $query ;
    }

    //一覧用データ取得
    function selectQuery($backcd)
    {
        $query  = "  SELECT * ";
        $query .= "  FROM ";
        $query .= "      BANK_MST ";
        if ($backcd != "all") $query .= "  WHERE BANKCD = '" .$backcd ."' ";//NO001
        $query .= "  ORDER BY BANKCD";
        return $query ;
    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM bank_mst ".
                 "WHERE BANKCD || '-' || BRANCHCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $data["BANKCD"][TEXT]          = $fields["BANKCD"];
        $data["BRANCHCD"][TEXT]        = $fields["BRANCHCD"];
        $data["BANKNAME"][TEXT]        = $fields["BANKNAME"];
        $data["BANKNAME_KANA"][TEXT]   = $fields["BANKNAME_KANA"];
        $data["BRANCHNAME"][TEXT]      = $fields["BRANCHNAME"];
        $data["BRANCHNAME_KANA"][TEXT] = $fields["BRANCHNAME_KANA"];
        $data["BANKZIPCD"][TEXT]       = $fields["BANKZIPCD"];
        $data["BANKADDR1"][TEXT]       = $fields["BANKADDR1"];
        $data["BANKADDR2"][TEXT]       = $fields["BANKADDR2"];
        $data["BANKTELNO"][TEXT]       = $fields["BANKTELNO"];
        $data["BANKFAXNO"][TEXT]       = $fields["BANKFAXNO"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "bank_mst");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["BRANCHCD"][TEXT]        = $fields["BRANCHCD"];
        $data["BANKNAME"][TEXT]        = $fields["BANKNAME"];
        $data["BANKNAME_KANA"][TEXT]   = $fields["BANKNAME_KANA"];
        $data["BRANCHNAME"][TEXT]      = $fields["BRANCHNAME"];
        $data["BRANCHNAME_KANA"][TEXT] = $fields["BRANCHNAME_KANA"];
        $data["BANKZIPCD"][TEXT]       = $fields["BANKZIPCD"];
        $data["BANKADDR1"][TEXT]       = $fields["BANKADDR1"];
        $data["BANKADDR2"][TEXT]       = $fields["BANKADDR2"];
        $data["BANKTELNO"][TEXT]       = $fields["BANKTELNO"];
        $data["BANKFAXNO"][TEXT]       = $fields["BANKFAXNO"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $where  = "WHERE BANKCD    = '" .$fields["BANKCD"] ."'";
        $where .= "  AND BRANCHCD  = '" .$fields["BRANCHCD"] ."'";
        $query  = Query::updateSQL($data, "bank_mst", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM bank_mst ";
        $query .= "WHERE BANKCD    = '" .$fields["BANKCD"] ."'";
        $query .= "  AND BRANCHCD  = '" .$fields["BRANCHCD"] ."'";
        $query;
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
?>
