<?php

require_once('for_php7.php');

class knjl110kquery extends Query {

    //試験区分
    function getTestdiv($year)
    {
        return "SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = 'L003'";
    }

    //試験区分名称
    function getTestdivName($model)
    {
        $query  = " SELECT name1 FROM v_name_mst ";
        $query .= "  WHERE year     = '".$model->examyear."' ";
        $query .= "    AND namecd1  = 'L003' ";
        $query .= "    AND namecd2  = '".$model->testdiv."' ";
        return $query;
    }

    //実行
    function selectCvsQuery($model,$jhflg)
    {
        // 2005.09.01 minei
        //高校用
        if($jhflg == 0){
            $query  = " SELECT ";
            $query .= "     T1.examno, ";
            $query .= "     CASE WHEN T1.judgement IN ('1','2','3','4','5','6','9') THEN 1 END AS pass, ";
            $query .= "     T2.examcourse_abbv ";
            $query .= " FROM ";
            $query .= "     entexam_applicantbase_dat T1 ";
            $query .= " LEFT OUTER JOIN entexam_course_mst T2 ";
            $query .= "              ON T2.entexamyear  = T1.entexamyear ";
            $query .= "             AND T2.coursecd     = T1.suc_coursecd ";
            $query .= "             AND T2.majorcd      = T1.suc_majorcd ";
            $query .= "             AND T2.examcoursecd = T1.suc_coursecode ";
            $query .= " WHERE ";
            $query .= "     T1.entexamyear  = '".$model->examyear."' AND ";
            $query .= "     T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
            $query .= "     T1.testdiv      = '1' AND ";	//2006.01.11 alp m-yama
            $query .= "     T1.applicantdiv NOT IN('1','2')";     // 1:附属推薦、2:中高一貫は除く 2005.08.16 minei
            $query .= " ORDER BY ";			//2006.02.12 alp m-yama
            $query .= "     T1.examno ";	//2006.02.12 alp m-yama

        //中学用
        }else{
            #出力データを13項目毎に、左右に振分     2005/08/18 
            $query  = " WITH SELECT01 AS( ";
            $query .= "     SELECT ";
            $query .= "        row_number() over()         AS RECORD_CNT01, ";
            $query .= "        MOD((CASE WHEN MOD(row_number() over(),13)=0 THEN (row_number() over()/13)-1 ELSE (row_number() over()/13) END),2) AS REMAINDER01, ";
            $query .= "        T1.examno,  ";
//            $query .= "        T2.examcourse_abbv  ";
            $query .= "        CASE WHEN value(T2.examcourse_abbv,'') = '標' THEN '' ELSE value(T2.examcourse_abbv,'') END AS EXAMCOURSE_ABBV ";	//2006.01.11 alp m-yama
            $query .= "    FROM  ";
            $query .= "        entexam_applicantbase_dat T1  ";
            $query .= "    LEFT OUTER JOIN entexam_course_mst T2  ";
            $query .= "                 ON T2.entexamyear  = T1.entexamyear  ";
            $query .= "                AND T2.coursecd     = T1.suc_coursecd  ";
            $query .= "                AND T2.majorcd      = T1.suc_majorcd  ";
            $query .= "                AND T2.examcoursecd = T1.suc_coursecode  ";
            $query .= "    WHERE  ";
            $query .= "     T1.entexamyear  = '".$model->examyear."' AND ";//2006.01.11 alp o-naka
            $query .= "     T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
//            $query .= "        T1.entexamyear  = '2005' AND  ";
//            $query .= "        T1.testdiv      = '1' AND  ";
            $query .= "        T1.testdiv      = '".$model->testdiv."' AND ";	//2006.01.11 alp m-yama
            $query .= "        T1.judgement IN ('1','2','3','4','5','6','9') AND ";
            $query .= "        T1.applicantdiv NOT IN('1','2') ";
            $query .= "    ORDER BY T1.examno ";
            $query .= "),SELECT_LEFT AS( ";
            $query .= "    SELECT ";
            $query .= "        RECORD_CNT01, ";
            $query .= "        row_number() over()         AS RECORD_CNT02, ";
            $query .= "        REMAINDER01, ";
            $query .= "        examno,  ";
            $query .= "        examcourse_abbv  ";
            $query .= "    FROM  ";
            $query .= "        SELECT01  ";
            $query .= "    WHERE REMAINDER01=0 ";
            $query .= "    ORDER BY examno ";
            $query .= "),SELECT_RIGHT AS( ";
            $query .= "    SELECT ";
            $query .= "        RECORD_CNT01, ";
            $query .= "        row_number() over()         AS RECORD_CNT02, ";
            $query .= "        REMAINDER01, ";
            $query .= "        examno,  ";
            $query .= "        examcourse_abbv  ";
            $query .= "    FROM  ";
            $query .= "        SELECT01  ";
            $query .= "    WHERE REMAINDER01=1 ";
            $query .= "    ORDER BY examno ";
            $query .= ") ";
            $query .= "SELECT ";
            $query .= "    L.RECORD_CNT01       AS L_RECORD_CNT01, ";
            $query .= "    L.RECORD_CNT02       AS L_RECORD_CNT02, ";
            $query .= "    L.REMAINDER01        AS L_REMAINDER01, ";
            $query .= "    L.examno             AS L_examno, ";
            $query .= "    L.examcourse_abbv    AS L_examcourse_abbv, ";
            $query .= "    R.RECORD_CNT01       AS R_RECORD_CNT01, ";
            $query .= "    R.RECORD_CNT02       AS R_RECORD_CNT02, ";
            $query .= "    R.REMAINDER01        AS R_REMAINDER01, ";
            $query .= "    R.examno             AS R_examno, ";
            $query .= "    R.examcourse_abbv    AS R_examcourse_abbv ";
            $query .= "FROM ";
            $query .= "    SELECT_LEFT     AS L ";
            $query .= "    LEFT OUTER JOIN SELECT_RIGHT AS R ON L.RECORD_CNT02=R.RECORD_CNT02";
            $query .= "    ORDER BY L_examno ";
        }

/* 2005.09.01 ↓commented out by minei 


        #出力データを13項目毎に、左右に振分     2005/08/18 
        $query  = " WITH SELECT01 AS( ";
        $query .= "     SELECT ";
        $query .= "        row_number() over()         AS RECORD_CNT01, ";
        $query .= "        MOD((CASE WHEN MOD(row_number() over(),13)=0 THEN (row_number() over()/13)-1 ELSE (row_number() over()/13) END),2) AS REMAINDER01, ";
        $query .= "        T1.examno,  ";
        $query .= "        T2.examcourse_abbv  ";
        $query .= "    FROM  ";
        $query .= "        entexam_applicantbase_dat T1  ";
        $query .= "    LEFT OUTER JOIN entexam_course_mst T2  ";
        $query .= "                 ON T2.entexamyear  = T1.entexamyear  ";
        $query .= "                AND T2.coursecd     = T1.suc_coursecd  ";
        $query .= "                AND T2.majorcd      = T1.suc_majorcd  ";
        $query .= "                AND T2.examcoursecd = T1.suc_coursecode  ";
        $query .= "    WHERE  ";
        $query .= "        T1.entexamyear  = '2005' AND  ";
        $query .= "        T1.testdiv      = '1' AND  ";
        $query .= "        T1.judgement IN ('1','2','3','4','5','6') AND ";
        $query .= "        T1.applicantdiv NOT IN('1','2') ";
        $query .= "    ORDER BY examno ";
        $query .= "),SELECT_LEFT AS( ";
        $query .= "    SELECT ";
        $query .= "        RECORD_CNT01, ";
        $query .= "        row_number() over()         AS RECORD_CNT02, ";
        $query .= "        REMAINDER01, ";
        $query .= "        examno,  ";
        $query .= "        examcourse_abbv  ";
        $query .= "    FROM  ";
        $query .= "        SELECT01  ";
        $query .= "    WHERE REMAINDER01=0 ";
        $query .= "),SELECT_RIGHT AS( ";
        $query .= "    SELECT ";
        $query .= "        RECORD_CNT01, ";
        $query .= "        row_number() over()         AS RECORD_CNT02, ";
        $query .= "        REMAINDER01, ";
        $query .= "        examno,  ";
        $query .= "        examcourse_abbv  ";
        $query .= "    FROM  ";
        $query .= "        SELECT01  ";
        $query .= "    WHERE REMAINDER01=1 ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    L.RECORD_CNT01       AS L_RECORD_CNT01, ";
        $query .= "    L.RECORD_CNT02       AS L_RECORD_CNT02, ";
        $query .= "    L.REMAINDER01        AS L_REMAINDER01, ";
        $query .= "    L.examno             AS L_examno, ";
        $query .= "    L.examcourse_abbv    AS L_examcourse_abbv, ";
        $query .= "    R.RECORD_CNT01       AS R_RECORD_CNT01, ";
        $query .= "    R.RECORD_CNT02       AS R_RECORD_CNT02, ";
        $query .= "    R.REMAINDER01        AS R_REMAINDER01, ";
        $query .= "    R.examno             AS R_examno, ";
        $query .= "    R.examcourse_abbv    AS R_examcourse_abbv ";
        $query .= "FROM ";
        $query .= "    SELECT_LEFT     AS L ";
        $query .= "    LEFT OUTER JOIN SELECT_RIGHT AS R ON L.RECORD_CNT02=R.RECORD_CNT02";


#2005/08/17
#        $query  = " SELECT ";
#        $query .= "     T1.examno, ";
#        $query .= "     CASE WHEN T1.judgement IN ('1','2','3','4','5','6') THEN 1 END AS pass, ";
#        $query .= "     T2.examcourse_abbv ";
#        $query .= " FROM ";
#        $query .= "     entexam_applicantbase_dat T1 ";
#        $query .= " LEFT OUTER JOIN entexam_course_mst T2 ";
#        $query .= "              ON T2.entexamyear  = T1.entexamyear ";
#        $query .= "             AND T2.coursecd     = T1.suc_coursecd ";
#        $query .= "             AND T2.majorcd      = T1.suc_majorcd ";
#        $query .= "             AND T2.examcoursecd = T1.suc_coursecode ";
#        $query .= " WHERE ";
#        $query .= "     T1.entexamyear  = '".$model->examyear."' AND ";
#        $query .= "     T1.testdiv      = '".$model->testdiv."' AND ";
#        $query .= "     T1.applicantdiv NOT IN('1','2')";     // 1:附属推薦、2:中高一貫は除く 2005.08.16 minei
#2005/08/17

*/
        return $query;
    }

}
?>
