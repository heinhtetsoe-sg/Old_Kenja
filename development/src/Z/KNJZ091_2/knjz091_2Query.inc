<?php

require_once('for_php7.php');

class knjz091_2Query extends Query {

    //一覧用データ取得
    function selectQuery($model)
    {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      PRISCHOOLCD,";
        $query .= "      PRISCHOOL_NAME,";
        $query .= "      PRISCHOOL_KANA,";
        $query .= "      PRINCNAME,";
        $query .= "      PRINCNAME_SHOW,";
        $query .= "      PRINCKANA,";
        $query .= "      DISTRICTCD || ':' || MEISYOU_GET(DISTRICTCD,'Z003',1) AS DISTRICTCD,";
        $query .= "      PRISCHOOL_ZIPCD,";
        $query .= "      PRISCHOOL_ADDR1,";
        $query .= "      PRISCHOOL_ADDR2,";
        $query .= "      PRISCHOOL_TELNO,";
        $query .= "      PRISCHOOL_FAXNO,";
        $query .= "      GRP_PRISCHOOLCD";
        $query .= "  FROM";
        $query .= "      PRISCHOOL_MST";
        $query .= "  ORDER BY";
        $query .= "      PRISCHOOLCD";
        return $query ;
    }

    //１レコード取得
    function getRow($prischoolcd)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT * FROM PRISCHOOL_MST WHERE PRISCHOOLCD = '" .$prischoolcd ."'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //地区コード
    function getDistinct()
    {
        return "SELECT DISTINCT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z003' ORDER BY NAMECD2";
    }

    //年度データの確認
    function IsExisting($prischoolcd)
    {
        $db    = Query::dbCheckOut();
        $query = "select * from PRISCHOOL_YDAT where PRISCHOOLCD = '".$prischoolcd."'"; 
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

        $data["PRISCHOOLCD"][TEXT]          = $fields["PRISCHOOLCD"];
        $data["PRISCHOOL_NAME"][TEXT]       = $fields["PRISCHOOL_NAME"];
        $data["PRISCHOOL_KANA"][TEXT]       = $fields["PRISCHOOL_KANA"];
        $data["PRINCNAME"][TEXT]            = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]       = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]            = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]           = $fields["DISTRICTCD"];
        $data["PRISCHOOL_ZIPCD"][TEXT]      = $fields["PRISCHOOL_ZIPCD"];
        $data["PRISCHOOL_ADDR1"][TEXT]      = $fields["PRISCHOOL_ADDR1"];
        $data["PRISCHOOL_ADDR2"][TEXT]      = $fields["PRISCHOOL_ADDR2"];
        $data["PRISCHOOL_TELNO"][TEXT]      = $fields["PRISCHOOL_TELNO"];
        $data["PRISCHOOL_FAXNO"][TEXT]      = $fields["PRISCHOOL_FAXNO"];
        $data["GRP_PRISCHOOLCD"][TEXT]      = $fields["GRP_PRISCHOOLCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "PRISCHOOL_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();

        $data["PRISCHOOLCD"][TEXT]          = $fields["PRISCHOOLCD"];
        $data["PRISCHOOL_NAME"][TEXT]       = $fields["PRISCHOOL_NAME"];
        $data["PRISCHOOL_KANA"][TEXT]       = $fields["PRISCHOOL_KANA"];
        $data["PRINCNAME"][TEXT]            = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]       = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]            = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]           = $fields["DISTRICTCD"];
        $data["PRISCHOOL_ZIPCD"][TEXT]      = $fields["PRISCHOOL_ZIPCD"];
        $data["PRISCHOOL_ADDR1"][TEXT]      = $fields["PRISCHOOL_ADDR1"];
        $data["PRISCHOOL_ADDR2"][TEXT]      = $fields["PRISCHOOL_ADDR2"];
        $data["PRISCHOOL_TELNO"][TEXT]      = $fields["PRISCHOOL_TELNO"];
        $data["PRISCHOOL_FAXNO"][TEXT]      = $fields["PRISCHOOL_FAXNO"];
        $data["GRP_PRISCHOOLCD"][TEXT]      = $fields["GRP_PRISCHOOLCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where = "WHERE PRISCHOOLCD         = '" .$fields["PRISCHOOLCD"] ."'";
        $query = Query::updateSQL($data, "PRISCHOOL_MST", $where);

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
        $query = "DELETE FROM PRISCHOOL_MST WHERE PRISCHOOLCD = '".$fields["PRISCHOOLCD"]."'";
        $query;
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
?>
