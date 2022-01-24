<?php

require_once('for_php7.php');

class knjl620aQuery extends Query {

    //名称マスタ取得
    function getNameMst($model, $namecd1, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "     , NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->entexamyear."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //存在チェック -- ENTEXAM_RECEPT_DAT
    function checkEntexamReceptDat($model, $testdiv, $examno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$testdiv."' ";
        $query .= "     AND EXAMNO = '".$examno."' ";

        return $query;
    }

    //存在チェック -- ENTEXAM_HALL_YDAT
    function checkEntexamHallYdat($model, $testdiv, $examhallno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$testdiv."' ";
        $query .= "     AND EXAMHALLCD = '".$examhallno."' ";

        return $query;
    }

    //会場の収容人数を取得
    function getHallCapa($model, $testdiv, $examhallno) {
        $query  = " SELECT ";
        $query .= "     CAPA_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$testdiv."' ";
        $query .= "     AND EXAMHALLCD = '".$examhallno."' ";

        return $query;
    }

    //志願者受付データ取得
    function getEntexamReceptDat($model, $data) {
        $query  = " SELECT ";
        $query .= "     EXAM_TYPE ";
        $query .= "     , RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$data["TESTDIV"]."' ";
        $query .= "     AND EXAMNO = '".$data["EXAMNO"]."' ";

        return $query;
    }

    //更新（ＣＳＶ取込）
    function updateQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND SEQ = '010' ";
        $db->query($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $query = knjl620aQuery::getEntexamReceptDat($model, $data_arr[$i]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->entexamyear;
            $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]       = $data_arr[$i]["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]     = $row["EXAM_TYPE"];
            $data["RECEPTNO"][TEXT]      = $row["RECEPTNO"];
            $data["SEQ"][TEXT]           = '010';
            $data["REMARK1"][TEXT]       = $data_arr[$i]["EXAMHALLCD"];
            $data["REMARK2"][TEXT]       = $model->seatno[$data_arr[$i]["EXAMHALLCD"]][$data_arr[$i]["EXAMNO"]];
            $data["REMARK3"][TEXT]       = $data_arr[$i]["TWINS_FLG"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL620A' ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = "KNJL620A";
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CSVエラー作成用のQUERY
    function selectCsvErrQuery() {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJL620A' ";
        $query .= " ORDER BY ";
        $query .= "     MSGROW ";

        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     RCP.TESTDIV ";
        $query .= "     , RCP.EXAMNO ";
        $query .= "     , RCP_DE.REMARK1 ";
        $query .= "     , ADDR.GNAME ";
        $query .= "     , ADDR.GTELNO ";
        $query .= "     , RCP_DE.REMARK3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCP ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "         ON ADDR.ENTEXAMYEAR = RCP.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV = RCP.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO = RCP.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RCP_DE ";
        $query .= "         ON RCP_DE.ENTEXAMYEAR = RCP.ENTEXAMYEAR ";
        $query .= "         AND RCP_DE.APPLICANTDIV = RCP.APPLICANTDIV ";
        $query .= "         AND RCP_DE.TESTDIV = RCP.TESTDIV ";
        $query .= "         AND RCP_DE.EXAM_TYPE = RCP.EXAM_TYPE ";
        $query .= "         AND RCP_DE.RECEPTNO = RCP.RECEPTNO ";
        $query .= "         AND RCP_DE.SEQ = '010' ";
        $query .= " WHERE ";
        $query .= "     RCP.ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND RCP.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND RCP.TESTDIV = '".$model->field["TESTDIV"]."' ";

        return $query;
    }
}
?>
