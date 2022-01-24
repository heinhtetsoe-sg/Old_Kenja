<?php

require_once('for_php7.php');

// kanji=漢字
class knja146Query extends Query {

    //学期マスタ
    function getSemeMst($year,$semester)
    {
        $query  = "SELECT * FROM SEMESTER_MST WHERE YEAR='".$year."' AND SEMESTER='".$semester."' ";

        return $query;
    }

    //月末日取得
    function getLastDay($sdate)
    {
        $query  = "VALUES LAST_DAY(DATE('".str_replace("/","-",$sdate)."'))";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "  FROM SCHREG_REGD_HDAT ";
            $query .= " WHERE YEAR = '" .$year ."' ";
            $query .= "   AND SEMESTER = '".$semester ."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "  AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= "    OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }
            $query .= "ORDER BY 1 ";

        return $query;
    }

    //生徒取得(1:Ａ４用紙(新入生))
    function getSchno1($model,$year,$semester)
    {
        $query  = "SELECT T1.SCHREGNO, ";
        $query .= "       T1.ATTENDNO, ";
        $query .= "       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ";
        $query .= "            THEN T3.NAME ELSE T2.NAME END AS NAME ";
        $query .= "       ,T1.GRADE,T1.HR_CLASS,T4.HR_NAME ";
        $query .= "FROM   CLASS_FORMATION_DAT T1 ";
        $query .= "       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO "; //在学生
        $query .= "       LEFT JOIN FRESHMAN_DAT T3 ON T3.ENTERYEAR=T1.YEAR AND T3.SCHREGNO=T1.SCHREGNO "; //新入生
        $query .= "       LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR=T1.YEAR AND T4.SEMESTER=T1.SEMESTER AND ";
        $query .= "                                        T4.GRADE=T1.GRADE AND T4.HR_CLASS=T1.HR_CLASS ";
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.ATTENDNO ";

        return $query;
    }
    //生徒取得(2:カード(在籍), 3:Ａ４用紙(在籍))
    function getSchno2($model,$year,$semester)
    {
        $query  = "SELECT T1.SCHREGNO,T1.ATTENDNO,T1.GRADE,T1.HR_CLASS,T2.NAME,T3.HR_NAME ";
        $query .= "FROM   SCHREG_REGD_DAT T1 ";
        $query .= "       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ";
        $query .= "       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND ";
        $query .= "                                        T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ";
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.ATTENDNO ";

        return $query;
    }

}
?>
