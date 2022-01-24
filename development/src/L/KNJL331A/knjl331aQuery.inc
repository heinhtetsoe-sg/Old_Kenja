<?php

require_once('for_php7.php');

class knjl331aQuery extends Query
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
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験回コンボ
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        if ($testdiv) {
            $query .= "     TESTDIV_NAME ";
        } else {
            $query .= "     TESTDIV AS VALUE, ";
            $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
            $query .= "     DEFAULT_FLG AS NAMESPARE2, ";
            $query .= "     TESTDIV, ";
            $query .= "     TESTDIV_NAME, ";
            $query .= "     PRETEST_FLG "; //プレテストフラグ
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND PRETEST_FLG IS NULL ";
        if ($testdiv) {
            $query .= "     AND TESTDIV      = '{$testdiv}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入学コース取得
    public function getEnterCourse($model)
    {
        $query   = " SELECT ";
        $query .= "     L012.NAMECD2 || ':' || L012.NAME1 AS LABEL, ";
        $query .= "     L012.NAMECD2 AS VALUE ";
//        $query .= "     ,L012.NAMESPARE2 ";
        $query  .= " FROM ";
        $query  .= "   V_NAME_MST L012 ";
        $query  .= "   INNER JOIN V_NAME_MST L003 ";
        $query  .= "     ON L003.YEAR = L012.YEAR ";
        $query  .= "     AND L003.NAMECD1 = 'L003' ";
        $query  .= "     AND L003.NAMECD2 = '".$model->field["APPLICANTDIV"]."' ";
        $query  .= " WHERE ";
        $query  .= "   L012.YEAR = '".$model->examyear."' ";
        $query  .= "   AND L012.NAMECD1 = 'L' || L003.NAMESPARE3 || '12' ";

        return $query;
    }

    //合格コース取得
    public function getPassCourse($model)
    {
        $query   = " SELECT ";
        $query .= "     L013.NAMECD2 || ':' || L013.NAME1 AS LABEL, ";
        $query .= "     L013.NAMECD2 AS VALUE ";
//        $query .= "     ,L013.NAMESPARE2 ";
        $query  .= " FROM ";
        $query  .= "   V_NAME_MST L013 ";
        $query  .= "   INNER JOIN V_NAME_MST L003 ";
        $query  .= "     ON L003.YEAR = L013.YEAR ";
        $query  .= "     AND L003.NAMECD1 = 'L003' ";
        $query  .= "     AND L003.NAMECD2 = '".$model->field["APPLICANTDIV"]."' ";
        $query  .= " WHERE ";
        $query  .= "   L013.YEAR = '".$model->examyear."' ";
        $query  .= "   AND L013.NAMECD1 = 'L' || L003.NAMESPARE3 || '13' ";
        $query  .= "   AND L013.NAMECD2 <> '0' ";
        $query  .= "   AND L013.NAMESPARE1 = '1' ";

        return $query;
    }
}
