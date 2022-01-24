<?php

require_once('for_php7.php');


class knjd625eQuery extends Query {

    function getStaffData($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     C_STF.STAFFCD AS VALUE, ";
        $query .= "     STF.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT C_STF ";
        $query .= "     INNER JOIN STAFF_MST STF ON C_STF.STAFFCD = STF.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     C_STF.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     C_STF.STAFFCD ";

        return $query;
    }

}
?>
