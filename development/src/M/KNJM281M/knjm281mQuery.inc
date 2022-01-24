<?php

require_once('for_php7.php');

class knjm281mQuery extends Query {

    //担当科目一覧取得
    function selectSubclassQuery($model, $flg)
    {
        $query  = "SELECT  ";
        if ($flg === 'COUNT') {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T1.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        //権限が更新可能(管理者の場合)
        if (AUTHORITY == DEF_UPDATABLE) {
            $query .= "     LEFT JOIN SUBCLASS_YDAT L1 ON L1.YEAR          = '".$model->year."'";
            $query .= "                               AND L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                               AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                               AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                               AND L1.SUBCLASSCD    = T1.SUBCLASSCD ";
            $query .= " WHERE T1.YEAR = '".$model->year."' ";
        } else {
            $query .= " WHERE EXISTS (SELECT 'X' ";
            $query .= "                   FROM REP_STF_DAT T2 ";
            $query .= "                   WHERE T2.YEAR          = T1.YEAR ";
            $query .= "                     AND T2.CLASSCD       = T1.CLASSCD ";
            $query .= "                     AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                     AND T2.SUBCLASSCD    = T1.SUBCLASSCD ";
            $query .= "                     AND T2.STAFFCD       = '".STAFFCD."') ";
            $query .= "   AND T1.YEAR = '".$model->year."' ";
        }
        if ($flg !== 'COUNT') {
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD   ";
        }
        
        return $query;
    }

    //科目名
    function getSubcalssName($subclassCd) {
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassCd."' ";

        return $query;
    }

}
?>
