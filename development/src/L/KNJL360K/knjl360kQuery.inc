<?php

require_once('for_php7.php');

class knjl360kQuery extends Query {
    //特別理由区分
    function getSpecialReasonDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1 = 'L017' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験区分
    function GetTestdiv($model){
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L003' ";

        return $query;
    }

    //スカラシップ
    function GetScalashipdiv($model,$flg){
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'Z006' ";
        if ($flg == "1"){
            $query .= "     AND NAMECD2 IN ('01','02') ";
        }else {
            $query .= "     AND NAMECD2 = '".$model->scalashipdiv."' ";
        }

        return $query;
    }

    //中高判別
    function GetJorH($model){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }

    //CSVデータ取得
    function selectCvsQuery($model){

        $query  = " SELECT ";
        $query .= "     RANK() OVER(ORDER BY (value(t1.A_TOTAL,0)) desc) as RANK, ";
        $query .= "     t1.EXAMNO, ";
        $query .= "     t1.NAME, ";
        $query .= "     CASE WHEN t1.SEX = '2' THEN '*' ELSE '' END AS SEX, ";
        $query .= "     t1.SHDIV, ";
        $query .= "     n1.NAME1 AS SHNAME, ";
        $query .= "     VALUE(t1.SUC_COURSECD,'') || VALUE(t1.SUC_MAJORCD,'') || VALUE(t1.SUC_COURSECODE,'') AS PASSCOURSE, ";
        $query .= "     t2.EXAMCOURSE_ABBV, ";
        $query .= "     t1.FS_CD, ";
        $query .= "     t3.FINSCHOOL_NAME, ";
        $query .= "     t1.A_TOTAL AS SCORE, ";
        $query .= "     '' AS REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT t1 ";
        $query .= "     LEFT JOIN NAME_MST n1 ON n1.NAMECD1 = 'L006' ";
        $query .= "     AND n1.NAMECD2 = t1.SHDIV ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.ENTEXAMYEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND VALUE(t2.COURSECD,'') || VALUE(t2.MAJORCD,'') || VALUE(t2.EXAMCOURSECD,'') = VALUE(t1.SUC_COURSECD,'') || VALUE(t1.SUC_MAJORCD,'') || VALUE(t1.SUC_COURSECODE,'') ";
        $query .= "     LEFT JOIN FINSCHOOL_MST t3 ON t3.FINSCHOOLCD = t1.FS_CD ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        if ($model->special_reason_div != '9') {
            $query .= " AND t1.SPECIAL_REASON_DIV = '{$model->special_reason_div}' ";
        }
        $query .= "     AND t1.TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND VALUE(t1.A_TOTAL,0) >= ".$model->score." ";
        $query .= "     AND ((VALUE(t1.JUDGEMENT,'88') > '0' AND VALUE(t1.JUDGEMENT,'88') <= '6') OR VALUE(t1.JUDGEMENT,'88') = '9') ";
        $query .= " ORDER BY ";
        $query .= "     RANK,t1.EXAMNO ";
        return $query;
    }
}
?>
