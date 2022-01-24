<?php

require_once('for_php7.php');

class knjl008fQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $sitei='') {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //登録済み生徒データ
    function getSch($model, $testdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     BASE_D_012.REMARK{$testdiv} AS RECEPTNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        //入試区分(TESTDIV)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_010 ";
        $query .= "          ON BASE_D_010.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND BASE_D_010.EXAMNO      = T1.EXAMNO ";
        $query .= "         AND BASE_D_010.SEQ         = '010' ";
        //受験番号(RECEPTNO)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_012 ";
        $query .= "          ON BASE_D_012.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND BASE_D_012.EXAMNO      = T1.EXAMNO ";
        $query .= "         AND BASE_D_012.SEQ         = '012' ";
        $query .= "     LEFT OUTER JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field['APPLICANTDIV']."' ";
        $query .= "     AND BASE_D_010.REMARK{$testdiv} = '".$model->field['TESTDIV']."'  ";

        if ($model->field['S_NAME'] != '') { // 氏名
            $query .= "     AND T1.NAME like '%".$model->field['S_NAME']."%'  ";
        }
        if ($model->field['S_NAME_KANA'] != '') { // ふりがな
            $query .= "     AND T1.NAME_KANA like '%".$model->field['S_NAME_KANA']."%'  ";
        }
        if ($model->field['S_RECEPTNO'] != '') { // 受験番号
            $query .= "     AND BASE_D_012.REMARK{$testdiv} like '%".$model->field['S_RECEPTNO']."%'  ";
        }
        $query.= " ORDER BY ";
        $query.= "     T1.EXAMNO, ";
        $query.= "     BASE_D_012.REMARK{$testdiv} ";

        return $query;
    }
}
?>