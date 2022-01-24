<?php

require_once('for_php7.php');

class knjl325Query extends Query {

    //入試制度取得
    function get_apct_div($namecd)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_RECEPT_DAT)";
        return $query;
    }
    //入試区分取得
    function get_test_div($namecd)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_RECEPT_DAT)";
        return $query;
    }
    //合格者取得（CSV用）
    function getNo($model)
    {
        $query  = " SELECT TESTDIV, EXAMNO ";
        $query .= " FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= " APPLICANTDIV = '".$model->field["APDIV"]."' AND ";
	    //入試区分が全て以外
		if ($model->field["TESTDV"] != "0") 
	        $query .= " TESTDIV = '".$model->field["TESTDV"]."' AND ";
        $query .= " JUDGEDIV = '1' ";
        $query .= " ORDER BY TESTDIV, EXAMNO ";
        return $query;
    }
    //入試区分・入試区分名称・入試日付を取得（CSV用）
    function getNameMst()
    {
        $query  = " SELECT NAMECD2, NAME1, NAMESPARE1 ";
        $query .= " FROM   NAME_MST ";
        $query .= " WHERE  NAMECD1 = 'L004' ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }
    //入試区分取得（CSV用：ループ用）
    function getRoopTestdiv($model)
    {
        $query  = "SELECT DISTINCT TESTDIV ";
        $query .= "FROM   ENTEXAM_RECEPT_DAT ";
        $query .= "WHERE  ENTEXAMYEAR='".$model->ObjYear."' AND ";
	    //入試区分が全て以外
		if ($model->field["TESTDV"] != "0") 
	        $query .= "   TESTDIV = '".$model->field["TESTDV"]."' AND ";
        $query .= "       APPLICANTDIV='".$model->field["APDIV"]."' ";
        $query .= "ORDER BY TESTDIV ";

        return $query;
    }
    //速報データ（人数）取得
    function getCsvNumber($model, $testdiv)
    {
		//志願者
        $query  = "SELECT  'A' TYPE_CNT, ";
        $query .= "        SUM(CASE WHEN W2.EXAMNO BETWEEN '20000' AND '29999' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE2_1,  ";
        $query .= "        SUM(CASE WHEN W2.EXAMNO BETWEEN '40000' AND '49999' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE4_1,  ";
        $query .= "        SUM(CASE WHEN W3.SEX='1' THEN 1 ELSE 0 END) AS TOTAL1,  ";
        $query .= "        SUM(CASE WHEN W2.EXAMNO BETWEEN '20000' AND '29999' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE2_2,  ";
        $query .= "        SUM(CASE WHEN W2.EXAMNO BETWEEN '40000' AND '49999' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE4_2,  ";
        $query .= "        SUM(CASE WHEN W3.SEX='2' THEN 1 ELSE 0 END) AS TOTAL2,  ";
        $query .= "        SUM(CASE WHEN W2.EXAMNO BETWEEN '20000' AND '29999' THEN 1 ELSE 0 END) AS TYPE2_3,  ";
        $query .= "        SUM(CASE WHEN W2.EXAMNO BETWEEN '40000' AND '49999' THEN 1 ELSE 0 END) AS TYPE4_3,  ";
        $query .= "        COUNT(*) AS TOTAL3  ";
        $query .= "FROM    ENTEXAM_DESIRE_DAT W2  ";
        $query .= "        INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W2.ENTEXAMYEAR AND  ";
        $query .= "                                                   W3.EXAMNO = W2.EXAMNO  ";
        $query .= "WHERE   W2.ENTEXAMYEAR = '".$model->ObjYear."' AND  ";
        $query .= "        W2.APPLICANTDIV = '".$model->field["APDIV"]."' AND  ";   //志願者データ：入試制度
        $query .= "        W2.TESTDIV = '".$testdiv."' AND  ";                     //志願者データ：入試区分
        $query .= "        W2.APPLICANT_DIV = '1'  ";                 //志願者データ：志願者区分
        $query .= "GROUP BY W2.TESTDIV  ";
		//受験者
        $query .= "UNION ALL  ";
        $query .= "SELECT  'B' TYPE_CNT, ";
        $query .= "        SUM(CASE WHEN W1.EXAM_TYPE='1' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE2_1,  ";
        $query .= "        SUM(CASE WHEN W1.EXAM_TYPE='2' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE4_1,  ";
        $query .= "        SUM(CASE WHEN W3.SEX='1' THEN 1 ELSE 0 END) AS TOTAL1,  ";
        $query .= "        SUM(CASE WHEN W1.EXAM_TYPE='1' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE2_2,  ";
        $query .= "        SUM(CASE WHEN W1.EXAM_TYPE='2' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE4_2,  ";
        $query .= "        SUM(CASE WHEN W3.SEX='2' THEN 1 ELSE 0 END) AS TOTAL2,  ";
        $query .= "        SUM(CASE WHEN W1.EXAM_TYPE='1' THEN 1 ELSE 0 END) AS TYPE2_3,  ";
        $query .= "        SUM(CASE WHEN W1.EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TYPE4_3,  ";
        $query .= "        SUM(CASE WHEN W1.EXAM_TYPE='1' OR W1.EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TOTAL3  ";
        $query .= "FROM    ENTEXAM_RECEPT_DAT W1  ";
        $query .= "        INNER JOIN ENTEXAM_DESIRE_DAT W2 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ";
        $query .= "                                            W2.APPLICANTDIV = W1.APPLICANTDIV AND  ";
        $query .= "                                            W2.TESTDIV = W1.TESTDIV AND  ";
        $query .= "                                            W2.EXAMNO = W1.EXAMNO  ";
        $query .= "        INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ";
        $query .= "                                                   W3.EXAMNO = W1.EXAMNO  ";
        $query .= "WHERE   W1.ENTEXAMYEAR = '".$model->ObjYear."' AND  ";
        $query .= "        W1.APPLICANTDIV = '".$model->field["APDIV"]."' AND  ";   //志願者受付データ：入試制度
        $query .= "        W1.TESTDIV = '".$testdiv."' AND  ";                     //志願者受付データ：入試区分
        $query .= "        W2.EXAMINEE_DIV = '1' AND  ";				//志願者データ：受験者区分
        $query .= "        W2.APPLICANT_DIV = '1'  ";					//志願者データ：志願者区分
        $query .= "GROUP BY W1.TESTDIV  ";
		//合格者
        $query .= "UNION ALL  ";
        $query .= "SELECT  'C' TYPE_CNT, ";
        $query .= "        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='1' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE2_1,  "; 
        $query .= "        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='2' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE4_1,  "; 
        $query .= "        SUM(CASE WHEN W3.SEX='1' THEN 1 ELSE 0 END) AS TOTAL1,  ";
        $query .= "        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='1' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE2_2,  "; 
        $query .= "        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='2' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE4_2,  "; 
        $query .= "        SUM(CASE WHEN W3.SEX='2' THEN 1 ELSE 0 END) AS TOTAL2,  ";
        $query .= "        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='1' THEN 1 ELSE 0 END) AS TYPE2_3,  ";                
        $query .= "        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TYPE4_3,  ";                
        $query .= "        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='1' OR W1.JUDGE_EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TOTAL3  "; 
        $query .= "FROM    ENTEXAM_RECEPT_DAT W1  ";
        $query .= "        INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ";
        $query .= "                                                   W3.EXAMNO = W1.EXAMNO  ";
        $query .= "WHERE   W1.ENTEXAMYEAR = '".$model->ObjYear."' AND  ";
        $query .= "        W1.APPLICANTDIV = '".$model->field["APDIV"]."' AND  ";   //志願者受付データ：入試制度
        $query .= "        W1.TESTDIV = '".$testdiv."' AND  ";                     //志願者受付データ：入試区分
        $query .= "        W1.JUDGEDIV = '1'  ";						//志願者受付データ：合否区分

        //前回までの合格者を対象に含めない場合 05/01/05
        if( $model->field["CHECK1"] == "" ){
            $query .=     "AND W1.EXAMNO NOT IN(";
            $query .=              "SELECT  DISTINCT EXAMNO ";
            $query .=              "FROM    ENTEXAM_RECEPT_DAT W1  ";
            $query .=              "WHERE   W1.ENTEXAMYEAR = '".$model->ObjYear."' AND  ";
            $query .=                      "W1.APPLICANTDIV = '".$model->field["APDIV"]."' AND  ";   //志願者受付データ：入試制度
            $query .=                      "W1.TESTDIV < '".$testdiv."' AND  ";                     //志願者受付データ：入試区分
            $query .=                      "W1.JUDGEDIV = '1' ";				         //志願者受付データ：合否区分
            $query .=              ") ";
        }

        $query .= "GROUP BY W1.TESTDIV  ";
        $query .= "ORDER BY TYPE_CNT ";

        return $query;
    }
    //速報データ（得点）取得
    function getCsvScore($model, $testdiv)
    {
		//志願者受付・基礎データ
        $query  = "WITH RECEPT AS ( ";
        $query .= "    SELECT  W3.SEX, W1.*  ";
        $query .= "    FROM    ENTEXAM_RECEPT_DAT W1,ENTEXAM_APPLICANTBASE_DAT W3  ";
        $query .= "    WHERE   W1.ENTEXAMYEAR = '".$model->ObjYear."' AND  ";
        $query .= "            W1.APPLICANTDIV = '".$model->field["APDIV"]."' AND  ";
        $query .= "            W1.TESTDIV = '".$testdiv."' AND  ";
        $query .= "            W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ";
        $query .= "            W3.EXAMNO = W1.EXAMNO AND  ";
        //前回までの合格者を対象に含めない場合 05/01/05
        if( $model->field["CHECK1"] == "" ){
            $query .=         "W1.EXAMNO NOT IN(";
            $query .=              "SELECT  DISTINCT EXAMNO ";
            $query .=              "FROM    ENTEXAM_RECEPT_DAT W1  ";
            $query .=              "WHERE   W1.ENTEXAMYEAR = '".$model->ObjYear."' AND  ";
            $query .=                      "W1.APPLICANTDIV = '".$model->field["APDIV"]."' AND  ";   //志願者受付データ：入試制度
            $query .=                      "W1.TESTDIV < '".$testdiv."' AND  ";                     //志願者受付データ：入試区分
            $query .=                      "W1.JUDGEDIV = '1' ";				         //志願者受付データ：合否区分
            $query .=              ") AND ";
        }
        $query .= "            W1.JUDGEDIV = '1' ),  ";

		//科目別	平均点（合計）最高点・最低点
        $query .= "SUB_AVG AS ( ";
        $query .= "    SELECT  TESTSUBCLASSCD AS SUBCLASSCD,  ";
        $query .= "            MAX(SCORE) AS MAXSCORE,  ";
        $query .= "            MIN(SCORE) AS MINSCORE,  ";
        $query .= "            ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "            INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ";
        $query .= "                                               W4.APPLICANTDIV = W1.APPLICANTDIV AND  ";
        $query .= "                                               W4.EXAM_TYPE = W1.EXAM_TYPE AND  ";
        $query .= "                                               W4.TESTDIV = W1.TESTDIV AND  ";
        $query .= "                                               W4.RECEPTNO = W1.RECEPTNO  ";
        $query .= "    GROUP BY W1.TESTDIV,TESTSUBCLASSCD ),  ";
					//平均点（男子）
        $query .= "SUB_B_AVG AS ( ";
        $query .= "    SELECT  TESTSUBCLASSCD AS SUBCLASSCD,  ";
        $query .= "            ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "            INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ";
        $query .= "                                               W4.APPLICANTDIV = W1.APPLICANTDIV AND  ";
        $query .= "                                               W4.EXAM_TYPE = W1.EXAM_TYPE AND  ";
        $query .= "                                               W4.TESTDIV = W1.TESTDIV AND  ";
        $query .= "                                               W4.RECEPTNO = W1.RECEPTNO  ";
        $query .= "    WHERE   W1.SEX = '1' ";
        $query .= "    GROUP BY W1.TESTDIV,TESTSUBCLASSCD ),  ";
					//平均点（女子）
        $query .= "SUB_G_AVG AS ( ";
        $query .= "    SELECT  TESTSUBCLASSCD AS SUBCLASSCD,  ";
        $query .= "            ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "            INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ";
        $query .= "                                               W4.APPLICANTDIV = W1.APPLICANTDIV AND  ";
        $query .= "                                               W4.EXAM_TYPE = W1.EXAM_TYPE AND  ";
        $query .= "                                               W4.TESTDIV = W1.TESTDIV AND  ";
        $query .= "                                               W4.RECEPTNO = W1.RECEPTNO  ";
        $query .= "    WHERE   W1.SEX = '2' ";
        $query .= "    GROUP BY W1.TESTDIV,TESTSUBCLASSCD ),  ";

		//２科計	平均点（合計）最高点・最低点
        $query .= "TYPE2_AVG AS ( ";
        $query .= "    SELECT  'A' AS SUBCLASSCD,  ";
        $query .= "            MAX(TOTAL2) AS MAXSCORE,  ";
        $query .= "            MIN(TOTAL2) AS MINSCORE,  ";
        $query .= "            ROUND(AVG(FLOAT(TOTAL2))*10,0)/10 AS AVERAGE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "    WHERE   VALUE(W1.JUDGE_EXAM_TYPE,'0') = '1' ";    
        $query .= "    GROUP BY W1.TESTDIV ),  ";
					//平均点（男子）
        $query .= "TYPE2_B_AVG AS ( ";
        $query .= "    SELECT  'A' AS SUBCLASSCD,  ";
        $query .= "            ROUND(AVG(FLOAT(TOTAL2))*10,0)/10 AS AVERAGE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "    WHERE   W1.SEX = '1' AND ";
        $query .= "            VALUE(W1.JUDGE_EXAM_TYPE,'0') = '1' ";    
        $query .= "    GROUP BY W1.TESTDIV ),  ";
					//平均点（女子）
        $query .= "TYPE2_G_AVG AS ( ";
        $query .= "    SELECT  'A' AS SUBCLASSCD,  ";
        $query .= "            ROUND(AVG(FLOAT(TOTAL2))*10,0)/10 AS AVERAGE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "    WHERE   W1.SEX = '2' AND ";
        $query .= "            VALUE(W1.JUDGE_EXAM_TYPE,'0') = '1' ";    
        $query .= "    GROUP BY W1.TESTDIV ),  ";

		//４科計	平均点（合計）最高点・最低点
        $query .= "TYPE4_AVG AS ( ";
        $query .= "    SELECT  'B' AS SUBCLASSCD,  ";
        $query .= "            MIN(TOTAL4) AS MINSCORE,  ";
        $query .= "            ROUND(AVG(FLOAT(TOTAL4))*10,0)/10 AS AVERAGE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "    WHERE   VALUE(W1.JUDGE_EXAM_TYPE,'0') = '2' ";    
        $query .= "    GROUP BY W1.TESTDIV ),  ";
					//最高点
        $query .= "TYPE4_MAX AS ( ";
        $query .= "    SELECT  'B' AS SUBCLASSCD,  ";
        $query .= "            MAX(TOTAL4) AS MAXSCORE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "    WHERE   VALUE(W1.JUDGE_EXAM_TYPE,'0') = '2' ";    
        $query .= "    GROUP BY W1.TESTDIV ),  ";
					//平均点（男子）
        $query .= "TYPE4_B_AVG AS ( ";
        $query .= "    SELECT  'B' AS SUBCLASSCD,  ";
        $query .= "            ROUND(AVG(FLOAT(TOTAL4))*10,0)/10 AS AVERAGE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "    WHERE   W1.SEX = '1' AND ";
        $query .= "            VALUE(W1.JUDGE_EXAM_TYPE,'0') = '2' ";    
        $query .= "    GROUP BY W1.TESTDIV ),  ";
					//平均点（女子）
        $query .= "TYPE4_G_AVG AS ( ";
        $query .= "    SELECT  'B' AS SUBCLASSCD,  ";
        $query .= "            ROUND(AVG(FLOAT(TOTAL4))*10,0)/10 AS AVERAGE  ";
        $query .= "    FROM    RECEPT W1  ";
        $query .= "    WHERE   W1.SEX = '2' AND ";
        $query .= "            VALUE(W1.JUDGE_EXAM_TYPE,'0') = '2' ";    
        $query .= "    GROUP BY W1.TESTDIV )  ";

		//メイン
		//科目別
        $query .= "SELECT  T1.SUBCLASSCD, T2.AVERAGE AS BOYAVERAGE, T3.AVERAGE AS GRLAVERAGE,  ";
        $query .= "        T1.AVERAGE, T1.MAXSCORE, T1.MINSCORE  ";
        $query .= "FROM    SUB_AVG T1  ";
        $query .= "        LEFT JOIN SUB_B_AVG T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD ";
        $query .= "        LEFT JOIN SUB_G_AVG T3 ON T1.SUBCLASSCD=T3.SUBCLASSCD ";

		//２科計
        $query .= "UNION ALL  ";
        $query .= "SELECT  T1.SUBCLASSCD, T2.AVERAGE AS BOYAVERAGE, T3.AVERAGE AS GRLAVERAGE,  ";
        $query .= "        T1.AVERAGE, T1.MAXSCORE, T1.MINSCORE  ";
        $query .= "FROM    TYPE2_AVG T1  ";
        $query .= "        LEFT JOIN TYPE2_B_AVG T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD ";
        $query .= "        LEFT JOIN TYPE2_G_AVG T3 ON T1.SUBCLASSCD=T3.SUBCLASSCD ";

		//４科計
        $query .= "UNION ALL  ";
        $query .= "SELECT  T1.SUBCLASSCD, T2.AVERAGE AS BOYAVERAGE, T3.AVERAGE AS GRLAVERAGE,  ";
        $query .= "        T1.AVERAGE, T4.MAXSCORE, T1.MINSCORE  ";
        $query .= "FROM    TYPE4_AVG T1  ";
        $query .= "        LEFT JOIN TYPE4_B_AVG T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD ";
        $query .= "        LEFT JOIN TYPE4_G_AVG T3 ON T1.SUBCLASSCD=T3.SUBCLASSCD ";
        $query .= "        LEFT JOIN TYPE4_MAX T4 ON T1.SUBCLASSCD=T4.SUBCLASSCD ";
        $query .= "ORDER BY SUBCLASSCD ";

        return $query;
    }
}
?>
