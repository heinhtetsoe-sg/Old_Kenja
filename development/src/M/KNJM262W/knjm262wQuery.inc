<?php

require_once('for_php7.php');

class knjm262wQuery extends Query
{
    //CSV出力
    public function getCsvQuery($sDate, $eDate)
    {
        $query .= " WITH REP_PRESENT AS ( ";
        $query .= " SELECT ";
        $query .= "     T0.*, ";
        $query .= "     T1.STAFFCD AS STAFFCD2, ";
        $query .= "     T2.STAFFNAME AS STAFFNAME, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT T0 ";
        $query .= " INNER JOIN ";
        $query .= "     REP_STF_DAT T1 ";
        $query .= "      ON T1.YEAR = T0.YEAR ";
        $query .= "     AND T1.CLASSCD = T0.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = T0.SUBCLASSCD ";
        $query .= "     AND T1.CHAIRCD = T0.CHAIRCD ";
        $query .= " INNER JOIN ";
        $query .= "     STAFF_MST T2 ";
        $query .= "      ON T2.STAFFCD = T1.STAFFCD ";
        $query .= " INNER JOIN ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= "      ON T3.CLASSCD = T0.CLASSCD ";
        $query .= "     AND T3.SCHOOL_KIND = T0.SCHOOL_KIND ";
        $query .= "     AND T3.CURRICULUM_CD = T0.CURRICULUM_CD ";
        $query .= "     AND T3.SUBCLASSCD = T0.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T0.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T0.RECEIPT_DATE BETWEEN '".$sDate."' AND '".$eDate."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     VALUE(T1.STAFFCD2, 'ALL') AS STAFFCD, ";
        $query .= "     VALUE(T1.STAFFNAME, '') AS STAFFNAME_SHOW, ";
        $query .= "     VALUE(T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, 'TOTAL') AS SUBCLASSKEY, ";
        $query .= "     VALUE(T1.SUBCLASSNAME, '計') AS SUBCLASSNAME, ";
        $query .= "     COUNT(T1.SCHREGNO) AS COUNT ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT T1 ";
        $query .= " GROUP BY ";
        $query .= " GROUPING SETS( ";
        $query .= "     (), (T1.STAFFCD2, T1.STAFFNAME), (T1.STAFFCD2, T1.STAFFNAME, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SUBCLASSNAME) ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCD2, SUBCLASSKEY ";

        return $query;
    }
}
