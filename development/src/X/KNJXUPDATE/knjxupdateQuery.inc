<?php

require_once('for_php7.php');

class knjxupdateQuery extends Query {

    //���݂̃v���O���������擾
    function getPrgname($menuid)
    {
        $query  = " SELECT ";
        $query .= "    MENUNAME ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        MENUID, ";
        $query .= "        MENUNAME ";
        $query .= "    FROM ";
        $query .= "        MENU_MST ";
        $query .= "    UNION ";
        $query .= "    SELECT ";
        $query .= "        MENUID, ";
        $query .= "        MENUNAME ";
        $query .= "    FROM ";
        $query .= "        MENU_STAFF_MST ";
        $query .= "    UNION";
        $query .= "    SELECT ";
        $query .= "        SUBID as MENUID, ";
        $query .= "        case ";
        $query .= "        when SUBNAME IS NULL then MENUNAME ";
        $query .= "        else SUBNAME ";
        $query .= "        end as MENUNAME ";
        $query .= "    FROM ";
        $query .= "        MENU_SUB_MST t1 ";
        $query .= "        left join MENU_MST t2 on t1.MAINID = t2.MENUID ";
        $query .= "    ORDER BY ";
        $query .= "        MENUID ";
        $query .= "    ) ";
        $query .= " WHERE ";
        $query .= "    MENUID = '".$menuid."' ";
        
        
        return $query;
    }
    
    //�w�Z���擾
    function getGakuName()
    {
        $query  = " SELECT ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "    NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 = 'Z010' ";
        $query .= " AND ";
        $query .= "    NAMECD2 = '00' ";
        
        return $query;
    }
}
?>
