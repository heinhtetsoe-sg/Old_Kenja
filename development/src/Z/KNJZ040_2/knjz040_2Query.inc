<?php

require_once('for_php7.php');

class knjz040_2Query extends Query {

    //リスト    
    function getList()
    {
        $query  = " SELECT T1.*,T2.name1 AS s_name,T3.name1 AS e_name FROM course_mst T1  ";
        $query .= " LEFT OUTER JOIN name_mst T2 ";
        $query .= "              ON T1.s_periodcd = T2.namecd2 ";
        $query .= "             AND T2.namecd1 = 'B001' ";
        $query .= " LEFT OUTER JOIN name_mst T3 ";
        $query .= "              ON T1.e_periodcd = T3.namecd2 ";
        $query .= "             AND T3.namecd1 = 'B001' ";
        $query .= " ORDER BY T1.coursecd ";
        return $query;

    }

    //校時名称取得
    function getName()
    {
        return " SELECT namecd2,name1 FROM name_mst WHERE namecd1 = 'B001' ORDER BY namecd2 ";
    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM course_mst ".
                 " WHERE coursecd = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度データの確認
    function IsExisting($coursecd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM course_ydat WHERE coursecd = '".$coursecd."'"; 
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }

    //学科マスタの確認
    function checkMajormst($coursecd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM MAJOR_MST WHERE coursecd = '".$coursecd."'"; 
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

        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["COURSENAME"][TEXT]       = $fields["COURSENAME"];
        $data["COURSEABBV"][TEXT]       = $fields["COURSEABBV"];
        $data["COURSEENG"][TEXT]        = $fields["COURSEENG"];
        $data["S_PERIODCD"][TEXT]       = $fields["S_PERIODCD"];
        $data["E_PERIODCD"][TEXT]       = $fields["E_PERIODCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "course_mst");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["COURSENAME"][TEXT]       = $fields["COURSENAME"];
        $data["COURSEABBV"][TEXT]       = $fields["COURSEABBV"];
        $data["COURSEENG"][TEXT]        = $fields["COURSEENG"];
        $data["S_PERIODCD"][TEXT]       = $fields["S_PERIODCD"];
        $data["E_PERIODCD"][TEXT]       = $fields["E_PERIODCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE COURSECD = '" .$fields["COURSECD"]."'";

        $query = Query::updateSQL($data, "course_mst", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM course_mst "
                ." WHERE coursecd  = '" .$fields["COURSECD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
