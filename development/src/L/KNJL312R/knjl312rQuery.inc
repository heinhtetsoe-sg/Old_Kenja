<?php

require_once('for_php7.php');

class knjl312rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
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
        if ($namecd1 == "L009") {
            if ($model->field["TESTDIV"] === '1') {
                $query .= " AND NAMESPARE2 = '1' ";
            } else {
                $query .= " AND NAMESPARE3 = '1' ";
            }
        }
        if ($namecd1 == 'L003') {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目
    function getTestSubclasscd($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME".$model->field["APPLICANTDIV"]." AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '{$model->ObjYear}' ";
        $query .= "     AND T1.NAMECD1     = 'L009' ";
        //満点マスタの設定が前提
        $query .= "     AND T1.NAMECD2 IN ( ";
        $query .= "         SELECT T5.TESTSUBCLASSCD ";
        $query .= "           FROM ENTEXAM_PERFECT_MST T5 ";
        $query .= "          WHERE T5.ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "            AND T5.APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T5.TESTDIV       = '{$model->field["TESTDIV"]}' ";
        $query .= "          GROUP BY T5.TESTSUBCLASSCD ";
        $query .= "         ) ";
        $query .= "     AND T1.NAME".$model->field["APPLICANTDIV"]." IS NOT NULL ";
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     AND T1.NAMESPARE1 = '{$model->field["TESTDIV"]}' ";
        } else {
            if ($model->field["TESTDIV"] == "1") {
                $query .= "     AND T1.NAMESPARE2 = '1' ";
            } else {
                $query .= "     AND T1.NAMESPARE3 = '1' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //試験会場設定データより取得
    function getEntexamHallDat($year, $applicantdiv, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     EXAMHALL_NAME, ";
        $query .= "     S_RECEPTNO, ";
        $query .= "     E_RECEPTNO, ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     EXAMHALLCD || '-' || S_RECEPTNO || '-' || E_RECEPTNO as VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= " AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= " AND TESTDIV = '".$testdiv."' ";
        $query .= " AND EXAM_TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
