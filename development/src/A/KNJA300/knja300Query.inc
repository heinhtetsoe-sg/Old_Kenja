<?php

require_once('for_php7.php');

class knja300Query extends Query
{

    //�Ȗڃ��X�g�擾
    function getSubclass(&$model)
    {
		$query  = "SELECT DISTINCT t1.SUBCLASSCD, t1.SUBCLASSABBV ";
		$query .= "FROM subclass_mst t1,subclass_ydat t2 ";
		$query .= "WHERE t2.year = '" .$model->field["YEAR"] ."' AND ";
		$query .= "      t1.subclasscd = t2.subclasscd ";
		$query .= "ORDER BY t1.subclasscd ";
        return $query;
    }
    //���|�[�g�񐔎擾
    function getReportcount(&$model)
    {
		$query  = "SELECT STANDARD_NO ";
		$query .= "FROM subclass_ydat ";
		$query .= "WHERE year = '" .$model->field["YEAR"] ."' AND subclasscd = '" .$model->field["KAMOKU"]. "' ";
		$query .= "      AND STANDARD_NO is not null ";
        return $query;
    }

}
?>
