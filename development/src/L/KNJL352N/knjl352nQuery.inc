<?php

require_once('for_php7.php');
class knjl352nQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ
    public function selectMainQuery($model)
    {
        $query  = "";
        $query .= " WITH T_BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         BASE.FS_CD, ";
        $query .= "         FIN.FINSCHOOL_NAME_ABBV, ";
        $query .= "         BASE.ENTEXAMYEAR, ";
        $query .= "         BASE.SHDIV, ";
        $query .= "         BASE.ENTDIV ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "         INNER JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     WHERE ";
        $query .= "         BASE.ENTEXAMYEAR BETWEEN '{$model->oldYear2}' AND '{$model->ObjYear}' ";
        $query .= "         AND BASE.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND BASE.JUDGEMENT NOT IN ('4','5') ";
        $query .= " ) ";

        $query .= " SELECT  ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear2}' AND T1.SHDIV  = '1' THEN 1 ELSE 0 END) AS APPLICANT_NUM1_1, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear2}' AND T1.SHDIV  = '2' THEN 1 ELSE 0 END) AS APPLICANT_NUM1_2, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear2}' AND T1.SHDIV  = '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM1_3, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear2}' AND T1.SHDIV <= '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM1_4, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear1}' AND T1.SHDIV  = '1' THEN 1 ELSE 0 END) AS APPLICANT_NUM2_1, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear1}' AND T1.SHDIV  = '2' THEN 1 ELSE 0 END) AS APPLICANT_NUM2_2, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear1}' AND T1.SHDIV  = '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM2_3, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear1}' AND T1.SHDIV <= '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM2_4, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->ObjYear}' AND T1.SHDIV  = '1' THEN 1 ELSE 0 END) AS APPLICANT_NUM4_1, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->ObjYear}' AND T1.SHDIV  = '2' THEN 1 ELSE 0 END) AS APPLICANT_NUM4_2, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->ObjYear}' AND T1.SHDIV  = '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM4_3, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->ObjYear}' AND T1.SHDIV <= '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM4_4, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '{$model->oldYear2}' AND '{$model->ObjYear}' AND T1.SHDIV  = '1' THEN 1 ELSE 0 END) AS APPLICANT_NUM5_1, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '{$model->oldYear2}' AND '{$model->ObjYear}' AND T1.SHDIV  = '2' THEN 1 ELSE 0 END) AS APPLICANT_NUM5_2, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '{$model->oldYear2}' AND '{$model->ObjYear}' AND T1.SHDIV  = '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM5_3, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '{$model->oldYear2}' AND '{$model->ObjYear}' AND T1.SHDIV <= '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM5_4, ";
        $query .= "     T1.FINSCHOOL_NAME_ABBV, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '{$model->oldYear2}' AND '{$model->ObjYear}' AND T1.SHDIV <= '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM_TOTAL, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear2}' AND T1.SHDIV <= '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM2, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->oldYear1}' AND T1.SHDIV <= '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM3, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->ObjYear}' AND T1.SHDIV <= '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM4, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->ObjYear}' AND T1.SHDIV  = '1' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM5_1, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->ObjYear}' AND T1.SHDIV  = '2' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM5_2, ";
        $query .= "     SUM(CASE WHEN T1.ENTEXAMYEAR = '{$model->ObjYear}' AND T1.SHDIV  = '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM5_3, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM T_BASE T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T1.FINSCHOOL_NAME_ABBV ";
        $query .= " ORDER BY ";
        $query .= "     T1.FS_CD ";
        return $query;
    }
}
