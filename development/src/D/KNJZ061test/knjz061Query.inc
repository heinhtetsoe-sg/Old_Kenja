<?php

require_once('for_php7.php');

class knjz061Query extends Query {

    //ANOTHER_CLASS_MST データ取得
    function getData($model)
    {
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      ANOTHER_CLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND ";

        return $query;
    }

    //学校校種、教育課程コード取得
    function getNamecd($cd)
    {
        $query  = " SELECT ";
        if ($cd === 'A023') {
            $query .= "      NAME1 AS VALUE, ";
            $query .= "      NAME1 || '：' || ABBV1 AS LABEL ";
        } else {
            $query .= "      NAMECD2 AS VALUE, ";
            $query .= "      NAMECD2 || '：' || NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "      NAMECD1 = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($cd, $model, $school_kind)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      ANOTHER_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "      CLASSCD      = '" .$cd ."'";
        $query .= "  AND SCHOOL_KIND  = '" .$school_kind."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model, $school_kind)
    {
        $db = Query::dbCheckOut();
        
        $data["CLASSCD"][TEXT]              = $fields["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]          = $fields["SCHOOL_KIND"];
        $data["CLASSNAME"][TEXT]            = $fields["CLASSNAME"];
        $data["CLASSABBV"][TEXT]            = $fields["CLASSABBV"];
        $data["CLASSNAME_ENG"][TEXT]        = $fields["CLASSNAME_ENG"];
        $data["CLASSABBV_ENG"][TEXT]        = $fields["CLASSABBV_ENG"];
        $data["CLASSORDERNAME1"][TEXT]      = $fields["CLASSORDERNAME1"];
        $data["CLASSORDERNAME2"][TEXT]      = $fields["CLASSORDERNAME2"];
        $data["CLASSORDERNAME3"][TEXT]      = $fields["CLASSORDERNAME3"];
        $data["SUBCLASSES"][NUMBER]         = $fields["SUBCLASSES"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["SHOWORDER2"][NUMBER]         = $fields["SHOWORDER2"];
        $data["SHOWORDER3"][NUMBER]         = $fields["SHOWORDER3"];
        $data["SHOWORDER4"][NUMBER]         = $fields["SHOWORDER4"];
        $data["ELECTDIV"][TEXT]             = ($fields["ELECTDIV"]==1) ? 1 : 0;
        $data["SPECIALDIV"][TEXT]           = ($fields["SPECIALDIV"]==1) ? 1 : 0;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "ANOTHER_CLASS_MST");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields, $model, $school_kind)
    {
        $db = Query::dbCheckOut();

        $data["CLASSCD"][TEXT]              = $fields["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]          = $fields["SCHOOL_KIND"];
        $data["CLASSNAME"][TEXT]            = $fields["CLASSNAME"];
        $data["CLASSABBV"][TEXT]            = $fields["CLASSABBV"];
        $data["CLASSNAME_ENG"][TEXT]        = $fields["CLASSNAME_ENG"];
        $data["CLASSABBV_ENG"][TEXT]        = $fields["CLASSABBV_ENG"];
        $data["CLASSORDERNAME1"][TEXT]      = $fields["CLASSORDERNAME1"];
        $data["CLASSORDERNAME2"][TEXT]      = $fields["CLASSORDERNAME2"];
        $data["CLASSORDERNAME3"][TEXT]      = $fields["CLASSORDERNAME3"];
        $data["SUBCLASSES"][NUMBER]         = $fields["SUBCLASSES"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["SHOWORDER2"][NUMBER]         = $fields["SHOWORDER2"];
        $data["SHOWORDER3"][NUMBER]         = $fields["SHOWORDER3"];
        $data["SHOWORDER4"][NUMBER]         = $fields["SHOWORDER4"];
        $data["ELECTDIV"][TEXT]             = ($fields["ELECTDIV"]==1) ? 1 : 0;
        $data["SPECIALDIV"][TEXT]           = ($fields["SPECIALDIV"]==1) ? 1 : 0;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where = " WHERE CLASSCD      = '".$fields["CLASSCD"]."'";
        $where .= "  AND SCHOOL_KIND  = '".$fields["SCHOOL_KIND"]."' ";
        $query = Query::updateSQL($data, "ANOTHER_CLASS_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($fields, $model, $school_kind)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      ANOTHER_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "      CLASSCD     = '" .$fields["CLASSCD"]."'";
        $query .= "  AND SCHOOL_KIND = '" .$fields["SCHOOL_KIND"]."' ";
        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
