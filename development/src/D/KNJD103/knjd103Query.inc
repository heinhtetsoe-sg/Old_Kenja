<?php

require_once('for_php7.php');


class knjd103Query extends Query {

    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."'";
			$query .= "AND SEMESTER='".$semester ."'";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."' ";
			$query .= "AND SEMESTER='".$semester ."' ";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
		}

        return $query;
    }

    //ＨＲ担任名を取得
    function getStaffName($model)
    {
			$query  = "SELECT STAFFNAME ";
			$query .= "FROM SCHREG_REGD_HDAT T1 ";
			$query .= "     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD=T1.TR_CD1 ";
			$query .= "WHERE YEAR='" .$model->control["年度"] ."' ";
			$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
			$query .= "AND GRADE||HR_CLASS='".$model->field["GRADE_HR_CLASS"] ."' ";

        return $query;
    }

    //科目一覧を取得
    function getSubclassCsvData($model)
    {
        $test_name = ($model->field["TESTKINDCD"] == "01") ? "INTR" : "TERM" ;  //成績種別
        $test_name = "SEM" . $model->field["GAKKI"] . "_" . $test_name . "_SCORE";  //成績種別

            //在籍の表
    	$query  = "WITH SCHNO_A AS( ";
    	$query .= "    SELECT  SCHREGNO ";
    	$query .= "    FROM    SCHREG_REGD_DAT ";
    	$query .= "    WHERE   YEAR = '" . $model->control["年度"] . "' AND ";
    	$query .= "            SEMESTER = '" . $model->field["GAKKI"] . "' AND ";
    	$query .= "            SCHREGNO IN ('" . implode("','", $model->select_data["selectdata"]) . "') ";
    	$query .= "    ) ";

		    //成績の表
    	$query .= ",RECORD_A AS( ";
    	$query .= "	SELECT  SCHREGNO, SUBCLASSCD, ";
    	$query .= "		    " . $test_name . " AS SCORE ";
    	$query .= "	FROM    RECORD_DAT T1 ";
    	$query .= "	WHERE   YEAR='" . $model->control["年度"] . "' AND ";
    	$query .= " 	    " . $test_name . " IS NOT NULL AND ";
    	$query .= "         SCHREGNO IN ('" . implode("','", $model->select_data["selectdata"]) . "') ";
    	$query .= "    ) ";


			//メイン表
    	$query .= "SELECT DISTINCT ";
    	$query .= "       T2.SUBCLASSCD, ";
    	$query .= "       T5.SUBCLASSABBV ";
    	$query .= "FROM   SCHNO_A T1 ";
    	$query .= "       INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    	$query .= "		  LEFT JOIN SUBCLASS_MST T5 ON T5.SUBCLASSCD = T2.SUBCLASSCD ";
    	$query .= "ORDER BY T2.SUBCLASSCD ";

        return $query;
    }


    //ＣＳＶ出力
    function getCsvData($model)
    {
        $test_name = ($model->field["TESTKINDCD"] == "01") ? "INTR" : "TERM" ;  //成績種別
        $test_name = "SEM" . $model->field["GAKKI"] . "_" . $test_name . "_SCORE";  //成績種別

            //在籍の表
    	$query  = "WITH SCHNO_A AS( ";
    	$query .= "    SELECT  T2.SCHREGNO, T3.NAME, T4.HR_NAME, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ";
    	$query .= "    FROM    SCHREG_REGD_DAT T2, ";
    	$query .= "            SCHREG_BASE_MST T3, ";
    	$query .= "            SCHREG_REGD_HDAT T4 ";
    	$query .= "    WHERE   T2.YEAR = '" . $model->control["年度"] . "' AND ";
    	$query .= "            T2.SEMESTER = '" . $model->field["GAKKI"] . "' AND ";
    	$query .= "            T2.GRADE = '" . substr($model->field["GRADE_HR_CLASS"],0,2) . "' AND ";
    	$query .= "            T2.SCHREGNO = T3.SCHREGNO AND T4.YEAR = T2.YEAR AND ";
    	$query .= "            T4.SEMESTER = T2.SEMESTER AND T4.GRADE = T2.GRADE AND ";
    	$query .= "            T4.HR_CLASS = T2.HR_CLASS ";
    	$query .= "    ) ";

		    //成績の表
    	$query .= ",RECORD_A AS( ";
    	$query .= "	SELECT  SCHREGNO, SUBCLASSCD, CHAIRCD,'CMN' AS CMN_FLG, ";
    	$query .= "		    " . $test_name . " AS SCORE ";
    	$query .= "	FROM    RECORD_DAT T1 ";
    	$query .= "	WHERE   YEAR='" . $model->control["年度"] . "' AND ";
    	$query .= " 	    " . $test_name . " IS NOT NULL ";
    	$query .= "    ) ";

		    //学級：平均点および順位の人数
    	$query .= ",AVG_CLS AS ( ";
    	$query .= "    SELECT  COUNT(*) AS CNT_CLS, ";
    	$query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS AVG_CLS, ";
    	$query .= "            SUBCLASSCD ";
    	$query .= "    FROM    SCHNO_A T1 ";
    	$query .= "            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    	$query .= "    WHERE   GRADE||HR_CLASS = '" . $model->field["GRADE_HR_CLASS"] . "' ";
    	$query .= "    GROUP BY SUBCLASSCD ";
    	$query .= "    ) ";

		    //学年：平均点および順位の人数
    	$query .= ",AVG_GRD AS ( ";
    	$query .= "    SELECT  COUNT(*) AS CNT_GRD, ";
    	$query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS AVG_GRD, ";
    	$query .= "            SUBCLASSCD ";
    	$query .= "    FROM    SCHNO_A T1 ";
    	$query .= "            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    	$query .= "    GROUP BY SUBCLASSCD ";
    	$query .= "    ) ";

		    //順位：個人
    	$query .= ",RNK_GRD_CLS AS ( ";
    	$query .= "    SELECT  RANK() OVER (PARTITION BY SUBCLASSCD ORDER BY SCORE DESC) AS RNK_GRD, ";
    	$query .= "            RANK() OVER (PARTITION BY SUBCLASSCD,HR_CLASS ORDER BY SCORE DESC) AS RNK_CLS, ";
    	$query .= "            T2.SCHREGNO, ";
    	$query .= "            SUBCLASSCD ";
    	$query .= "    FROM    SCHNO_A T1 ";
    	$query .= "            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    	$query .= "    ) ";

		    //総合成績：学級順位：個人
    	$query .= ",RNK_CLS_SCH AS ( ";
    	$query .= "    SELECT  T2.SCHREGNO, ";
    	$query .= "            SUM(SCORE) AS SCORE_SUM, ";
    	$query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS SCORE_AVG, ";
    	$query .= "            RANK() OVER (ORDER BY AVG(FLOAT(SCORE)) DESC) AS RNK_CLS_SCH ";
    	$query .= "    FROM    SCHNO_A T1 ";
    	$query .= "            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    	$query .= "    WHERE   GRADE||HR_CLASS = '" . $model->field["GRADE_HR_CLASS"] . "' ";
    	$query .= "    GROUP BY T2.SCHREGNO ";
    	$query .= "    ) ";

		    //総合成績：学年順位：個人
    	$query .= ",RNK_GRD_SCH AS ( ";
    	$query .= "    SELECT  T2.SCHREGNO, ";
    	$query .= "            RANK() OVER (ORDER BY AVG(FLOAT(SCORE)) DESC) AS RNK_GRD_SCH ";
    	$query .= "    FROM    SCHNO_A T1 ";
    	$query .= "            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    	$query .= "    GROUP BY T2.SCHREGNO ";
    	$query .= "    ) ";

		    //総合成績：学級順位の人数
    	$query .= ",CNT_CLS AS ( ";
    	$query .= "    SELECT  COUNT(*) AS CNT,'CMN' AS CMN_FLG FROM RNK_CLS_SCH ";
    	$query .= "    ) ";

		    //総合成績：学年順位の人数
    	$query .= ",CNT_GRD AS ( ";
    	$query .= "    SELECT  COUNT(*) AS CNT,'CMN' AS CMN_FLG FROM RNK_GRD_SCH ";
    	$query .= "    ) ";

		    //総合成績：学級平均
    	$query .= ",CLSAVG AS ( ";
    	$query .= "    SELECT  'CMN' AS CMN_FLG, ";
    	$query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS CLSAVG ";
    	$query .= "    FROM    SCHNO_A T1 ";
    	$query .= "            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    	$query .= "    WHERE   GRADE||HR_CLASS = '" . $model->field["GRADE_HR_CLASS"] . "' ";
    	$query .= "    ) ";

		    //総合成績：学年平均
    	$query .= ",GRDAVG AS ( ";
    	$query .= "    SELECT  'CMN' AS CMN_FLG, ";
    	$query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS GRDAVG ";
    	$query .= "    FROM    SCHNO_A T1 ";
    	$query .= "            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    	$query .= "    ) ";

			//メイン表
    	$query .= "SELECT ";
    	$query .= "     T1.HR_NAME, ";
    	$query .= "     T1.SCHREGNO, ";
    	$query .= "     INT(T1.ATTENDNO) AS ATTENDNO, ";
    	$query .= "     T1.NAME, ";
    	$query .= "     T2.SUBCLASSCD, ";
    	$query .= "     T5.SUBCLASSABBV, ";
    	$query .= "		T2.SCORE AS SCORE1, ";
    	$query .= "		S1.AVG_CLS AS SCORE2, ";
    	$query .= "		S2.AVG_GRD AS SCORE4, ";
    	$query .= "		' '||RTRIM(CHAR(S3.RNK_CLS))||'/'||RTRIM(CHAR(S1.CNT_CLS)) AS SCORE3, ";
    	$query .= "		' '||RTRIM(CHAR(S3.RNK_GRD))||'/'||RTRIM(CHAR(S2.CNT_GRD)) AS SCORE5, ";
    	$query .= "		R1.SCORE_SUM AS SCORE1_SUM, ";
    	$query .= "		'-' AS SCORE2_SUM,'-' AS SCORE3_SUM,'-' AS SCORE4_SUM,'-' AS SCORE5_SUM, ";
    	$query .= "		R1.SCORE_AVG AS SCORE1_AVG, ";
    	$query .= "		G1.CLSAVG AS SCORE2_AVG, ";
    	$query .= "		G2.GRDAVG AS SCORE4_AVG, ";
    	$query .= "		' '||RTRIM(CHAR(R1.RNK_CLS_SCH))||'/'||RTRIM(CHAR(R3.CNT)) AS SCORE3_AVG, ";
    	$query .= "		' '||RTRIM(CHAR(R2.RNK_GRD_SCH))||'/'||RTRIM(CHAR(R4.CNT)) AS SCORE5_AVG ";
    	$query .= "FROM SCHNO_A T1 ";
    	$query .= "     INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    	$query .= "		LEFT JOIN SUBCLASS_MST T5 ON T5.SUBCLASSCD = T2.SUBCLASSCD ";
    	$query .= "		LEFT JOIN AVG_CLS S1 ON S1.SUBCLASSCD = T2.SUBCLASSCD ";
    	$query .= "		LEFT JOIN AVG_GRD S2 ON S2.SUBCLASSCD = T2.SUBCLASSCD ";
    	$query .= "		LEFT JOIN RNK_GRD_CLS S3 ON S3.SCHREGNO=T2.SCHREGNO AND S3.SUBCLASSCD=T2.SUBCLASSCD ";
    	$query .= "     LEFT JOIN CLSAVG G1 ON G1.CMN_FLG = T2.CMN_FLG ";
    	$query .= "     LEFT JOIN GRDAVG G2 ON G2.CMN_FLG = T2.CMN_FLG ";
    	$query .= "     LEFT JOIN RNK_CLS_SCH R1 ON R1.SCHREGNO = T2.SCHREGNO ";
    	$query .= "     LEFT JOIN RNK_GRD_SCH R2 ON R2.SCHREGNO = T2.SCHREGNO ";
    	$query .= "     LEFT JOIN CNT_CLS R3 ON R3.CMN_FLG = T2.CMN_FLG ";
    	$query .= "     LEFT JOIN CNT_GRD R4 ON R4.CMN_FLG = T2.CMN_FLG ";
    	$query .= "WHERE   T1.SCHREGNO IN ('" . implode("','", $model->select_data["selectdata"]) . "') ";
    	$query .= "ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T2.SUBCLASSCD ";

        return $query;
    }


}
?>
