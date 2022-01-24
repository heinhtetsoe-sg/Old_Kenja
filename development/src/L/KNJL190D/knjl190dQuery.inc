<?php

require_once('for_php7.php');

class knjl190dQuery extends Query
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

    //受験種別取得
    public function getTestDivList($year)
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = 'L004' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //MAX_TESTDIV取得
    public function getMaxTestDiv($year)
    {
        $query  = " SELECT ";
        $query .= "     max(TESTDIV) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMstSpare($model, $spare)
    {
        $query  = " SELECT DISTINCT ";
        if ($spare != "4") {
            $query .= "     NAMESPARE{$spare} ";
        } else {
            $query .= "     ABBV3 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->examyear."' ";
        $query .= "     AND NAMECD1 = 'L045' ";
        $query .= "     AND NAMECD2 = '".$model->testdiv."' ";

        return $query;
    }

    //時候の挨拶取得
    public function getGreet($month = '')
    {
        $query  = " SELECT ";
        if ($month == "") {
            $query .= "     MONTH AS VALUE, ";
            $query .= "     MONTH AS LABEL ";
        } else {
            $query .= "     SEQ AS VALUE, ";
            $query .= "     GREETING AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SEASON_GREETINGS_MST ";
        if ($month == "") {
            $query .= " GROUP BY ";
            $query .= "     MONTH ";
            $query .= " ORDER BY ";
            $query .= "     case when int(MONTH) < 04 then int(MONTH) + 12 else int(MONTH) end ";
        } else {
            $query .= " WHERE ";
            $query .= "     MONTH = '{$month}' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //List to List 右側生徒
    public function getPassList($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO as VALUE, ";
        $query .= "     BASE.EXAMNO || '　' || BASE.NAME as LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN NAME_MST L013 ON L013.NAMECD1    = 'L013' ";
        $query .= "                             AND L013.NAMECD2    = BASE.JUDGEMENT ";
        $query .= "                             AND L013.NAMESPARE1 = '1' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND BASE.DESIREDIV   <> '1' "; // 新入生以外
        $query .= "     AND BASE.DESIREDIV    = '".$model->desirediv."' ";
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }
}
