<?php

require_once('for_php7.php');

class knjl360wQuery extends Query {
    //�����`�F�b�N�i�����Ǘ��ҁj
    function getAdminFlg() {
        $query  = " SELECT ";
        $query .= "     FIELD1 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND STAFF_SEQ = '009' ";

        $db = Query::dbCheckOut();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }
}
?>
