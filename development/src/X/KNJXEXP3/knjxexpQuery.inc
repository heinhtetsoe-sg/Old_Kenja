<?php

require_once('for_php7.php');

class knjxexpQuery extends Query {
   //学年と組
   function GetHr_Class($model)
   {
        $query = "";
        $query .= "SELECT ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T1.HR_NAME, ";
        $query .= "  T1.HR_NAMEABBV, ";
        $query .= "  T2.STAFFCD, ";
        $query .= "  T2.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "  SCHREG_REGD_HDAT T1 ";
        $query .= "  LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = T1.YEAR ";
        $query .= "                               AND L1.GRADE = T1.GRADE, ";
        $query .= "  STAFF_MST T2 ";
        $query .= "WHERE ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= " (T1.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "  T1.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "  T1.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "  T1.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "  T1.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "  T1.SUBTR_CD3 = '". STAFFCD ."') AND";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "  L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "  L1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  T1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

#        echo $query;
        return $query;
    }
    //コース取得
    function GetCourseCode()
    {
        $query = " SELECT ";
        $query .= "   COURSECODE, ";
        $query .= "   COURSECODENAME ";
        $query .= " FROM ";
        $query .= "   COURSECODE_MST ";

        return $query;
    }
    //卒業年度
    function GetGrdYear()
    {
        $query = " SELECT DISTINCT ";
        $query .= "   FISCALYEAR(GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_BASE_MST ";

        return $query;
    }
    function GetStudents($model)
    {
        if ($model->cmd == "list" && $model->grade){
            $grade = $model->grade;
        }else if ($model->cmd == "list" && !$model->grade){
            $grade = "";
        }else if ($model->cmd == "search" && $model->search["GRADE"]){
            $grade = $model->search["GRADE"];
        }

        $query = " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T3.HR_NAME, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.SEX, ";
        $query .= "   T2.NAME_SHOW, ";
        $query .= "   T2.NAME_KANA, ";
        if ($model->mode == "grd"){   //卒業
            $query .= "   T2.GRD_SEMESTER, ";
            $query .= "   T2.GRD_GRADE, ";
            $query .= "   T2.GRD_HR_CLASS, ";
            $query .= "   T2.GRD_ATTENDNO, ";
        }
        $query .= "   FISCALYEAR(T2.ENT_DATE) AS ENT_YEAR, ";
        $query .= "   FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR, ";
        $query .= "   T4.SCHOOL_KIND, ";
        $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) AS DUMMY1, ";
        $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) AS DUMMY2";
        $query .= " FROM ";
        if ($model->mode == "grd"){   //卒業
            $query .= "   GRD_REGD_DAT T1 ";
            $query .= "   INNER JOIN GRD_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "   INNER JOIN GRD_REGD_HDAT T3 ON ";
            $query .= "   T1.YEAR     = T3.YEAR AND ";
            $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
            $query .= "   T1.GRADE    = T3.GRADE AND ";
            $query .= "   T1.HR_CLASS = T3.HR_CLASS ";
        }else if($model->mode == "ungrd"){ //在籍
            $query .= "   SCHREG_REGD_DAT T1 ";
            $query .= "   INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "   INNER JOIN SCHREG_REGD_HDAT T3 ON ";
            $query .= "   T1.YEAR     = T3.YEAR AND ";
            $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
            $query .= "   T1.GRADE    = T3.GRADE AND ";
            $query .= "   T1.HR_CLASS = T3.HR_CLASS ";
        }
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T4 ON ";
        $query .= "   T1.YEAR     = T4.YEAR AND ";
        $query .= "   T1.GRADE = T4.GRADE ";
        $query .= " WHERE ";
        if (isset($grade) && $model->mode == "ungrd" ){
            $query .= "   T1.GRADE || '-' || T1.HR_CLASS = '" .$grade."' AND ";
        }
        if ($model->mode == "grd" && $model->search["HR_CLASS"]){
            $query .= "   T2.GRD_HR_CLASS = '" .sprintf("%03s", $model->search["HR_CLASS"]). "' AND ";
        }
        if($model->mode == "ungrd"){  //在籍生
            $query .= "   T1.YEAR = '" .CTRL_YEAR. "' AND ";
            $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER. "' AND ";
        }else if($model->mode == "grd" && $model->search["GRD_YEAR"]){  //卒業生
            $query .= "   FISCALYEAR(T2.GRD_DATE) = '" .$model->search["GRD_YEAR"]. "' AND ";
        }
        if ($model->search["COURSECODE"]){
            $query .= "   T1.COURSECODE = '" .$model->search["COURSECODE"]. "' AND ";
        }
        if ($model->search["NAME"]){
            $query .= "   T2.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' AND ";
        }
        if ($model->search["NAME_SHOW"]){
            $query .= "   T2.NAME_SHOW LIKE '%" .str_replace("'","''",$model->search["NAME_SHOW"]). "%' AND ";
        }
        if ($model->search["NAME_KANA"]){
            $query .= "   T2.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' AND ";
        }
        if ($model->search["NAME_ENG"]){
            $query .= "   T2.NAME_ENG LIKE '%" .str_replace("'","''",$model->search["NAME_ENG"]). "%' AND ";
        }
        if ($model->search["SEX"]){
            $query .= "   T2.SEX = '" .$model->search["SEX"]. "' AND ";
        }

        if($model->mode == "grd"){  //卒業生
            $query .= "   T1.YEAR     = FISCALYEAR(T2.GRD_DATE) AND ";
            $query .= "   T1.SEMESTER = T2.GRD_SEMESTER AND ";
            $query .= "   T1.GRADE    = T2.GRD_GRADE AND ";
            $query .= "   T1.HR_CLASS = T2.GRD_HR_CLASS AND ";
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "  T4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "  T4.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "   T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "   T1.GRADE,";
        $query .= "   T1.HR_CLASS,";
        if ($model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO") {
            $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) ";
        } else {
            $query .= "   T1.ATTENDNO ";
        }

//        echo $query;
        return $query;
   }
}
?>
