<?php

require_once('for_php7.php');

class knjxothersystemQuery extends Query {
    //氏名取得
    function getName($schregno) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //データ取得
    function getSchregOtherSystemUserDat($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     T1.SYSTEMID || ':' || T2.SYSTEM_NAME AS SYSTEM, ";
        $query .= "     T1.LOGINID, ";
        $query .= "     T1.PASSWORD ";
        $query .= " FROM ";
        $query .= "     SCHREG_OTHER_SYSTEM_USER_DAT T1 ";
        $query .= "     INNER JOIN OTHER_SYSTEM_MST T2 ON T1.SYSTEMID = T2.SYSTEMID ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }
}
?>
