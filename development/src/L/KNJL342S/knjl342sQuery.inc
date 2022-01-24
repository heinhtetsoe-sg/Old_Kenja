<?php

require_once('for_php7.php');

class knjl342sQuery extends Query {
    //最終学期を取得
    function getSemesterLast() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";

        $db = Query::dbCheckOut();
        $rtnVal = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnVal;
    }
}
?>
