<?php

require_once('for_php7.php');

class knjl332aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $defualtFlg, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            if ($defualtFlg) {
                $query .= "     NAME2, ";
                $query .= "     NAMESPARE2 ";
            } else {
                $query .= "     NAME2 ";
            }
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

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
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV      = '{$testdiv}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //受験校種の初期値を取得
    public function getApplicantDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1    = 'L003' ";
        $query .= "     AND NAMESPARE2 = '1' ";

        return $query;
    }
}
