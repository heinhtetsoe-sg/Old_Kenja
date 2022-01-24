<?php

require_once('for_php7.php');

class knjm504wQuery extends Query {

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
        $query .= "  AND SEMESTER <> '9' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getSubclass($year,$semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT DISTINCT T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
            $query .= "     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD  || ' ' || T1.SUBCLASSNAME AS LABEL ";
            $query .= "FROM V_SUBCLASS_MST T1 ";
            $query .= "INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "  AND T2.SEMESTER = '".$semester ."'";
            $query .= "  AND T1.CLASSCD = T2.CLASSCD ";
            $query .= "  AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "  AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            $query .= "  AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "INNER JOIN CHAIR_STF_DAT T3 ON T3.YEAR = T2.YEAR ";
                $query .= "  AND T3.SEMESTER = T2.SEMESTER ";
                $query .= "  AND T3.CHAIRCD = T2.CHAIRCD ";
                $query .= "  AND T3.STAFFCD = '".STAFFCD."' ";
            }
            $query .= " WHERE ";
            $query .= "   T1.YEAR = '".$year."' ";
            $query .= "   AND T1.CLASSCD <= '90' ";
        }
        return $query;
    }

}
?>
