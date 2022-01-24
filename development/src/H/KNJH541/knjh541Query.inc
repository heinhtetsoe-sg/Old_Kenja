<?php

require_once('for_php7.php');

class knjh541Query extends Query {

    //リスト
    function getList()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PREF_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     PREF_SUBCLASSCD ";

        return $query;
    }

    //１レコード取得
    function getRow($cd, $db)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PREF_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= " PREF_SUBCLASSCD = '" .$cd ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //県下実力平均点レコード取得
    function getProficiencyPerfectDat($cd, $db)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PERFECT_DAT ";
        $query .= " WHERE ";
        $query .= " PROFICIENCY_SUBCLASS_CD = '" .$cd ."' ";

        $count = $db->getOne($query);
        return $count;
    }

    //実力科目による考査科目レコード取得
    function getProficiencySubclassYdat($cd, $db)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_YDAT ";
        $query .= " WHERE ";
        $query .= " PROFICIENCY_SUBCLASS_CD = '" .$cd ."' ";

        $count = $db->getOne($query);
        return $count;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["PREF_SUBCLASSCD"][TEXT]  = $fields["PREF_SUBCLASSCD"];
        $data["SUBCLASS_NAME"][TEXT]    = $fields["SUBCLASS_NAME"];
        $data["SUBCLASS_ABBV"][TEXT]    = $fields["SUBCLASS_ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]	    = "sysdate()";

        $query = Query::insertSQL($data, "PROFICIENCY_PREF_SUBCLASS_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["PREF_SUBCLASSCD"][TEXT]  = $fields["PREF_SUBCLASSCD"];
        $data["SUBCLASS_NAME"][TEXT]    = $fields["SUBCLASS_NAME"];
        $data["SUBCLASS_ABBV"][TEXT]    = $fields["SUBCLASS_ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE PREF_SUBCLASSCD = '" .$fields["PREF_SUBCLASSCD"]."'";

        $query = Query::updateSQL($data, "PROFICIENCY_PREF_SUBCLASS_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     PROFICIENCY_PREF_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_SUBCLASSCD  = '" .$fields["PREF_SUBCLASSCD"]."' ";

      $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
