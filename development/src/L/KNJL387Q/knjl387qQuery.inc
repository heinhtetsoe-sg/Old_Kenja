<?php

require_once('for_php7.php');

class knjl387qQuery extends Query {
    
    //SAT_APP_FORM_MSTの対象データ削除
    function delSatApp()
    {
        $query  = " DELETE FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLCD = '2008005' AND ";
        $query .= "     GRADUATION = '09' ";
        
        return $query;
    }
    //SAT_EXAM_DATの対象データ削除
    function deleteExam()
    {
        $query  = " DELETE FROM ";
        $query .= "     SAT_EXAM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SAT_NO in ( ";
        $query .= "                 SELECT ";
        $query .= "                     SAT_NO ";
        $query .= "                 FROM ";
        $query .= "                     SAT_APP_FORM_MST ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     SCHOOLCD = '2008005' AND ";
        $query .= "                     GRADUATION = '09' ";
        $query .= "                 ) ";
        
        return $query;
    }
    
    //SAT_EXAM_NO_DATの対象受験番号数取得
    function getExamNoCnt($mode="")
    {
        if($mode == ""){
            $query  = " SELECT ";
            $query .= "     JUKEN_NO_TO - JUKEN_NO_FROM + 1 ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         JUKEN_NO_FROM, ";
        $query .= "         JUKEN_NO_TO ";
        $query .= "     FROM ";
        $query .= "         SAT_EXAM_NO_DAT ";
        $query .= "     WHERE ";
        $query .= "         INOUT_KUBUN = '1' AND ";
        $query .= "         IND_KUBUN = '2' AND ";
        $query .= "         SEND_KUBUN = '2' AND ";
        $query .= "         PLACECD = '80' AND ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        if($mode == ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
    
    //SCHREG_REGD_DATの対象データ件数
    function getSchregCnt()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE = '09' AND ";
        $query .= "     SEMESTER = (SELECT ";
        $query .= "                     MAX(SEMESTER) ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ";
        
        return $query;
    }
    
    //SCHREG_REGD_DAT/SCHREG_BASE_MST/SCHREG_ADDRESS_DATからデータ取得
    function getSchregData()
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t2.NAME, ";
        $query .= "     t2.NAME_KANA, ";
        $query .= "     t2.SEX, ";
        $query .= "     t2.BIRTHDAY, ";
        $query .= "     t3.ZIPCD, ";
        $query .= "     t3.ADDR1, ";
        $query .= "     t3.ADDR2, ";
        $query .= "     t3.TELNO ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         GRADE = '09' AND ";
        $query .= "         SEMESTER = (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                         GRADE = '09' ";
        $query .= "                     ) ";
        $query .= "     ) t1 ";
        $query .= "     left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "     left join (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_ADDRESS_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     (SCHREGNO, ISSUEDATE) in (SELECT ";
        $query .= "                                                 SCHREGNO, ";
        $query .= "                                                 MAX(ISSUEDATE) ";
        $query .= "                                             FROM ";
        $query .= "                                                 SCHREG_ADDRESS_DAT ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 SCHREGNO ";
        $query .= "                                             ) ";
        $query .= "                 ) t3 on t1.SCHREGNO = t3.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     NAME_KANA  ";
        
        return $query;
    }
}
?>
