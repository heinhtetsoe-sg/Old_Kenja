<?php

require_once('for_php7.php');

class knjm438mQuery extends Query {

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

    //CSV出力
    function getCsvQuery($model) {

        $subclass_set = "('";
        $comma = "";
        $arr = explode(",", $model->selectdata);
        foreach ($arr as $var) {
            $subclass_set .= $comma.$var;
            $comma = "', '";
        }
        $subclass_set .= "')";
        $query  = " WITH SUBCLASS_STD_SELECT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         TSL.YEAR, ";
        $query .= "         TSL.SEMESTER, ";
        $query .= "         TSL.CLASSCD, ";
        $query .= "         TSL.SCHOOL_KIND, ";
        $query .= "         TSL.CURRICULUM_CD, ";
        $query .= "         TSL.SUBCLASSCD, ";
        $query .= "         TSL.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_SELECT_DAT TSL ";
        $query .= "     WHERE ";
        $query .= "         TSL.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND TSL.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND TSL.CLASSCD || '-' || TSL.SCHOOL_KIND || '-' || TSL.CURRICULUM_CD || '-' || TSL.SUBCLASSCD IN ".$subclass_set." ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     TSL.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     TSL.CLASSCD || '-' || TSL.SCHOOL_KIND || '-' || TSL.CURRICULUM_CD || '-' || TSL.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     SUB_M.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT TSL ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = TSL.SCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_STD_PASS_DAT TPS ON TSL.YEAR = TPS.YEAR ";
        $query .= "         AND TSL.SEMESTER = TPS.SEMESTER ";
        $query .= "         AND TSL.CLASSCD = TPS.CLASSCD ";
        $query .= "         AND TSL.SCHOOL_KIND = TPS.SCHOOL_KIND ";
        $query .= "         AND TSL.CURRICULUM_CD = TPS.CURRICULUM_CD ";
        $query .= "         AND TSL.SUBCLASSCD = TPS.SUBCLASSCD ";
        $query .= "         AND TSL.SCHREGNO = TPS.SCHREGNO ";
        $query .= "     LEFT JOIN TEST_ATTEND_DAT TAT ON TSL.YEAR = TAT.YEAR ";
        $query .= "         AND TSL.SEMESTER = TAT.SEMESTER ";
        $query .= "         AND TSL.CLASSCD = TAT.CLASSCD ";
        $query .= "         AND TSL.SCHOOL_KIND = TAT.SCHOOL_KIND ";
        $query .= "         AND TSL.CURRICULUM_CD = TAT.CURRICULUM_CD ";
        $query .= "         AND TSL.SUBCLASSCD = TAT.SUBCLASSCD ";
        $query .= "         AND TSL.SCHREGNO = TAT.SCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON TSL.CLASSCD = SUB_M.CLASSCD ";
        $query .= "         AND TSL.SCHOOL_KIND = SUB_M.SCHOOL_KIND ";
        $query .= "         AND TSL.CURRICULUM_CD = SUB_M.CURRICULUM_CD ";
        $query .= "         AND TSL.SUBCLASSCD = SUB_M.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     VALUE(BASE.INOUTCD, '') <> '8' ";
        $query .= "     AND VALUE(TPS.SEM_PASS_FLG, '0') = '1' ";
        $query .= "     AND TAT.SCHREGNO IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     TSL.CLASSCD || TSL.SCHOOL_KIND || TSL.CURRICULUM_CD || TSL.SUBCLASSCD, ";
        $query .= "     SUBSTR(TSL.SCHREGNO, 1, 4) DESC, ";
        $query .= "     SUBSTR(TSL.SCHREGNO, 5, 4) ASC ";
        return $query;
    }
}
?>
