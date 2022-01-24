<?php

require_once('for_php7.php');

class knjxselect_staffQuery extends Query
{
    //職員データ
    public function getStaffDate()
    {
        $query  = "    SELECT SY.STAFFCD ";
        $query .= "         , SM.STAFFNAME ";
        $query .= "      FROM STAFF_YDAT SY ";
        $query .= " LEFT JOIN STAFF_MST SM ";
        $query .= "        ON SM.STAFFCD = SY.STAFFCD ";
        $query .= "     WHERE SY.YEAR = '".CTRL_YEAR."' ";
        $query .= "  ORDER BY SY.STAFFCD ";

        return $query;
    }
}
