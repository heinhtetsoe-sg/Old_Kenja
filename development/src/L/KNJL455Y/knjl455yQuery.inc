<?php

require_once('for_php7.php');

class knjl455yQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($namecd, $year)
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
    public function getCsvQuery($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     N0.ABBV1 AS SEX_NAME, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T1.FS_NAME, ";
        $query .= "     T3.GNAME, ";
        $query .= "     T3.ZIPCD, ";
        $query .= "     VALUE(T3.ADDRESS1,'') || VALUE(T3.ADDRESS2,'') AS ADDRESS, ";
        $query .= "     T3.TELNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N0 ON N0.NAMECD1 = 'Z002' AND N0.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND N1.NAMESPARE1   = '1' ";
        $query .= "     AND T1.PROCEDUREDIV = '1' ";
        $query .= "     AND T1.ENTDIV       = '1' ";
        $query .= " ORDER BY ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "     T1.EXAMNO ";
        } else {
            $query .= "     T1.NAME_KANA ";
        }

        return $query;
    }
}
