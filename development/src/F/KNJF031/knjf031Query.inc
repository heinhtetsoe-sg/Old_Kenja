<?php

require_once('for_php7.php');


class knjf031Query extends Query {

    //中学か高校かを判断
    function getSchoolJudge()
    {
        $db = Query::dbCheckOut();
        $school = $db->getOne("SELECT COUNT(*) FROM SCHOOL_MST WHERE SCHOOLNAME1 LIKE '%中学%'");
        Query::dbCheckIn($db);

        return ($school) ? "J" : "H" ;
    }

    //生徒データ
    function getSchno($model)
    {
		$query  = "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW,T2.GRADE,T2.HR_CLASS,T3.HR_NAME ";
		$query .= "FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2,SCHREG_REGD_HDAT T3 ";
		$query .= "WHERE T2.YEAR = '".CTRL_YEAR."' AND ";
		$query .= "		 T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
		$query .= "		 T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
		$query .= "		 T2.SCHREGNO = T1.SCHREGNO AND ";
		$query .= "		 T2.YEAR = T3.YEAR AND T2.SEMESTER = T3.SEMESTER AND ";
		$query .= "		 T2.GRADE = T3.GRADE AND T2.HR_CLASS = T3.HR_CLASS ";
		$query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }

}
?>
