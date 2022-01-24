<?php

require_once('for_php7.php');

class knjl371qQuery extends Query {
    
    //受験者数取得
    function getCnt()
    {
        $query  = " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.AUG_CNT, ";
        $query .= "     t2.SEP_CNT, ";
        $query .= "     t3.SAT_CNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         COUNT(MOCK_AUG_NO) as AUG_CNT ";
        $query .= "     FROM ";
        $query .= "         SAT_APP_FORM_MST ";
        $query .= "     WHERE ";
        $query .= "         MOCK_AUG_NO IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR ";
        $query .= "     ) t1  ";
        $query .= "     left join  (SELECT ";
        $query .= "                     YEAR, ";
        $query .= "                     COUNT(MOCK_SEP_NO) as SEP_CNT ";
        $query .= "                 FROM ";
        $query .= "                     SAT_APP_FORM_MST ";
        $query .= "                 WHERE ";
        $query .= "                     MOCK_SEP_NO IS NOT NULL ";
        $query .= "                 GROUP BY ";
        $query .= "                     YEAR ";
        $query .= "                 ) t2 on t1.YEAR = t2.YEAR  ";
        $query .= "     left join  (SELECT ";
        $query .= "                     YEAR, ";
        $query .= "                     COUNT(SAT_NO) as SAT_CNT ";
        $query .= "                 FROM ";
        $query .= "                     SAT_APP_FORM_MST ";
        $query .= "                 GROUP BY ";
        $query .= "                     YEAR ";
        $query .= "                 ) t3 on t1.YEAR = t3.YEAR ";
        $query .= " WHERE  ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //取込時エラーチェック用
    function checkSatNo($satno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "     SAT_NO = '".$satno."' ";
        $query .= " AND ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //CSV出力
    function getExportData($mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         t1.SAT_NO, ";
        $query .= "         t1.NAME1, ";
        $query .= "         t1.NAME2, ";
        $query .= "         t1.KANA1 || ' ' || t1.KANA2 as NAME_KANA, ";
        $query .= "         t1.SEX as SEXCD, ";
        $query .= "         t2.NAME2 as SEX, ";
        $query .= "         RIGHT(t1.SCHOOLCD, 5) as SCHOOLCD, ";
        $query .= "         t3.FINSCHOOL_NAME_ABBV, ";
        $query .= "         RIGHT(REPLACE(t1.BIRTHDAY, '-', ''), 6) as BIRTHDAY, ";
        $query .= "         t1.TELNO1, ";
        $query .= "         t1.PREFCD, ";
        $query .= "         t4.PREF_NAME ";
        $query .= "     FROM ";
        $query .= "         SAT_APP_FORM_MST t1 ";
        $query .= "         left join NAME_MST t2 on t1.SEX = t2.NAMECD2 and t2.NAMECD1 = 'Z002' ";
        $query .= "         left join FINSCHOOL_MST t3 on t1.SCHOOLCD = t3.FINSCHOOLCD and t3.FINSCHOOL_TYPE = '3' ";
        $query .= "         left join PREF_MST t4 on t1.PREFCD = t4.PREF_CD ";
        $query .= "     WHERE ";
        $query .= "         t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     ORDER BY ";
        $query .= "         t1.SAT_NO ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
}
?>
