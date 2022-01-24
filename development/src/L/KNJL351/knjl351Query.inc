<?php

require_once('for_php7.php');

class knjl351Query extends Query {

    //入試区分取得
    function getTestdivMst($model) {
        $query  = " SELECT TESTDIV ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= " ORDER BY SHOWORDER, TESTDIV ";
        return $query;
    }

    //CSV取得
    function getcsvdata($model)
    {
        $field = ($model->field["OUTPUT"] == 2) ? "TOTAL2" : "TOTAL4";

		$query  = " with test1(EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN) as ";
    	$query .= "	(select EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN ";
    	$query .= "	 from ENTEXAM_RECEPT_DAT ";
    	$query .= "	 where ENTEXAMYEAR = '".$model->ObjYear."' and TESTDIV = '1'), ";

    	$query .= "	test2(EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN) as ";
    	$query .= "	(select EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN ";
    	$query .= "	 from ENTEXAM_RECEPT_DAT ";
    	$query .= "	 where ENTEXAMYEAR = '".$model->ObjYear."' and TESTDIV = '2'), ";

    	$query .= "	test3(EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN) as ";
    	$query .= "	(select EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN ";
    	$query .= "	 from ENTEXAM_RECEPT_DAT ";
    	$query .= "	 where ENTEXAMYEAR = '".$model->ObjYear."' and TESTDIV = '3'), ";

    	$query .= "	test5(EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN) as ";
    	$query .= "	(select EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN ";
    	$query .= "	 from ENTEXAM_RECEPT_DAT ";
    	$query .= "	 where ENTEXAMYEAR = '".$model->ObjYear."' and TESTDIV = '4'), ";

    	$query .= "	test6(EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN) as ";
    	$query .= "	(select EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN ";
    	$query .= "	 from ENTEXAM_RECEPT_DAT ";
    	$query .= "	 where ENTEXAMYEAR = '".$model->ObjYear."' and TESTDIV = '5'), ";

    	$query .= "	test7(EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN) as ";
    	$query .= "	(select EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN ";
    	$query .= "	 from ENTEXAM_RECEPT_DAT ";
    	$query .= "	 where ENTEXAMYEAR = '".$model->ObjYear."' and TESTDIV = '6'), ";

    	$query .= "	test0(EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN) as ";
    	$query .= "	(select EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,JUDGECLASS,KATEN ";
    	$query .= "	 from ENTEXAM_RECEPT_DAT ";
    	$query .= "	 where ENTEXAMYEAR = '".$model->ObjYear."' and TESTDIV = '0'), ";

    	$query .= "	test4(EXAMNO,TOTAL_AVG,TOTAL_RANK) as ";
    	$query .= "	 (SELECT EXAMNO, DECIMAL(ROUND(AVG(FLOAT(".$field."))*10,0)/10,4,1) AS TOTAL_AVG, ";
    	$query .= "	    RANK() OVER(ORDER BY (value(ROUND(AVG(FLOAT(".$field."))*10,0)/10,-1)) DESC) AS TOTAL_RANK ";
    	$query .= "	FROM   ENTEXAM_RECEPT_DAT W1 ";
    	$query .= "	WHERE  ENTEXAMYEAR='".$model->ObjYear."' AND ";
    	$query .= "	APPLICANTDIV='1' AND ";
    	$query .= "	NOT EXISTS (SELECT 'X' ";
    	$query .= "	            FROM   ENTEXAM_SCORE_DAT W2 ";
    	$query .= "	            WHERE  W2.ENTEXAMYEAR=W1.ENTEXAMYEAR AND ";
    	$query .= "	                   W2.APPLICANTDIV=W1.APPLICANTDIV AND ";
    	$query .= "	                   W2.TESTDIV=W1.TESTDIV AND ";
    	$query .= "	                   W2.EXAM_TYPE=W1.EXAM_TYPE AND ";
    	$query .= "	                   W2.RECEPTNO=W1.RECEPTNO AND ";
    	$query .= "	                   W2.TESTSUBCLASSCD in ('1','2') AND ";
    	$query .= "	                   W2.ATTEND_FLG='0' ) ";
    	$query .= "	GROUP BY EXAMNO) ";

    	$query .= "	, T_PASSINGMARK AS ( ";
    	$query .= "	    SELECT DISTINCT ";
    	$query .= "	        ENTEXAMYEAR, ";
    	$query .= "	        TESTDIV ";
    	$query .= "	    FROM ";
    	$query .= "	        ENTEXAM_PASSINGMARK_MST ";
    	$query .= "	    WHERE ";
    	$query .= "	        ENTEXAMYEAR='".$model->ObjYear."' ";
    	$query .= "	) ";

    	$query .= "	SELECT ";
    	$query .= "	    w1.EXAMNO, ";
    	$query .= "	    w1.NAME, ";
    	$query .= "	    w1.NAME_KANA, ";
    	$query .= "	    Meisyou_Get(w1.SEX,'Z002',2) SEX, ";
        $query .= "	    w1.FS_AREA_CD, ";
    	$query .= "	    w1.FS_NAME, ";
    	$query .= "	    case when w0.HONORDIV ='1' then '☆' when w0.JUDGECLASS ='3' then '◎' when w0.JUDGECLASS ='4' then '○' when w0.JUDGECLASS ='6' then '○' when w0.JUDGEDIV = '1' and w0.JUDGECLASS = '2' then '◎' when w0.JUDGEDIV = '1' and w0.JUDGECLASS = '1' then '○' when d0.APPLICANTDIV = '1' and d0.EXAMINEE_DIV = '2' then '▲' when w0.TOTAL2 is null and w0.TOTAL4 is null then '' when p0.TESTDIV is null then '＊' else '×' end GOUHI0, ";
    	$query .= "	    w0.TOTAL4 FOUR0, ";
//    	$query .= "	    case when w2.HONORDIV ='1' then '☆' when w2.JUDGEDIV = '1' then '◎' else '×' end GOUHI1, ";
//    	$query .= "	    case when w2.HONORDIV ='1' then '☆' when w2.JUDGEDIV = '1' then '◎' when w2.TOTAL2 is null and w2.TOTAL4 is null then '' else '×' end GOUHI1, ";//NO002
    	$query .= "	    case when w2.HONORDIV ='1' then '☆' when w2.JUDGECLASS ='3' then '◎' when w2.JUDGECLASS ='4' then '○' when w2.JUDGECLASS ='6' then '○' when w2.JUDGEDIV = '1' and w2.JUDGECLASS = '2' then '◎' when w2.JUDGEDIV = '1' and w2.JUDGECLASS = '1' then '○' when w6.APPLICANTDIV = '1' and w6.EXAMINEE_DIV = '2' then '▲' when w2.TOTAL2 is null and w2.TOTAL4 is null then '' when p1.TESTDIV is null then '＊' else '×' end GOUHI1, ";//NO003
    	$query .= "	    w2.TOTAL4 FOUR1, ";
//    	$query .= "	    case when w3.HONORDIV ='1' then '☆' when w3.JUDGEDIV = '1' then '◎' else '×' end GOUHI2, ";
//    	$query .= "	    case when w3.HONORDIV ='1' then '☆' when w3.JUDGEDIV = '1' then '◎' when w3.TOTAL2 is null and w3.TOTAL4 is null then '' else '×' end GOUHI2, ";//NO002
    	$query .= "	    case when w3.HONORDIV ='1' then '☆' when w3.JUDGECLASS ='3' then '◎' when w3.JUDGECLASS ='4' then '○' when w3.JUDGECLASS ='6' then '○' when w3.JUDGEDIV = '1' and w3.JUDGECLASS = '2' then '◎' when w3.JUDGEDIV = '1' and w3.JUDGECLASS = '1' then '○' when w7.APPLICANTDIV = '1' and w7.EXAMINEE_DIV = '2' then '▲' when w3.TOTAL2 is null and w3.TOTAL4 is null then '' when p2.TESTDIV is null then '＊' else '×' end GOUHI2, ";//NO003
    	$query .= "	    w3.TOTAL4 FOUR2, ";
//    	$query .= "	    case when w4.HONORDIV ='1' then '☆' when w4.JUDGEDIV = '1' then '◎' else '×' end GOUHI3, ";
//    	$query .= "	    case when w4.HONORDIV ='1' then '☆' when w4.JUDGEDIV = '1' then '◎' when w4.TOTAL2 is null and w4.TOTAL4 is null then '' else '×' end GOUHI3, ";//NO002
    	$query .= "	    case when w4.HONORDIV ='1' then '☆' when w4.JUDGECLASS ='3' then '◎' when w4.JUDGECLASS ='4' then '○' when w4.JUDGECLASS ='6' then '○' when w4.JUDGEDIV = '1' and w4.JUDGECLASS = '2' then '◎' when w4.JUDGEDIV = '1' and w4.JUDGECLASS = '1' then '○' when w8.APPLICANTDIV = '1' and w8.EXAMINEE_DIV = '2' then '▲' when w4.TOTAL2 is null and w4.TOTAL4 is null then '' when p3.TESTDIV is null then '＊' else '×' end GOUHI3, ";//NO003
    	$query .= "	    w4.TOTAL4 FOUR3, ";
    	$query .= "	    case when w9.HONORDIV ='1' then '☆' when w9.JUDGECLASS ='3' then '◎' when w9.JUDGECLASS ='4' then '○' when w9.JUDGECLASS ='6' then '○' when w9.JUDGEDIV = '1' and w9.JUDGECLASS = '2' then '◎' when w9.JUDGEDIV = '1' and w9.JUDGECLASS = '1' then '○' when d4.APPLICANTDIV = '1' and d4.EXAMINEE_DIV = '2' then '▲' when w9.TOTAL2 is null and w9.TOTAL4 is null then '' when p4.TESTDIV is null then '＊' else '×' end GOUHI4, ";
    	$query .= "	    w9.TOTAL4 FOUR4, ";
    	$query .= "	    case when w10.HONORDIV ='1' then '☆' when w10.JUDGECLASS ='3' then '◎' when w10.JUDGECLASS ='4' then '○' when w10.JUDGECLASS ='6' then '○' when w10.JUDGEDIV = '1' and w10.JUDGECLASS = '2' then '◎' when w10.JUDGEDIV = '1' and w10.JUDGECLASS = '1' then '○' when d5.APPLICANTDIV = '1' and d5.EXAMINEE_DIV = '2' then '▲' when w10.TOTAL2 is null and w10.TOTAL4 is null then '' when p5.TESTDIV is null then '＊' else '×' end GOUHI5, ";
    	$query .= "	    w10.TOTAL4 FOUR5, ";
    	$query .= "	    case when w11.HONORDIV ='1' then '☆' when w11.JUDGECLASS ='3' then '◎' when w11.JUDGECLASS ='4' then '○' when w11.JUDGECLASS ='6' then '○' when w11.JUDGEDIV = '1' and w11.JUDGECLASS = '2' then '◎' when w11.JUDGEDIV = '1' and w11.JUDGECLASS = '1' then '○' when d6.APPLICANTDIV = '1' and d6.EXAMINEE_DIV = '2' then '▲' when w11.TOTAL2 is null and w11.TOTAL4 is null then '' when p6.TESTDIV is null then '＊' else '×' end GOUHI6, ";
    	$query .= "	    w11.TOTAL4 FOUR6, ";
    	$query .= "	    w5.TOTAL_AVG REC_AVG, ";
    	$query .= "	    RANK() OVER(ORDER BY (value(w5.TOTAL_AVG,-1)) DESC) AS TOTAL_DESC, ";
    	$query .= "	    case when w0.EXAMNO is not null then '○' when w2.EXAMNO is not null then '○' when w3.EXAMNO is not null then '○' when w4.EXAMNO is not null then '○' when w9.EXAMNO is not null then '○' when w10.EXAMNO is not null then '○' when w11.EXAMNO is not null then '○' else '▲' end RECEPT, ";
    	$query .= "	    case when w0.KATEN is not null then w0.KATEN when w2.KATEN is not null then w2.KATEN when w3.KATEN is not null then w3.KATEN when w4.KATEN is not null then w4.KATEN when w9.KATEN is not null then w9.KATEN when w10.KATEN is not null then w10.KATEN when w11.KATEN is not null then w11.KATEN end KATEN, ";
//    	$query .= "	    case when w1.HONORDIV ='1' then '☆' when w1.JUDGEMENT = '1' then '◎' else '×' end GOUHI, ";
    	$query .= "	    case when w0.HONORDIV ='1' or w2.HONORDIV ='1' or w3.HONORDIV ='1' or w4.HONORDIV ='1' or w9.HONORDIV ='1' or w10.HONORDIV ='1' or w11.HONORDIV ='1' then '☆' when w1.JUDGEMENT = '1' and w1.ENTCLASS = '2' then '◎' when w1.JUDGEMENT = '1' and w1.ENTCLASS = '1' then '○' when w0.EXAMNO is null and w2.EXAMNO is null and w3.EXAMNO is null and w4.EXAMNO is null and w9.EXAMNO is null and w10.EXAMNO is null and w11.EXAMNO is null then '' else '×' end GOUHI, ";//NO002
    	$query .= "	    case when w0.ADJOURNMENTDIV = '1' then '○' when w2.ADJOURNMENTDIV = '1' then '○' when w3.ADJOURNMENTDIV = '1' then '○' when w4.ADJOURNMENTDIV = '1' then '○' when w9.ADJOURNMENTDIV = '1' then '○' when w10.ADJOURNMENTDIV = '1' then '○' when w11.ADJOURNMENTDIV = '1' then '○' else '' end ADJOUR, ";
    	$query .= "	    case w1.SPECIAL_MEASURES when '3' then '○' else '' end SPECIAL_MEASURES3, ";
    	$query .= "	    case w1.PROCEDUREDIV when '1' then '○' else '' end PROCEDUREDIV, ";
    	$query .= "	    case w1.ENTDIV when '1' then '○' else '' end ENTDIV, ";
    	$query .= "	    w1.REMARK1, ";
    	$query .= "	    CASE WHEN W1.ENTDIV = '2' THEN '辞退' ";
    	$query .= "	         WHEN VALUE(W11.HONORDIV, VALUE(W10.HONORDIV, VALUE(W9.HONORDIV, VALUE(W4.HONORDIV, VALUE(W3.HONORDIV, VALUE(W2.HONORDIV, W0.HONORDIV)))))) = '1' THEN '特待' ";
    	$query .= "	         WHEN W1.SPECIAL_MEASURES = '1' THEN '繰上' ";
    	$query .= "	         WHEN W1.SPECIAL_MEASURES = '2' THEN '特別' END AS REMARK2 ";//NO001
    	$query .= "	FROM ";
    	$query .= "	    ENTEXAM_APPLICANTBASE_DAT w1 ";
    	$query .= "	    left join test0 w0 on w1.examno = w0.examno ";
    	$query .= "	    left join test1 w2 on w1.examno = w2.examno ";
    	$query .= "	    left join test2 w3 on w1.examno = w3.examno ";
    	$query .= "	    left join test3 w4 on w1.examno = w4.examno ";
    	$query .= "	    left join test4 w5 on w1.examno = w5.examno ";
    	$query .= "	    left join test5 w9 on w1.examno = w9.examno ";
    	$query .= "	    left join test6 w10 on w1.examno = w10.examno ";
    	$query .= "	    left join test7 w11 on w1.examno = w11.examno ";
//NO003↓
		$query .= "    LEFT JOIN ENTEXAM_DESIRE_DAT d0 ON d0.ENTEXAMYEAR = w1.ENTEXAMYEAR ";
		$query .= "        AND d0.APPLICANTDIV = '1' ";
		$query .= "        AND d0.TESTDIV = '0' ";
		$query .= "        AND d0.examno = w1.examno ";
		$query .= "    LEFT JOIN ENTEXAM_DESIRE_DAT w6 ON w6.ENTEXAMYEAR = w1.ENTEXAMYEAR ";
		$query .= "        AND w6.APPLICANTDIV = '1' ";
		$query .= "        AND w6.TESTDIV = '1' ";
		$query .= "        AND w6.examno = w1.examno ";
		$query .= "    LEFT JOIN ENTEXAM_DESIRE_DAT w7 ON w7.ENTEXAMYEAR = w1.ENTEXAMYEAR ";
		$query .= "        AND w7.APPLICANTDIV = '1' ";
		$query .= "        AND w7.TESTDIV = '2' ";
		$query .= "        AND w7.examno = w1.examno ";
		$query .= "    LEFT JOIN ENTEXAM_DESIRE_DAT w8 ON w8.ENTEXAMYEAR = w1.ENTEXAMYEAR ";
		$query .= "        AND w8.APPLICANTDIV = '1' ";
		$query .= "        AND w8.TESTDIV = '3' ";
		$query .= "        AND w8.examno = w1.examno ";
		$query .= "    LEFT JOIN ENTEXAM_DESIRE_DAT d4 ON d4.ENTEXAMYEAR = w1.ENTEXAMYEAR ";
		$query .= "        AND d4.APPLICANTDIV = '1' ";
		$query .= "        AND d4.TESTDIV = '4' ";
		$query .= "        AND d4.examno = w1.examno ";
		$query .= "    LEFT JOIN ENTEXAM_DESIRE_DAT d5 ON d5.ENTEXAMYEAR = w1.ENTEXAMYEAR ";
		$query .= "        AND d5.APPLICANTDIV = '1' ";
		$query .= "        AND d5.TESTDIV = '5' ";
		$query .= "        AND d5.examno = w1.examno ";
		$query .= "    LEFT JOIN ENTEXAM_DESIRE_DAT d6 ON d6.ENTEXAMYEAR = w1.ENTEXAMYEAR ";
		$query .= "        AND d6.APPLICANTDIV = '1' ";
		$query .= "        AND d6.TESTDIV = '6' ";
		$query .= "        AND d6.examno = w1.examno ";
//NO003↑
		$query .= "    LEFT JOIN T_PASSINGMARK p0 ON p0.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p0.TESTDIV = '0' ";
		$query .= "    LEFT JOIN T_PASSINGMARK p1 ON p1.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p1.TESTDIV = '1' ";
		$query .= "    LEFT JOIN T_PASSINGMARK p2 ON p2.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p2.TESTDIV = '2' ";
		$query .= "    LEFT JOIN T_PASSINGMARK p3 ON p3.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p3.TESTDIV = '3' ";
		$query .= "    LEFT JOIN T_PASSINGMARK p4 ON p4.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p4.TESTDIV = '4' ";
		$query .= "    LEFT JOIN T_PASSINGMARK p5 ON p5.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p5.TESTDIV = '5' ";
		$query .= "    LEFT JOIN T_PASSINGMARK p6 ON p6.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p6.TESTDIV = '6' ";

    	$query .= "	WHERE w1.ENTEXAMYEAR = '".$model->ObjYear."' ";
		if ($model->field["OUTPUT"] == 1){
			$query .= "	ORDER BY w1.examno ";
		}else {
			$query .= "	ORDER BY TOTAL_DESC ";
		}
		return $query;
	}
}
?>
