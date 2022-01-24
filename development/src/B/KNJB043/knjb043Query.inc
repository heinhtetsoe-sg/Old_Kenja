<?php

require_once('for_php7.php');

class knjb043Query extends Query
{
    public function getSection($model)
    {
        $query .= " SELECT ";
        $query .= "   T2.SECTIONCD AS VALUE, ";
        $query .= "   T2.SECTIONNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   SECTION_YDAT T1 ";
        $query .= "   LEFT JOIN SECTION_MST T2 ";
        $query .= "     ON T2.SECTIONCD = T1.SECTIONCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->year."' ";

        return $query;
    }
}
?>
