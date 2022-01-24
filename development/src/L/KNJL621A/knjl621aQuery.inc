<?php

require_once('for_php7.php');

class knjl621aQuery extends Query {

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

    //リストを取得
    function getList ($model) {
        $query  = " SELECT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= "     , APPLICANTDIV ";
        $query .= "     , EXEC_TIME ";
        $query .= "     , EXAMNO ";
        $query .= "     , TESTDIV ";
        $query .= "     , NAME ";
        $query .= "     , EXAMHALLNO ";
        $query .= "     , SEATNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SEAT_EXEC_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     EXEC_TIME DESC ";
        $query .= " FETCH FIRST 30 ROWS ONLY ";

        return $query;
    }

    //存在チェック -- ENTEXAM_RECEPT_DAT
    function checkEntexamReceptDat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND EXAMNO = '".$model->field["EXAMNO"]."' ";

        return $query;
    }

    //存在チェック -- ENTEXAM_HALL_YDAT
    function checkEntexamHallYdat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND EXAMHALLCD = '".substr($model->field["SEATNO"], 0, 2)."' ";

        return $query;
    }

    //座席番号チェック
    function checkSeatno($model) {
        $query  = " SELECT ";
        $query .= "     RCP.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT RCP_DE  ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT RCP  ";
        $query .= "         ON RCP.ENTEXAMYEAR = RCP_DE.ENTEXAMYEAR  ";
        $query .= "         AND RCP.APPLICANTDIV = RCP_DE.APPLICANTDIV  ";
        $query .= "         AND RCP.TESTDIV = RCP_DE.TESTDIV  ";
        $query .= "         AND RCP.EXAM_TYPE = RCP_DE.EXAM_TYPE  ";
        $query .= "         AND RCP.RECEPTNO = RCP_DE.RECEPTNO  ";
        $query .= " WHERE ";
        $query .= "     RCP_DE.ENTEXAMYEAR = '".$model->entexamyear."'  ";
        $query .= "     AND RCP_DE.APPLICANTDIV = '".$model->ajaxParam["APPLICANTDIV"]."'  ";
        $query .= "     AND RCP_DE.TESTDIV = '".$model->ajaxParam["TESTDIV"]."'  ";
        $query .= "     AND RCP_DE.SEQ = '010'  ";
        $query .= "     AND RCP_DE.REMARK1 = '".substr($model->ajaxParam["SEATNO"], 0, 2)."'  ";
        $query .= "     AND RCP_DE.REMARK2 = '".substr($model->ajaxParam["SEATNO"], 2, 2)."' ";

        return $query;
    }

    //受験番号登録チェック
    function checkExamno($model) {
        $query  = " SELECT ";
        $query .= "     RCP_DE.REMARK1 || RCP_DE.REMARK2 AS S_CD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCP  ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RCP_DE  ";
        $query .= "         ON RCP_DE.ENTEXAMYEAR = RCP.ENTEXAMYEAR  ";
        $query .= "         AND RCP_DE.APPLICANTDIV = RCP.APPLICANTDIV  ";
        $query .= "         AND RCP_DE.TESTDIV = RCP.TESTDIV  ";
        $query .= "         AND RCP_DE.EXAM_TYPE = RCP.EXAM_TYPE  ";
        $query .= "         AND RCP_DE.RECEPTNO = RCP.RECEPTNO  ";
        $query .= "         AND RCP_DE.SEQ = '010'  ";
        $query .= " WHERE ";
        $query .= "     RCP.ENTEXAMYEAR = '".$model->entexamyear."'  ";
        $query .= "     AND RCP.APPLICANTDIV = '".$model->ajaxParam["APPLICANTDIV"]."'  ";
        $query .= "     AND RCP.TESTDIV = '".$model->ajaxParam["TESTDIV"]."'  ";
        $query .= "     AND RCP.EXAMNO = '".$model->ajaxParam["EXAMNO"]."'";

        return $query;
    }

    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //UPDATE 座席番号が重複するデータの会場番号、座席番号をクリア
        if ($model->duplSeatnoFlg) {
            $data = array();
            $data["REMARK1"][TEXT]       = "";
            $data["REMARK2"][TEXT]       = "";
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
            $where .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
            $where .= "     AND SEQ = '010' ";
            $where .= "     AND REMARK1 = '".substr($model->field["SEATNO"], 0, 2)."' ";
            $where .= "     AND REMARK2 = '".substr($model->field["SEATNO"], 2, 2)."' ";

            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            $db->query($query);
        }

        //志願者受付データを取得
        $query = knjl621AQuery::getEntexamReceptDat($model->entexamyear, $model->field["APPLICANTDIV"], $model->field["TESTDIV"], $model->field["EXAMNO"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //DELETE
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND EXAM_TYPE = '".$row["EXAM_TYPE"]."' ";
        $query .= "     AND RECEPTNO = '".$row["RECEPTNO"]."' ";
        $query .= "     AND SEQ = '010' ";
        $db->query($query);

        //INSERT -- ENTEXAM_RECEPT_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->entexamyear;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]     = $row["EXAM_TYPE"];
        $data["RECEPTNO"][TEXT]      = $row["RECEPTNO"];
        $data["SEQ"][TEXT]           = '010';
        $data["REMARK1"][TEXT]       = substr($model->field["SEATNO"], 0, 2);
        $data["REMARK2"][TEXT]       = substr($model->field["SEATNO"], 2, 2);
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "SYSDATE()";

        $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        $db->query($query);

        //INSERT -- ENTEXAM_SEAT_EXEC_HIST_DAT
        $query = knjl621AQuery::getEntexamApplicantbaseDat($model->entexamyear, $model->field["APPLICANTDIV"], $model->field["EXAMNO"], $model->field["TESTDIV"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->entexamyear;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["EXAMNO"][TEXT]        = $model->field["EXAMNO"];
        $data["EXEC_TIME"][NUMBER]   = "SYSDATE()";
        $data["NAME"][TEXT]          = $row["NAME"];
        $data["EXAMHALLNO"][TEXT]    = substr($model->field["SEATNO"], 0, 2);
        $data["SEATNO"][TEXT]        = substr($model->field["SEATNO"], 2, 2);
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "SYSDATE()";

        $query = Query::insertSQL($data, "ENTEXAM_SEAT_EXEC_HIST_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //志願者受付データ取得
    function getEntexamReceptDat($entexamyear, $applicantdiv, $testdiv, $examno) {
        $query  = " SELECT ";
        $query .= "     EXAM_TYPE ";
        $query .= "     , RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND TESTDIV = '".$testdiv."' ";
        $query .= "     AND EXAMNO = '".$examno."' ";

        return $query;
    }

    //データ取得 -- ENTEXAM_APPLICANTBASE_DAT
    function getEntexamApplicantbaseDat($entexamyear, $applicantdiv, $examno, $testdiv) {
        $query  = " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND EXAMNO = '".$examno."' ";

        return $query;
    }

    //削除
    function getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl621AQuery::getEntexamReceptDat($model->list["ENTEXAMYEAR"], $model->list["APPLICANTDIV"], $model->list["TESTDIV"], $model->list["EXAMNO"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->list["ENTEXAMYEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$model->list["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->list["TESTDIV"]."' ";
        $query .= "     AND EXAM_TYPE = '".$row["EXAM_TYPE"]."' ";
        $query .= "     AND RECEPTNO = '".$row["RECEPTNO"]."' ";
        $query .= "     AND SEQ = '010' ";
        $db->query($query);

        $query  = " DELETE FROM ENTEXAM_SEAT_EXEC_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->list["ENTEXAMYEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$model->list["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->list["TESTDIV"]."' ";
        $query .= "     AND EXAMNO = '".$model->list["EXAMNO"]."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

}
?>
