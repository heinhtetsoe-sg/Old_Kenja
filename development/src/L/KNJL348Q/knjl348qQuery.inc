<?php

require_once('for_php7.php');
class knjl348qQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2    = '".$namecd2."' ";
        }
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMstName($year, $namecd1, $namecd2 = "", $abbv = "")
    {
        $query  = " SELECT DISTINCT ";
        if ($abbv) {
            $query .= "     ABBV1 ";
        } else {
            $query .= "     NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2    = '".$namecd2."' ";
        }

        return $query;
    }

    //テスト科目取得（中学）
    public function getTestKamokuList()
    {
        $nameNo = (SCHOOLKIND == "P") ? "NAME3": "NAME2";

        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     {$nameNo} as NAME ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1   = 'L009' ";
        $query .= "     AND {$nameNo} IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //CSVデータ出力
    public function selectCsvQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     Z002.NAME2 AS SEX_NAME, ";
        $query .= "     R1.DIV_RANK4, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.SEX, ";
        $query .= "     B1.NAME_KANA, ";
        $cnt = 1;
        foreach ($model->testKoumoku as $key => $testName) {
            $query .= "     S{$cnt}.SCORE AS SCORE{$cnt}, ";
            $cnt++;
        }
        $query .= "     R1.TOTAL4, ";
        $query .= "     N1.NAME2 AS INTERVIEW_VALUE, ";
        $query .= "     N2.NAME2 AS INTERVIEW_VALUE2, ";
        $query .= "     B1.FS_CD, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     CASE WHEN B1.SIMAI_SCHOOL_KIND1 = 'P' THEN '1' ";
        $query .= "          WHEN B1.SIMAI_SCHOOL_KIND1 = 'J' THEN '2' ";
        $query .= "          WHEN B1.SIMAI_SCHOOL_KIND1 = 'H' THEN '3' ";
        $query .= "     END AS SIMAI_SCHOOL_KIND1, ";
        $query .= "     CASE WHEN B1.SIMAI_SCHOOL_KIND2 = 'P' THEN '1' ";
        $query .= "          WHEN B1.SIMAI_SCHOOL_KIND2 = 'J' THEN '2' ";
        $query .= "          WHEN B1.SIMAI_SCHOOL_KIND2 = 'H' THEN '3' ";
        $query .= "     END AS SIMAI_SCHOOL_KIND2, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002'  ";
        $query .= "                            AND Z002.NAMECD2 = B1.SEX ";
        $query .= "       LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ";
        $query .= "       INNER JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "                                       AND R1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "                                       AND R1.TESTDIV      = B1.TESTDIV ";
        $query .= "                                       AND R1.EXAMNO       = B1.EXAMNO ";
        $cnt = 1;
        foreach ($model->testKoumoku as $key => $testName) {
            $query .= "       LEFT JOIN ENTEXAM_SCORE_DAT S{$cnt} ";
            $query .= "                                      ON S{$cnt}.ENTEXAMYEAR    = R1.ENTEXAMYEAR ";
            $query .= "                                     AND S{$cnt}.APPLICANTDIV   = R1.APPLICANTDIV ";
            $query .= "                                     AND S{$cnt}.TESTDIV        = R1.TESTDIV ";
            $query .= "                                     AND S{$cnt}.RECEPTNO       = R1.RECEPTNO ";
            $query .= "                                     AND S{$cnt}.TESTSUBCLASSCD = '{$key}' ";
            $cnt++;
        }
        $query .= "       LEFT JOIN ENTEXAM_INTERVIEW_DAT I1 ON I1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "                                         AND I1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "                                         AND I1.TESTDIV      = B1.TESTDIV ";
        $query .= "                                         AND I1.EXAMNO       = B1.EXAMNO ";
        $query .= "       LEFT JOIN V_NAME_MST N1 ON N1.YEAR    = B1.ENTEXAMYEAR ";
        $query .= "                              AND N1.NAMECD1 = 'L027' ";
        $query .= "                              AND N1.NAMECD2 = I1.INTERVIEW_VALUE ";
        $query .= "       LEFT JOIN V_NAME_MST N2 ON N2.YEAR    = B1.ENTEXAMYEAR ";
        $query .= "                              AND N2.NAMECD1 = 'L027' ";
        $query .= "                              AND N2.NAMECD2 = I1.INTERVIEW_VALUE2 ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND B1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        if ($model->field["TAISYOU"] == "3") {
            $query .= "     B1.FS_CD, ";
        } elseif ($model->field["TAISYOU"] == "2") {
            $query .= "     VALUE(R1.TOTAL4,-1) DESC, ";
        }
        $query .= "     B1.EXAMNO ";

        return $query;
    }
}
