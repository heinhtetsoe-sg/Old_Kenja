<?php

require_once('for_php7.php');


class knja142Query extends Query {

    //学期マスタ
    function getSemeMst($year,$semester)
    {
        $query  = "SELECT * FROM SEMESTER_MST WHERE YEAR='".$year."' AND SEMESTER='".$semester."' ";

        return $query;
    }

    //有効期限取得(1:仮身分証明書)
    function getLastDay($sdate)
    {
        $query  = "VALUES LAST_DAY(DATE('".str_replace("/","-",$sdate)."'))";

        return $query;
    }

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
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得(1:仮身分証明書)---2005.03.31
    function getSchno1($model,$year,$semester)
    {
        $query  = "SELECT T1.SCHREGNO, ";
        $query .= "       L1.HR_NAME, ";
        $query .= "       T1.ATTENDNO, ";
        $query .= "       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ";
        $query .= "            THEN T3.NAME ELSE T2.NAME END AS NAME ";
        $query .= "FROM   CLASS_FORMATION_DAT T1 ";
        //在学生
        $query .= "       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ";
        //新入生
        $query .= "       LEFT JOIN FRESHMAN_DAT T3 ON T3.ENTERYEAR=T1.YEAR AND T3.SCHREGNO=T1.SCHREGNO ";

        $query .= "       LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "            AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "            AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ";
/*
        //在学生
        if( substr($model->field["GRADE_HR_CLASS"],0,2) > "01" ) {
            $query .= "       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ";
        //新入生
        } else {
//        if( substr($model->field["GRADE_HR_CLASS"],0,2) == "01" ) 
            $query .= "       INNER JOIN FRESHMAN_DAT T2 ON T2.ENTERYEAR=T1.YEAR AND T2.SCHREGNO=T1.SCHREGNO ";
        }
*/
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.ATTENDNO ";

        return $query;
    }
    //生徒取得(2:身分証明書)
    function getSchno2($model,$year,$semester)
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
