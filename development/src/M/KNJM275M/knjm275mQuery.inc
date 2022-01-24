<?php

require_once('for_php7.php');

class knjm275mQuery extends Query {

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
            $query .= "                   WHERE T2.YEAR          = '".$model->year."'";
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
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.CLASSCD, ";
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
        $query  = "  WITH MAX_REP_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      T1.YEAR, ";
        $query .= "      T1.CLASSCD, ";
        $query .= "      T1.SCHOOL_KIND, ";
        $query .= "      T1.CURRICULUM_CD, ";
        $query .= "      T1.SUBCLASSCD, ";
        $query .= "      T1.STANDARD_SEQ, ";
        $query .= "      T1.REPRESENT_SEQ, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECEIPT_DATE, ";
        $query .= "      T1.GRAD_DATE, ";
        $query .= "      T1.GRAD_VALUE ";
        $query .= "  FROM ";
        $query .= "      REP_PRESENT_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '".$model->year."' ";
        $query .= "  AND CHAR(T1.REPRESENT_SEQ) || CHAR(T1.RECEIPT_DATE) IN  ";
        $query .= "      (SELECT ";
        $query .= "          CHAR(MAX(M1.REPRESENT_SEQ)) || CHAR(MAX(M1.RECEIPT_DATE)) ";
        $query .= "      FROM ";
        $query .= "          REP_PRESENT_DAT M1 ";
        $query .= "      WHERE ";
        $query .= "          M1.YEAR = T1.YEAR ";
        $query .= "      AND M1.CLASSCD = T1.CLASSCD ";
        $query .= "      AND M1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "      AND M1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "      AND M1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "      AND M1.STANDARD_SEQ = T1.STANDARD_SEQ ";
        $query .= "      AND M1.SCHREGNO = T1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  GROUP BY ";
        $query .= "      T1.YEAR, ";
        $query .= "      T1.CLASSCD, ";
        $query .= "      T1.SCHOOL_KIND, ";
        $query .= "      T1.CURRICULUM_CD, ";
        $query .= "      T1.SUBCLASSCD, ";
        $query .= "      T1.STANDARD_SEQ, ";
        $query .= "      T1.REPRESENT_SEQ, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECEIPT_DATE, ";
        $query .= "      T1.GRAD_DATE, ";
        $query .= "      T1.GRAD_VALUE ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      B1.NAME, ";
        $query .= "      B1.NAME_KANA, ";
        $query .= "      N1.NAME1 AS SEX_NAME, ";
        $query .= "      B2.BASE_REMARK1, ";
        $query .= "      T1.CURRICULUM_CD, ";
        $query .= "      T1.SUBCLASSCD, ";
        $query .= "      N2.SUBCLASSNAME, ";
        $query .= "      N3.CHAIRCD, ";
        $query .= "      N3.CHAIRNAME, ";
        $query .= "      L1.STANDARD_SEQ, ";
        $query .= "      L3.STANDARD_DATE, ";
        $query .= "      L1.RECEIPT_DATE, ";
        $query .= "      L2.GRAD_DATE, ";
        $query .= "      L2.GRAD_VALUE, ";
        $query .= "      L2.REPRESENT_SEQ ";
        $query .= "  FROM ";
        $query .= "      SUBCLASS_STD_SELECT_DAT T1 ";
        $query .= "      LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ";
        $query .= "      LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B2 ON B2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                              AND B2.YEAR = T1.YEAR ";
        $query .= "                                              AND B2.BASE_SEQ = '001' ";
        $query .= "      LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ";
        $query .= "                           AND N1.NAMECD2 = B1.SEX ";
        $query .= "      LEFT JOIN SUBCLASS_MST N2 ON N2.CLASSCD = T1.CLASSCD ";
        $query .= "                               AND N2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                               AND N2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                               AND N2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "      LEFT JOIN CHAIR_DAT N3 ON N3.YEAR = T1.YEAR ";
        $query .= "                            AND N3.SEMESTER = '1' ";
        $query .= "                            AND N3.CLASSCD = T1.CLASSCD ";
        $query .= "                            AND N3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                            AND N3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                            AND N3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "      LEFT JOIN REP_PRESENT_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                  AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "                                  AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                  AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                  AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                  AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND L1.REPRESENT_SEQ = 0 ";
        $query .= "      LEFT JOIN MAX_REP_DATA L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                  AND L2.CLASSCD = T1.CLASSCD ";
        $query .= "                                  AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                  AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                  AND L2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                  AND L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND L2.STANDARD_SEQ = L1.STANDARD_SEQ  ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "      LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ";
            $query .= "                                  AND REGD.SEMESTER = T1.SEMESTER ";
            $query .= "                                  AND REGD.SCHREGNO = T1.SCHREGNO ";
            $query .= "      LEFT JOIN REP_STANDARDDATE_COURSE_DAT L3 ON L3.YEAR = T1.YEAR ";
            $query .= "                                       AND L3.CLASSCD = T1.CLASSCD ";
            $query .= "                                       AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                       AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                                       AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                                       AND L3.STANDARD_SEQ = L1.STANDARD_SEQ ";
            $query .= "                                       AND L3.COURSECD = REGD.COURSECD ";
            $query .= "                                       AND L3.MAJORCD = REGD.MAJORCD ";
            $query .= "                                       AND L3.COURSECODE = REGD.COURSECODE ";
        } else {
            $query .= "      LEFT JOIN REP_STANDARDDATE_DAT L3 ON L3.YEAR = T1.YEAR ";
            $query .= "                                       AND L3.CLASSCD = T1.CLASSCD ";
            $query .= "                                       AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                       AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                                       AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                                       AND L3.STANDARD_SEQ = L1.STANDARD_SEQ ";
        }
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '".$model->year."' ";
        $query .= "  AND T1.SEMESTER = '".CTRL_SEMESTER."' ";        
        $query .= "  AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ".$subclass_set." ";
        $query .= "  ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.STANDARD_SEQ ";

        return $query;
    }
}
?>
