<?php

require_once('for_php7.php');

class knjf334aQuery extends Query {
    //教育委員会チェック
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }
        
    //年度
    function getYear() {
        $query  = " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS VALUE, ";
        $query .= "     '".CTRL_YEAR."' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象学校
    function getSchoolData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN L1.ANSWER_FLG = '1' ";
        $query .= "          THEN '【回済】' ";
        $query .= "          ELSE '　　　　' ";
        $query .= "     END AS FLG_NAME, ";
        $query .= "     '0' AS FLG, ";
        $query .= "     L1.EDBOARD_SCHOOLCD, ";
        $query .= "     L2.EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT T1  ";
        $query .= " INNER JOIN AFT_SEARCH_REPORT_SCHOOL_DAT L1 ON L1.YEAR = T1.YEAR  ";
        $query .= "                                           AND L1.DOC_NUMBER = T1.DOC_NUMBER  ";
        $query .= " LEFT JOIN EDBOARD_SCHOOL_MST L2 ON L2.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= " AND T1.REQUEST_ANSWER_PRG = 'KNJF334'  ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '【未発】' AS FLG_NAME, ";
        $query .= "     '1' AS FLG, ";
        $query .= "     L1.EDBOARD_SCHOOLCD, ";
        $query .= "     L2.EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT T1  ";
        $query .= " INNER JOIN AFT_SEARCH_REPORT_SCHOOL_DAT L1 ON L1.YEAR = T1.YEAR  ";
        $query .= "                                           AND L1.DOC_NUMBER = T1.DOC_NUMBER  ";
        $query .= " LEFT JOIN EDBOARD_SCHOOL_MST L2 ON L2.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= " AND L1.EDBOARD_SCHOOLCD NOT IN ( ";
        $query .= "                                 SELECT DISTINCT ";
        $query .= "                                     N2.EDBOARD_SCHOOLCD ";
        $query .= "                                 FROM ";
        $query .= "                                     AFT_SEARCH_REPORT_DAT N1  ";
        $query .= "                                 INNER JOIN AFT_SEARCH_REPORT_SCHOOL_DAT N2 ON N1.YEAR = N2.YEAR  ";
        $query .= "                                                                           AND N1.DOC_NUMBER = N2.DOC_NUMBER  ";
        $query .= "                                 WHERE ";
        $query .= "                                     N1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "                                 AND N1.REQUEST_ANSWER_PRG = 'KNJF334' ";
        $query .= "                                 ) ";
        $query .= " ORDER BY ";
        $query .= "     EDBOARD_SCHOOLCD ";

        return $query;
    }

}
?>
