<?php

require_once('for_php7.php');

class knjl333aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAME2, ";
            $query .= "     NAMESPARE2 ";
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

    //名称マスタより和暦の元号を取得
    public function getCalendarno($year, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
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

    //受験生の出身校データ取得
    public function getFinschool($model)
    {
        $query  = " WITH FINSCHOOL_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         BASE.FS_CD VALUE, ";
        $query .= "         CASE ";
        $query .= "             WHEN FINS.FINSCHOOL_NAME IS NULL THEN BASE.FS_CD || ':' ";
        $query .= "             ELSE BASE.FS_CD || ':' || FINS.FINSCHOOL_NAME ";
        $query .= "         END AS LABEL ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "         INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "              ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "             AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "             AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "         INNER JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= "     WHERE ";
        $query .= "             RCPT.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "         AND RCPT.APPLICANTDIV   = '".$model->field["APPLICANTDIV"]."' ";
        if ($model->field["TESTDIV"] != "ALL") {
            $query .= "         AND RCPT.TESTDIV        = '".$model->field["TESTDIV"]."' ";
        }
        $query .= "         AND RCPT.EXAM_TYPE      = '".$model->examType."' ";
        if ($model->field["DOC_TYPE"] == "4") {
            $query .= "         AND BASE.ENTDIV IS NOT NULL ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_DAT ";
        $query .= " GROUP BY ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
