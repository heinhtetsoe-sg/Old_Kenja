<?php
class knjd126sQuery extends Query {

    function getTableNameList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM  ";
        $query .= "     SYSIBM.TABLES ";
        $query .= " WHERE ";
        $query .= "     TABLE_CATALOG = 'ALPOKIDB' ";
        $query .= "     TABLE_SCHEMA  = 'DB2INST1' ";

        return $query;
    }
}
?>
