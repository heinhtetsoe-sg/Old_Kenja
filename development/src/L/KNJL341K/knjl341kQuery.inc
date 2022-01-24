<?php

require_once('for_php7.php');

class knjl341kQuery extends Query {
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

    //中高判別
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }

    //ＣＳＶ出力---NO001
    function selectCsvQuery($model){

        $query  = "";
        //志願者基礎データ
        $query .= "WITH EXAM_BASE AS ( ";
        $query .= "    SELECT TESTDIV,EXAMNO,SEX,DESIREDIV,JUDGEMENT, ";
        $query .= "           SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ";
        $query .= "    FROM   ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->ObjYear."' ";
        if ($model->special_reason_div != '9') {
            $query .= "       AND SPECIAL_REASON_DIV = '{$model->special_reason_div}' ";
        }
        if ($model->testdiv != "99") //試験区分
            $query .= "       AND TESTDIV = '".$model->testdiv."' ";
        if ($model->output2 == "1") //一般受験者
            $query .= "       AND (EXAMNO < '3000' OR '4000' <= EXAMNO) ";
        if ($model->output2 == "2") //附属出身者
            $query .= "       AND '3000' <= EXAMNO AND EXAMNO < '4000' ";
        $query .= "    ) ";
        //志望区分マスタ・受験コースマスタ1
        $query .= ",EXAM_WISH AS ( ";
        $query .= "    SELECT W1.TESTDIV,W1.DESIREDIV,W1.WISHNO, ";
        $query .= "           W1.COURSECD||W1.MAJORCD||W1.EXAMCOURSECD AS COURSE, ";
        $query .= "           EXAMCOURSE_NAME,EXAMCOURSE_ABBV,EXAMCOURSE_MARK ";
        $query .= "    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ";
        $query .= "    WHERE  W1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        if ($model->testdiv != "99") //試験区分
            $query .= "       W1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ";
        $query .= "           W1.COURSECD = W2.COURSECD AND ";
        $query .= "           W1.MAJORCD = W2.MAJORCD AND ";
        $query .= "           W1.EXAMCOURSECD = W2.EXAMCOURSECD ";
        $query .= "    ) ";
        //志望区分マスタ・受験コースマスタ2
        $query .= ",EXAM_WISH2 AS ( ";
        $query .= "    SELECT TESTDIV,DESIREDIV, ";
        $query .= "           MAX(CASE WHEN WISHNO = '1' THEN EXAMCOURSE_ABBV ELSE NULL END) AS ABBV1, ";
        $query .= "           MAX(CASE WHEN WISHNO = '2' THEN EXAMCOURSE_ABBV ELSE NULL END) AS ABBV2, ";
        $query .= "           MAX(CASE WHEN WISHNO = '3' THEN EXAMCOURSE_ABBV ELSE NULL END) AS ABBV3, ";
        $query .= "           MAX(CASE WHEN WISHNO = '1' THEN EXAMCOURSE_MARK ELSE NULL END) AS MARK1, ";
        $query .= "           MAX(CASE WHEN WISHNO = '2' THEN EXAMCOURSE_MARK ELSE NULL END) AS MARK2, ";
        $query .= "           MAX(CASE WHEN WISHNO = '3' THEN EXAMCOURSE_MARK ELSE NULL END) AS MARK3 ";
        $query .= "    FROM   EXAM_WISH ";
        $query .= "    GROUP BY TESTDIV,DESIREDIV ";
        $query .= "    ) ";
        //志望者・受験者・欠席者・不合格者
        $query .= ",EACH_TOTAL AS ( ";
        $query .= "    SELECT TESTDIV,DESIREDIV, ";
        $query .= "           SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SEX1_A, ";
        $query .= "           SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SEX2_A, ";
        $query .= "           SUM(CASE WHEN SEX = '1' OR SEX = '2' THEN 1 ELSE 0 END) AS KEI_A, ";
        $query .= "           SUM(CASE WHEN SEX = '1' AND VALUE(INT(JUDGEMENT),88) <> 8 THEN 1 ELSE 0 END) AS SEX1_B, ";
        $query .= "           SUM(CASE WHEN SEX = '2' AND VALUE(INT(JUDGEMENT),88) <> 8 THEN 1 ELSE 0 END) AS SEX2_B, ";
        $query .= "           SUM(CASE WHEN (SEX = '1' OR SEX = '2') AND VALUE(INT(JUDGEMENT),88) <> 8 THEN 1 ELSE 0 END) AS KEI_B, ";
        $query .= "           SUM(CASE WHEN SEX = '1' AND INT(JUDGEMENT) = 8 THEN 1 ELSE 0 END) AS SEX1_C, ";
        $query .= "           SUM(CASE WHEN SEX = '2' AND INT(JUDGEMENT) = 8 THEN 1 ELSE 0 END) AS SEX2_C, ";
        $query .= "           SUM(CASE WHEN (SEX = '1' OR SEX = '2') AND INT(JUDGEMENT) = 8 THEN 1 ELSE 0 END) AS KEI_C, ";
        $query .= "           SUM(CASE WHEN SEX = '1' AND INT(JUDGEMENT) = 7 THEN 1 ELSE 0 END) AS SEX1_D, ";
        $query .= "           SUM(CASE WHEN SEX = '2' AND INT(JUDGEMENT) = 7 THEN 1 ELSE 0 END) AS SEX2_D, ";
        $query .= "           SUM(CASE WHEN (SEX = '1' OR SEX = '2') AND INT(JUDGEMENT) = 7 THEN 1 ELSE 0 END) AS KEI_D ";
        $query .= "    FROM   EXAM_BASE ";
        $query .= "    GROUP BY TESTDIV,DESIREDIV ";
        $query .= "    ) ";
        //合格者
        $query .= ",EACH_TOTAL2 AS ( ";
        $query .= "    SELECT TESTDIV,DESIREDIV,SUC_COURSE, ";
        $query .= "           SUM(CASE WHEN SEX = '1' AND ((INT(JUDGEMENT) > 0 AND INT(JUDGEMENT) <= 6) OR INT(JUDGEMENT) = 9) THEN 1 ELSE 0 END) AS SEX1_E, ";
        $query .= "           SUM(CASE WHEN SEX = '2' AND ((INT(JUDGEMENT) > 0 AND INT(JUDGEMENT) <= 6) OR INT(JUDGEMENT) = 9) THEN 1 ELSE 0 END) AS SEX2_E, ";
        $query .= "           SUM(CASE WHEN (SEX = '1' OR SEX = '2') AND ((INT(JUDGEMENT) > 0 AND INT(JUDGEMENT) <= 6) OR INT(JUDGEMENT) = 9) THEN 1 ELSE 0 END) AS KEI_E ";
        $query .= "    FROM   EXAM_BASE ";
        $query .= "    GROUP BY TESTDIV,DESIREDIV,SUC_COURSE ";
        $query .= "    ) ";

        //メイン
        $query .= "SELECT T1.TESTDIV,N1.NAME1 AS TEST_NAME,T1.DESIREDIV, ";
        $query .= "       CASE WHEN MARK1 = 'I' AND MARK2 = 'T' AND MARK3 = 'H' THEN '3' ";
        $query .= "            WHEN MARK1 = 'I' AND MARK2 = 'T' THEN '2' ";
        $query .= "            WHEN MARK1 = 'T' AND MARK2 = 'H' THEN '5' ";
        $query .= "            WHEN MARK1 = 'I' THEN '1' ";
        $query .= "            WHEN MARK1 = 'T' THEN '4' ";
        $query .= "            WHEN MARK1 = 'H' THEN '6' ";
        $query .= "            ELSE '99' END AS SORT, ";
        $query .= "       VALUE(W1.ABBV1,'')||VALUE(W1.ABBV2,'')||VALUE(W1.ABBV3,'') AS ABBV, ";
        $query .= "       VALUE(SEX1_A,0) AS A_KEI1,VALUE(SEX2_A,0) AS A_KEI2,VALUE(KEI_A,0) AS A_KEI3, ";
        $query .= "       VALUE(SEX1_B,0) AS B_KEI1,VALUE(SEX2_B,0) AS B_KEI2,VALUE(KEI_B,0) AS B_KEI3, ";
        $query .= "       VALUE(SEX1_C,0) AS C_KEI1,VALUE(SEX2_C,0) AS C_KEI2,VALUE(KEI_C,0) AS C_KEI3, ";
        $query .= "       VALUE(SEX1_D,0) AS D_KEI1,VALUE(SEX2_D,0) AS D_KEI2,VALUE(KEI_D,0) AS D_KEI3, ";
        $query .= "       EXAMCOURSE_NAME,EXAMCOURSE_MARK, ";
        $query .= "       CASE WHEN EXAMCOURSE_MARK = 'I' THEN '0' ";
        $query .= "            WHEN EXAMCOURSE_MARK = 'T' THEN '1' ";
        $query .= "            WHEN EXAMCOURSE_MARK = 'H' THEN '2' ";
        $query .= "            ELSE '0' END AS MARK, ";
        $query .= "       VALUE(SEX1_E,0) AS E_KEI1,VALUE(SEX2_E,0) AS E_KEI2,VALUE(KEI_E,0) AS E_KEI3 ";
        $query .= "FROM   EXAM_WISH T1 ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.TESTDIV ";
        $query .= "       LEFT JOIN EXAM_WISH2 W1 ON W1.TESTDIV=T1.TESTDIV AND W1.DESIREDIV=T1.DESIREDIV ";
        $query .= "       LEFT JOIN EACH_TOTAL T2 ON T2.TESTDIV=T1.TESTDIV AND T2.DESIREDIV=T1.DESIREDIV ";
        $query .= "       LEFT JOIN EACH_TOTAL2 T3 ON T3.TESTDIV=T1.TESTDIV AND T3.DESIREDIV=T1.DESIREDIV AND T3.SUC_COURSE=T1.COURSE ";
        $query .= "ORDER BY T1.TESTDIV,SORT ";

        return $query;
    }

}
?>
