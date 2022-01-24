<?php

require_once('for_php7.php');


class knjd040Query extends Query {

    //１レコード取得 ※未使用
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    function getClassData($model)
    {
        $query  = "SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        } else {
            $query .= "     substr(T1.SUBCLASSCD,1,2) AS VALUE, ";
        }
        $query .= "     T2.CLASSNAME AS LABEL ";
        $query .= " FROM chair_dat T1 INNER JOIN class_mst T2";
        $query .= "                         ON substr(T1.SUBCLASSCD,1,2)=T2.CLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND";
        }
        $query .= " WHERE ((T1.YEAR='".CTRL_YEAR."')";
        if ($model->field["GAKKI"] != 9 ) {
            $query .= " AND (T1.SEMESTER='".$model->field["GAKKI"]."')";
        }
        $query .= " AND (substr(T1.SUBCLASSCD,1,2) < '"."90"."'))";
        $query .= " order by ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
