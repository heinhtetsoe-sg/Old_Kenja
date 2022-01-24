<?php

require_once('for_php7.php');

class knjl216qQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //重複チェック項目
    function getCenterTitle() {
        $query  = " WITH T_LIST (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1','（氏名○、出身学校×）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2','（氏名×、出身学校○）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3','（氏名○、出身学校○）リスト') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     VALUE,";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     T_LIST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //公的テストデータ
    function getBeforeDat($model, $testNo = "") {
        $query  = " SELECT ";
        $query .= "     T1.TEST_NO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PUBLIC_TEST_DAT T1 ";
        $query .= "     LEFT JOIN V_FINSCHOOL_MST T2 ";
        $query .= "         ON  T2.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($testNo) {
            $query .= "     AND T1.TEST_NO = '{$testNo}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.TEST_NO ";
        return $query;
    }

    //志願者基礎データ
    function getBaseDat($model, $examno = "") {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T3.FINSCHOOL_NAME, ";
        $query .= "     T2.REMARK1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "         AND T2.SEQ = '003' ";
        $query .= "     LEFT JOIN V_FINSCHOOL_MST T3 ";
        $query .= "         ON  T3.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if (strlen($examno)) {
            $query .= "     AND T1.EXAMNO = '{$examno}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    function ExecuteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //志願者基礎データで公的テストデータを更新
        for ($i = 0; $i < get_count($model->upd_data_base); $i++) {
            $upd_data = preg_split("/-/", $model->upd_data_base[$i]);
            $query = knjl216qQuery::getBaseDat($model, $upd_data[0]);
            $updRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $data = array();
            $data["NAME"][TEXT]             = $updRow["NAME"];
            $data["FS_CD"][TEXT]            = $updRow["FS_CD"];
            $data["registercd"][TEXT]       = STAFFCD;
            $data["updated"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR = '{$model->examyear}' ";
            $where .= "   AND TEST_NO = '{$upd_data[1]}' ";
            $query = Query::updateSQL($data, "ENTEXAM_PUBLIC_TEST_DAT", $where);
            $db->query($query);
        }

        //公的テストデータで志願者基礎データを更新
        for ($i = 0; $i < get_count($model->upd_data_before); $i++) {
            $upd_data = preg_split("/-/", $model->upd_data_before[$i]);
            $query = knjl216qQuery::getBeforeDat($model, $upd_data[1]);
            $updRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //志願者基礎データの更新
            $data = array();
            $data["NAME"][TEXT]             = $updRow["NAME"];
            $data["FS_CD"][TEXT]            = $updRow["FS_CD"];
            $data["registercd"][TEXT]       = STAFFCD;
            $data["updated"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' ";
            $where .= "   AND EXAMNO        = '{$upd_data[0]}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    } 
}
?>

