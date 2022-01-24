<?php

require_once('for_php7.php');

class knjj144hquery extends Query {

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            
            //削除
            $query  = " DELETE FROM ";
            $query .= "     ROU_HATSU_SESSIN_KAI_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR          = '".CTRL_YEAR."' AND ";
            $query .= "     SCHREGNO      = '".$model->data_arr[$i]["SCHREGNO"]."' ";

            $db->query($query);

            //追加
            $data = array();
            $data["YEAR"][TEXT]       = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]   = $model->data_arr[$i]["SCHREGNO"];
            $data["KAIKIN_FLG"][TEXT] = $model->data_arr[$i]["KAIKIN_FLG"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "SYSDATE()";

            $query = Query::insertSQL($data, "ROU_HATSU_SESSIN_KAI_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
?>
