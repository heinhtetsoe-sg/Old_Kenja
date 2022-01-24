<?php

require_once('for_php7.php');

class knjm502mQuery extends Query {

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

    //規定回数(スクーリング・レポート)
    function getKiteiCount($subclassCd)
    {
        $query  = " SELECT ";
        $query .= "     SCH_SEQ_MIN, ";
        $query .= "     REP_SEQ_ALL ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassCd."' ";

        return $query;
    }

    //提出基準日
    function getStandardDate($subclassCd, $model) {
        $query  = " SELECT ";
        $query .= "     STANDARD_SEQ, ";
        $query .= "     STANDARD_DATE ";
        $query .= " FROM ";
        $query .= "     REP_STANDARDDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassCd."' ";
        $query .= " ORDER BY ";
        $query .= "     STANDARD_SEQ ";

        return $query;
    }

    //対象生徒
    function getStudent($subclassCd, $model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     SUBSTR(T2.SCHREGNO, 1, 4), ";
        $query .= "     SUBSTR(T2.SCHREGNO, 5), ";
        $query .= "     L1.NAME, ";
        $query .= "     L1.NAME_KANA, ";
        $query .= "     L2.BASE_REMARK1 ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T2.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ON T2.YEAR = L2.YEAR ";
        $query .= "          AND T2.SCHREGNO = L2.SCHREGNO ";
        $query .= "          AND L2.BASE_SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$subclassCd}' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     SUBSTR(T2.SCHREGNO, 1, 4) DESC, ";
        $query .= "     SUBSTR(T2.SCHREGNO, 5) ";

        return $query;
    }

    //スクーリング
    function getSchooling($model, $schregNo, $chairCd) {
        $query .= " WITH SCH_ATTEND AS ( ";
        $query .= "     SELECT EXECUTEDATE,CREDIT_TIME,SCHOOLINGKINDCD ";
        $query .= "     FROM   SCH_ATTEND_DAT ";
        $query .= "     WHERE  YEAR = '".$model->year."' AND ";
        $query .= "            SCHREGNO = '".$schregNo."' AND ";
        $query .= "            CHAIRCD = '".$chairCd."' ";
        $query .= " ), MIN_DATE AS ( ";
        $query .= "     SELECT EXECUTEDATE ";
        $query .= "     FROM   SCH_ATTEND ";
        $query .= "     WHERE  SCHOOLINGKINDCD='1' ";
        $query .= "     GROUP BY EXECUTEDATE ";
        $query .= " ), MAIN_T AS ( ";
        $query .= " SELECT '1' AS SCHOOLINGKINDCD, ";
        $query .= "        EXECUTEDATE, ";
        $query .= "        1 AS CREDIT_TIME, ";
        $query .= "        EXECUTEDATE AS E_DATE ";
        $query .= " FROM   MIN_DATE ";
        $query .= " UNION ALL ";
        $query .= " SELECT '2' AS SCHOOLINGKINDCD, ";
        $query .= "        EXECUTEDATE, ";
        $query .= "        VALUE(CREDIT_TIME, 1) AS CREDIT_TIME, ";
        $query .= "        EXECUTEDATE AS E_DATE ";
        $query .= " FROM   SCH_ATTEND ";
        $query .= " WHERE  SCHOOLINGKINDCD='2' ";
        $query .= " UNION ALL ";
        $query .= " SELECT '1' AS SCHOOLINGKINDCD, ";
        $query .= "        EXECUTEDATE, ";
        $query .= "        VALUE(CREDIT_TIME, 1) AS CREDIT_TIME, ";
        $query .= "        EXECUTEDATE AS E_DATE ";
        $query .= " FROM   SCH_ATTEND ";
        $query .= " WHERE  SCHOOLINGKINDCD='3' ";
        $query .= " UNION ALL ";
        $query .= " SELECT '2' AS SCHOOLINGKINDCD, ";
        $query .= "        EXECUTEDATE, ";
        $query .= "        VALUE(CREDIT_TIME, 1) AS CREDIT_TIME, ";
        $query .= "        EXECUTEDATE AS E_DATE ";
        $query .= " FROM   SCH_ATTEND ";
        $query .= " WHERE  SCHOOLINGKINDCD='4' ";
        $query .= " ORDER BY EXECUTEDATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCHOOLINGKINDCD, ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     SUM(CREDIT_TIME) AS CREDIT_TIME, ";
        $query .= "     E_DATE ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " GROUP BY ";
        $query .= "     SCHOOLINGKINDCD, ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     E_DATE ";
        $query .= " ORDER BY ";
        $query .= "     SCHOOLINGKINDCD, ";
        $query .= "     EXECUTEDATE ";

        return $query;
    }

    //レポート
    function getReport($model, $schregNo, $chairCd, $subclassCd) {
        $query  = " WITH STANDARD AS ( ";
        $query .= "     SELECT * ";
        $query .= "     FROM   REP_STANDARDDATE_DAT ";
        $query .= "     WHERE  YEAR = '".$model->year."' AND ";
        $query .= "            CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassCd."' ) ";
        $query .= " ,REP_MAIN AS ( ";
        $query .= "     SELECT STANDARD_SEQ,RECEIPT_DATE,REPRESENT_SEQ,GRAD_VALUE,GRAD_DATE,GRAD_TIME ";
        $query .= "     FROM   REP_PRESENT_DAT ";
        $query .= "     WHERE  YEAR = '".$model->year."' AND ";
        $query .= "            SCHREGNO = '".$schregNo."' AND ";
        $query .= "            CHAIRCD = '".$chairCd."' ) ";
        $query .= " ,REP_DATE_MAX AS ( ";
        $query .= "     SELECT STANDARD_SEQ,MAX(RECEIPT_DATE) AS RECEIPT_DATE ";
        $query .= "     FROM   REP_MAIN ";
        $query .= "     GROUP BY STANDARD_SEQ ) ";
        $query .= " ,REP_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.STANDARD_SEQ, ";
        $query .= "         T1.RECEIPT_DATE, ";
        $query .= "         MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ";
        $query .= "     FROM ";
        $query .= "         REP_MAIN T1, ";
        $query .= "         REP_DATE_MAX T2 ";
        $query .= "     WHERE  T1.STANDARD_SEQ = T2.STANDARD_SEQ ";
        $query .= "            AND T1.RECEIPT_DATE = T2.RECEIPT_DATE ";
        $query .= "     GROUP BY T1.STANDARD_SEQ, ";
        $query .= "              T1.RECEIPT_DATE ) ";
        $query .= " SELECT ";
        $query .= "     MAIN.STANDARD_SEQ, ";
        $query .= "     CASE WHEN T3.NAMESPARE1 = '1' ";
        $query .= "                    THEN T4.RECEIPT_DATE  ";
        $query .= "                    ELSE T1.RECEIPT_DATE  ";
        $query .= "          END AS RECEIPT_DATE, ";
        $query .= "     CASE WHEN T2.REPRESENT_SEQ > 0 ";
        $query .= "                    THEN T2.REPRESENT_SEQ ";
        $query .= "                    ELSE NULL ";
        $query .= "          END AS REPRESENT_SEQ, ";
        $query .= "     CASE WHEN T1.STANDARD_SEQ IS NULL ";
        $query .= "          THEN NULL ";
        $query .= "          WHEN T2.GRAD_VALUE = '1' AND T2.REPRESENT_SEQ = 0 ";
        $query .= "          THEN '再' ";
        $query .= "          ELSE CASE WHEN (T2.GRAD_VALUE IS NULL OR T2.GRAD_VALUE = '') ";
        $query .= "                      AND T2.GRAD_DATE IS NULL ";
        $query .= "                      AND T2.GRAD_TIME IS NULL ";
        $query .= "                    THEN '受' ";
        $query .= "                    ELSE T2.GRAD_VALUE END ";
        $query .= "          END AS HYOUKA, ";
        $query .= "     T3.NAMESPARE1 AS PASS ";
        $query .= " FROM   STANDARD MAIN ";
        $query .= "        LEFT JOIN REP_SEQ T1 ON MAIN.STANDARD_SEQ = T1.STANDARD_SEQ ";
        $query .= "        LEFT JOIN REP_MAIN T2 ON T2.STANDARD_SEQ = T1.STANDARD_SEQ ";
        $query .= "                                 AND T2.RECEIPT_DATE = T1.RECEIPT_DATE ";
        $query .= "                                 AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ";
        $query .= "        LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'M003' ";
        $query .= "                                 AND T3.NAMECD2 = T2.GRAD_VALUE ";
        $query .= "        LEFT JOIN REP_MAIN T4 ON T4.STANDARD_SEQ = MAIN.STANDARD_SEQ ";//再提出の受付日セット用
        $query .= "                                 AND T4.REPRESENT_SEQ = 0 ";
        $query .= " ORDER BY MAIN.STANDARD_SEQ ";

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
        $query .= "      L1.RECEIPT_INPUT_DATE, ";
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
        $query .= "      LEFT JOIN REP_STANDARDDATE_DAT L3 ON L3.YEAR = T1.YEAR ";
        $query .= "                                       AND L3.CLASSCD = T1.CLASSCD ";
        $query .= "                                       AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                       AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                       AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                       AND L3.STANDARD_SEQ = L1.STANDARD_SEQ ";
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
