<?php

require_once('for_php7.php');

class knjz010Query extends Query {
        //年度
        function getYear()
        {
        $query  = "SELECT DISTINCT YEAR FROM semester_mst ORDER BY YEAR DESC ";
        return $query;
        }
        
        function getSemester($year)
        {
        $query  = "SELECT SEMESTER,SEMESTERNAME ";
                $query .= " FROM semester_mst ";
                $query .= " WHERE YEAR = '".$year."'";
                $query .= " AND SEMESTER <> '9' ";
        return $query;
        }
    //１レコード取得
    function getRow()
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM control_mst ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($fields)
    {
        $cnt = knjz010Query::ctrlRow();
        $db = Query::dbCheckOut();

        $data = array();
        $data["CTRL_YEAR"][TEXT]        = $fields["YEAR"];
        $data["CTRL_SEMESTER"][TEXT]    = $fields["SEMESTER"];
        $data["CTRL_DATE"][TEXT]        = strtr($fields["CTRL_DATE"],"/","-");
        $data["ATTEND_CTRL_DATE"][TEXT] = strtr($fields["ATTEND_CTRL_DATE"],"/","-");
        $data["ATTEND_TERM"][NUMBER]    = $fields["ATTEND_TERM"];
        $data["PWDVALIDTERM"][NUMBER]   = $fields["PWDVALIDTERM"];
        $data["IMAGEPATH"][TEXT]        = $fields["IMAGEPATH"];
        $data["EXTENSION"][TEXT]        = $fields["EXTENSION"];
        $data["MESSAGE"][TEXT]          = $fields["MESSAGE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
                if ($cnt<1) {
                        $data["CTRL_YEAR"][TEXT]        = "01";
                        $query = Query::insertSQL($data, "control_mst");
                } else {
                        $where  = " WHERE CTRL_NO       = '01'";
                $query = Query::updateSQL($data, "control_mst", $where);
                }
        
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    
    function ctrlRow() 
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) as cnt FROM control_mst ";
        $query .= " WHERE CTRL_NO = '01'";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
        
        function semesterRow() 
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) as cnt FROM semester_mst ";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
        
}
?>
