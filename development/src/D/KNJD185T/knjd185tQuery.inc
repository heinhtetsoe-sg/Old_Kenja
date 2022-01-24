<?php

require_once('for_php7.php');

class knjd185tQuery extends Query {

    //学期
    function getSemester()
    {
        $query  = " SELECT";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //出欠を出力しない学期
    function getAttendSemester()
    {
        $query  = " SELECT";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
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
            $query .= "     T2.SEMESTER = '".$semester."' AND ";
            $query .= "     T1.GRADE = T2.GRADE AND ";
            $query .= "     T1.SCHOOL_KIND = 'J' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
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
            $query .= "     T2.SEMESTER = '".$semester."' AND ";
            $query .= "     T1.GRADE = T2.GRADE AND ";
            $query .= "     T1.SCHOOL_KIND = 'J' AND ";
            $query .= "    (T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "     T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "     T2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "     T2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "     T2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "     T2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($year, $semester, $grade_hr_class, $date)
    {
        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '$year' AND ";
        $query .= "        T1.SEMESTER = '$semester' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T1.GRADE||T1.HR_CLASS = '$grade_hr_class' AND ( ";
                           //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                           //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '$date' ";
        $query .= "                              THEN T2.EDATE ELSE '$date' END) OR ";
        $query .= "                     (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '$date' ";
        $query .= "                              THEN T2.EDATE ELSE '$date' END)) ) OR ";
                           //留学(1)・休学(2)者
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                      S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                      CASE WHEN T2.EDATE < '$date' ";
        $query .= "                           THEN T2.EDATE ELSE '$date' END  ";
        $query .= "                      BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }


    //対象者リスト
    function getSchno($year, $semester, $grade_hr_class)
    {
        $query  = "";
        $query .= "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW ";
        $query .= "FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2 ";
        $query .= "WHERE T2.YEAR = '$year' AND ";
        $query .= "      T2.SEMESTER = '$semester' AND ";
        $query .= "      T2.GRADE || T2.HR_CLASS = '$grade_hr_class' AND ";
        $query .= "      T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }


}
?>
