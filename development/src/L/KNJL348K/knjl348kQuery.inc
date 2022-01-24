<?php

require_once('for_php7.php');

class knjl348kquery extends Query {

    //試験区分
    function GetTestdiv($model){
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->examyear."' AND ";
        $query .= "     NAMECD1 = 'L003' ";

        return $query;
    }

    //試験区分
    function getTestdivName($model)
    {
        return "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' and NAMECD2 = '".$model->testdiv."' ";
    }

    //実行---2:SS(標準偏差値)相関データ
    function selectCvsQuery2($model)
    {
        //志願者基礎データ
        $query  = "WITH EXAM_BASE AS ( ";
        $query .= "    SELECT TESTDIV,EXAMNO,DESIREDIV,NAME,NAME_KANA,SEX,NATPUBPRIDIV,JUDGEMENT ";
        $query .= "    FROM   ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->examyear."' AND ";
        $query .= "           SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "           TESTDIV = '".$model->testdiv."' ";
        $query .= "    ) ";
        //志望区分マスタ・受験コースマスタ
        $query .= ",EXAM_WISH AS ( ";
        $query .= "    SELECT W1.TESTDIV, W1.DESIREDIV, ";
        $query .= "           MAX(CASE WHEN W1.WISHNO = '1' THEN W2.EXAMCOURSE_MARK END) AS MARK1, ";
        $query .= "           MAX(CASE WHEN W1.WISHNO = '2' THEN W2.EXAMCOURSE_MARK END) AS MARK2, ";
        $query .= "           MAX(CASE WHEN W1.WISHNO = '3' THEN W2.EXAMCOURSE_MARK END) AS MARK3 ";
        $query .= "    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ";
        $query .= "    WHERE  W1.ENTEXAMYEAR = '".$model->examyear."' AND ";
        $query .= "           W1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ";
        $query .= "           W1.COURSECD = W2.COURSECD AND ";
        $query .= "           W1.MAJORCD = W2.MAJORCD AND ";
        $query .= "           W1.EXAMCOURSECD = W2.EXAMCOURSECD ";
        $query .= "    GROUP BY W1.TESTDIV, W1.DESIREDIV ";
        $query .= "    ) ";
        //事前相談ヘッダデータ
        $query .= ",EXAM_CONSUL AS ( ";
        $query .= "    SELECT DISTINCT TESTDIV, EXAMNO, PS_ITEM4, PS_ITEM5 ";
        $query .= "    FROM   ENTEXAM_CONSULTATION_HDAT ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->examyear."' AND ";
        $query .= "           TESTDIV = '".$model->testdiv."' AND ";
        $query .= "           EXAMNO IS NOT NULL AND ";
        $query .= "          (PS_ITEM4 IS NOT NULL OR PS_ITEM5 IS NOT NULL) ";
        $query .= "    ) ";
        //志願者得点データ
        $query .= ",EXAM_SCORE AS ( ";
        $query .= "    SELECT TESTDIV, EXAMNO, ";
        $query .= "           MAX(CASE WHEN TESTSUBCLASSCD = '1' THEN A_SCORE END) AS SCORE1, ";
        $query .= "           MAX(CASE WHEN TESTSUBCLASSCD = '2' THEN A_SCORE END) AS SCORE2, ";
        $query .= "           MAX(CASE WHEN TESTSUBCLASSCD = '3' THEN A_SCORE END) AS SCORE3, ";
        $query .= "           MAX(CASE WHEN TESTSUBCLASSCD = '4' THEN A_SCORE END) AS SCORE4 ";
        $query .= "    FROM   ENTEXAM_SCORE_DAT ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->examyear."' AND ";
        $query .= "           TESTDIV = '".$model->testdiv."' AND ";
        $query .= "           A_SCORE IS NOT NULL ";
        $query .= "    GROUP BY TESTDIV, EXAMNO ";
        $query .= "    ) ";

        //メイン表
        $query .= "SELECT T2.EXAMNO AS EXAMNO2 ";
        $query .= "      ,VALUE(W2.MARK1,'')||VALUE(W2.MARK2,'')||VALUE(W2.MARK3,'') AS DESIREDIV2 ";
        $query .= "      ,CASE WHEN T2.NATPUBPRIDIV = '9' THEN 'F' END AS FUZOKU2 ";
        $query .= "      ,CASE WHEN T2.SEX = '2' THEN '*' END AS SEX2 ";
        $query .= "      ,T2.NAME AS NAME2 ";
        $query .= "      ,CASE WHEN (T2.JUDGEMENT > '0' AND T2.JUDGEMENT <= '6') OR T2.JUDGEMENT = '9' THEN '合'  ";
        $query .= "            WHEN T2.JUDGEMENT  = '7' THEN '否' END AS JUDGEMENT2 ";
        $query .= "      ,C2.PS_ITEM4 AS ITUGI2 ";
        $query .= "      ,C2.PS_ITEM5 AS MOGI2 ";
        $query .= "      ,S2.SCORE1 AS SCORE2_1 ";
        $query .= "      ,S2.SCORE2 AS SCORE2_2 ";
        $query .= "      ,S2.SCORE3 AS SCORE2_3 ";
        $query .= "      ,S2.SCORE4 AS SCORE2_4 ";

//NO003
        $query .= "      ,S2.SCORE1 + S2.SCORE2 + S2.SCORE3 + S2.SCORE4 AS KEI2_4 ";
        $query .= "      ,S2.SCORE1 + S2.SCORE2 + S2.SCORE3 AS KEI2_SHAKAI ";
        $query .= "      ,S2.SCORE1 + S2.SCORE2 + S2.SCORE4 AS KEI2_RIKA ";
        $query .= "      ,S2.SCORE1 + S2.SCORE2 + ";
        $query .= "       CASE WHEN VALUE(S2.SCORE3,0) < VALUE(S2.SCORE4,0) THEN S2.SCORE4 ";
        $query .= "            ELSE S2.SCORE3 END AS KEI2_A ";
//        $query .= "      ,VALUE(S2.SCORE1,0) + VALUE(S2.SCORE2,0) + VALUE(S2.SCORE3,0) + VALUE(S2.SCORE4,0) AS KEI2_4 ";
//        $query .= "      ,VALUE(S2.SCORE1,0) + VALUE(S2.SCORE2,0) + VALUE(S2.SCORE3,0) AS KEI2_SHAKAI ";
//        $query .= "      ,VALUE(S2.SCORE1,0) + VALUE(S2.SCORE2,0) + VALUE(S2.SCORE4,0) AS KEI2_RIKA ";
//        $query .= "      ,VALUE(S2.SCORE1,0) + VALUE(S2.SCORE2,0) + ";
//        $query .= "       CASE WHEN VALUE(S2.SCORE3,0) < VALUE(S2.SCORE4,0) THEN VALUE(S2.SCORE4,0) ";
//        $query .= "            ELSE VALUE(S2.SCORE3,0) END AS KEI2_A ";
//NO003

        $query .= "FROM   EXAM_BASE T2 ";
        $query .= "       INNER JOIN EXAM_CONSUL C2 ON C2.TESTDIV=T2.TESTDIV AND C2.EXAMNO=T2.EXAMNO ";
//        $query .= "       INNER JOIN EXAM_SCORE S2 ON S2.TESTDIV=T2.TESTDIV AND S2.EXAMNO=T2.EXAMNO ";//NO001
        $query .= "       LEFT JOIN EXAM_WISH W2 ON W2.TESTDIV=T2.TESTDIV AND W2.DESIREDIV=T2.DESIREDIV ";
        $query .= "       LEFT JOIN EXAM_SCORE S2 ON S2.TESTDIV=T2.TESTDIV AND S2.EXAMNO=T2.EXAMNO ";//NO005
//NO005↓
        $query .= "WHERE  S2.SCORE1 IS NOT NULL OR S2.SCORE2 IS NOT NULL OR ";
        $query .= "       S2.SCORE3 IS NOT NULL OR S2.SCORE4 IS NOT NULL ";
//        $query .= "WHERE  S2.SCORE1 IS NOT NULL AND ";//NO002
//        $query .= "       S2.SCORE2 IS NOT NULL ";//NO002
//        $query .= "  AND (S2.SCORE3 IS NOT NULL OR S2.SCORE4 IS NOT NULL) ";//NO004
//NO005↑
        $query .= "ORDER BY T2.EXAMNO ";
        return $query;
    }

    //実行---1:前後期重複受験者名簿
    function selectCvsQuery1($model)
    {
        //志願者基礎データ
        $query  = "WITH EXAM_BASE AS ( ";
        $query .= "    SELECT TESTDIV,EXAMNO,DESIREDIV,NAME,NAME_KANA,SEX,NATPUBPRIDIV,JUDGEMENT ";
        $query .= "    FROM   ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->examyear."' AND ";
        $query .= "           SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "          (EXAMNO < '3000' OR '3999' < EXAMNO) AND ";
        $query .= "           VALUE(JUDGEMENT,'88') NOT IN('8') ";
        $query .= "    ) ";
        $query .= ",EXAM_BASE1 AS (SELECT * FROM EXAM_BASE WHERE TESTDIV = '1') ";
        $query .= ",EXAM_BASE2 AS (SELECT * FROM EXAM_BASE WHERE TESTDIV = '2') ";
        //志望区分マスタ・受験コースマスタ
        $query .= ",EXAM_WISH AS ( ";
        $query .= "    SELECT W1.TESTDIV, W1.DESIREDIV, ";
        $query .= "           MAX(CASE WHEN W1.WISHNO = '1' THEN W2.EXAMCOURSE_MARK END) AS MARK1, ";
        $query .= "           MAX(CASE WHEN W1.WISHNO = '2' THEN W2.EXAMCOURSE_MARK END) AS MARK2, ";
        $query .= "           MAX(CASE WHEN W1.WISHNO = '3' THEN W2.EXAMCOURSE_MARK END) AS MARK3 ";
        $query .= "    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ";
        $query .= "    WHERE  W1.ENTEXAMYEAR = '".$model->examyear."' AND ";
        $query .= "           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ";
        $query .= "           W1.COURSECD = W2.COURSECD AND ";
        $query .= "           W1.MAJORCD = W2.MAJORCD AND ";
        $query .= "           W1.EXAMCOURSECD = W2.EXAMCOURSECD ";
        $query .= "    GROUP BY W1.TESTDIV, W1.DESIREDIV ";
        $query .= "    ) ";
        //事前相談ヘッダデータ
        $query .= ",EXAM_CONSUL AS ( ";
        $query .= "    SELECT DISTINCT TESTDIV, EXAMNO, PS_ITEM4, PS_ITEM5 ";
        $query .= "    FROM   ENTEXAM_CONSULTATION_HDAT ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->examyear."' AND ";
        $query .= "           EXAMNO IS NOT NULL AND ";
        $query .= "          (PS_ITEM4 IS NOT NULL OR PS_ITEM5 IS NOT NULL) ";
        $query .= "    ) ";
        //志願者得点データ
        $query .= ",EXAM_SCORE AS ( ";
        $query .= "    SELECT TESTDIV, EXAMNO, ";
        $query .= "           MAX(CASE WHEN TESTSUBCLASSCD = '1' THEN A_SCORE END) AS SCORE1, ";
        $query .= "           MAX(CASE WHEN TESTSUBCLASSCD = '2' THEN A_SCORE END) AS SCORE2, ";
        $query .= "           MAX(CASE WHEN TESTSUBCLASSCD = '3' THEN A_SCORE END) AS SCORE3, ";
        $query .= "           MAX(CASE WHEN TESTSUBCLASSCD = '4' THEN A_SCORE END) AS SCORE4 ";
        $query .= "    FROM   ENTEXAM_SCORE_DAT ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->examyear."' AND ";
        $query .= "           A_SCORE IS NOT NULL ";
        $query .= "    GROUP BY TESTDIV, EXAMNO ";
        $query .= "    ) ";

        //メイン表
                        //後期
        $query .= "SELECT T2.EXAMNO AS EXAMNO2 ";
        $query .= "      ,VALUE(W2.MARK1,'')||VALUE(W2.MARK2,'')||VALUE(W2.MARK3,'') AS DESIREDIV2 ";
        $query .= "      ,CASE WHEN T2.NATPUBPRIDIV = '9' THEN 'F' END AS FUZOKU2 ";
        $query .= "      ,CASE WHEN T2.SEX = '2' THEN '*' END AS SEX2 ";
        $query .= "      ,T2.NAME AS NAME2 ";
        $query .= "      ,CASE WHEN (T2.JUDGEMENT > '0' AND T2.JUDGEMENT <= '6') OR T2.JUDGEMENT = '9' THEN '合'  ";
        $query .= "            WHEN T2.JUDGEMENT  = '7' THEN '否' END AS JUDGEMENT2 ";
        $query .= "      ,S2.SCORE1 AS SCORE2_1 ";
        $query .= "      ,S2.SCORE2 AS SCORE2_2 ";
        $query .= "      ,S2.SCORE3 AS SCORE2_3 ";
        $query .= "      ,S2.SCORE4 AS SCORE2_4 ";

//NO003
        $query .= "      ,S2.SCORE1 + S2.SCORE2 + S2.SCORE3 + S2.SCORE4 AS KEI2_4 ";
        $query .= "      ,S2.SCORE1 + S2.SCORE2 + S2.SCORE3 AS KEI2_SHAKAI ";
        $query .= "      ,S2.SCORE1 + S2.SCORE2 + S2.SCORE4 AS KEI2_RIKA ";
        $query .= "      ,S2.SCORE1 + S2.SCORE2 + ";
        $query .= "       CASE WHEN VALUE(S2.SCORE3,0) < VALUE(S2.SCORE4,0) THEN S2.SCORE4 ";
        $query .= "            ELSE S2.SCORE3 END AS KEI2_A ";
//        $query .= "      ,VALUE(S2.SCORE1,0) + VALUE(S2.SCORE2,0) + VALUE(S2.SCORE3,0) + VALUE(S2.SCORE4,0) AS KEI2_4 ";
//        $query .= "      ,VALUE(S2.SCORE1,0) + VALUE(S2.SCORE2,0) + VALUE(S2.SCORE3,0) AS KEI2_SHAKAI ";
//        $query .= "      ,VALUE(S2.SCORE1,0) + VALUE(S2.SCORE2,0) + VALUE(S2.SCORE4,0) AS KEI2_RIKA ";
//        $query .= "      ,VALUE(S2.SCORE1,0) + VALUE(S2.SCORE2,0) + ";
//        $query .= "       CASE WHEN VALUE(S2.SCORE3,0) < VALUE(S2.SCORE4,0) THEN VALUE(S2.SCORE4,0) ";
//        $query .= "            ELSE VALUE(S2.SCORE3,0) END AS KEI2_A ";
//NO003

                        //前期
        $query .= "      ,T1.EXAMNO AS EXAMNO1 ";
        $query .= "      ,VALUE(W1.MARK1,'')||VALUE(W1.MARK2,'')||VALUE(W1.MARK3,'') AS DESIREDIV1 ";
        $query .= "      ,CASE WHEN (T1.JUDGEMENT > '0' AND T1.JUDGEMENT <= '6') OR T1.JUDGEMENT = '9' THEN '合'  ";
        $query .= "            WHEN T1.JUDGEMENT  = '7' THEN '否' END AS JUDGEMENT1 ";
        $query .= "      ,S1.SCORE1 AS SCORE1_1 ";
        $query .= "      ,S1.SCORE2 AS SCORE1_2 ";
        $query .= "      ,S1.SCORE3 AS SCORE1_3 ";
        $query .= "      ,S1.SCORE4 AS SCORE1_4 ";

//NO003
        $query .= "      ,S1.SCORE1 + S1.SCORE2 + S1.SCORE3 + S1.SCORE4 AS KEI1_4 ";
        $query .= "      ,S1.SCORE1 + S1.SCORE2 + S1.SCORE3 AS KEI1_SHAKAI ";
        $query .= "      ,S1.SCORE1 + S1.SCORE2 + S1.SCORE4 AS KEI1_RIKA ";
        $query .= "      ,S1.SCORE1 + S1.SCORE2 + ";
        $query .= "       CASE WHEN VALUE(S1.SCORE3,0) < VALUE(S1.SCORE4,0) THEN S1.SCORE4 ";
        $query .= "            ELSE S1.SCORE3 END AS KEI1_A ";
//        $query .= "      ,VALUE(S1.SCORE1,0) + VALUE(S1.SCORE2,0) + VALUE(S1.SCORE3,0) + VALUE(S1.SCORE4,0) AS KEI1_4 ";
//        $query .= "      ,VALUE(S1.SCORE1,0) + VALUE(S1.SCORE2,0) + VALUE(S1.SCORE3,0) AS KEI1_SHAKAI ";
//        $query .= "      ,VALUE(S1.SCORE1,0) + VALUE(S1.SCORE2,0) + VALUE(S1.SCORE4,0) AS KEI1_RIKA ";
//        $query .= "      ,VALUE(S1.SCORE1,0) + VALUE(S1.SCORE2,0) + ";
//        $query .= "       CASE WHEN VALUE(S1.SCORE3,0) < VALUE(S1.SCORE4,0) THEN VALUE(S1.SCORE4,0) ";
//        $query .= "            ELSE VALUE(S1.SCORE3,0) END AS KEI1_A ";
//NO003

        $query .= "FROM   EXAM_BASE2 T2 ";
        $query .= "       LEFT JOIN EXAM_WISH W2 ON W2.TESTDIV=T2.TESTDIV AND W2.DESIREDIV=T2.DESIREDIV ";
        $query .= "       LEFT JOIN EXAM_CONSUL C2 ON C2.TESTDIV=T2.TESTDIV AND C2.EXAMNO=T2.EXAMNO ";
        $query .= "       LEFT JOIN EXAM_SCORE S2 ON S2.TESTDIV=T2.TESTDIV AND S2.EXAMNO=T2.EXAMNO ";
        $query .= "      ,EXAM_BASE1 T1 ";
        $query .= "       LEFT JOIN EXAM_WISH W1 ON W1.TESTDIV=T1.TESTDIV AND W1.DESIREDIV=T1.DESIREDIV ";
        $query .= "       LEFT JOIN EXAM_CONSUL C1 ON C1.TESTDIV=T1.TESTDIV AND C1.EXAMNO=T1.EXAMNO ";
        $query .= "       LEFT JOIN EXAM_SCORE S1 ON S1.TESTDIV=T1.TESTDIV AND S1.EXAMNO=T1.EXAMNO ";
        $query .= "WHERE  T1.NAME = T2.NAME AND ";
        $query .= "       T1.NAME_KANA = T2.NAME_KANA ";
//NO005↓
        $query .= "  AND (S1.SCORE1 IS NOT NULL OR S1.SCORE2 IS NOT NULL OR ";
        $query .= "       S1.SCORE3 IS NOT NULL OR S1.SCORE4 IS NOT NULL) ";
        $query .= "  AND (S2.SCORE1 IS NOT NULL OR S2.SCORE2 IS NOT NULL OR ";
        $query .= "       S2.SCORE3 IS NOT NULL OR S2.SCORE4 IS NOT NULL) ";
//        $query .= "  AND  S1.SCORE1 IS NOT NULL ";//NO002
//        $query .= "  AND  S1.SCORE2 IS NOT NULL ";//NO002
//        $query .= "  AND  S2.SCORE1 IS NOT NULL ";//NO002
//        $query .= "  AND  S2.SCORE2 IS NOT NULL ";//NO002
//        $query .= "  AND (S1.SCORE3 IS NOT NULL OR S1.SCORE4 IS NOT NULL) ";//NO004
//        $query .= "  AND (S2.SCORE3 IS NOT NULL OR S2.SCORE4 IS NOT NULL) ";//NO004
//NO005↑
        $query .= "ORDER BY T2.EXAMNO ";
        return $query;
    }
}
?>
