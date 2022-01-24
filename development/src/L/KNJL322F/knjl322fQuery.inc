<?php

require_once('for_php7.php');

class knjl322fQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        if ($namecd1 == 'L004') {
            $query .= " UNION ALL ";
            $query .= " SELECT DISTINCT ";
            $query .= "     '9:学力診断テスト' AS LABEL, ";
            $query .= "     '9' AS VALUE,";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS NAMESPARE2 ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試回数
    function getTestCount($year, $testdiv) {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.NAMECD1  = 'L034' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース区分
    function getCourseDiv($model)
    {
        $query  = " WITH TMP (VALUE, LABEL) AS (";
        $query .= "  VALUES('1', '理数キャリア') ";
        $query .= "  UNION ALL ";
        $query .= "  VALUES('2', '国際教養') ";
        $query .= "  UNION ALL ";
        $query .= "  VALUES('3', 'スポーツ科学') ";
        $query .= "  UNION ALL ";
        $query .= "  VALUES('9', '-- 全コース --') ";
        $query .= " ) ";
        $query .= " SELECT  ";
        $query .= "      VALUE ";
        $query .= "    , VALUE || ':' || LABEL AS LABEL ";
        $query .= " FROM TMP T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
