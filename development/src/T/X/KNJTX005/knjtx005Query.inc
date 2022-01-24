<?php

require_once('for_php7.php');

class knjtx005Query extends Query {

    //１レコード取得
    function getRow($kojin_no, $s_date)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query  = " 	* ";
        $query .= " FROM ";
        $query .= " 	KOJIN_FURIKOMI_BANK_DAT ";
        $query .= " WHERE ";
        $query .= " 	KOJIN_NO = $kojin_no ";
        $query .= " AND S_DATE 	 = '" . str_replace("/","-", $s_date) ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $data["KOJIN_NO"][TEXT]     		 = $fields["KOJIN_NO"];
        $data["S_DATE"][TEXT]   		     = str_replace("/","-",$fields["S_DATE"]);
        $data["BANKCD"][TEXT] 			     = $fields["BANKCD"];
        $data["BRANCHCD"][TEXT]  			 = $fields["BRANCHCD"];
        $data["CHOKIN_DIV"][TEXT]   		 = $fields["CHOKIN_DIV"];
        $data["ACCOUNT_NO"][TEXT]   		 = $fields["ACCOUNT_NO"];
        $data["BANK_MEIGI_SEI_KANA"][TEXT]   = $fields["BANK_MEIGI_SEI_KANA"];
        $data["BANK_MEIGI_MEI_KANA"][TEXT]   = $fields["BANK_MEIGI_MEI_KANA"];
        $data["BANK_MEIGI_SEI_NAME"][TEXT]   = $fields["BANK_MEIGI_SEI_NAME"];
        $data["BANK_MEIGI_MEI_NAME"][TEXT]   = $fields["BANK_MEIGI_MEI_NAME"];
        $data["ZIPCD"][TEXT]   				 = $fields["ZIPCD"];
        $data["ADDR1"][TEXT]   				 = $fields["ADDR1"];
        $data["ADDR2"][TEXT]   				 = $fields["ADDR2"];
        $data["TELNO1"][TEXT]   			 = $fields["TELNO1"];
        $data["TELNO2"][TEXT]    			 = $fields["TELNO2"];
        $data["REGISTERCD"][TEXT]   		 = STAFFCD;
        $data["UPDATED"][NUMBER]    		 = "sysdate()";

        $query = Query::insertSQL($data, "KOJIN_FURIKOMI_BANK_DAT");

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["KOJIN_NO"][TEXT]     		 = $fields["KOJIN_NO"];
        $data["S_DATE"][TEXT]   		     = str_replace("/","-",$fields["S_DATE"]);
        $data["BANKCD"][TEXT] 			     = $fields["BANKCD"];
        $data["BRANCHCD"][TEXT]  			 = $fields["BRANCHCD"];
        $data["CHOKIN_DIV"][TEXT]   		 = $fields["CHOKIN_DIV"];
        $data["ACCOUNT_NO"][TEXT]   		 = $fields["ACCOUNT_NO"];
        $data["BANK_MEIGI_SEI_KANA"][TEXT]   = $fields["BANK_MEIGI_SEI_KANA"];
        $data["BANK_MEIGI_MEI_KANA"][TEXT]   = $fields["BANK_MEIGI_MEI_KANA"];
        $data["BANK_MEIGI_SEI_NAME"][TEXT]   = $fields["BANK_MEIGI_SEI_NAME"];
        $data["BANK_MEIGI_MEI_NAME"][TEXT]   = $fields["BANK_MEIGI_MEI_NAME"];
        $data["ZIPCD"][TEXT]   				 = $fields["ZIPCD"];
        $data["ADDR1"][TEXT]   				 = $fields["ADDR1"];
        $data["ADDR2"][TEXT]   				 = $fields["ADDR2"];
        $data["TELNO1"][TEXT]   			 = $fields["TELNO1"];
        $data["TELNO2"][TEXT]    			 = $fields["TELNO2"];
        $data["REGISTERCD"][TEXT] 	 		 = STAFFCD;
        $data["UPDATED"][NUMBER]  	 		 = "sysdate()";
        $where  = " WHERE KOJIN_NO   		 = $fields["KOJIN_NO"];
        $where .= "   AND   S_DATE    		 = '" . str_replace("/","-",$fields["S_DATE"]) ."' ";

        $query = Query::updateSQL($data, "KOJIN_FURIKOMI_BANK_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= " 	KOJIN_FURIKOMI_BANK_DAT ";
        $query .= " WHERE ";
        $query .= " 	KOJIN_NO = $fields["KOJIN_NO"] ";
        $query .= " AND S_DATE   = '" . str_replace("/","-", $fields["S_DATE"]) ."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
