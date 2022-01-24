<?php

require_once('for_php7.php');

class knjz050_2Query extends Query {

    //１レコード取得
    function getRow($majorcd, $coursecd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM major_mst ".
                 " WHERE majorcd = '" .$majorcd ."' and coursecd = '".$coursecd."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //学科英字
    function getMajorengLength() {
        $query = "SELECT LENGTH FROM (SELECT T1.LENGTH FROM SYSIBM.SYSCOLUMNS T1 WHERE T1.TBNAME = 'MAJOR_MST' AND T1.NAME = 'MAJORENG') T1 ";
        return $query;
    }

    //年度データの確認
    function IsExisting($majorcd, $coursecd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM major_ydat WHERE majorcd = '".$majorcd."' and coursecd = '".$coursecd."'";
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }

    //課程コード
    function getCourse()
    {
        $db = Query::dbCheckOut();

        $query = "SELECT DISTINCT coursecd, coursename FROM course_mst ORDER BY coursecd";
        $result    = $db->query($query);
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["COURSECD"]."  ".$row["COURSENAME"],
                           "value" => $row["COURSECD"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        return $opt;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["MAJORNAME"][TEXT]        = $fields["MAJORNAME"];
        $data["MAJORNAME2"][TEXT]       = $fields["MAJORNAME2"];
        $data["MAJORABBV"][TEXT]        = $fields["MAJORABBV"];
        $data["MAJORENG"][TEXT]         = $fields["MAJORENG"];
        $data["MAJORBANKCD"][TEXT]      = $fields["MAJORBANKCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "major_mst");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["MAJORNAME"][TEXT]        = $fields["MAJORNAME"];
        $data["MAJORNAME2"][TEXT]       = $fields["MAJORNAME2"];
        $data["MAJORABBV"][TEXT]        = $fields["MAJORABBV"];
        $data["MAJORENG"][TEXT]         = $fields["MAJORENG"];
        $data["MAJORBANKCD"][TEXT]      = $fields["MAJORBANKCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
          $where = " WHERE MAJORCD = '" .$fields["MAJORCD"]."' and COURSECD = '".$fields["COURSECD"]."'";

        $query = Query::updateSQL($data, "major_mst", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    //--- DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM major_mst "
                ." WHERE majorcd  = '" .$fields["MAJORCD"]."' and coursecd = '".$fields["COURSECD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
