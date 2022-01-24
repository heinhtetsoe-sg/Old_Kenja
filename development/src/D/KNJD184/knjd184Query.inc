<?php

require_once('for_php7.php');

class knjd184Query extends Query {

    //学期
    function getSemester($model)
    {
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query  = " SELECT";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$setNameCd}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }


    //年組取得（権限チェック）
    function getAuth($semester)
    {
        $query  = " SELECT ";
        $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL, ";
        $query .= "     T2.TR_CD1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        if ($semester == "9") {
            $query .= " T2.SEMESTER = '".CTRL_SEMESTER ."' AND ";
        } else {
            $query .= " T2.SEMESTER = '".$semester."' AND ";
        }
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.SCHOOL_KIND = 'P' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "    AND (T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "         T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "         T2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "         T2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "         T2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "         T2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //対象者リスト
    function getSchno($year, $semester, $grade_hr_class)
    {
        $query  = "";
        $query .= "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW ";
        $query .= "FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2 ";
        $query .= "WHERE T2.YEAR = '$year' AND ";
        if ($semester == "9") {
            $query .= " T2.SEMESTER = '".CTRL_SEMESTER ."' AND ";
        } else {
            $query .= " T2.SEMESTER = '".$semester."' AND ";
        }
        $query .= "      T2.GRADE || T2.HR_CLASS = '$grade_hr_class' AND ";
        $query .= "      T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }


}
?>
