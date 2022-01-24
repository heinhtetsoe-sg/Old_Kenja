<?php

require_once('for_php7.php');

class knjs510Query extends Query {

    //公休日マスタリスト取得
    function getPubHolidayList()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEQ, ";
        $query .= "     HOLIDAY_DIV, ";
        $query .= "     INT(HOLIDAY_MONTH) AS HOLIDAY_MONTH, ";
        $query .= "     INT(HOLIDAY_DAY) AS HOLIDAY_DAY, ";
        $query .= "     HOLIDAY_WEEK_PERIOD, ";
        $query .= "     HOLIDAY_WEEKDAY, ";
        $query .= "     HOLIDAY_NAME ";
        $query .= " FROM ";
        $query .= "     PUBLIC_HOLIDAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     HOLIDAY_MONTH, ";
        $query .= "     HOLIDAY_DAY, ";
        $query .= "     HOLIDAY_WEEK_PERIOD, ";
        $query .= "     HOLIDAY_WEEKDAY, ";
        $query .= "     SEQ ";

        return $query;
    }

    //公休日マスタ取得
    function getPubHolidayData($year, $seq="", $max="")
    {
        $query  = " SELECT ";
        if($max) {
            $query .= "     MAX(SEQ) AS MAX_SEQ ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     PUBLIC_HOLIDAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if($seq) {
            $query .= "     AND SEQ = ".$seq." ";
        }

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields, $model)
    {         
        $db = Query::dbCheckOut();
        $seq = $db->getOne(knjs510Query::getPubHolidayData(CTRL_YEAR, "","max"))+1;   

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEQ"][NUMBER]                = $seq;
        $data["HOLIDAY_DIV"][TEXT]          = $fields["HOLIDAY_DIV"];
        $data["HOLIDAY_MONTH"][TEXT]        = ($fields["HOLIDAY_MONTH"]) ? sprintf("%02d", $fields["HOLIDAY_MONTH"]) : "";
        $data["HOLIDAY_DAY"][TEXT]          = ($fields["HOLIDAY_DAY"]) ? sprintf("%02d", $fields["HOLIDAY_DAY"]) : "";
        $data["HOLIDAY_WEEK_PERIOD"][TEXT]  = ($fields["HOLIDAY_WEEK_PERIOD"]) ? $fields["HOLIDAY_WEEK_PERIOD"] : "";
        $data["HOLIDAY_WEEKDAY"][TEXT]      = ($fields["HOLIDAY_WEEKDAY"]) ? $fields["HOLIDAY_WEEKDAY"] : "";
        $data["HOLIDAY_NAME"][TEXT]         = $fields["HOLIDAY_NAME"];
        if ($model->Properties["useHOLIDAY_KIND"] == "1") {
            $data["HOLIDAY_KIND"][TEXT]         = $fields["HOLIDAY_KIND"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "PUBLIC_HOLIDAY_MST");
        $db->query($query);
        Query::dbCheckIn($db);

        return $seq;
    }

    //UPDATE
    function &getUpdateQuery($fields, $seq, $model)
    {        
        $db = Query::dbCheckOut();

        $data["HOLIDAY_DIV"][TEXT]          = $fields["HOLIDAY_DIV"];
        $data["HOLIDAY_MONTH"][TEXT]        = ($fields["HOLIDAY_MONTH"]) ? sprintf("%02d", $fields["HOLIDAY_MONTH"]) : "";
        $data["HOLIDAY_DAY"][TEXT]          = ($fields["HOLIDAY_DAY"]) ? sprintf("%02d", $fields["HOLIDAY_DAY"]) : "";
        $data["HOLIDAY_WEEK_PERIOD"][TEXT]  = ($fields["HOLIDAY_WEEK_PERIOD"]) ? $fields["HOLIDAY_WEEK_PERIOD"] : "";
        $data["HOLIDAY_WEEKDAY"][TEXT]      = ($fields["HOLIDAY_WEEKDAY"]) ? $fields["HOLIDAY_WEEKDAY"] : "";
        $data["HOLIDAY_NAME"][TEXT]         = $fields["HOLIDAY_NAME"];
        if ($model->Properties["useHOLIDAY_KIND"] == "1") {
            $data["HOLIDAY_KIND"][TEXT]         = $fields["HOLIDAY_KIND"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $where .= "     SEQ     = ".$seq." ";

        $query = Query::updateSQL($data, "PUBLIC_HOLIDAY_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //DELETE
    function &getDeleteQuery($fields, $seq)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     PUBLIC_HOLIDAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SEQ     = ".$seq." ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //前年度からのデータを今年度にCOPY
    function &copyInsert($Row, $model) {

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEQ"][NUMBER]                = $Row["SEQ"];
        $data["HOLIDAY_DIV"][TEXT]          = $Row["HOLIDAY_DIV"];
        $data["HOLIDAY_MONTH"][TEXT]        = $Row["HOLIDAY_MONTH"];
        $data["HOLIDAY_DAY"][TEXT]          = $Row["HOLIDAY_DAY"];
        $data["HOLIDAY_WEEK_PERIOD"][TEXT]  = $Row["HOLIDAY_WEEK_PERIOD"];
        $data["HOLIDAY_WEEKDAY"][TEXT]      = $Row["HOLIDAY_WEEKDAY"];
        $data["HOLIDAY_NAME"][TEXT]         = $Row["HOLIDAY_NAME"];
        if ($model->Properties["useHOLIDAY_KIND"] == "1") {
            $data["HOLIDAY_KIND"][TEXT]         = $Row["HOLIDAY_KIND"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "PUBLIC_HOLIDAY_MST");
        return $query;
    }
}
?>
