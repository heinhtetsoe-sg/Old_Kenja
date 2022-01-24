<?php

require_once('for_php7.php');

class knjz130a_2Query extends Query {

    function check($model){
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     MODIFY_FLG ";
        $query .= " FROM ";
        $query .= "     NAMECDDESC_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD = '".$model->field["NAMECD1"]."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    function getCombo()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COALESCE(T1.NAMECD, T2.NAMECD1) AS NAMECD ";
        $query .= "     , T1.CDMEMO ";
        $query .= " FROM ";
        $query .= "     NAMECDDESC_MST T1 ";
        $query .= "     FULL OUTER JOIN NAME_MST T2 ";
        $query .= "         ON T1.NAMECD = T2.NAMECD1 ";
        $query .= " WHERE ";
        $query .= "     T1.MODIFY_FLG = '2' ";
        $query .= " ORDER BY ";
        $query .= "     1 ";
        return $query;
    }

    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$model->namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1 ";

        return $query;
    }

    function getRow($cd1,$cd2)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   T2.CDMEMO ";
        $query .= " FROM ";
        $query .= "   NAME_MST T1 LEFT OUTER JOIN NAMECDDESC_MST T2  ";
        $query .= "   ON T1.NAMECD1 = T2.NAMECD ";
        $query .= " WHERE ";
        $query .= "   T1.NAMECD1 = '" .$cd1 ."' AND ";
        $query .= "   T1.NAMECD2 = '" .$cd2 ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    function getNamecd2Combo($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= "     , NAMECD2 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["NAMECD1"][TEXT]        = $model->field["NAMECD1"];
        $data["NAMECD2"][TEXT]        = $model->field["NAMECD2"];
        $data["NAME1"][TEXT]          = $model->field["NAME1"];
        $data["NAME2"][TEXT]          = $model->field["NAME2"];
        $data["NAME3"][TEXT]          = $model->field["NAME3"];
        $data["ABBV1"][TEXT]          = $model->field["ABBV1"];
        $data["ABBV2"][TEXT]          = $model->field["ABBV2"];
        $data["ABBV3"][TEXT]          = $model->field["ABBV3"];
        $data["NAMESPARE1"][TEXT]     = $model->field["NAMESPARE1"];
        $data["NAMESPARE2"][TEXT]     = $model->field["NAMESPARE2"];
        $data["NAMESPARE3"][TEXT]     = $model->field["NAMESPARE3"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "name_mst");
        
        $db->query($query);
      
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {     
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["NAMECD1"][TEXT]        = $model->field["NAMECD1"];
        $data["NAMECD2"][TEXT]        = $model->field["NAMECD2"];
        $data["NAME1"][TEXT]          = $model->field["NAME1"];
        $data["NAME2"][TEXT]          = $model->field["NAME2"];
        $data["NAME3"][TEXT]          = $model->field["NAME3"];
        $data["ABBV1"][TEXT]          = $model->field["ABBV1"];
        $data["ABBV2"][TEXT]          = $model->field["ABBV2"];
        $data["ABBV3"][TEXT]          = $model->field["ABBV3"];
        $data["NAMESPARE1"][TEXT]     = $model->field["NAMESPARE1"];
        $data["NAMESPARE2"][TEXT]     = $model->field["NAMESPARE2"];
        $data["NAMESPARE3"][TEXT]     = $model->field["NAMESPARE3"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";
        
        $where  = " WHERE NAMECD1    = '" .$model->field["NAMECD1"]."'";
        $where .= " AND NAMECD2  = '" .$model->field["NAMECD2"] ."'";
        $query = Query::updateSQL($data, "name_mst", $where);
        
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        if (knjz130a_2Query::year_data_chk($model->field["NAMECD1"],$model->field["NAMECD2"])==0) {
            $db = Query::dbCheckOut();

            $db->autoCommit(false);
            $query  = " DELETE FROM name_mst ";
            $query .= " WHERE NAMECD1  = '" .$model->field["NAMECD1"] ."'";
            $query .= " AND NAMECD2  = '" .$model->field["NAMECD2"] ."'";

            $db->query($query);
        
            $db->commit();
        
            Query::dbCheckIn($db);
            $result = true;
        } else {
            $result = false;
        }    
        return $result;
    }
    
    function year_data_chk($cd1,$cd2)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_YDAT ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '" .$cd1 ."' ";
        $query .= "     AND NAMECD2 = '".$cd2."' ";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
}
?>
