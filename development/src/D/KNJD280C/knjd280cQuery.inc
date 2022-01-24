<?php

require_once('for_php7.php');


class knjd280cQuery extends Query {

    //学期マスタ
    function getSemestername() {
        $query .= "  SELECT ";
        $query .= "      SEMESTERNAME ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '" .CTRL_YEAR ."'";
        $query .= "      AND SEMESTER = '" .CTRL_SEMESTER."'";

        return $query;
    }

    //学科
    function getMajor($model) {
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.COURSECD || T1.MAJORCD AS VALUE ";
        $query .= "     ,T2.MAJORNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT T1 ";
        $query .= "      INNER JOIN MAJOR_MST T2 ON T2.COURSECD = T1.COURSECD AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '" .CTRL_YEAR ."'";
        $query .= "      AND T1.SEMESTER = '" .$model->field["GAKKI"]."'";
        $query .= "  ORDER BY ";
        $query .= "      T1.COURSECD || T1.MAJORCD ";

        return $query;
    }

    //教科取得
    function getClass($model) {
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
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "  INNER JOIN CHAIR_STF_DAT L3 ON L3.YEAR = T1.YEAR ";
            $query .= "                             AND L3.SEMESTER = T1.SEMESTER ";
            $query .= "                             AND L3.CHAIRCD = T1.CHAIRCD ";
            $query .= "                             AND L3.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "  WHERE ";
        $query .= "      T1.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "  AND T1.SEMESTER = '" .$model->field["GAKKI"]."'";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";        
        return $query;
    }

    //科目取得
    function getSubclass($model, $semester, $classcd_set) {
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
        $query .= "  INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = L2.SCHREGNO ";
        $query .= "                             AND REGD.YEAR = T1.YEAR ";
        $query .= "                             AND REGD.SEMESTER = T1.SEMESTER ";
        $query .= "                             AND REGD.COURSECD = '".substr($model->field["MAJOR"], 0, 1)."' ";
        $query .= "                             AND REGD.MAJORCD = '".substr($model->field["MAJOR"], 1, 3)."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "  INNER JOIN CHAIR_STF_DAT L3 ON L3.YEAR = T1.YEAR ";
            $query .= "                             AND L3.SEMESTER = T1.SEMESTER ";
            $query .= "                             AND L3.CHAIRCD = T1.CHAIRCD ";
            $query .= "                             AND L3.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "  WHERE ";
        $query .= "      T1.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "  AND T1.SEMESTER = '" .$semester ."'";
        $query .= "  AND T1.CLASSCD || '-' || T1.SCHOOL_KIND = '{$classcd_set}' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }
}
?>

