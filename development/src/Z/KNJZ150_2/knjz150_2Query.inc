<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjz150_2Query.inc 56580 2017-10-22 12:35:29Z maeshiro $
class knjz150_2Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM TEXTBOOK_MST ".
                 " WHERE TEXTBOOKCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度データの確認
    function IsExisting($textbookcd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM TEXTBOOK_YDAT WHERE TEXTBOOKCD = '".$textbookcd."'"; 
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }

    //教科書区分データ取得
    function getName()
    {

        $query  = "SELECT ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND NAMECD1 = 'M004' ";
        $query .= "ORDER BY NAMECD2 ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $data["TEXTBOOKCD"][TEXT]           = $fields["TEXTBOOKCD"];
        $data["TEXTBOOKDIV"][TEXT]          = $fields["TEXTBOOKDIV"];
        $data["TEXTBOOKNAME"][TEXT]         = $fields["TEXTBOOKNAME"];
        $data["TEXTBOOKABBV"][TEXT]         = $fields["TEXTBOOKABBV"];
        $data["TEXTBOOKWRITINGNAME"][TEXT]  = $fields["TEXTBOOKWRITINGNAME"];
        $data["TEXTBOOKPRICE"][NUMBER]      = $fields["TEXTBOOKPRICE"];
        $data["TEXTBOOKUNITPRICE"][NUMBER]  = $fields["TEXTBOOKUNITPRICE"];
        $data["ISSUECOMPANY"][TEXT]         = $fields["ISSUECOMPANY"];
        $data["CONTRACTORNAME"][TEXT]       = $fields["CONTRACTORNAME"];
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "TEXTBOOK_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["TEXTBOOKCD"][TEXT]           = $fields["TEXTBOOKCD"];
        $data["TEXTBOOKDIV"][TEXT]          = $fields["TEXTBOOKDIV"];
        $data["TEXTBOOKNAME"][TEXT]         = $fields["TEXTBOOKNAME"];
        $data["TEXTBOOKABBV"][TEXT]         = $fields["TEXTBOOKABBV"];
        $data["TEXTBOOKWRITINGNAME"][TEXT]  = $fields["TEXTBOOKWRITINGNAME"];
        $data["TEXTBOOKPRICE"][NUMBER]      = $fields["TEXTBOOKPRICE"];
        $data["TEXTBOOKUNITPRICE"][NUMBER]  = $fields["TEXTBOOKUNITPRICE"];
        $data["ISSUECOMPANY"][TEXT]         = $fields["ISSUECOMPANY"];
        $data["CONTRACTORNAME"][TEXT]       = $fields["CONTRACTORNAME"];
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $where = " WHERE TEXTBOOKCD = '" .$fields["TEXTBOOKCD"]."'";

        $query = Query::updateSQL($data, "TEXTBOOK_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM TEXTBOOK_MST "
                ." WHERE TEXTBOOKCD  = '" .$fields["TEXTBOOKCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
