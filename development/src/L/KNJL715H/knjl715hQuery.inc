<?php
class knjl715hQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    function getTestDiv($year, $applicantDiv, $testDiv = "") {
        $query  = " SELECT ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($testDiv) {
            $query .= " AND TESTDIV = '{$testDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //変換実行
    function executeQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変換用出身学校コード格納
        $fscd = array();
        $query = knjl715hQuery::getFsCd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $fscd[$row["MIRAI_FS_CD"]] = $row["FS_CD"];
        }

        $query  = knjl715hQuery::selectMainQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["FS_CD"][TEXT]            = $fscd[$row["MIRAI_FS_CD"]];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR = '{$model->examyear}' ";
            $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $where .= "     AND EXAMNO = '{$row["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //賢者の出身学校コード取得
    function getFsCd() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_FS_REP_DAT ";

        return $query;
    }

    //未変換リストCSV出力
    function selectMainQuery($model, $cnt = "") {
        $query  = " SELECT ";
        if (!strlen($cnt)) {
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     T2.REMARK3 AS MIRAI_FS_NAME, ";
            $query .= "     T2.REMARK2 AS MIRAI_FS_CD ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "         AND T2.SEQ = '009' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND T2.REMARK2 IS NOT NULL ";
        $query .= "     AND T1.FS_CD IS NULL ";
        if (!strlen($cnt)) {
            $query .= " ORDER BY ";
            $query .= "     MIRAI_FS_CD, ";
            $query .= "     T1.EXAMNO ";
        }

        return $query;
    }
}
?>
