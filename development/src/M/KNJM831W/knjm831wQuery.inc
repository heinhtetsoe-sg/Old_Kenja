<?php

class knjm831wQuery extends Query {

    //学期マスタ
    function getSemeMst()
    {
        $query .= "  SELECT DISTINCT";
        $query .= "      SEMESTERNAME LABEL, ";
        $query .= "      SEMESTER AS VALUE    ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '" .CTRL_YEAR ."'";

        return $query;
    }

    //教科取得
    function getClass($semester)
    {
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || ' ' || L1.CLASSNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      CHAIR_DAT T1 ";
        $query .= "  LEFT JOIN CLASS_MST L1 ON L1.CLASSCD = T1.CLASSCD ";
        $query .= "                        AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "  INNER JOIN CHAIR_STD_DAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                             AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                             AND L2.CHAIRCD = T1.CHAIRCD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR     = '" .CTRL_YEAR ."'";
        if ($semester === '9') {
            $query .= "  AND T1.SEMESTER = '" .'2' ."'";
        } else {
            $query .= "  AND T1.SEMESTER = '" .$semester ."'";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";        

        return $query;
    }

    //科目取得
    function getSubclass($model, $semester, $classcd_set)
    {
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '　' || L1.SUBCLASSNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      CHAIR_DAT T1 ";
        $query .= "  LEFT JOIN SUBCLASS_MST L1 ON L1.CLASSCD = T1.CLASSCD ";
        $query .= "                           AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                           AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                           AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "  INNER JOIN CHAIR_STD_DAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                             AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                             AND L2.CHAIRCD = T1.CHAIRCD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR     = '" .CTRL_YEAR ."'";
        if ($semester === '9') {
            $query .= "  AND T1.SEMESTER = '" .'2' ."'";
        } else {
            $query .= "  AND T1.SEMESTER = '" .$semester ."'";
        }
        $query .= "  AND T1.CLASSCD || '-' || T1.SCHOOL_KIND = '{$classcd_set}' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }
}
?>

