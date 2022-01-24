<?php

require_once('for_php7.php');

class knjl214rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 || ':' || ABBV2 AS LABEL2, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        if ($namecd1 == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //重複チェック項目
    function getCenterTitle() {
        $query  = " WITH T_LIST (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1','（カナ氏名○、出身学校○、性別×）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2','（カナ氏名○、出身学校×、性別○）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3','（カナ氏名○、出身学校×、性別×）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('4','（カナ氏名×、出身学校○、性別○）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('5','（カナ氏名○、出身学校○、性別○）リスト') ";
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
    function getBeforeDat($model, $before_page = "", $before_seq = "") {
        $query  = " SELECT ";
        $query .= "     T1.BEFORE_PAGE, ";
        $query .= "     T1.BEFORE_SEQ, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T2.FINSCHOOL_NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     CASE WHEN T1.SEX = '1' THEN '男' WHEN T1.SEX = '2' THEN '女' ELSE NULL END AS SEX_NAME, ";
        $query .= "     T1.SCHOLARSHIP, ";
        $query .= "     T1.RECOM_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT T1 ";
        $query .= "     LEFT JOIN V_FINSCHOOL_MST T2 ";
        $query .= "         ON  T2.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if (strlen($before_page)) {
            $query .= "     AND T1.BEFORE_PAGE = '{$before_page}' ";
        }
        if (strlen($before_seq)) {
            $query .= "     AND T1.BEFORE_SEQ = '{$before_seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.BEFORE_PAGE, ";
        $query .= "     T1.BEFORE_SEQ ";
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
        $query .= "     T1.SEX, ";
        $query .= "     CASE WHEN T1.SEX = '1' THEN '男' WHEN T1.SEX = '2' THEN '女' ELSE NULL END AS SEX_NAME, ";
        $query .= "     T1.JUDGE_KIND, ";
        $query .= "     T1.SPORTS_FLG, ";
        $query .= "     T2.REMARK1, ";
        $query .= "     T2.REMARK2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND T2.SEQ          = '002' ";
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

        //志願者基礎データで事前相談データを更新
        for ($i = 0; $i < get_count($model->upd_data_base); $i++) {
            $upd_data = preg_split("/-/", $model->upd_data_base[$i]);
            $query = knjl214rQuery::getBaseDat($model, $upd_data[0]);
            $updRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $data = array();
            $data["NAME_KANA"][TEXT]        = $updRow["NAME_KANA"];
            $data["FS_CD"][TEXT]            = $updRow["FS_CD"];
            $data["SEX"][TEXT]              = $updRow["SEX"];
            $data["registercd"][TEXT]       = STAFFCD;
            $data["updated"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
            $where .= "   AND TESTDIV       = '{$model->field["TESTDIV"]}' ";
            $where .= "   AND BEFORE_PAGE   = '{$upd_data[1]}' ";
            $where .= "   AND BEFORE_SEQ    = '{$upd_data[2]}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANT_BEFORE_DAT", $where);
            $db->query($query);
        }

        //事前相談データで志願者基礎データを更新
        for ($i = 0; $i < get_count($model->upd_data_before); $i++) {
            $upd_data = preg_split("/-/", $model->upd_data_before[$i]);
            $query = knjl214rQuery::getBeforeDat($model, $upd_data[1], $upd_data[2]);
            $updRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //志願者基礎データの更新
            $data = array();
            $data["NAME_KANA"][TEXT]        = $updRow["NAME_KANA"];
            $data["FS_CD"][TEXT]            = $updRow["FS_CD"];
            $data["SEX"][TEXT]              = $updRow["SEX"];
            $data["registercd"][TEXT]       = STAFFCD;
            $data["updated"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
            $where .= "   AND EXAMNO        = '{$upd_data[0]}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    } 
}
?>

