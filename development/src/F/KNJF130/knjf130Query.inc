<?php

require_once('for_php7.php');


class knjf130Query extends Query {

    //名称マスタから来室校時名を取得
    function getSelectName($namecd1)
    {
        $query = "select namecd2,name1 from name_mst where namecd1='" .$namecd1 ."' order by 1";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model)
    {
        $query  = "SELECT TBL1.grade,";
        $query .= "       TBL1.namecd2,";
        $query .= "       COALESCE(SUM(TBL2.DANSI_SUU),0) AS DANSI,";
        $query .= "       COALESCE(SUM(TBL2.JOSI_SUU),0) AS JOSI ";
        $query .= "FROM ";
        $query .= "(";
        $query .= "	SELECT ";
        $query .= "		   T3.grade,";
        $query .= "	       T4.namecd2,";
        $query .= "    	   T4.name1 ";
        $query .= "	  FROM ";
        $query .= "	(select distinct tt1.grade from schreg_regd_dat tt1,schreg_regd_hdat tt2 where tt1.year='" .$model->field["YEAR"] ."' and tt1.year = tt2.year and tt1.semester = tt2.semester and tt1.grade = tt2.grade and tt1.hr_class = tt2.hr_class) T3,";
//        $query .= "	(select grade from schreg_regd_dat where year='" .$model->field["YEAR"] ."') T3,";
        $query .= "	(select namecd2,name1 from name_mst where namecd1='F700') T4 ";
        $query .= ") TBL1 ";

        $query .= "LEFT JOIN (";
        $query .= "SELECT T2.GRADE,";
        $query .= "       T1.PERIOD,";
        $query .= "       SUM(CASE T0.SEX WHEN '1' THEN 1 ELSE 0 END) AS DANSI_SUU,";
        $query .= "       SUM(CASE T0.SEX WHEN '2' THEN 1 ELSE 0 END) AS JOSI_SUU ";
        $query .= "  FROM schreg_base_mst    T0,";
        $query .= "       nurseoffice_dat  T1, ";
        $query .= "	(select distinct tt1.schregno,tt1.year,tt1.grade from schreg_regd_dat tt1,schreg_regd_hdat tt2 where tt1.year='" .$model->field["YEAR"] ."' and tt1.year = tt2.year and tt1.semester = tt2.semester and tt1.grade = tt2.grade and tt1.hr_class = tt2.hr_class) T2 ";
//        $query .= "       schreg_regd_dat    T2 ";
        $query .= " WHERE T2.YEAR           = '" .$model->field["YEAR"] ."'";
        $query .= "   AND T1.PERIOD    > '00'";
        $query .= "   AND T2.YEAR           = T1.YEAR ";
        $query .= "   AND T0.SCHREGNO       = T1.SCHREGNO ";
        $query .= "   AND T0.SCHREGNO       = T2.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "       T2.GRADE,";
        $query .= "       T1.PERIOD ";
        $query .= ") TBL2 ON (TBL1.grade = TBL2.GRADE AND TBL1.namecd2 = TBL2.PERIOD) ";

        $query .= "GROUP BY TBL1.grade,TBL1.namecd2 ";
        $query .= "ORDER BY 1,2";

        return $query;
    }


}
?>
