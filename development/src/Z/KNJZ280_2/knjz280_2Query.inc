<?php

require_once('for_php7.php');

class knjz280_2Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM job_mst ".
                 " WHERE JOBCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //年度データの確認
    function IsExisting($jobcd)
    {
        $db    = Query::dbCheckOut();
        $query = "select * from job_ydat where jobcd = '".$jobcd."'";
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
        
        $data["JOBCD"][TEXT]             = $fields["JOBCD"];
        $data["JOBNAME"][TEXT]           = $fields["JOBNAME"];
        $data["BASE_JOBNAME"][TEXT]      = $fields["BASE_JOBNAME"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        $query = Query::insertSQL($data, "job_mst");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["JOBCD"][TEXT]             = $fields["JOBCD"];
        $data["JOBNAME"][TEXT]           = $fields["JOBNAME"];
        $data["BASE_JOBNAME"][TEXT]      = $fields["BASE_JOBNAME"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";
          $where = " WHERE JOBCD = '" .$fields["JOBCD"]."'";

        $query = Query::updateSQL($data, "job_mst", $where);
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

        $query = "DELETE FROM job_mst "
                ." WHERE JOBCD  = '" .$fields["JOBCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
