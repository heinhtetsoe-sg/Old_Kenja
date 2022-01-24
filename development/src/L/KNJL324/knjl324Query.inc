<?php

require_once('for_php7.php');

class knjl324Query extends Query {

    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        return $query;
    }
    //入試区分取得
    function get_test_div($year)
    {
        $query  = " SELECT NAME AS NAME1,TESTDIV AS NAMECD2,TESTDAY AS NAMESPARE1,SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$year."' AND ";
        $query .= " TESTDIV IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        $query .= " ORDER BY SHOWORDER, TESTDIV ";
        return $query;
    }
    //合格者取得（CSV用）
    function getNo($model)
    {
        $query  = " SELECT TESTDIV, EXAMNO, JUDGECLASS, HONORDIV, '0' as DIV ";
        $query .= " FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= " APPLICANTDIV = '".$model->field["APDIV"]."' AND ";
        if ($model->field["TESTDV"] != "9") $query .= " TESTDIV = '".$model->field["TESTDV"]."' AND ";
        $query .= " JUDGEDIV = '1' AND JUDGECLASS not in ('4','6') ";

        $query .= " UNION ALL ";
        $query .= " SELECT TESTDIV, EXAMNO, JUDGECLASS, HONORDIV, '3' as DIV ";
        $query .= " FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= " APPLICANTDIV = '".$model->field["APDIV"]."' AND ";
        if ($model->field["TESTDV"] != "9") $query .= " TESTDIV = '".$model->field["TESTDV"]."' AND ";
        $query .= " JUDGEDIV = '1' AND JUDGECLASS = '3' ";

        $query .= " UNION ALL ";
        $query .= " SELECT TESTDIV, EXAMNO, JUDGECLASS, HONORDIV, '4' as DIV ";
        $query .= " FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= " APPLICANTDIV = '".$model->field["APDIV"]."' AND ";
        if ($model->field["TESTDV"] != "9") $query .= " TESTDIV = '".$model->field["TESTDV"]."' AND ";
        $query .= " JUDGEDIV = '1' AND JUDGECLASS = '4' ";

        $query .= " UNION ALL ";
        $query .= " SELECT TESTDIV, EXAMNO, JUDGECLASS, HONORDIV, '6' as DIV ";
        $query .= " FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= " APPLICANTDIV = '".$model->field["APDIV"]."' AND ";
        if ($model->field["TESTDV"] != "9") $query .= " TESTDIV = '".$model->field["TESTDV"]."' AND ";
        $query .= " JUDGEDIV = '1' AND JUDGECLASS = '6' ";

        $query .= " ORDER BY TESTDIV, DIV, EXAMNO ";
        return $query;
    }
    //入試区分・入試区分名称・入試日付を取得（CSV用）
    function getNameMst($model)
    {
        $query  = " SELECT TESTDIV AS NAMECD2, NAME AS NAME1, TESTDAY AS NAMESPARE1 ";
        $query .= " FROM   ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE  ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= " ORDER BY SHOWORDER, TESTDIV ";

        return $query;
    }
    //アップ・スライド合格を取得（CSV用）
    function getNameMst2()
    {
        $query  = " SELECT NAMECD2, NAME1 ";
        $query .= " FROM   NAME_MST ";
        $query .= " WHERE  NAMECD1 = 'L016' ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }
    //特別アップ合格を取得（CSV用）
    function getSpUpJudge($model, $row) {
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= " FROM   ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE  ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "    AND EXAMNO = '".$row["EXAMNO"]."' ";
        $query .= "    AND SPECIAL_MEASURES = '3' ";//3:特別アップ合格
        $query .= "    AND ENTCLASS = '2' ";        //2:英数特科クラス
        $query .= "    AND JUDGEMENT = '1' ";       //1:合格
        return $query;
    }
}
?>
