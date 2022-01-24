<?php

require_once('for_php7.php');

class knjl400qQuery extends Query {
    
    //試験日取得
    function getExamDate()
    {
        $query  = " SELECT ";
        $query .= "     EXAM_DATE ";
        $query .= " FROM ";
        $query .= "     SAT_INFO_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    
    //CSVデータ取得
    function getExportData($check, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT  ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         t1.SAT_NO, ";
        $query .= "         t1.NAME1, ";
        $query .= "         t1.KANA1 || ' ' || t1.KANA2 as NAME_KANA, ";
        $query .= "         t1.KANA1, ";
        $query .= "         t1.KANA2, ";
        $query .= "         t1.SEX as SEXCD, ";
        $query .= "         t2.NAME2 as SEX, ";
        $query .= "         RIGHT(t1.SCHOOLCD, 5) as SCHOOLCD, ";
        $query .= "         t3.FINSCHOOL_NAME_ABBV, ";
        $query .= "         RIGHT(replace(t1.BIRTHDAY, '-', ''), 6) as BIRTHDAY, ";
        $query .= "         t1.GRADUATION, ";
        $query .= "         t4.NAME1 as GRADE, ";
        $query .= "         a1.SCORE_ENGLISH, ";
        $query .= "         a1.SCORE_MATH, ";
        $query .= "         a1.SCORE_JAPANESE, ";
        $query .= "         a1.ABSENCE, ";
        $query .= "         a1.ABSENCE_ENGLISH, ";
        $query .= "         a1.ABSENCE_MATH, ";
        $query .= "         a1.ABSENCE_JAPANESE, ";
        $query .= "         a1.SCORE_TOTAL, ";
        $query .= "         a1.AVERAGE, ";
        $query .= "         a1.JUDGE_SAT, ";
        $query .= "         a2.NAME1 as JUDGE, ";
        $query .= "         a1.DEVIATION, ";
        $query .= "         a1.AREA_RANK_ENGLISH, ";
        $query .= "         a1.AREA_RANK_MATH, ";
        $query .= "         a1.AREA_RANK_JAPANESE, ";
        $query .= "         a1.AREA_RANK_TOTAL, ";
        $query .= "         a1.ALL_RANK_ENGLISH, ";
        $query .= "         a1.ALL_RANK_MATH, ";
        $query .= "         a1.ALL_RANK_JAPANESE, ";
        $query .= "         a1.ALL_RANK_TOTAL, ";
        $query .= "         t1.HOPECD1, ";
        $query .= "         t5.FINSCHOOL_NAME_ABBV as HOPE1, ";
        $query .= "         t1.HOPECD2, ";
        $query .= "         t6.FINSCHOOL_NAME_ABBV as HOPE2, ";
        $query .= "         t1.HOPECD3, ";
        $query .= "         t7.FINSCHOOL_NAME_ABBV as HOPE3, ";
        $query .= "         t1.MOCK_AUG_NO, ";
        $query .= "         t1.MOCK_AUG_DEV, ";
        $query .= "         t1.MOCK_SEP_NO, ";
        $query .= "         t1.MOCK_SEP_DEV, ";
        $query .= "         t1.PLACECD, ";
        $query .= "         t8.PLACEAREA, ";
        $query .= "         t8.PLACENAME_SHORT, ";
        $query .= "         t1.INOUT_KUBUN, ";
        $query .= "         t9.NAME1 as INOUT, ";
        $query .= "         t1.SEND_KUBUN, ";
        $query .= "         t10.NAME1 as SEND, ";
        $query .= "         t1.IND_KUBUN, ";
        $query .= "         t11.NAME1 as IND, ";
        $query .= "         t1.INSIDERNO, ";
        $query .= "         t1.ABSENCE as ABSENCE2, ";
        $query .= "         t1.GROUPCD, ";
        $query .= "         t12.GROUPNAME, ";
        $query .= "         t1.ZIPCODE, ";
        $query .= "         t1.ADDR1, ";
        $query .= "         t1.ADDR2, ";
        $query .= "         t1.TELNO1, ";
        $query .= "         t1.TELNO2 ";
        $query .= "     FROM ";
        $query .= "         SAT_APP_FORM_MST t1 ";
        $query .= "         left join NAME_MST t2 on t1.SEX = t2.NAMECD2 and t2.NAMECD1 = 'Z002' ";
        $query .= "         left join FINSCHOOL_MST t3 on t1.SCHOOLCD = t3.FINSCHOOLCD and t3.FINSCHOOL_TYPE = '3' ";
        $query .= "         left join NAME_MST t4 on t1.GRADUATION = t4.NAMECD2 and t4.NAMECD1 = 'L205' ";
        $query .= "         left join FINSCHOOL_MST t5 on RIGHT('00' || t1.HOPECD1, 5) = RIGHT(t5.FINSCHOOLCD,5) and t5.FINSCHOOL_TYPE = '4' ";
        $query .= "         left join FINSCHOOL_MST t6 on RIGHT('00' || t1.HOPECD2, 5) = RIGHT(t6.FINSCHOOLCD,5) and t6.FINSCHOOL_TYPE = '4' ";
        $query .= "         left join FINSCHOOL_MST t7 on RIGHT('00' || t1.HOPECD3, 5) = RIGHT(t7.FINSCHOOLCD,5) and t7.FINSCHOOL_TYPE = '4' ";
        $query .= "         left join SAT_EXAM_PLACE_DAT t8 on t1.PLACECD = t8.PLACECD and t1.YEAR = t8.YEAR ";
        $query .= "         left join NAME_MST t9 on t1.INOUT_KUBUN = t9.NAMECD2 and t9.NAMECD1 = 'L201' ";
        $query .= "         left join NAME_MST t10 on t1.SEND_KUBUN = t10.NAMECD2 and t10.NAMECD1 = 'L203' ";
        $query .= "         left join NAME_MST t11 on t1.IND_KUBUN = t11.NAMECD2 and t11.NAMECD1 = 'L202' ";
        $query .= "         left join SAT_GROUP_DAT t12 on t1.GROUPCD = t12.GROUPCD and t1.YEAR = t12.YEAR ";
        $query .= "      ";
        $query .= "         left join SAT_EXAM_DAT a1 on t1.YEAR = a1.YEAR and t1.SAT_NO = a1.SAT_NO ";
        $query .= "         left join NAME_MST a2 on a1.JUDGE_SAT = a2.NAMECD2 and a2.NAMECD1 = 'L200' ";
        $query .= "     WHERE ";
        $query .= "         t1.YEAR = '".CTRL_YEAR."' ";
        if(!empty($check) && in_array("0", $check)){
            $query .= "     AND ";
            $query .= "         t1.ABSENCE != 0 ";
        }
        if(!empty($check) && in_array("1", $check)){
            $query .= "     AND ";
            $query .= "         t1.INOUT_KUBUN != 4 ";
        }
        if(!empty($check) && in_array("2", $check)){
            $query .= "     AND  ";
            $query .= "         t1.IND_KUBUN != 1 ";
        }
        if(!empty($check) && in_array("3", $check)){
            $query .= "     AND ";
            $query .= "         t1.IND_KUBUN != 2 ";
        }
        if(!empty($check) && in_array("4", $check)){
            $query .= "     AND ";
            $query .= "         t1.IND_KUBUN != 3 ";
        }
        $query .= "     ORDER BY ";
        $query .= "         t1.SAT_NO ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
}
?>
