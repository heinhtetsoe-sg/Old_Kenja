<?php

require_once('for_php7.php');

class knjl376qQuery extends Query {
    
    function getData($radio, $mode="")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "    COUNT(t1.SAT_NO) ";
        }else{
            $query .= "    t1.SAT_NO, ";
            $query .= "    t2.NAME1, ";
            $query .= "    t1.SCORE_ENGLISH, ";
            $query .= "    t1.SCORE_ENGLISH2, ";
            $query .= "    t1.ABSENCE_ENGLISH, ";
            $query .= "    t1.SCORE_MATH, ";
            $query .= "    t1.SCORE_MATH2, ";
            $query .= "    t1.ABSENCE_MATH, ";
            $query .= "    t1.SCORE_JAPANESE, ";
            $query .= "    t1.SCORE_JAPANESE2, ";
            $query .= "    t1.ABSENCE_JAPANESE ";
        }

        $query .= " FROM ";
        $query .= "    SAT_EXAM_DAT t1 left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR ";
        $query .= " WHERE ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";

        if($radio == "2"){
            $query .= " AND ";
            $query .= " ( ";
            $query .= "    t1.SCORE_ENGLISH != t1.SCORE_ENGLISH2 ";
            $query .= " OR ";
            $query .= "    t1.SCORE_MATH != t1.SCORE_MATH2 ";
            $query .= " OR ";
            $query .= "    t1.SCORE_JAPANESE != t1.SCORE_JAPANESE2 ";
            $query .= " OR ";
            $query .= "    t1.ABSENCE_ENGLISH != 1 AND (t1.SCORE_ENGLISH IS NOT NULL OR t1.SCORE_ENGLISH2 IS NOT NULL) ";
            $query .= " OR";
            $query .= "    t1.ABSENCE_MATH != 1 AND (t1.SCORE_MATH IS NOT NULL OR t1.SCORE_MATH2 IS NOT NULL) ";
            $query .= " OR ";
            $query .= "    t1.ABSENCE_JAPANESE != 1 AND (t1.SCORE_JAPANESE IS NOT NULL OR t1.SCORE_JAPANESE2 IS NOT NULL) ";
            $query .= " ) ";
        }else if($radio == "3"){
            $query .= " AND ";
            $query .= " ( ";
            $query .= "    t1.ABSENCE_ENGLISH != 0 AND (t1.SCORE_ENGLISH IS NULL OR t1.SCORE_ENGLISH2 IS NULL) ";
            $query .= " OR ";
            $query .= "    t1.ABSENCE_MATH != 0 AND (t1.SCORE_MATH IS NULL OR t1.SCORE_MATH2 IS NULL) ";
            $query .= " OR ";
            $query .= "    t1.ABSENCE_JAPANESE != 0 AND (t1.SCORE_JAPANESE IS NULL OR t1.SCORE_JAPANESE2 IS NULL) ";
            $query .= " ) ";
        }
        if($mode == ""){
            $query .= " ORDER BY ";
            $query .= "    t1.SAT_NO ";
        }
        
        return $query;
    }
}
?>
