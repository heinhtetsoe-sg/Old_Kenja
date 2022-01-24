<?php

require_once('for_php7.php');


class knjf070Query extends Query {

    //名称マスタ
    function getSchoolKind($model)
    {
        $query  = "SELECT NAME1 AS VALUE, ABBV1 AS LABEL ";
        $query .= "FROM   NAME_MST T1 ";
        $query .= "INNER JOIN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."') T2 ON T2.SCHOOL_KIND = T1.NAME1 ";
        $query .= "WHERE  T1.NAMECD1 = 'A023' AND T2.SCHOOL_KIND <> 'K' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY NAMECD2 ";

        return $query;
    }

}
?>
