<?php

require_once('for_php7.php');


class knje131Query extends Query {


    //学年取得
    function getSelectGrade()
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
		$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."' ";
	    	$query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD3 = '" .STAFFCD ."') ";
		}
        $query .= "order by grade";

        return $query;
    }


    //年組取得（権限チェック）
    function getAuth($model)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
			$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
			$query .= "AND GRADE='".$model->field["GRADE"] ."' ";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
			$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
			$query .= "AND GRADE='".$model->field["GRADE"] ."' ";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."' ";
	    	$query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD3 = '" .STAFFCD ."') ";
		}

        return $query;
    }


    //教科一覧リスト
    function getSelectClassMst()
    {
        $query  = "SELECT CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, CLASSNAME FROM CLASS_MST ORDER BY CLASSCD || '-' || SCHOOL_KIND ";

        return $query;
    }


    //科目
    function getSubClassName($model)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='" .CTRL_YEAR ."' AND SEMESTER='".CTRL_SEMESTER ."' AND ";
        $query .= "            GRADE||HR_CLASS IN ('" . $model->selectdata . "') ) ";
        $query .= " , STUDYREC AS ( ";
        $query .= "     SELECT T1.YEAR, T1.SCHREGNO, ANNUAL, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CLASSNAME, SUBCLASSNAME, VALUATION ";
        $query .= "     FROM   SCHREG_STUDYREC_DAT T1";
        $query .= "     LEFT JOIN (SELECT DISTINCT T1.SCHREGNO, T1.YEAR "; // 留年した年度
        $query .= "                       FROM    SCHREG_REGD_DAT T1 ";
        $query .= "                       LEFT JOIN (SELECT SCHREGNO, GRADE, MAX(YEAR) AS YEAR ";
        $query .= "                                  FROM SCHREG_REGD_DAT ";
        $query .= "                                  GROUP BY SCHREGNO, GRADE ";
        $query .= "                                 ) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.YEAR AND L1.GRADE = T1.GRADE ";
        $query .= "                       WHERE L1.SCHREGNO IS NULL ";
        $query .= "               ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ";
        $query .= "     WHERE  VALUATION IS NOT NULL AND ";
        $query .= "            T2.YEAR IS NULL AND ";
        $query .= "            T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "            NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '" .CTRL_YEAR ."' AND NAMECD1 = 'D065') AND ";
        $query .= "            NOT EXISTS(SELECT  'X' ";
        $query .= "                       FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ";
        $query .= "                       WHERE   T2.REPLACECD = '1' AND ";
        $query .= "                               T2.YEAR = T1.YEAR AND ";
        $query .= "                               T2.ATTEND_CLASSCD = T1.CLASSCD AND ";
        $query .= "                               T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        $query .= "                               T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        $query .= "                               T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ) ";
        $query .= "  ";
        $query .= " SELECT DISTINCT T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, T1.ANNUAL, S1.SUBCLASSNAME ";
        $query .= " FROM   SCHNO T2 ";
        $query .= "        INNER JOIN STUDYREC T1 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN SUBCLASS_MST S1 ON S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= " ORDER BY T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD, T1.ANNUAL ";

        return $query;
    }


    //教科
    function getClassName($model)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='" .CTRL_YEAR ."' AND SEMESTER='".CTRL_SEMESTER ."' AND ";
        $query .= "            GRADE||HR_CLASS IN ('" . $model->selectdata . "') ) ";
        $query .= " , STUDYREC AS ( ";
        $query .= "     SELECT T1.YEAR, T1.SCHREGNO, ANNUAL, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CLASSNAME, SUBCLASSNAME, VALUATION ";
        $query .= "     FROM   SCHREG_STUDYREC_DAT T1";
        $query .= "     LEFT JOIN (SELECT DISTINCT T1.SCHREGNO, T1.YEAR "; // 留年した年度
        $query .= "                       FROM    SCHREG_REGD_DAT T1 ";
        $query .= "                       LEFT JOIN (SELECT SCHREGNO, GRADE, MAX(YEAR) AS YEAR ";
        $query .= "                                  FROM SCHREG_REGD_DAT ";
        $query .= "                                  GROUP BY SCHREGNO, GRADE ";
        $query .= "                                 ) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.YEAR AND L1.GRADE = T1.GRADE ";
        $query .= "                       WHERE L1.SCHREGNO IS NULL ";
        $query .= "               ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ";
        $query .= "     WHERE  VALUATION IS NOT NULL AND ";
        $query .= "            T2.YEAR IS NULL AND ";
        $query .= "            T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "            NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '" .CTRL_YEAR ."' AND NAMECD1 = 'D065') AND ";
        $query .= "            NOT EXISTS(SELECT  'X' ";
        $query .= "                       FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ";
        $query .= "                       WHERE   T2.REPLACECD = '1' AND ";
        $query .= "                               T2.YEAR = T1.YEAR AND ";
        $query .= "                               T2.ATTEND_CLASSCD = T1.CLASSCD AND ";
        $query .= "                               T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        $query .= "                               T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        $query .= "                               T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ) ";
        $query .= "  ";
        $query .= " SELECT DISTINCT T1.CLASSCD || '_' || T1.SCHOOL_KIND AS CLASSCD, C1.CLASSNAME ";
        $query .= " FROM   SCHNO T2 ";
        $query .= "        INNER JOIN STUDYREC T1 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN CLASS_MST C1 ON C1.CLASSCD = T1.CLASSCD AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " ORDER BY T1.CLASSCD || '_' || T1.SCHOOL_KIND ";

        return $query;
    }


    //性別
    function getSexName()
    {
        $query  = " SELECT NAMECD2, NAME1, ABBV1 ";
        $query .= " FROM   V_NAME_MST ";
        $query .= " WHERE  YEAR = '" .CTRL_YEAR ."' AND NAMECD1 = 'Z002' ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }


    //CSV
    function getSelectCsv($model)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='" .CTRL_YEAR ."' AND SEMESTER='".CTRL_SEMESTER ."' AND ";
        $query .= "            GRADE||HR_CLASS IN ('" . $model->selectdata . "') ) ";
        $query .= " ,STUDYREC AS ( ";
        $query .= "     SELECT T1.YEAR, T1.SCHREGNO, ANNUAL, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CLASSNAME, SUBCLASSNAME, VALUATION ";
        $query .= "     FROM   SCHREG_STUDYREC_DAT T1";
        $query .= "     LEFT JOIN (SELECT DISTINCT T1.SCHREGNO, T1.YEAR "; // 留年した年度
        $query .= "                       FROM    SCHREG_REGD_DAT T1 ";
        $query .= "                       LEFT JOIN (SELECT SCHREGNO, GRADE, MAX(YEAR) AS YEAR ";
        $query .= "                                  FROM SCHREG_REGD_DAT ";
        $query .= "                                  GROUP BY SCHREGNO, GRADE ";
        $query .= "                                 ) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.YEAR AND L1.GRADE = T1.GRADE ";
        $query .= "                       WHERE L1.SCHREGNO IS NULL ";
        $query .= "               ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ";
        $query .= "     WHERE  VALUATION IS NOT NULL AND ";
        $query .= "            T2.YEAR IS NULL AND ";
        $query .= "            T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "            NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '" .CTRL_YEAR ."' AND NAMECD1 = 'D065') AND ";
        $query .= "            NOT EXISTS(SELECT  'X' ";
        $query .= "                       FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ";
        $query .= "                       WHERE   T2.REPLACECD = '1' AND ";
        $query .= "                               T2.YEAR = T1.YEAR AND ";
        $query .= "                               T2.ATTEND_CLASSCD = T1.CLASSCD AND ";
        $query .= "                               T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        $query .= "                               T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        $query .= "                               T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ) ";
        //各科目の評定および全教科の評定平均
        $query .= " ,STUDYREC_2 AS ( ";
        $query .= "     SELECT SCHREGNO,  ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,3,1) AS VAL_ALL ";
		for ($i = 0; $i < get_count($model->opt_subclasscd); $i++) {
            $query .= "        ,SUM(CASE WHEN CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD||'-'||ANNUAL = '".$model->opt_subclasscd[$i]."' THEN VALUATION ELSE NULL END) AS VALUATION".$i." ";
       	}
        $query .= "     FROM   STUDYREC ";
        $query .= "     GROUP BY SCHREGNO ) ";
        //各教科の評定平均
        $query .= " ,STUDYREC_3 AS ( ";
        $query .= "     SELECT T1.SCHREGNO ";
		for ($i = 0; $i < get_count($model->opt_classcd); $i++) {
            $query .= "        ,SUM(CASE WHEN T1.CLASSCD || '_' || T1.SCHOOL_KIND = '".$model->opt_classcd[$i]."' THEN T1.VAL_AVG ELSE NULL END) AS VAL".$model->opt_classcd[$i]." ";
       	}
        $query .= "     FROM ( ";
        $query .= "         SELECT SCHREGNO, CLASSCD, SCHOOL_KIND,  ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,3,1) AS VAL_AVG ";
        $query .= "         FROM   STUDYREC ";
        $query .= "         GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND ) T1  ";
        $query .= "     GROUP BY T1.SCHREGNO ) ";
        //主要教科の評定平均
        $query .= " ,STUDYREC_5 AS ( ";
        $query .= "     SELECT SCHREGNO,  ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,3,1) AS VAL_5ALL ";
        $query .= "     FROM   STUDYREC ";
        $query .= "     WHERE  CLASSCD || '-' || SCHOOL_KIND IN ('" . $model->selectdata2 . "') ";
        $query .= "     GROUP BY SCHREGNO ) ";

        //メイン
        $query .= " SELECT T1.SCHREGNO, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, B1.NAME, B1.SEX ";
		for ($i = 0; $i < get_count($model->opt_subclasscd); $i++) {
            $query .= ",T1.VALUATION" . $i . " ";
       	}
		for ($i = 0; $i < get_count($model->opt_classcd); $i++) {
            $query .= ",T3.VAL" . $model->opt_classcd[$i] . " ";
       	}
        $query .= "    ,T5.VAL_5ALL ,T1.VAL_ALL ";
        $query .= " FROM   SCHNO T2 ";
        $query .= "        LEFT JOIN STUDYREC_2 T1 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "        LEFT JOIN STUDYREC_3 T3 ON T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "        LEFT JOIN STUDYREC_5 T5 ON T5.SCHREGNO = T2.SCHREGNO ";
        $query .= "        LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ";

        return $query;
    }


}
?>
