<?php

require_once('for_php7.php');

class knjz090_2Query extends Query {

    //一覧用データ取得
    function selectQuery($model)
    {
#        $query  = "";
#        $query .= "  SELECT";
#        $query .= "      T1.FINSCHOOLCD,";
#        $query .= "      T1.FINSCHOOL_DISTCD || ':' || T2.NAME1 AS FINSCHOOL_DISTCD,";
#        $query .= "      T1.FINSCHOOL_NAME,";
#        $query .= "      T1.FINSCHOOL_KANA,";
#        $query .= "      T1.PRINCNAME,";
#        $query .= "      T1.PRINCNAME_SHOW,";
#        $query .= "      T1.PRINCKANA,";
#        $query .= "      T1.DISTRICTCD || ':' || T3.NAME1 AS DISTRICTCD,";
#        $query .= "      T1.FINSCHOOL_ZIPCD,";
#        $query .= "      T1.FINSCHOOL_ADDR1,";
#        $query .= "      T1.FINSCHOOL_ADDR2,";
#        $query .= "      T1.FINSCHOOL_TELNO,";
#        $query .= "      T1.FINSCHOOL_FAXNO,";
#        $query .= "      T1.EDBOARDCD || ':' || EDBOARDNAME AS EDBOARDCD,";
#        $query .= "      T1.REGISTERCD,";
#        $query .= "      T1.UPDATED";
#        $query .= "  FROM";
#        $query .= "      FINSCHOOL_MST T1";
#        $query .= "      LEFT OUTER JOIN NAME_MST    T2 ON T2.NAMECD1   = 'L001' AND T2.NAMECD2 = T1.FINSCHOOL_DISTCD";
#        $query .= "      LEFT OUTER JOIN NAME_MST    T3 ON T3.NAMECD1   = 'Z003' AND T3.NAMECD2 = T1.DISTRICTCD";
#        $query .= "      LEFT OUTER JOIN EDBOARD_MST T4 ON T4.EDBOARDCD = T1.EDBOARDCD";
#        $query .= "  ORDER BY FINSCHOOLCD";
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.FINSCHOOLCD,";
        $query .= "      T1.FINSCHOOL_DISTCD || ':' || MEISYOU_GET(T1.FINSCHOOL_DISTCD,'L001',1) AS FINSCHOOL_DISTCD,";
        $query .= "      T1.FINSCHOOL_NAME,";
        $query .= "      T1.FINSCHOOL_KANA,";
        $query .= "      T1.PRINCNAME,";
        $query .= "      T1.PRINCNAME_SHOW,";
        $query .= "      T1.PRINCKANA,";
        $query .= "      T1.DISTRICTCD || ':' || MEISYOU_GET(T1.DISTRICTCD,'Z003',1) AS DISTRICTCD,";
        $query .= "      T1.FINSCHOOL_ZIPCD,";
        $query .= "      T1.FINSCHOOL_ADDR1,";
        $query .= "      T1.FINSCHOOL_ADDR2,";
        $query .= "      T1.FINSCHOOL_TELNO,";
        $query .= "      T1.FINSCHOOL_FAXNO,";
        $query .= "      T1.EDBOARDCD || ':' || T2.EDBOARDNAME AS EDBOARDCD,";
        $query .= "      T1.REGISTERCD,";
        $query .= "      T1.UPDATED";
        $query .= "  FROM";
        $query .= "      FINSCHOOL_MST T1";
        $query .= "      LEFT OUTER JOIN EDBOARD_MST T2 ON T2.EDBOARDCD = T1.EDBOARDCD";
        $query .= "  ORDER BY FINSCHOOLCD";
        return $query ;
    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM FINSCHOOL_MST ".
                 "WHERE FINSCHOOLCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //出身学校学区コード
    function getFinschoolDistcd()
    {
        return "SELECT namecd2, name1 FROM v_name_mst WHERE year = '".CTRL_YEAR."' AND namecd1 = 'L001' ORDER BY namecd2";
    }

    //教育委員会コード
    function getEdboard()
    {
        return "select distinct EDBOARDCD, EDBOARDNAME from edboard_mst order by EDBOARDCD";
    }

    //地区コード
    function getDistinct()
    {
        return "SELECT namecd2, name1 FROM v_name_mst WHERE year = '".CTRL_YEAR."' AND namecd1 = 'Z003' ORDER BY namecd2";
    }

    //年度データの確認
    function IsExisting($finschoolcd)
    {
        $db    = Query::dbCheckOut();
        $query = "select * from FINSCHOOL_YDAT where FINSCHOOLCD = '".$finschoolcd."'"; 
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
        
        $data["FINSCHOOLCD"][TEXT]          = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_DISTCD"][TEXT]     = $fields["FINSCHOOL_DISTCD"];
        $data["FINSCHOOL_NAME"][TEXT]       = $fields["FINSCHOOL_NAME"];
        $data["FINSCHOOL_KANA"][TEXT]       = $fields["FINSCHOOL_KANA"];
        $data["PRINCNAME"][TEXT]            = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]       = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]            = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]           = $fields["DISTRICTCD"];
        $data["FINSCHOOL_ZIPCD"][TEXT]      = $fields["FINSCHOOL_ZIPCD"];
        $data["FINSCHOOL_ADDR1"][TEXT]      = $fields["FINSCHOOL_ADDR1"];
        $data["FINSCHOOL_ADDR2"][TEXT]      = $fields["FINSCHOOL_ADDR2"];
        $data["FINSCHOOL_TELNO"][TEXT]      = $fields["FINSCHOOL_TELNO"];
        $data["FINSCHOOL_FAXNO"][TEXT]      = $fields["FINSCHOOL_FAXNO"];
        $data["EDBOARDCD"][TEXT]            = $fields["EDBOARDCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        
        $query = Query::insertSQL($data, "FINSCHOOL_MST");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["FINSCHOOLCD"][TEXT]          = $fields["FINSCHOOLCD"];        
        $data["FINSCHOOL_DISTCD"][TEXT]     = $fields["FINSCHOOL_DISTCD"];
        $data["FINSCHOOL_NAME"][TEXT]       = $fields["FINSCHOOL_NAME"];
        $data["FINSCHOOL_KANA"][TEXT]       = $fields["FINSCHOOL_KANA"];
        $data["PRINCNAME"][TEXT]            = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]       = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]            = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]           = $fields["DISTRICTCD"];
        $data["FINSCHOOL_ZIPCD"][TEXT]      = $fields["FINSCHOOL_ZIPCD"];
        $data["FINSCHOOL_ADDR1"][TEXT]      = $fields["FINSCHOOL_ADDR1"];
        $data["FINSCHOOL_ADDR2"][TEXT]      = $fields["FINSCHOOL_ADDR2"];
        $data["FINSCHOOL_TELNO"][TEXT]      = $fields["FINSCHOOL_TELNO"];
        $data["FINSCHOOL_FAXNO"][TEXT]      = $fields["FINSCHOOL_FAXNO"];
        $data["EDBOARDCD"][TEXT]            = $fields["EDBOARDCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where = "WHERE FINSCHOOLCD         = '" .$fields["FINSCHOOLCD"] ."'";
        $query = Query::updateSQL($data, "FINSCHOOL_MST", $where);

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

        $query = "DELETE FROM FINSCHOOL_MST "
                ." WHERE FINSCHOOLCD  = '" .$fields["FINSCHOOLCD"] ."'";
        $query;
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
?>
