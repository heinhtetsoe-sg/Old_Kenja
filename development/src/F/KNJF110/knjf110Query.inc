<?php

require_once('for_php7.php');


class knjf110Query extends Query {

    //名称マスタから来室理由を取得
    function getSelectName($namecd1)
    {
        $query = "select namecd2,name1 from name_mst where namecd1='" .$namecd1 ."' order by 1";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery1($model)
    {
        $query  = "SELECT '外科的症状' AS KUBUN,";
        $query .= "       T1.NAMECD2,";
        $query .= "       COALESCE(SUM(T2.NINZUU4),0) AS NINZUU4,";
        $query .= "       COALESCE(SUM(T2.NINZUU5),0) AS NINZUU5,";
        $query .= "       COALESCE(SUM(T2.NINZUU6),0) AS NINZUU6,";
        $query .= "       COALESCE(SUM(T2.NINZUU7),0) AS NINZUU7,";
        $query .= "       COALESCE(SUM(T2.NINZUU8),0) AS NINZUU8,";
        $query .= "       COALESCE(SUM(T2.NINZUU9),0) AS NINZUU9,";
        $query .= "       COALESCE(SUM(T2.NINZUU10),0) AS NINZUU10,";
        $query .= "       COALESCE(SUM(T2.NINZUU11),0) AS NINZUU11,";
        $query .= "       COALESCE(SUM(T2.NINZUU12),0) AS NINZUU12,";
        $query .= "       COALESCE(SUM(T2.NINZUU1),0) AS NINZUU1,";
        $query .= "       COALESCE(SUM(T2.NINZUU2),0) AS NINZUU2,";
        $query .= "       COALESCE(SUM(T2.NINZUU3),0) AS NINZUU3,";
        $query .= "       COALESCE(SUM(T2.NINZUU),0) AS NINZUU ";
        $query .= "FROM ";
        $query .= "(select namecd2,name1 from name_mst where namecd1='F730') T1 ";

        $query .= "LEFT JOIN (";
        $query .= "SELECT VISIT_REASON,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '04' THEN 1 ELSE 0 END) AS NINZUU4,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '05' THEN 1 ELSE 0 END) AS NINZUU5,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '06' THEN 1 ELSE 0 END) AS NINZUU6,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '07' THEN 1 ELSE 0 END) AS NINZUU7,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '08' THEN 1 ELSE 0 END) AS NINZUU8,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '09' THEN 1 ELSE 0 END) AS NINZUU9,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '10' THEN 1 ELSE 0 END) AS NINZUU10,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '11' THEN 1 ELSE 0 END) AS NINZUU11,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '12' THEN 1 ELSE 0 END) AS NINZUU12,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '01' THEN 1 ELSE 0 END) AS NINZUU1,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '02' THEN 1 ELSE 0 END) AS NINZUU2,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '03' THEN 1 ELSE 0 END) AS NINZUU3,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN 'NULL' THEN 0 ELSE 1 END) AS NINZUU";
        $query .= "  FROM nurseoffice_dat";
        $query .= " WHERE YEAR = '" .$model->field["YEAR"] ."'";
        $query .= "   AND TREATMENT_DIV = '02'";
        $query .= " GROUP BY YEAR, SUBSTR(CHAR(DATE),1,7), VISIT_REASON";
        $query .= ") T2 ON (T1.NAMECD2 = T2.VISIT_REASON) ";

        $query .= "GROUP BY '外科的症状', T1.NAMECD2 ";
        $query .= "ORDER BY T1.NAMECD2";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery2($model)
    {
        $query  = "SELECT '内科的症状' AS KUBUN,";
        $query .= "       T1.NAMECD2,";
        $query .= "       COALESCE(SUM(T2.NINZUU4),0) AS NINZUU4,";
        $query .= "       COALESCE(SUM(T2.NINZUU5),0) AS NINZUU5,";
        $query .= "       COALESCE(SUM(T2.NINZUU6),0) AS NINZUU6,";
        $query .= "       COALESCE(SUM(T2.NINZUU7),0) AS NINZUU7,";
        $query .= "       COALESCE(SUM(T2.NINZUU8),0) AS NINZUU8,";
        $query .= "       COALESCE(SUM(T2.NINZUU9),0) AS NINZUU9,";
        $query .= "       COALESCE(SUM(T2.NINZUU10),0) AS NINZUU10,";
        $query .= "       COALESCE(SUM(T2.NINZUU11),0) AS NINZUU11,";
        $query .= "       COALESCE(SUM(T2.NINZUU12),0) AS NINZUU12,";
        $query .= "       COALESCE(SUM(T2.NINZUU1),0) AS NINZUU1,";
        $query .= "       COALESCE(SUM(T2.NINZUU2),0) AS NINZUU2,";
        $query .= "       COALESCE(SUM(T2.NINZUU3),0) AS NINZUU3,";
        $query .= "       COALESCE(SUM(T2.NINZUU),0) AS NINZUU ";
        $query .= "FROM ";
        $query .= "(select namecd2,name1 from name_mst where namecd1='F720') T1 ";

        $query .= "LEFT JOIN (";
        $query .= "SELECT VISIT_REASON,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '04' THEN 1 ELSE 0 END) AS NINZUU4,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '05' THEN 1 ELSE 0 END) AS NINZUU5,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '06' THEN 1 ELSE 0 END) AS NINZUU6,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '07' THEN 1 ELSE 0 END) AS NINZUU7,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '08' THEN 1 ELSE 0 END) AS NINZUU8,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '09' THEN 1 ELSE 0 END) AS NINZUU9,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '10' THEN 1 ELSE 0 END) AS NINZUU10,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '11' THEN 1 ELSE 0 END) AS NINZUU11,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '12' THEN 1 ELSE 0 END) AS NINZUU12,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '01' THEN 1 ELSE 0 END) AS NINZUU1,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '02' THEN 1 ELSE 0 END) AS NINZUU2,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN '03' THEN 1 ELSE 0 END) AS NINZUU3,";
        $query .= "       SUM(CASE SUBSTR(CHAR(DATE),6,2) WHEN 'NULL' THEN 0 ELSE 1 END) AS NINZUU";
        $query .= "  FROM nurseoffice_dat";
        $query .= " WHERE YEAR = '" .$model->field["YEAR"] ."'";
        $query .= "   AND TREATMENT_DIV = '01'";
        $query .= " GROUP BY YEAR, SUBSTR(CHAR(DATE),1,7), VISIT_REASON";
        $query .= ") T2 ON (T1.NAMECD2 = T2.VISIT_REASON) ";

        $query .= "GROUP BY '内科的症状', T1.NAMECD2 ";
        $query .= "ORDER BY T1.NAMECD2";

        return $query;
    }


}
?>
