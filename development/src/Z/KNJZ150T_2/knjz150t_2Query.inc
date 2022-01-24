<?php

require_once('for_php7.php');

class knjz150t_2Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT ";
        $query .= "    t1.*, ";
        $query .= "    t2.ISSUECOMPANYNAME, ";
        $query .= "    t2.ISSUECOMPANYABBV ";
        $query .= "FROM ";
        $query .= "    TEXTBOOK_MST t1 left join ISSUECOMPANY_MST t2 ON t1.ISSUECOMPANYCD = t2.ISSUECOMPANYCD ";
        $query .= "WHERE TEXTBOOKCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //表示データ取得 NO001
    function getData()
    {

        $query  = "SELECT ";
        $query .= "    t1.*, ";
        $query .= "    t2.ISSUECOMPANYNAME, ";
        $query .= "    t2.ISSUECOMPANYABBV ";
        $query .= "FROM ";
        $query .= "    TEXTBOOK_MST t1 left join ISSUECOMPANY_MST t2 ON t1.ISSUECOMPANYCD = t2.ISSUECOMPANYCD ";
        $query .= "ORDER BY t1.TEXTBOOKCD ";

        return $query;
    }

    //教科書区分データ取得 NO001
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

    //発行者データ取得 NO001
    function getIssu()
    {

        $query  = "SELECT ";
        $query .= "    ISSUECOMPANYCD, ";
        $query .= "    ISSUECOMPANYNAME, ";
        $query .= "    ISSUECOMPANYABBV ";
        $query .= "FROM ";
        $query .= "    ISSUECOMPANY_MST ";
        $query .= "ORDER BY ISSUECOMPANYCD ";

        return $query;
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

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $data["TEXTBOOKCD"][TEXT]           = $fields["TEXTBOOKCD"];
        $data["TEXTBOOKDIV"][TEXT]          = $fields["TEXTBOOKDIV"];
        $data["TEXTBOOKNAME"][TEXT]         = $fields["TEXTBOOKNAME"];
        $data["TEXTBOOKABBV"][TEXT]         = $fields["TEXTBOOKABBV"];
        $data["TEXTBOOKMK"][TEXT]           = $fields["TEXTBOOKMK"];		//NO001
        $data["TEXTBOOKMS"][TEXT]           = $fields["TEXTBOOKMS"];		//NO001
        $data["TEXTBOOKWRITINGNAME"][TEXT]  = $fields["TEXTBOOKWRITINGNAME"];
        $data["TEXTBOOKPRICE"][NUMBER]      = $fields["TEXTBOOKPRICE"];
        $data["TEXTBOOKUNITPRICE"][NUMBER]  = $fields["TEXTBOOKUNITPRICE"];
        $data["ISSUECOMPANYCD"][TEXT]       = $fields["ISSUECOMPANYCD"];	//NO001
//        $data["CONTRACTORNAME"][TEXT]       = $fields["CONTRACTORNAME"];	//NO001
        $data["REMARK"][TEXT]               = $fields["REMARK"];			//NO001
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
        $data["TEXTBOOKMK"][TEXT]           = $fields["TEXTBOOKMK"];		//NO001
        $data["TEXTBOOKMS"][TEXT]           = $fields["TEXTBOOKMS"];		//NO001
        $data["TEXTBOOKWRITINGNAME"][TEXT]  = $fields["TEXTBOOKWRITINGNAME"];
        $data["TEXTBOOKPRICE"][NUMBER]      = $fields["TEXTBOOKPRICE"];
        $data["TEXTBOOKUNITPRICE"][NUMBER]  = $fields["TEXTBOOKUNITPRICE"];
        $data["ISSUECOMPANYCD"][TEXT]       = $fields["ISSUECOMPANYCD"];	//NO001
//        $data["CONTRACTORNAME"][TEXT]       = $fields["CONTRACTORNAME"];NO001
        $data["REMARK"][TEXT]                 = $fields["REMARK"];			//NO001
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
