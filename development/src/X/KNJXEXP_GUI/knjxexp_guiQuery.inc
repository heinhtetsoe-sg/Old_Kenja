<?php

require_once('for_php7.php');

class knjxexp_guiQuery extends Query {

   //登録済み学年
   function GetGrade($model)
   {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //対象生徒
    function GetStudents($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     FISCALYEAR(T2.ENT_DATE) AS ENT_YEAR, ";
        $query .= "     FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR, ";
        $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) AS DUMMY1, ";
        $query .= "     SUBSTR(T1.SCHREGNO, 5, 4) AS DUMMY2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST L1 ON L1.YEAR = T1.YEAR AND L1.SCHREGNO = T1.SCHREGNO AND L1.BASE_SEQ = '004'  ";
        $query .= "     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'A025' ";
        $query .= "                           AND N1.NAMECD2 = L1.BASE_REMARK1 ";
        $query .= "                           AND N1.NAMESPARE2 = '1' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS  ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T1.YEAR AND T6.GRADE = T1.GRADE ";
            $query .= " AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T3.GRADE = '" .$model->grade."' AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.GRADE    = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     FISCALYEAR(T2.ENT_DATE) AS ENT_YEAR, ";
        $query .= "     FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR, ";
        $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) AS DUMMY1, ";
        $query .= "     SUBSTR(T1.SCHREGNO, 5, 4) AS DUMMY2 ";
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS  ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'A025' ";
        $query .= "                           AND N1.NAMECD2 = T2.HANDICAP ";
        $query .= "                           AND N1.NAMESPARE2 = '1' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T1.YEAR AND T6.GRADE = T1.GRADE ";
            $query .= " AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     L1.SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_BASE_YEAR_DETAIL_MST L1 ";
        $query .= "                 WHERE ";
        $query .= "                         L1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "                     AND L1.BASE_SEQ = '004' ";
        $query .= "                 ) TAISHOUGAI ON TAISHOUGAI.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND T3.GRADE = '".$model->grade."' ";
        $query .= " AND TAISHOUGAI.SCHREGNO IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     GRADE,";
        $query .= "     HR_CLASS,";
        $query .= "     ATTENDNO ";

        return $query;
   }
}
?>
