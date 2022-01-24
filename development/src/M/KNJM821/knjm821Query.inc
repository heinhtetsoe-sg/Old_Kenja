<?php

require_once('for_php7.php');

class knjm821Query extends Query {

    //�N���X�f�[�^�擾
    function getClass($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";

        return $query;
    }

}
?>
