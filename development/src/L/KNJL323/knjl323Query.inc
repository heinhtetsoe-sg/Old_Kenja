<?php

require_once('for_php7.php');

class knjl323Query extends Query {

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
        $query  = " SELECT NAME AS NAME1,TESTDIV AS NAMECD2,SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$year."' AND ";
        $query .= " TESTDIV IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        $query .= " ORDER BY SHOWORDER, TESTDIV ";
        return $query;
    }
    //試験会場名称取得
    function get_hall_data($testdv,$exam_typ)
    {
        $query  = " SELECT EXAMHALL_NAME,S_RECEPTNO,E_RECEPTNO,EXAMHALLCD ";
        $query .= " FROM ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= " TESTDIV = '".$testdv."' AND ";
        $query .= " EXAM_TYPE = '".$exam_typ."'";
        return $query;
    }

    function getCsvSql($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T4.NAME AS TEST_NAME, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     SUBSTR(T1.EXAMNO,1,2) EXAMNO2, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T5.ABBV1 AS SEX_NAME, ";
        $query .= "     T1.FS_NAME  ";
        $query .= " FROM ";
        $query .= "     (SELECT W1.TESTDIV, W1.EXAMNO, W2.NAME, W2.NAME_KANA, W2.SEX, W2.FS_NAME  ";
        $query .= "      FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_APPLICANTBASE_DAT W2  ";
        $query .= "      WHERE  W1.ENTEXAMYEAR='{$model->ObjYear}'  ";
        $query .= "             AND W1.ENTEXAMYEAR=W2.ENTEXAMYEAR  ";
        $query .= "             AND W1.EXAMNO=W2.EXAMNO  ";
        $query .= "             AND W1.APPLICANTDIV='{$model->field["APDIV"]}' ";
        //'全て'以外の場合（入試区分）
        if ($model->field["TESTDV"] != "9") {
            $query .= "             AND W1.TESTDIV='{$model->field["TESTDV"]}' ";
        }
        //特待生合格者名簿
        if ($model->field["OUTPUT"] == "1") {
            $query .= "             AND W1.JUDGEDIV='1'  ";
            $query .= "             AND W1.HONORDIV='1'  ";
        }
        //不合格者名簿
        if ($model->field["OUTPUT"] == "2") {
            $query .= "             AND W1.JUDGEDIV='2'  ";
        }
        //合格者名簿
        if ($model->field["OUTPUT"] == "3") {
            $query .= "                 AND W1.JUDGEDIV='1'  ";
            if ($model->field["SUCTYPE"] == "1") {
                //Ｔ合格
                $query .= "            AND W1.JUDGECLASS IN ('2', '3')  ";
            }
            if ($model->field["SUCTYPE"] == "2") {
                //Ｓ合格
                $query .= "            AND W1.JUDGECLASS IN ('1', '4', '6')  ";
            }
        }
        //アップ合格者名簿
        if ($model->field["OUTPUT"] == "4") {
            $query .= "                 AND W1.JUDGEDIV='1'  ";
            $query .= "                 AND W1.JUDGECLASS='3'  ";
        }
        //スライド合格者名簿
        if ($model->field["OUTPUT"] == "5") {
            $query .= "                 AND W1.JUDGEDIV='1'  ";
            $query .= "                 AND W1.JUDGECLASS='4'  ";
        }
        //非正規合格者名簿
        if ($model->field["OUTPUT"] == "6") {
            $query .= "                 AND W1.JUDGEDIV='1'  ";
            $query .= "                 AND W1.JUDGECLASS='6'  ";
        }
        $query .= "             ) T1  ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST T4 ON T4.ENTEXAMYEAR='{$model->ObjYear}' AND T4.TESTDIV=T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST T5 ON T5.NAMECD1='Z002' AND T5.NAMECD2=T1.SEX ";
        $query .= " ORDER BY  ";
        $query .= "     T1.TESTDIV, T1.EXAMNO ";
        return $query;
    }
}
?>
