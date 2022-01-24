<?php

require_once('for_php7.php');

class knjz310Query extends Query {

    //１レコード取得
    function getRow($cd, $model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT * FROM usergroup_mst ";
        $query .= " WHERE GROUPCD = '" .$cd ."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //コンボボックス内年度取得 NO001
    function selectYearQuery($model)
    {
        $query  = " SELECT DISTINCT YEAR FROM USERGROUP_DAT ";
        $query .= " ORDER BY YEAR DESC";

        return $query;
    }

    //グループ一覧取得
    function getUsergroupMst($model) {
        $query  = " SELECT * FROM USERGROUP_MST ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY GROUPCD ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();
        
        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]    = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT] = SCHOOLKIND;
        }
        $data["GROUPCD"][TEXT]     = $fields["GROUPCD"];
        $data["GROUPNAME"][TEXT]   = $fields["GROUPNAME"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $query = Query::insertSQL($data, "usergroup_mst");
        
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery($fields, $model)
    {        
        $db = Query::dbCheckOut();
        $data = array();
        $data["GROUPCD"][TEXT]     = $fields["GROUPCD"];
        $data["GROUPNAME"][TEXT]   = $fields["GROUPNAME"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";
        
        $where  = " WHERE GROUPCD = '" .$fields["GROUPCD"] ."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $where .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $query = Query::updateSQL($data, "usergroup_mst", $where);
        
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($fields, $model)
    {
        if (knjz310Query::g_data_chk($fields["GROUPCD"], $model)==0) {
            $db = Query::dbCheckOut();

            $query = "DELETE FROM usergroup_mst "
                    ." WHERE GROUPCD  = '" .$fields["GROUPCD"] ."'";
            $query2 = "DELETE FROM groupauth_dat "
                    ." WHERE GROUPCD  = '" .$fields["GROUPCD"] ."'";
            $query3 = "";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query3 .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query3 .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $db->query($query.$query3);
            $db->query($query2.$query3);
            Query::dbCheckIn($db);
            
            $result = true;
        } else {
            $result = false;
        } 
        return $result;
    }
    function g_data_chk($cd, $model)
    {
        $db = Query::dbCheckOut();
        $query = "select COUNT(*) as cnt from usergroup_dat where GROUPCD = '" .$cd ."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);
        
        $cnt = $db->getone($query,null,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $cnt;
    }
    
}
?>
