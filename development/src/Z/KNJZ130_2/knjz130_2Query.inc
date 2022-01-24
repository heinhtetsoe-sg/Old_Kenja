<?php

require_once('for_php7.php');

class knjz130_2Query extends Query
{
    public function check($model)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= " MODIFY_FLG ";
        $query .= " FROM ";
        $query .= " NAMECDDESC_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD = '".$model->field["NAMECD1"]."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    public function getCombo()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   COALESCE(T1.NAMECD, T2.NAMECD1) AS NAMECD, ";
        $query .= "   T1.CDMEMO ";
        $query .= " FROM ";
        $query .= "   NAMECDDESC_MST T1 FULL OUTER JOIN NAME_MST T2 ON T1.NAMECD = T2.NAMECD1 ";
        $query .= " ORDER BY ";
        $query .= "   1 ";
        return $query;
    }

    public function getList($model)
    {
        $query  = " SELECT * FROM NAME_MST ";
        $query .= " WHERE NAMECD1='".$model->namecd1."' ";
        $query .= " ORDER BY NAMECD1";
        return $query;
    }

    public function getRow($cd1, $cd2)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   T2.CDMEMO, ";
        $query .= "   T2.MODIFY_FLG ";
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
    
    //INSERT
    public function &getInsertQuery($model)
    {
        $row = knjz130_2Query::check($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        if (get_count($row) != "0") {
            $data["NAMECD"][TEXT]       = $model->field["NAMECD1"];
            $data["CDMEMO"][TEXT]       = $model->field["CDMEMO"];
            $data["MODIFY_FLG"][TEXT]   = $model->field["MODIFY_FLG"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $where = " WHERE NAMECD    = '" .$model->field["NAMECD1"]."'";
            $query = Query::updateSQL($data, "NAMECDDESC_MST", $where);
            $db->query($query);
        } else {
            $query  = " DELETE FROM ";
            $query .= "   NAMECDDESC_MST ";
            $query .= " WHERE ";
            $query .= "   NAMECD = '" .$model->field["NAMECD1"] ."' ";

            $db->query($query);

            $data = array();

            $data["NAMECD"][TEXT]       = $model->field["NAMECD1"];
            $data["CDMEMO"][TEXT]       = $model->field["CDMEMO"];
            $data["MODIFY_FLG"][TEXT]   = $model->field["MODIFY_FLG"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $query = Query::insertSQL($data, "NAMECDDESC_MST");

            $db->query($query);
        }

        $data = array();
        $data["NAMECD1"][TEXT]        = $model->field["NAMECD1"];
        $data["NAMECD2"][TEXT]        = $model->field["NAMECD2"];
        $data["NAME1"][TEXT]          = $model->field["NAME1"];
        $data["NAME2"][TEXT]          = $model->field["NAME2"];
        $data["NAME3"][TEXT]          = $model->field["NAME3"];
        $data["NAME1MEMO"][TEXT]      = $model->field["NAME1MEMO"];
        $data["NAME2MEMO"][TEXT]      = $model->field["NAME2MEMO"];
        $data["NAME3MEMO"][TEXT]      = $model->field["NAME3MEMO"];
        $data["ABBV1"][TEXT]          = $model->field["ABBV1"];
        $data["ABBV2"][TEXT]          = $model->field["ABBV2"];
        $data["ABBV3"][TEXT]          = $model->field["ABBV3"];
        $data["ABBV1MEMO"][TEXT]      = $model->field["ABBV1MEMO"];
        $data["ABBV2MEMO"][TEXT]      = $model->field["ABBV2MEMO"];
        $data["ABBV3MEMO"][TEXT]      = $model->field["ABBV3MEMO"];
        $data["NAMESPARE1"][TEXT]     = $model->field["NAMESPARE1"];
        $data["NAMESPARE2"][TEXT]     = $model->field["NAMESPARE2"];
        $data["NAMESPARE3"][TEXT]     = $model->field["NAMESPARE3"];
        $data["NAMESPARE1MEMO"][TEXT] = $model->field["NAMESPARE1MEMO"];
        $data["NAMESPARE2MEMO"][TEXT] = $model->field["NAMESPARE2MEMO"];
        $data["NAMESPARE3MEMO"][TEXT] = $model->field["NAMESPARE3MEMO"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "name_mst");
        
        $db->query($query);
      
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $row = knjz130_2Query::check($model);
         
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (get_count($row) != "0") {
            $data["NAMECD"][TEXT]       = $model->field["NAMECD1"];
            $data["CDMEMO"][TEXT]       = $model->field["CDMEMO"];
            $data["MODIFY_FLG"][TEXT]   = $model->field["MODIFY_FLG"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $where  = " WHERE NAMECD    = '" .$model->field["NAMECD1"]."'";
            $query = Query::updateSQL($data, "NAMECDDESC_MST", $where);
            $db->query($query);
        } else {
            $query  = " DELETE FROM ";
            $query .= "   NAMECDDESC_MST ";
            $query .= " WHERE ";
            $query .= "   NAMECD = '" .$model->field["NAMECD1"] ."' ";

            $db->query($query);

            $data = array();

            $data["NAMECD"][TEXT]       = $model->field["NAMECD1"];
            $data["CDMEMO"][TEXT]       = $model->field["CDMEMO"];
            $data["MODIFY_FLG"][TEXT]   = $model->field["MODIFY_FLG"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $query = Query::insertSQL($data, "NAMECDDESC_MST");

            $db->query($query);
        }

        $data = array();
        $data["NAMECD1"][TEXT]        = $model->field["NAMECD1"];
        $data["NAMECD2"][TEXT]        = $model->field["NAMECD2"];
        $data["NAME1"][TEXT]          = $model->field["NAME1"];
        $data["NAME2"][TEXT]          = $model->field["NAME2"];
        $data["NAME3"][TEXT]          = $model->field["NAME3"];
        $data["NAME1MEMO"][TEXT]      = $model->field["NAME1MEMO"];
        $data["NAME2MEMO"][TEXT]      = $model->field["NAME2MEMO"];
        $data["NAME3MEMO"][TEXT]      = $model->field["NAME3MEMO"];
        $data["ABBV1"][TEXT]          = $model->field["ABBV1"];
        $data["ABBV2"][TEXT]          = $model->field["ABBV2"];
        $data["ABBV3"][TEXT]          = $model->field["ABBV3"];
        $data["ABBV1MEMO"][TEXT]      = $model->field["ABBV1MEMO"];
        $data["ABBV2MEMO"][TEXT]      = $model->field["ABBV2MEMO"];
        $data["ABBV3MEMO"][TEXT]      = $model->field["ABBV3MEMO"];
        $data["NAMESPARE1"][TEXT]     = $model->field["NAMESPARE1"];
        $data["NAMESPARE2"][TEXT]     = $model->field["NAMESPARE2"];
        $data["NAMESPARE3"][TEXT]     = $model->field["NAMESPARE3"];
        $data["NAMESPARE1MEMO"][TEXT] = $model->field["NAMESPARE1MEMO"];
        $data["NAMESPARE2MEMO"][TEXT] = $model->field["NAMESPARE2MEMO"];
        $data["NAMESPARE3MEMO"][TEXT] = $model->field["NAMESPARE3MEMO"];
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
    public function &getDeleteQuery($model)
    {
        if (knjz130_2Query::yearDataChk($model->field["NAMECD1"], $model->field["NAMECD2"]) == 0) {
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

    public function yearDataChk($cd1, $cd2)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT COUNT(*) as cnt FROM NAME_YDAT ";
        $query .= " WHERE NAMECD1 = '" .$cd1 ."'" ;
        $query .= " AND NAMECD2 = '" .$cd2 ."'";
        
        $retCnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $retCnt;
    }
}
