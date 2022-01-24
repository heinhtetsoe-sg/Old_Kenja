<?php

require_once('for_php7.php');

class knjm610Query extends Query {

    //１レコード取得
    function get_money($model)
    {
        $query  = " SELECT TUITION FROM TUITION_DAT ";
        $query .= " WHERE YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        $db = Query::dbCheckOut();
        
        $query  = " INSERT INTO TUITION_DAT ";
        $query .= " (YEAR,TUITION,REGISTERCD,UPDATED) ";
        $query .= " VALUES('".$model->field["YEAR"]."',".$model->field["MONEY"].",'".STAFFCD."',";
        $query .= " sysdate()) ";
        
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function getDeleteQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM TUITION_DAT ";
        $query .= " WHERE YEAR = '".$model->field["YEAR"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
