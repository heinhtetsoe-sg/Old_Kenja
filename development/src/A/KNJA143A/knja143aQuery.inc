<?php

require_once('for_php7.php');


class knja143aQuery extends Query {

    //学期マスタ
    function getSemeMst($year,$semester)
    {
		$query  = "SELECT * FROM SEMESTER_MST WHERE YEAR='".$year."' AND SEMESTER='".$semester."' ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     INT(GRADE) ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year, $semester, $grade)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."'";
			$query .= "AND SEMESTER='".$semester ."'";
			$query .= "AND GRADE='".$grade ."'";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."' ";
			$query .= "AND SEMESTER='".$semester ."' ";
			$query .= "AND GRADE='".$grade ."'";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
		}

        return $query;
    }

    //生徒取得
    function getSchno($model,$year,$semester)
    {
		$query  = "SELECT T1.SCHREGNO, ";
		$query .= "       L1.HR_NAME || '　' || T1.ATTENDNO || '番' || '　' || T2.NAME AS NAME ";
		$query .= "FROM   SCHREG_REGD_DAT T1 ";
		$query .= "       LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
		$query .= "            AND L1.SEMESTER = T1.SEMESTER ";
		$query .= "            AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS, ";
		$query .= "       SCHREG_BASE_MST T2 ";
		$query .= "WHERE  T1.YEAR       = '".$year."' AND ";
		$query .= "       T1.SEMESTER   = '".$semester."' AND ";
		$query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
		$query .= "       T2.SCHREGNO   = T1.SCHREGNO ";
		$query .= "ORDER BY T1.ATTENDNO ";

        return $query;
    }

}
?>
