<?php

require_once('for_php7.php');

class knjx020bQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX020B' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //マスタのSQL文作成(存在チェック用)
    function getMasterSQL($table, $where)
    {
        $query = "SELECT * FROM " . $table . " " . $where;

        return $query;
    }

    //マスタデータの削除
    function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM schreg_base_mst where SCHREGNO = '".$data["SCHREGNO"]."'";

        $result = $db->query($query);
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv(&$db, $data, $registercd)
    {
            $data1["SCHREGNO           "][TEXT]         = $data["SCHREGNO"];
            $data1["INOUTCD            "][TEXT]         = $data["INOUTCD"];
            $data1["NAME               "][TEXT]         = $data["NAME"];
            $data1["NAME_SHOW          "][TEXT]         = $data["NAME_SHOW"];
            $data1["NAME_KANA          "][TEXT]         = $data["NAME_KANA"];
            $data1["NAME_ENG           "][TEXT]         = $data["NAME_ENG"];
            $data1["BIRTHDAY           "][TEXT]         = $data["BIRTHDAY"];
            $data1["SEX                "][TEXT]         = $data["SEX"];
            $data1["BLOODTYPE          "][TEXT]         = $data["BLOODTYPE"];
            $data1["BLOOD_RH           "][TEXT]         = $data["BLOOD_RH"];
            $data1["HANDICAP           "][TEXT]         = $data["HANDICAP"];
            $data1["NATIONALITY        "][TEXT]         = $data["NATIONALITY"];
            $data1["FINSCHOOLCD        "][TEXT]         = $data["FINSCHOOLCD"];
            $data1["FINISH_DATE        "][TEXT]         = $data["FINISH_DATE"];
            $data1["PRISCHOOLCD        "][TEXT]         = $data["PRISCHOOLCD"];
            $data1["ENT_DATE           "][TEXT]         = $data["ENT_DATE"];
            $data1["ENT_DIV            "][TEXT]         = $data["ENT_DIV"];
            $data1["ENT_REASON         "][TEXT]         = $data["ENT_REASON"];
            $data1["ENT_SCHOOL         "][TEXT]         = $data["ENT_SCHOOL"];
            $data1["ENT_ADDR           "][TEXT]         = $data["ENT_ADDR"];
            $data1["GRD_DATE           "][TEXT]         = $data["GRD_DATE"];
            $data1["GRD_DIV            "][TEXT]         = $data["GRD_DIV"];
            $data1["GRD_REASON         "][TEXT]         = $data["GRD_REASON"];
            $data1["GRD_SCHOOL         "][TEXT]         = $data["GRD_SCHOOL"];
            $data1["GRD_ADDR           "][TEXT]         = $data["GRD_ADDR"];
            $data1["GRD_NO             "][TEXT]         = $data["GRD_NO"];
            $data1["GRD_TERM           "][TEXT]         = $data["GRD_TERM"];
            $data1["REMARK1            "][TEXT]         = $data["REMARK1"];
            $data1["REMARK2            "][TEXT]         = $data["REMARK2"];
            $data1["REMARK3            "][TEXT]         = $data["REMARK3"];
            $data1["EMERGENCYCALL      "][TEXT]         = $data["EMERGENCYCALL"];
            $data1["EMERGENCYNAME      "][TEXT]         = $data["EMERGENCYNAME"];
            $data1["EMERGENCYRELA_NAME "][TEXT]         = $data["EMERGENCYRELA_NAME"];
            $data1["EMERGENCYTELNO     "][TEXT]         = $data["EMERGENCYTELNO"];
            $data1["EMERGENCYCALL2     "][TEXT]         = $data["EMERGENCYCALL2"];
            $data1["EMERGENCYNAME2     "][TEXT]         = $data["EMERGENCYNAME2"];
            $data1["EMERGENCYRELA_NAME2"][TEXT]         = $data["EMERGENCYRELA_NAME2"];
            $data1["EMERGENCYTELNO2    "][TEXT]         = $data["EMERGENCYTELNO2"];

            $data1["REGISTERCD"][TEXT]          = $registercd;
            $data1["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data1, "schreg_base_mst");

            $result = $db->query($query);
    }

    //マスタの更新（ＣＳＶデータより読込）
    function updateQueryCsv(&$db, $data, $registercd)
    {
            $data1["SCHREGNO           "][TEXT]         = $data["SCHREGNO"];
            $data1["INOUTCD            "][TEXT]         = $data["INOUTCD"];
            $data1["NAME               "][TEXT]         = $data["NAME"];
            $data1["NAME_SHOW          "][TEXT]         = $data["NAME_SHOW"];
            $data1["NAME_KANA          "][TEXT]         = $data["NAME_KANA"];
            $data1["NAME_ENG           "][TEXT]         = $data["NAME_ENG"];
            $data1["BIRTHDAY           "][TEXT]         = $data["BIRTHDAY"];
            $data1["SEX                "][TEXT]         = $data["SEX"];
            $data1["BLOODTYPE          "][TEXT]         = $data["BLOODTYPE"];
            $data1["BLOOD_RH           "][TEXT]         = $data["BLOOD_RH"];
            $data1["HANDICAP           "][TEXT]         = $data["HANDICAP"];
            $data1["NATIONALITY        "][TEXT]         = $data["NATIONALITY"];
            $data1["FINSCHOOLCD        "][TEXT]         = $data["FINSCHOOLCD"];
            $data1["FINISH_DATE        "][TEXT]         = $data["FINISH_DATE"];
            $data1["PRISCHOOLCD        "][TEXT]         = $data["PRISCHOOLCD"];
            $data1["ENT_DATE           "][TEXT]         = $data["ENT_DATE"];
            $data1["ENT_DIV            "][TEXT]         = $data["ENT_DIV"];
            $data1["ENT_REASON         "][TEXT]         = $data["ENT_REASON"];
            $data1["ENT_SCHOOL         "][TEXT]         = $data["ENT_SCHOOL"];
            $data1["ENT_ADDR           "][TEXT]         = $data["ENT_ADDR"];
            $data1["GRD_DATE           "][TEXT]         = $data["GRD_DATE"];
            $data1["GRD_DIV            "][TEXT]         = $data["GRD_DIV"];
            $data1["GRD_REASON         "][TEXT]         = $data["GRD_REASON"];
            $data1["GRD_SCHOOL         "][TEXT]         = $data["GRD_SCHOOL"];
            $data1["GRD_ADDR           "][TEXT]         = $data["GRD_ADDR"];
            $data1["GRD_NO             "][TEXT]         = $data["GRD_NO"];
            $data1["GRD_TERM           "][TEXT]         = $data["GRD_TERM"];
            $data1["REMARK1            "][TEXT]         = $data["REMARK1"];
            $data1["REMARK2            "][TEXT]         = $data["REMARK2"];
            $data1["REMARK3            "][TEXT]         = $data["REMARK3"];
            $data1["EMERGENCYCALL      "][TEXT]         = $data["EMERGENCYCALL"];
            $data1["EMERGENCYNAME      "][TEXT]         = $data["EMERGENCYNAME"];
            $data1["EMERGENCYRELA_NAME "][TEXT]         = $data["EMERGENCYRELA_NAME"];
            $data1["EMERGENCYTELNO     "][TEXT]         = $data["EMERGENCYTELNO"];
            $data1["EMERGENCYCALL2     "][TEXT]         = $data["EMERGENCYCALL2"];
            $data1["EMERGENCYNAME2     "][TEXT]         = $data["EMERGENCYNAME2"];
            $data1["EMERGENCYRELA_NAME2"][TEXT]         = $data["EMERGENCYRELA_NAME2"];
            $data1["EMERGENCYTELNO2    "][TEXT]         = $data["EMERGENCYTELNO2"];

            $data1["REGISTERCD         "][TEXT]         = $registercd;
            $data1["UPDATED            "][NUMBER]       = "sysdate()";

            $where = " WHERE SCHREGNO = '" .$data["SCHREGNO"] ."' ";
            $query = Query::updateSQL($data1, "schreg_base_mst", $where);

            $result = $db->query($query);
    }

    //エラーデータの削除
    function deleteQueryErr()
    {
        $query  = "DELETE FROM w_csvmsg_dat ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
            $data1["MSGROW"][NUMBER]        = $record_no;
            $data1["MSGREMARK"][TEXT]       = $check_error;

            $query = Query::insertSQL($data1, "w_csvmsg_dat");

            $result = $db->query($query);
    }

    //CVS作成用のQUERY
    function selectCsvQuery()
    {
        $query  = "select * from w_csvmsg_dat";

        return $query;
    }
}
?>
