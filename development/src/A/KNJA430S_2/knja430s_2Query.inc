<?php

require_once('for_php7.php');

class knja430s_2Query extends Query {
    //走査日付を更新
    function &getUpdateQueryInkan($stampNo)
    {
        $db = Query::dbCheckOut();

        $data["DATE"][DATE]         = CTRL_DATE;

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = "WHERE STAMP_NO = '".$stampNo."' ";
        $query = Query::updateSQL($data, "ATTEST_INKAN_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
?>
