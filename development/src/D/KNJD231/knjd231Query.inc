<?php

require_once('for_php7.php');


class knjd231Query extends Query {

    //年組取得（権限チェック）
    function getAuth($semester)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."'";
			$query .= "AND SEMESTER='".$semester ."'";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
			$query .= "AND SEMESTER='".$semester ."' ";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
		}

        return $query;
    }


    //学期取得
    function getSemester_mst()
    {
		$query  = "SELECT SEMESTER AS VALUE,SEMESTERNAME AS LABEL ";
		$query .= "FROM SEMESTER_MST ";
		$query .= "WHERE YEAR='" .CTRL_YEAR ."'";
		$query .= "ORDER BY SEMESTER";

        return $query;
    }


    //組名称取得
    function getHr_Name($semester,$grade_class)
    {
		$query  = "SELECT HR_NAME ";
		$query .= "FROM SCHREG_REGD_HDAT ";
		$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
		$query .= "AND SEMESTER='".$semester ."' ";
		$query .= "AND GRADE || HR_CLASS = '".$grade_class ."' ";

        return $query;
    }


    //CSV出力
    function pre_stat($model,$grade_hr_class)
    {
		if ($model->field["GAKKI"] != "9") {
			$t_field = "SEM".$model->field["GAKKI"]."_REC";
		} else {
			$t_field = "GRADE_RECORD";
		}
		$subject_U = "89";

		//	該当生徒の表
			$query = "WITH SCHNO AS ("
						. "SELECT DISTINCT "
							. "W1.SCHREGNO,"
							. "W1.ATTENDNO,"
							. "W1.GRADE,"
							. "W1.COURSECD,"
							. "W1.MAJORCD,"
							. "W1.COURSECODE,"
							. "W2.NAME "
						. "FROM "
							. "SCHREG_REGD_DAT W1,"
							. "SCHREG_BASE_MST W2 "
						. "WHERE "
								. "W1.YEAR = '" . CTRL_YEAR . "' ";
			if( $model->field["GAKKI"]!="9" )	$query = $query
							. "AND W1.SEMESTER = '" . $model->field["GAKKI"] . "' ";
			else						$query = $query
							. "AND W1.SEMESTER = (SELECT MAX(SEMESTER) "
												. "FROM SCHREG_REGD_DAT "
												. "WHERE YEAR = '" . CTRL_YEAR . "')";
			$query = $query
							. "AND W1.GRADE || W1.HR_CLASS ='".$grade_hr_class."' "
							. "AND W1.SCHREGNO  = W2.SCHREGNO "
							. "AND (W2.GRD_DATE IS NULL OR W2.GRD_DATE>'" . str_replace("/","-",$model->control["学期終了日付"][$model->field["GAKKI"]]) . "'))"
		//	メイン表	// 04/11/12Modify RECORDSEMES_DAT,RECORDGRADE_DAT->KIN_RECORD_DAT
				. "SELECT "
					. "S2.SCHREGNO,"	//CSV出力用項目
					. "ATTENDNO,"
					. "NAME,"
					. "SUBCLASSNAME,"
					. "S1.SUBCLASSCD,";
			if( $model->field["GAKKI"]!="9" )	$query = $query
					. $t_field . " AS VALUATION,";
			else								$query = $query
					. " CASE JUDGE_PATTERN WHEN 'A' THEN INT(A_PATTERN_ASSESS) "
												. "WHEN 'B' THEN INT(B_PATTERN_ASSESS) "
												. "WHEN 'C' THEN INT(C_PATTERN_ASSESS) "
												. "ELSE NULL END AS VALUATION,";
			$query = $query
					. "ABSENT,"
					. "ABSENT2,"
					. "S4.CREDITS,"
					. "SUM(S4.CREDITS) OVER (PARTITION BY S1.SCHREGNO) AS A_CREDITS "
				. "FROM "
					. "KIN_RECORD_DAT S1 "
					. "INNER JOIN SCHNO S2 ON S1.SCHREGNO = S2.SCHREGNO "
					. "LEFT JOIN CREDIT_MST S4 ON S4.YEAR = '" . CTRL_YEAR . "' "
												. "AND S4.GRADE = S2.GRADE "
												. "AND S4.COURSECD = S2.COURSECD "
												. "AND S4.MAJORCD = S2.MAJORCD "
												. "AND S4.COURSECODE = S2.COURSECODE "
												. "AND S4.SUBCLASSCD = S1.SUBCLASSCD "
					. "LEFT JOIN("
						. "SELECT "
							. "SCHREGNO,"
							. "SUBCLASSCD,"
							. "SUM(VALUE(SICK,0)+VALUE(NOTICE,0)+VALUE(NURSEOFF,0))+SUM(NONOTICE) AS ABSENT," // 04/05/10変更
							. "SUM(NONOTICE) AS ABSENT2 " // 04/05/10
						. "FROM "
							. "ATTEND_SUBCLASS_DAT "
						. "WHERE "
								. "YEAR = '" . CTRL_YEAR . "' ";
			if( $model->field["GAKKI"]!="9" )	$query = $query
							. "AND SEMESTER = '" . $model->field["GAKKI"] . "' ";
			$query = $query
						. "GROUP BY "
							. "SCHREGNO,"
							. "SUBCLASSCD "
					. ")S3 ON S3.SCHREGNO = S1.SCHREGNO AND S3.SUBCLASSCD = S1.SUBCLASSCD "
					. "INNER JOIN SUBCLASS_MST S5 ON S5.SUBCLASSCD = S1.SUBCLASSCD "
				. "WHERE "
						. "S1.YEAR = '" . CTRL_YEAR . "' AND "
						. "SUBSTR(S1.SUBCLASSCD,1,2)<='".$subject_U."' AND ";
			if( $model->field["GAKKI"]!="9" )	$query = $query
						. "(S1.".$t_field." IS NOT NULL AND S1.".$t_field."<=". $model->field["ASSESS"] .") ";
			else								$query = $query
						. "(CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS "
											. "WHEN 'B' THEN B_PATTERN_ASSESS "
											. "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END IS NOT NULL AND "
						. "CASE JUDGE_PATTERN WHEN 'A' THEN INT(A_PATTERN_ASSESS) "
											. "WHEN 'B' THEN INT(B_PATTERN_ASSESS) "
											. "WHEN 'C' THEN INT(C_PATTERN_ASSESS) "
											. "ELSE NULL END<=". $model->field["ASSESS"] .") ";
			$query = $query
				. "ORDER BY "
					. "ATTENDNO ";

        return $query;
    }


}
?>
