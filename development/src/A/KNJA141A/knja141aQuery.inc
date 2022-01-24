<?php

require_once('for_php7.php');

class knja141aQuery extends Query {
    //中学か高校かを判断
    function getSchoolJudge() {
        $db = Query::dbCheckOut();

        $school = $db->getOne("SELECT COUNT(*) FROM SCHOOL_MST WHERE SCHOOLNAME1 LIKE '%中学%'");
        Query::dbCheckIn($db);

        return ($school) ? "J" : "H" ;
    }

    //学期名取得
    function getSemeMst() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ";

        return $query;
    }

    //クラス一覧取得
    function getHrClassAuth() {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '" .CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "          YEAR      = '".CTRL_YEAR."' ";
            $query .= "     AND  SEMESTER  = '".CTRL_SEMESTER."' ";
            $query .= "     AND (TR_CD1    = '".STAFFCD."' ";
            $query .= "      OR  TR_CD2    = '".STAFFCD."' ";
            $query .= "      OR  TR_CD3    = '".STAFFCD."' ";
            $query .= "      OR  SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "      OR  SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "      OR  SUBTR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }

    //クラス一覧取得
    function getGradeCdFlg($gradeHrclass) {
        $query  = " SELECT ";
        $query .= "     CASE ";
        $query .= "         WHEN GDAT.GRADE_CD = '01' THEN '3' ";
        $query .= "         WHEN GDAT.GRADE_CD = '02' THEN '2' ";
        $query .= "         WHEN GDAT.GRADE_CD = '03' THEN '1' ";
        $query .= "         ELSE '3' ";
        $query .= "     END ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON HDAT.YEAR  = GDAT.YEAR ";
        $query .= "                                    AND HDAT.GRADE = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND HDAT.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND HDAT.GRADE || HDAT.HR_CLASS = '".$gradeHrclass."' ";

        return $query;
    }

    //生徒取得(2:身分証明書)
    function getSchno2($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR     = T1.YEAR ";
        $query .= "                                  AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "                                  AND T3.GRADE    = T1.GRADE ";
        $query .= "                                  AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
