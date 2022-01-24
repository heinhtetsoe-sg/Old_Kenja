<?php

require_once('for_php7.php');

class knjz355query extends Query {

    function getselectDataSQL($model) {

        $query = "";
        $query .= " WITH USER AS ( ";
        $query .= "    SELECT DISTINCT ";
        $query .= "        T1.USERID ";
        $query .= "      , T1.STAFFCD ";
        $query .= "      , T2.STAFFNAME ";
        $query .= "    FROM USER_MST T1 ";
        $query .= "    INNER JOIN STAFF_MST T2 ";
        $query .= "    ON T1.STAFFCD = T2.STAFFCD ";
        $query .= " ) ";
        $query .= "  SELECT ";
        $query .= "    LOG.UPDATED, ";
        $query .= "    LOG.USERID, ";
        $query .= "    USER.STAFFCD, ";
        $query .= "    USER.STAFFNAME, ";
        $query .= "    LOG.PROGRAMID, ";
        $query .= "    LOG.IPADDRESS, ";
        $query .= "    LOG.ACCESS_CD ";
        $query .= "  FROM ";
        $query .= "    ACCESS_LOG LOG ";
        $query .= "    INNER JOIN USER ON LOG.USERID = USER.USERID ";
        $query .= "  WHERE ";
        $query .= "    DATE(LOG.UPDATED) >= '".str_replace("/", "-", $model->field["S_DATE"])."' ";
        $query .= "    AND DATE(LOG.UPDATED) <= '".str_replace("/", "-", $model->field["E_DATE"])."' ";
        $query .= "  ORDER BY ";
        $query .= "    LOG.UPDATED ";
        return $query;
    }

}
?>
