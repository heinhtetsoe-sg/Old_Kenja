<?php

require_once('for_php7.php');

class knjl410yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSV出力
    function getCsvQuery($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     N1.ABBV1 AS SEX_NAME, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_NAME, ";
        $query .= "     T3.TELNO, ";
        $query .= "     T3.ZIPCD, ";
        $query .= "     VALUE(T3.ADDRESS1,'') || VALUE(T3.ADDRESS2,'') AS ADDRESS, ";
        $query .= "     T3.GNAME, ";
        $query .= "     T3.GKANA, ";
        $query .= "     N2.NAME1 AS RELATIONSHIP_NAME, ";
        $query .= "     T2.REMARK1, ";
        $query .= "     T2.REMARK2, ";
        $query .= "     T2.REMARK3, ";
        $query .= "     T2.REMARK4, ";
        $query .= "     N3.NAME1 AS TESTDIV_NAME ";//テスト名セット用
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T2.EXAMNO = T1.EXAMNO AND T2.SEQ = '010' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'H201' AND N2.NAMECD2 = T3.RELATIONSHIP ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L004' AND N3.NAMECD2 = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($model->field["TESTDIV"] == "1") {
            $query .= "     AND T1.TESTDIV1      = '{$model->field["TESTDIV"]}' ";
        } else if ($model->field["TESTDIV"] == "2") {
            $query .= "     AND T1.TESTDIV2      = '{$model->field["TESTDIV"]}' ";
        } else if ($model->field["TESTDIV"] == "3") {
            $query .= "     AND T1.TESTDIV3      = '{$model->field["TESTDIV"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
    
    //家族・備考のデータ取得
    function getFamilyCsvDataQuery($model, $examno) {
        $query  = " SELECT ";
        $query .= "     T1.NAME AS FAMILY_NAME, ";
        $query .= "     T1.AGE, ";
        $query .= "     N2.NAME1 AS FAMILY_RELATIONSHIP_NAME, ";
        $query .= "     T1.REMARK AS FAMILY_REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_FAMILY_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'H201' AND N2.NAMECD2 = T1.RELATIONSHIP ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND T1.EXAMNO = '{$examno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.SEQ ";

        return $query;
    }
}
?>
