<?php

require_once('for_php7.php');

class knjl611a_2query extends Query {
    
    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $data = array();
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->year}' ";
            $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["TOTAL5"][TEXT]           = $data_arr[$i]["TOTAL5"];
            $data["ABSENCE_DAYS3"][TEXT]    = $data_arr[$i]["ABSENCE_DAYS3"];
            $data["SPECIALACTREC"][TEXT]    = $data_arr[$i]["SPECIALACTREC"];
            $data["TOTALSTUDYTIME"][TEXT]   = $data_arr[$i]["TOTALSTUDYTIME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    function selectCsvErrQuery() {
        $query  = " SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     EXAMNO, ";
        $query .= "     TOTAL5, ";
        $query .= "     ABSENCE_DAYS3, ";
        $query .= "     SPECIALACTREC, ";
        $query .= "     TOTALSTUDYTIME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }
}
?>
