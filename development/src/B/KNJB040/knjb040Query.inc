<?php

require_once('for_php7.php');

class knjb040Query extends Query {

    //時間割パターンヘッダクエリ
    function getBscHdQuery($model)
    {
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

        $db = Query::dbCheckOut();
        $row1[] = array('label' => "", 'value' => "");
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[] = array('label' => $row["SEMESTERNAME"]." Seq".$row["BSCSEQ"].":".$row["TITLE"],
                            'value' => $row["YEAR"].",".$row["BSCSEQ"].",".$row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
    }

    //所属クエリ
    function getSectQuery()
    {
        $query  = " SELECT ";
        $query .= "     T1.SECTIONCD AS VALUE, ";
        $query .= "     T1.SECTIONCD || '　' || T1.SECTIONNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SECTION_MST T1, ";
        $query .= "     SECTION_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SECTIONCD = T2.SECTIONCD AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SECTIONCD ";

        $db = Query::dbCheckOut();
        $row1 = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
    }
}
?>
