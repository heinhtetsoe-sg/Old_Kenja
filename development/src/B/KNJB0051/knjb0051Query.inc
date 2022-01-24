<?php

require_once('for_php7.php');

class knjb0051Query extends Query {
    //時間割パターンヘッダクエリ
    function getBscHdQuery($model, $db) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T2.SEMESTERNAME, ";
        $query .= "     T1.BSCSEQ, ";
        $query .= "     T1.TITLE ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.BSCSEQ ";

        $opt[] = array('label' => "", 'value' => "");
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array('label' => "{$row["SEMESTERNAME"]} Seq{$row["BSCSEQ"]}:{$row["TITLE"]}",
                           'value' => "{$row["YEAR"]},{$row["BSCSEQ"]},{$row["SEMESTER"]}");
        }
        $result->free();

        return $opt;
    }
}
?>
