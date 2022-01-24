<?php

require_once('for_php7.php');

class knjl016uquery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
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

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //CSVデータ出力（願書）
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     CASE WHEN N1.NAMESPARE1 = '1' THEN T1.TESTDIV || '01' ";
        $query .= "          WHEN T1.JUDGEMENT  = '2' THEN '99' ";
        $query .= "     END AS JUDGE_STATUS, ";
        $query .= "     '' AS REMARK1, ";
        $query .= "     T1.NAME AS REMARK2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1 = 'L013' ";
        $query .= "         AND N1.NAMECD2 = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND (N1.NAMESPARE1  = '1' "; //合格者
        $query .= "       OR T1.JUDGEMENT   = '2') ";//不合格者
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
