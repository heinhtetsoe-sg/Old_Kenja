<?php

require_once('for_php7.php');


class knja143mQuery extends Query {

    //学期マスタ
    function getSemeMst($year,$semester) {
        $query  = "SELECT * FROM SEMESTER_MST WHERE YEAR='".$year."' AND SEMESTER='".$semester."' ";

        return $query;
    }

    //職員取得
    function getStaff($model,$year,$semester) {
        $query  = "SELECT T1.STAFFCD AS VALUE, ";
        $query .= "       T1.STAFFCD || '　' || T1.STAFFNAME AS NAME ";
        $query .= "FROM   V_STAFF_MST T1 ";
        $query .= "WHERE  T1.YEAR       = '".$year."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND T1.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "ORDER BY T1.STAFFCD ";

        return $query;
    }

}
?>
