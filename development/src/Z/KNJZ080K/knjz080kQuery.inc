<?php

require_once('for_php7.php');

class knjz080kQuery extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     applicationcd, ";
        $query .= "     applicationname, ";
        $query .= "     applicationmoney, ";
		//mk 2005.06.20 以下５行追加
        $query .= "      bankcd, ";
        $query .= "      branchcd, ";
        $query .= "      deposit_item, ";
        $query .= "      accountno, ";
        $query .= "      accountname ";
        $query .= " FROM ";
        $query .= "     application_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     applicationcd ";

        return $query;
    }

    //１レコード取得
    function getRow($model,$flg)
    {
       $db = Query::dbCheckOut();

       $query  = " SELECT ";
       $query .= "     applicationcd, ";
       $query .= "     applicationname, ";
       $query .= "     applicationmoney, ";
	   //mk 2005.06.20 以下５行追加
       $query .= "     bankcd, ";
       $query .= "     branchcd, ";
       $query .= "     bankcd || '-' || branchcd as bank_branchcd, ";
       $query .= "     deposit_item, ";
       $query .= "     accountno, ";
       $query .= "     accountname ";
       $query .= " FROM ";
       $query .= "     application_mst ";
       $query .= " WHERE ";
       $query .= "     year = '".$model->year."' AND ";

       if($flg == 1){
           $query .= "  applicationcd = '".$model->appli_no."'";
       }elseif($flg == 2){
           $query .= "  applicationcd = '".sprintf("%04d",$model->field["APPLICATIONCD"])."'";
       }

       $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
       Query::dbCheckIn($db);
       return $Row;
    }


    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]                 = $fields["YEAR"];
        $data["APPLICATIONCD"][TEXT]        = sprintf("%04d",$fields["APPLICATIONCD"]);
        $data["APPLICATIONNAME"][TEXT]      = $fields["APPLICATIONNAME"];
        $data["APPLICATIONMONEY"][NUMBER]   = $fields["APPLICATIONMONEY"];
		//mk 2005.06.20 以下追加
        $data["BANKCD"][TEXT]   			= $fields["BANKCD"];
        $arr = explode('-',$fields["BANK_BRANCHCD"]);
        $data["BRANCHCD"][TEXT]   			= $arr[1];
        $data["DEPOSIT_ITEM"][TEXT]   		= $fields["DEPOSIT_ITEM"];
        $data["ACCOUNTNO"][TEXT]   			= $fields["ACCOUNTNO"];
        $data["ACCOUNTNAME"][TEXT]   		= $fields["ACCOUNTNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "APPLICATION_MST");
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

        $data["APPLICATIONNAME"][TEXT]      = $fields["APPLICATIONNAME"];
        $data["APPLICATIONMONEY"][NUMBER]   = $fields["APPLICATIONMONEY"];
		//mk 2005.06.20 以下５行追加
        $data["BANKCD"][TEXT]   			= $fields["BANKCD"];
        $arr = explode('-',$fields["BANK_BRANCHCD"]);
        $data["BRANCHCD"][TEXT]   			= $arr[1];
        $data["DEPOSIT_ITEM"][TEXT]   		= $fields["DEPOSIT_ITEM"];
        $data["ACCOUNTNO"][TEXT]   			= $fields["ACCOUNTNO"];
        $data["ACCOUNTNAME"][TEXT]   		= $fields["ACCOUNTNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE year           = '".$fields["YEAR"]."'";
        $where .= "   AND applicationcd  = '".sprintf("%04d",$fields["APPLICATIONCD"])."'";

        $query = Query::updateSQL($data, "APPLICATION_MST", $where);
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

        $query  = " DELETE FROM application_mst ";
        $query .= " WHERE year           = '".$fields["YEAR"]."'";
        $query .= "   AND applicationcd  = '".sprintf("%04d",$fields["APPLICATIONCD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM application_mst WHERE year = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    //前年度のデータをコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO application_mst ";
        $query .= "           ( YEAR, ";
        $query .= "             APPLICATIONCD, ";
        $query .= "             APPLICATIONNAME, ";
        $query .= "             APPLICATIONMONEY, ";
		//mk 2005.06.20 以下５行追加
        $query .= "             BANKCD, ";
        $query .= "             BRANCHCD, ";
        $query .= "             DEPOSIT_ITEM, ";
        $query .= "             ACCOUNTNO, ";
        $query .= "             ACCOUNTNAME, ";
        $query .= "             registercd, ";
        $query .= "             updated ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->year."', ";
        $query .= "         APPLICATIONCD, ";
        $query .= "         APPLICATIONNAME, ";
        $query .= "         APPLICATIONMONEY, ";
		//mk 2005.06.20 以下５行追加
        $query .= "         BANKCD, ";
        $query .= "         BRANCHCD, ";
        $query .= "         DEPOSIT_ITEM, ";
        $query .= "         ACCOUNTNO, ";
        $query .= "         ACCOUNTNAME, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         application_mst ";
        $query .= "     WHERE ";
        $query .= "         year = '".($model->year - 1)."'";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

	//mk 2005.06.17 以下追加（銀行情報）
    //銀行コードの取得
    function getBankcd()
    {
        $query  = "SELECT DISTINCT bankcd, bankname FROM bank_mst ORDER BY bankcd";
        return $query;
    }

    //支店コードの取得
    function getBranchcd($Row)
    {
        $query  = "SELECT bankcd, branchcd, branchname FROM bank_mst WHERE bankcd = '".$Row["BANKCD"]."' ORDER BY branchcd";
        return $query;
    }

    //預金種目の取得
    function nameGet($cd)
    {
        $query  = "SELECT namecd2, name1 FROM v_name_mst WHERE namecd1 = '".$cd."' AND year = '".CTRL_YEAR."'";
        return $query;
    }
}
?>
