<?php

require_once('for_php7.php');

class knji060dQuery extends Query
{
    //学校名取得
    public function getSchoolName()
    {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //記載責任者（職員一覧）取得
    public function getSelectStaff($year)
    {

        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFCD || ' ' || STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$year ."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    public function getSchoolDiv()
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //近大チェック
    public function checkKindai()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     UPPER(NAME1) IN ('KINDAI', 'KINJUNIOR') ";

        return $query;
    }
}
