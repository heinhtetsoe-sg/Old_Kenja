<?php

require_once('for_php7.php');

class knjl350Query extends Query {

    //�������x�擾
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        return $query;
    }
    //�����敪�擾
    function get_test_div($year)
    {
        $query  = " SELECT NAME,TESTDIV,SHOWDIV ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$year."' AND ";
        $query .= " TESTDIV IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        $query .= " ORDER BY SHOWORDER, TESTDIV ";
        return $query;
    }
}
?>
