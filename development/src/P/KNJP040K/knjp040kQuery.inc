<?php

require_once('for_php7.php');

class knjp040kQuery extends Query {

    //起動チェック
    function checktoStart()
    {
        return "SELECT COUNT(*) FROM bank_mst";
    }

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM registbank_dat WHERE schregno = '".$model->schregno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    function getStudentName($schregno)
    {
        return "SELECT SCHREGNO,NAME_SHOW,NAME_KANA FROM SCHREG_BASE_MST WHERE SCHREGNO ='".$schregno."'";       
    }

    function nameGet($cd)
    {
        $query  = "SELECT namecd2, name1 FROM v_name_mst WHERE namecd1 = '".$cd."' AND year = '".CTRL_YEAR."'";
        return $query;
    }
    
    //銀行の取得
    function getBankcd()
    {
#        $query  = "SELECT DISTINCT bankcd, bankname||'('||BANKNAME_KANA||')' AS bankname, bankname_kana FROM bank_mst ORDER BY bankname_kana";
#        $query  = "SELECT DISTINCT bankcd, bankname||'('||BANKNAME_KANA||')' AS bankname FROM bank_mst ORDER BY bankcd";   #2006/01/23
        $query  = "SELECT DISTINCT bankcd, coalesce(bankname,'')||'('||BANKNAME_KANA||')' AS bankname FROM bank_mst ORDER BY bankcd";
        return $query;
    }

    //銀行の取得(支店名)
    function getBranchcd($bankcd)
    {
#        $query  = "SELECT BANKCD,BRANCHCD,BRANCHNAME||'('||BRANCHNAME_KANA||')' AS BRANCHNAME FROM bank_mst "; #2006/01/23
        $query  = "SELECT BANKCD,BRANCHCD,coalesce(BRANCHNAME,'')||'('||BRANCHNAME_KANA||')' AS BRANCHNAME FROM bank_mst ";
        $query  .= "WHERE   BANKCD      = '".$bankcd ."' ";
        $query  .= "ORDER BY BANKCD,BRANCHCD";
        return $query;

    }

    //リストの取得
    function getList($schregno)
    {
        $query  = "SELECT relationship";
        $query .= "      ,MEISYOU_GET(relationship, 'H201', 1) AS rela_name";
        $query .= "      ,guard_name AS name_show";
        $query .= "      ,guard_kana AS name_kana";
        $query .= "  FROM guardian_dat ";
        $query .= " WHERE schregno = '".$schregno."'";

        return $query;
    }

   function &getUpdateQuery($model)
   {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM registbank_dat WHERE SCHREGNO = '" .$model->schregno." '";
        $db->query($query);

        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["BANKCD"][TEXT]       = $model->field["BANKCD"];
        $data["BRANCHCD"][TEXT]     = $model->field["BRANCHCD"];
        $data["DEPOSIT_ITEM"][TEXT] = $model->field["DEPOSIT_ITEM"];
        $data["ACCOUNTNO"][TEXT]    = $model->field["ACCOUNTNO"];
        $data["ACCOUNTNAME"][TEXT]  = $model->field["ACCOUNTNAME"];
        $data["RELATIONSHIP"][TEXT] = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "registbank_dat");
        $db->query($query);
        Query::dbCheckIn($db);
        return ;
   }

   function &getDeleteQuery($model)
   {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM registbank_dat WHERE SCHREGNO = '" .$model->schregno." '";
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

}
?>
