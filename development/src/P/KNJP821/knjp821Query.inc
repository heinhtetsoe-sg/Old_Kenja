<?php

require_once('for_php7.php');

class knjp821query extends Query {
    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //COLLECT_SGL_SCHREGNO_MAPPING_DATカウントチェック
    function getMappingCnt($data) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_SCHREGNO_MAPPING_DAT ";
        $query .= " WHERE ";
        if ($data["GRADE"] <= 3) {
            $query .= "     SCHREGNO_J = '".$data["SCHREGNO_J"]."' ";
        } else {
            $query .= "     SCHREGNO_H = '".$data["SCHREGNO_H"]."' ";
        }

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //データの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $data = array();
            if ($data_arr[$i]["GRADE"] <= 3) {
                $data["SCHREGNO_J"][TEXT] = $data_arr[$i]["SCHREGNO_J"];
                $data["SIGEL_NO_J"][TEXT] = $data_arr[$i]["SIGEL_NO_J"];
            } else {
                $data["SCHREGNO_H"][TEXT] = $data_arr[$i]["SCHREGNO_H"];
                $data["SIGEL_NO_H"][TEXT] = $data_arr[$i]["SIGEL_NO_H"];
            }

            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "SYSDATE()";

            $where  = " WHERE ";
            if ($data_arr[$i]["GRADE"] <= 3) {
                $where .= "     SCHREGNO_J = '".$data_arr[$i]["SCHREGNO_J"]."' ";
            } else {
                $where .= "     SCHREGNO_H = '".$data_arr[$i]["SCHREGNO_H"]."' ";
            }

            $query = Query::updateSQL($data, "COLLECT_SGL_SCHREGNO_MAPPING_DAT", $where);
            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/
    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }
}
?>
