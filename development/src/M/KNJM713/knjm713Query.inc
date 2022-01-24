<?php

require_once('for_php7.php');

class knjm713Query extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_BANK_MST ";
        $query .= " ORDER BY ";
        $query .= "     COLLECT_BANK_CD, ";
        $query .= "     COLLECT_BANK_DIV ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getColectBankCnt($cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_BANK_MST ";
        $query .= " WHERE ";
        $query .= "     COLLECT_BANK_CD = '".$cd."'";

        return $query;
    }

    //１レコード取得
    function getRow($model,$flg)
    {
       $db = Query::dbCheckOut();

       $query  = " SELECT ";
       $query .= "     * ";
       $query .= " FROM ";
       $query .= "     COLLECT_BANK_MST ";
       $query .= " WHERE ";
       if ($flg == 1){
           $query .= "      COLLECT_BANK_CD = '".$model->collect_bank_cd."'";
           $query .= " AND  COLLECT_BANK_DIV = '".$model->collect_bank_div."' ";
       } else if ($flg == 2){
           $query .= "      COLLECT_BANK_CD  = '".sprintf("%04d",$model->field["COLLECT_BANK_CD"])."'";
           $query .= " AND  COLLECT_BANK_DIV = '".$model->field["COLLECT_BANK_DIV"]."' ";
       }

       $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
       Query::dbCheckIn($db);
       return $Row;
    }

	//金融機関コード
    function getBankcd()
    {
        $query  = " SELECT DISTINCT  ";
        $query .= " 	BANKCD AS VALUE, ";
        $query .= " 	BANKCD || ':' || BANKNAME AS LABEL";
        $query .= "  FROM ";
        $query .= "     BANK_MST ";
        $query .= "  ORDER BY ";
        $query .= "  	VALUE ";

        return $query ;
    }

	//支店コード
    function getBanchcd($bankcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= " 	BRANCHCD AS VALUE, ";
        $query .= " 	BRANCHCD || ':' || BRANCHNAME AS LABEL";
        $query .= "  FROM ";
        $query .= "     BANK_MST ";
        $query .= "  WHERE ";
        $query .= "  	BANKCD = '" .$bankcd. "' ";
        $query .= "  ORDER BY ";
        $query .= "  	VALUE ";

        return $query ;
    }

	//預金種別
    function getNameMst($cd)
    {
        $query  = " SELECT DISTINCT  ";
        $query .= " 	T1.NAMECD2 AS VALUE, ";
        $query .= " 	T1.NAMECD2 || ':' || T2.NAME1 AS LABEL";
        $query .= "  FROM ";
        $query .= "     NAME_YDAT T1";
        $query .= "  LEFT JOIN NAME_MST T2 ON T1.NAMECD1 = T2.NAMECD1 ";
        $query .= "  					  AND T1.NAMECD2 = T2.NAMECD2 ";
        $query .= "  WHERE ";
        $query .= "  	  T1.YEAR    = '" .CTRL_YEAR. "' ";
        $query .= "  AND  T1.NAMECD1 = '" .$cd. "' ";
        $query .= "  ORDER BY ";
        $query .= "  	VALUE ";

        return $query ;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["COLLECT_BANK_CD"][TEXT]      = sprintf("%04d",$fields["COLLECT_BANK_CD"]);
        $data["COLLECT_BANK_NAME"][TEXT]    = $fields["COLLECT_BANK_NAME"];
        $data["COLLECT_BANK_DIV"][TEXT]     = $fields["COLLECT_BANK_DIV"];
        if ($fields["COLLECT_BANK_DIV"] === '1') {
            $data["BANK_CD"][TEXT]   			= $fields["BANK_CD"];
            $data["BRAN_CHCD"][TEXT]   			= $fields["BRAN_CHCD"];
            $data["BANK_DEPOSIT_ITEM"][TEXT]   	= $fields["BANK_DEPOSIT_ITEM"];
            $data["BANK_ACCOUNTNO"][TEXT]   	= $fields["BANK_ACCOUNTNO"];
            $data["BANK_ACCOUNTNAME"][TEXT]   	= $fields["BANK_ACCOUNTNAME"];
        } else {
            $data["YUUCYO_CD"][TEXT]   			= $fields["YUUCYO_CD"];
            $data["YUUCYO_DEPOSIT_ITEM"][TEXT]  = $fields["YUUCYO_DEPOSIT_ITEM"];
            $data["YUUCYO_ACCOUNTNO"][TEXT]   	= $fields["YUUCYO_ACCOUNTNO"];
            $data["YUUCYO_ACCOUNTNAME"][TEXT]   = $fields["YUUCYO_ACCOUNTNAME"];
        }
        $data["ZIPCD"][TEXT]       			= $fields["ZIPCD"];
        $data["ADDR1"][TEXT]       			= $fields["ADDR1"];
        $data["ADDR2"][TEXT]       			= $fields["ADDR2"];
        $data["TELNO1"][TEXT]       		= $fields["TELNO1"];
        $data["TELNO2"][TEXT]       		= $fields["TELNO2"];
        
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_BANK_MST");
        $db->query($query);
        
        //学校払込名称の更新
        $data2["COLLECT_BANK_NAME"][TEXT]    = $fields["COLLECT_BANK_NAME"];
        $where  = " WHERE COLLECT_BANK_CD  = '".sprintf("%04d",$fields["COLLECT_BANK_CD"])."'";
        
        $query = Query::updateSQL($data2, "COLLECT_BANK_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["COLLECT_BANK_NAME"][TEXT]    = $fields["COLLECT_BANK_NAME"];
        if ($fields["COLLECT_BANK_DIV"] === '1') {
            $data["BANK_CD"][TEXT]   			= $fields["BANK_CD"];
            $data["BRAN_CHCD"][TEXT]   			= $fields["BRAN_CHCD"];
            $data["BANK_DEPOSIT_ITEM"][TEXT]   	= $fields["BANK_DEPOSIT_ITEM"];
            $data["BANK_ACCOUNTNO"][TEXT]   	= $fields["BANK_ACCOUNTNO"];
            $data["BANK_ACCOUNTNAME"][TEXT]   	= $fields["BANK_ACCOUNTNAME"];
        } else {
            $data["YUUCYO_CD"][TEXT]   			= $fields["YUUCYO_CD"];
            $data["YUUCYO_DEPOSIT_ITEM"][TEXT]  = $fields["YUUCYO_DEPOSIT_ITEM"];
            $data["YUUCYO_ACCOUNTNO"][TEXT]   	= $fields["YUUCYO_ACCOUNTNO"];
            $data["YUUCYO_ACCOUNTNAME"][TEXT]   = $fields["YUUCYO_ACCOUNTNAME"];
        }
        $data["ZIPCD"][TEXT]       			= $fields["ZIPCD"];
        $data["ADDR1"][TEXT]       			= $fields["ADDR1"];
        $data["ADDR2"][TEXT]       			= $fields["ADDR2"];
        $data["TELNO1"][TEXT]       		= $fields["TELNO1"];
        $data["TELNO2"][TEXT]       		= $fields["TELNO2"];
        
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE COLLECT_BANK_CD  = '".sprintf("%04d",$fields["COLLECT_BANK_CD"])."'";
        $where .= "   AND COLLECT_BANK_DIV = '".$fields["COLLECT_BANK_DIV"]."'";

        $query = Query::updateSQL($data, "COLLECT_BANK_MST", $where);
        $db->query($query);

        //学校払込名称の更新
        $data2["COLLECT_BANK_NAME"][TEXT]    = $fields["COLLECT_BANK_NAME"];
        $where  = " WHERE COLLECT_BANK_CD  = '".sprintf("%04d",$fields["COLLECT_BANK_CD"])."'";
        
        $query = Query::updateSQL($data2, "COLLECT_BANK_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM COLLECT_BANK_MST ";
        $query .= " WHERE COLLECT_BANK_CD  = '".sprintf("%04d",$fields["COLLECT_BANK_CD"])."'";
        $query .= "   AND COLLECT_BANK_DIV = '".$fields["COLLECT_BANK_DIV"]."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
