<?php

require_once('for_php7.php');

class knjl345Query extends Query {

    //�������x�擾
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        return $query;
    }
}
?>