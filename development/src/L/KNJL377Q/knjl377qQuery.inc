<?php

require_once('for_php7.php');

class knjl377qQuery extends Query {
    
    //受験者数取得
    function getHopeWrkCnt()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SAT_HOPE_WRK_DAT ";
        
        return $query;
    }
    
    //受験番号重複チェック
    function checkSatno()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SAT_NO, ";
        $query .= "         COUNT(*) as CNT ";
        $query .= "     FROM ";
        $query .= "         SAT_HOPE_WRK_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SAT_NO ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     CNT > 1 ";
        
        return $query;
    }
    //受験番号不一致チェック
    function checkSatConnect()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SAT_HOPE_WRK_DAT t1  ";
        $query .= "     left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t2.YEAR = '".CTRL_YEAR."' ";
        $query .= " WHERE ";
        $query .= "     t2.SAT_NO IS NULL ";
        
        return $query;
    }
    
    //その他エラーチェック
    function checkError($field)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "      SELECT ";
        $query .= "          SAT_NO, ";
        $query .= "          case when BIRTHDAY != APP_BIRTHDAY then '1' else '0' end as BIRTHDAY, ";
        $query .= "          case when SEX != APP_SEX then '1' else '0' end as SEX, ";
        $query .= "          case when FINSCHOOL != APP_FINSCHOOL then '1' else '0' end as FINSCHOOL ";
        $query .= "      FROM ";
        $query .= "          (SELECT ";
        $query .= "              t1.SAT_NO, ";
        $query .= "              t1.BIRTHDAY, ";
        $query .= "              t1.SEX, ";
        $query .= "              '2' || RIGHT('000000' || t1.FINSCHOOL, 6) as FINSCHOOL, ";
        $query .= "              t2.BIRTHDAY as APP_BIRTHDAY, ";
        $query .= "              t2.SEX as APP_SEX, ";
        $query .= "              t2.SCHOOLCD as APP_FINSCHOOL ";
        $query .= "          FROM ";
        $query .= "              SAT_HOPE_WRK_DAT t1  ";
        $query .= "              left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "          ) ";
        $query .= "     ) ";
        $query .= " WHERE  ";
        $query .= "     ".$field." = 1 ";
        
        return $query;
    }
    
    //受験番号重複修正時
    //重複しているSAT_NO取得
    function getDubSatNo()
    {
        $query  = " SELECT ";
        $query .= "     SAT_NO ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SAT_NO, ";
        $query .= "         COUNT(*) as CNT ";
        $query .= "     FROM ";
        $query .= "         SAT_HOPE_WRK_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SAT_NO ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     CNT > 1 ";
        
        return $query;
    }
    //SAT_HOPE_WRK_DAT全データ取得
    function getSatError()
    {
        $query  = " SELECT ";
        $query .= "     t1.RECNO, ";
        $query .= "     t1.SAT_NO, ";
        $query .= "     t1.BIRTHDAY, ";
        $query .= "     t1.SEX, ";
        $query .= "     '2' || RIGHT('000000' || t1.FINSCHOOL, 6) as FINSCHOOLCD, ";
        $query .= "     t2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     SAT_HOPE_WRK_DAT t1  ";
        $query .= "     left join FINSCHOOL_MST t2 on '2' || RIGHT('000000' || t1.FINSCHOOL, 6) = t2.FINSCHOOLCD and t2.FINSCHOOL_TYPE = '3' ";
        $query .= " ORDER BY  ";
        $query .= "     t1.SAT_NO, t1.RECNO ";
        
        return $query;
    }
    
    //受験番号不一致修正時
    //一致しないデータ取得
    function getWrkData()
    {
        $query  = " SELECT ";
        $query .= "     a1.*, ";
        $query .= "     a2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         t1.RECNO, ";
        $query .= "         t1.SAT_NO, ";
        $query .= "         t1.BIRTHDAY, ";
        $query .= "         t1.SEX, ";
        $query .= "         '2' || RIGHT('000000' || t1.FINSCHOOL, 6) as FINSCHOOL ";
        $query .= "     FROM ";
        $query .= "         SAT_HOPE_WRK_DAT t1 ";
        $query .= "         left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     WHERE ";
        $query .= "         t2.SAT_NO IS NULL ";
        $query .= "     ) a1 ";
        $query .= "     left join FINSCHOOL_MST a2 on a1.FINSCHOOL = a2.FINSCHOOLCD and a2.FINSCHOOL_TYPE = '3' ";
        $query .= " ORDER BY ";
        $query .= "     SAT_NO ";
        
        return $query;
    }
    //一致するデータがないSAT_APP_FORM_MST取得
    function getAppData()
    {
        $query  = " SELECT ";
        $query .= "     a1.*, ";
        $query .= "     a2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         t1.SAT_NO, ";
        $query .= "         t1.BIRTHDAY, ";
        $query .= "         t1.SEX, ";
        $query .= "         t1.SCHOOLCD ";
        $query .= "     FROM ";
        $query .= "         SAT_APP_FORM_MST t1  ";
        $query .= "         left join SAT_HOPE_WRK_DAT t2 on t1.SAT_NO = t2.SAT_NO ";
        $query .= "     WHERE ";
        $query .= "         t2.SAT_NO IS NULL  ";
        $query .= "     AND ";
        $query .= "         t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     ) a1 ";
        $query .= "     left join FINSCHOOL_MST a2 on a1.SCHOOLCD = a2.FINSCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "     SAT_NO ";
        
        return $query;
    }
    
    //その他エラーデータ取得
    function getError($field)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         t1.RECNO, ";
        $query .= "         t1.SAT_NO, ";
        $query .= "         t1.".$field.", ";
        $query .= "         t2.BIRTHDAY as APP_BIRTHDAY, ";
        $query .= "         t2.SEX as APP_SEX, ";
        $query .= "         t2.SCHOOLCD as APP_FINSCHOOL, ";
        $query .= "         t3.FINSCHOOL_NAME as APP_FINSCHOOLNAME ";
        $query .= "     FROM ";
        $query .= "         SAT_HOPE_WRK_DAT t1  ";
        $query .= "         left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "         left join FINSCHOOL_MST t3 on t2.SCHOOLCD = t3.FINSCHOOLCD and t3.FINSCHOOL_TYPE = '3' ";
        $query .= "     ) ";
        $query .= " WHERE  ";
        if($field == "FINSCHOOL"){
            $query .= "     RIGHT('00000' || FINSCHOOL, 5) != SUBSTR(APP_FINSCHOOL, 3, 5) ";
        }else{
            $query .= "     ".$field." != APP_".$field." ";
        }
        $query .= " ORDER BY ";
        $query .= "     SAT_NO ";
        
        return $query;
    }
    
    //志望校データ作成
    function getUpdateData()
    {
        $query  = " UPDATE ";
        $query .= "     SAT_APP_FORM_MST U ";
        $query .= " SET ";
        $query .= "     (HOPECD1, HOPECD2, HOPECD3, REGISTERCD, UPDATED) =  (SELECT ";
        $query .= "                                                             HOPE1, ";
        $query .= "                                                             HOPE2, ";
        $query .= "                                                             HOPE3, ";
        $query .= "                                                             '".STAFFCD."', ";
        $query .= "                                                             current timestamp ";
        $query .= "                                                         FROM ";
        $query .= "                                                             (SELECT ";
        $query .= "                                                                 SAT_NO, ";
        $query .= "                                                                 HOPE1, ";
        $query .= "                                                                 HOPE2, ";
        $query .= "                                                                 HOPE3 ";
        $query .= "                                                             FROM ";
        $query .= "                                                                 SAT_HOPE_WRK_DAT ";
        $query .= "                                                             ) as Y ";
        $query .= "                                                         WHERE ";
        $query .= "                                                             U.SAT_NO = Y.SAT_NO ";
        $query .= "                                                         ) ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }

    //CSVデータ取得
    function getExportData($mode = "")
    {
        if($mode != ""){
            $query  = " SELECT  ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SAT_NO, ";
        $query .= "     SUBSTR(T1.BIRTHDAY,1,4) as BIRTH_YEAR, ";
        $query .= "     SUBSTR(T1.BIRTHDAY,6,2) as BIRTH_MONTH, ";
        $query .= "     SUBSTR(T1.BIRTHDAY,9,2) as BIRTH_DAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T2.NAME2, ";
        $query .= "     SUBSTR(T1.SCHOOLCD,3,5) as SCHOOLCD, ";
        $query .= "     T1.HOPECD1, ";
        $query .= "     T1.HOPECD2, ";
        $query .= "     T1.HOPECD3 ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST T2 ON T1.GRADUATION = T2.NAMECD2 and T2.NAMECD1 = 'L205' ";
        $query .= "     LEFT JOIN SAT_HOPE_WRK_DAT T3 ON T1.SAT_NO = T3.SAT_NO ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND T3.SAT_NO IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     SAT_NO ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }


}
?>
