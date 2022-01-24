<?php

require_once('for_php7.php');


class knjl329rQuery extends Query {
    //入試制度取得
    function getApctDiv($model, $namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' ";
        if ($namecd == "L003") {
            $query .= " AND NAMECD2 = '{$model->fixApplicantDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //入試区分取得
    function getTestDiv($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
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
    //一覧取得
    function getList($model) {
        $query  = " SELECT DISTINCT  ";
        if ($model->field["FINSCHOOLDIV"] == "1") {
            //出身学校
            $query .= "     F1.FINSCHOOLCD AS VALUE, ";
            $query .= "     F1.FINSCHOOLCD || '　' || F1.FINSCHOOL_NAME AS LABEL ";
        } else if ($model->field["FINSCHOOLDIV"] == "2") {
            //塾
            $query .= "     P1.PRISCHOOLCD AS VALUE, ";
            $query .= "     P1.PRISCHOOLCD || '　' || P1.PRISCHOOL_NAME AS LABEL ";
        } else {
            //受験者
            $query .= "     T1.RECEPTNO || '-' || T2.EXAMNO AS VALUE, ";
            $query .= "     T1.RECEPTNO || '(' || T1.EXAMNO || ')' || '　' || T2.NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        if ($model->field["FINSCHOOLDIV"] == "1") {
            $query .= "     INNER JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T2.FS_CD ";
        }
        if ($model->field["FINSCHOOLDIV"] == "2") {
            $query .= "     INNER JOIN PRISCHOOL_MST P1 ON P1.PRISCHOOLCD = T2.PRISCHOOLCD ";
        }
        $query .= "     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ";
        $query .= "         AND L013.NAMECD2 = T1.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND L013.NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

}
?>
