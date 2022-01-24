<?php

require_once('for_php7.php');


class knjf120Query extends Query {

    //名称マスタから（内科・外科）／（性別）を取得
    function getSelectName($model)
    {
        $query  = "	SELECT TT1.namecd2 || TT2.namecd2 AS NAMECD,";
        $query .= "	       TT1.name1 || TT2.name2 AS NAME ";
        $query .= "	  FROM ";
        $query .= "	(select namecd2,name1 from name_mst where namecd1='F710') TT1,";
        $query .= "	(select namecd2,name2 from name_mst where namecd1='Z002') TT2 ";
        $query .= "order by 1";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model)
    {
        $query  = "SELECT T1.KUBUN1 || T1.KUBUN2 AS KUBUN,";
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
        $query .= "       COALESCE(SUM(T2.NINZUU3),0) AS NINZUU3 ";
        $query .= "FROM ";
        $query .= "(";
        $query .= "	SELECT TT1.namecd2 AS KUBUN1,";
        $query .= "    	   TT2.namecd2 AS KUBUN2,";
        $query .= "	       TT1.name1,";
        $query .= "    	   TT2.name2 ";
        $query .= "	  FROM ";
        $query .= "	(select namecd2,name1 from name_mst where namecd1='F710') TT1,";
        $query .= "	(select namecd2,name2 from name_mst where namecd1='Z002') TT2 ";
        $query .= ") T1 ";

        $query .= "LEFT JOIN (";
        $query .= "SELECT TT3.TREATMENT_DIV,";
        $query .= "		  TT4.SEX,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '04' THEN 1 ELSE 0 END) AS NINZUU4,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '05' THEN 1 ELSE 0 END) AS NINZUU5,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '06' THEN 1 ELSE 0 END) AS NINZUU6,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '07' THEN 1 ELSE 0 END) AS NINZUU7,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '08' THEN 1 ELSE 0 END) AS NINZUU8,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '09' THEN 1 ELSE 0 END) AS NINZUU9,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '10' THEN 1 ELSE 0 END) AS NINZUU10,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '11' THEN 1 ELSE 0 END) AS NINZUU11,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '12' THEN 1 ELSE 0 END) AS NINZUU12,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '01' THEN 1 ELSE 0 END) AS NINZUU1,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '02' THEN 1 ELSE 0 END) AS NINZUU2,";
        $query .= "       SUM(CASE SUBSTR(CHAR(TT3.DATE),6,2) WHEN '03' THEN 1 ELSE 0 END) AS NINZUU3 ";
        $query .= "  FROM nurseoffice_dat TT3,schreg_base_mst TT4 ";
        $query .= " WHERE TT3.YEAR = '" .$model->field["YEAR"] ."'";
        $query .= "   AND TT3.VISIT_REASON > '00'";
        $query .= "   AND TT3.SCHREGNO = TT4.SCHREGNO";
        $query .= " GROUP BY TT3.YEAR, SUBSTR(CHAR(TT3.DATE),1,7), TT3.TREATMENT_DIV, TT4.SEX ";
        $query .= ") T2 ON (T1.KUBUN1 = T2.TREATMENT_DIV AND T1.KUBUN2 = T2.SEX) ";

        $query .= "GROUP BY T1.KUBUN1,T1.KUBUN2 ";
        $query .= "ORDER BY 1";

        return $query;
    }


}
?>
