<?php

require_once('for_php7.php');

class knji050Query extends Query
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

    //未履修科目出力　履修のみ科目出力
    public function getRisyuMirsyu($model)
    {
        //参照・更新可
        $query  = "";
        $query .= " SELECT NAMESPARE1, NAMESPARE2, NAMESPARE3 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'A027' ";
        $query .= " AND NAMECD2 = '107' ";
        return $query;
    }
}
