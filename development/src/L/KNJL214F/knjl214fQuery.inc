<?php

require_once('for_php7.php');

class knjl214fQuery extends Query {

    //中学は固定
    function getTestDivKotei() {
        $query  = " WITH VAL_T(LABEL, VALUE) AS ( ";
        $query .= " VALUES('1:帰国生入試', '1') ";
        $query .= " UNION ";
        $query .= " VALUES('2:一般入試', '2') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     VAL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試回数(2:高校のみ)
    function getTestdiv0($year, $testdiv) {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.NAMECD1 = 'L034' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //重複チェック項目
    function getCenterTitle() {
        $query  = " WITH T_LIST (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1','（氏名かな○、出身学校×）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2','（氏名かな×、出身学校○）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3','（氏名かな○、出身学校○）リスト') ";
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

    //事前相談データ
    function getBeforeDat($model, $recruitNo = "") {
        $query  = " SELECT ";
        $query .= "     T1.RECRUIT_NO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.FINSCHOOLCD AS FS_CD, ";
        $query .= "     T2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT T1 ";
        $query .= "     LEFT JOIN V_FINSCHOOL_MST T2 ";
        $query .= "         ON  T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->examyear}' ";
        if ($recruitNo) {
            $query .= "     AND T1.RECRUIT_NO = '{$recruitNo}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.RECRUIT_NO ";
        return $query;
    }

    //志願者基礎データ
    function getBaseDat($model, $examno = "") {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T3.FINSCHOOL_NAME, ";
        $query .= "     T2.REMARK1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "         AND T2.SEQ = '002' ";
        $query .= "     LEFT JOIN V_FINSCHOOL_MST T3 ";
        $query .= "         ON  T3.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     AND T1.TESTDIV0 = '{$model->field["TESTDIV0"]}' ";
        }
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

        //志願者基礎データで事前相談データを更新
        for ($i = 0; $i < get_count($model->upd_data_base); $i++) {
            $upd_data = preg_split("/-/", $model->upd_data_base[$i]);
            $query = knjl214fQuery::getBaseDat($model, $upd_data[0]);
            $updRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $data = array();
            $data["NAME_KANA"][TEXT]        = $updRow["NAME_KANA"];
            $data["FINSCHOOLCD"][TEXT]      = $updRow["FS_CD"];
            $data["registercd"][TEXT]       = STAFFCD;
            $data["updated"][FUNC]          = "sysdate()";
            $where  = " WHERE YEAR   = '{$model->examyear}' ";
            $where .= "   AND RECRUIT_NO   = '{$upd_data[1]}' ";
            $query = Query::updateSQL($data, "RECRUIT_DAT", $where);
            $db->query($query);
        }

        //事前相談データで志願者基礎データを更新
        for ($i = 0; $i < get_count($model->upd_data_before); $i++) {
            $upd_data = preg_split("/-/", $model->upd_data_before[$i]);
            $query = knjl214fQuery::getBeforeDat($model, $upd_data[1]);
            $updRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //志願者基礎データの更新
            $data = array();
            $data["NAME_KANA"][TEXT]        = $updRow["NAME_KANA"];
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

