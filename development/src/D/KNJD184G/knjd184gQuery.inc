<?php

require_once('for_php7.php');
class knjd184gQuery extends Query {

    //学期
    function getSemester() {
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

    //年組取得（権限チェック）
    function getGradeHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.SCHOOL_KIND = 'H' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((T2.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD3  = '".STAFFCD."') ";
            $query .= "         OR (T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model) {
        $date = str_replace("/","-",$model->field["DATE"]);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST1 T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T2.GRADE = '".substr($grade_hr_class, 0, 2)."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     ( ";
                            //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                            //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "         EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                 WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                     ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                       CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                            THEN T2.EDATE ELSE '$date' END) OR ";
        $query .= "                      (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                       CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                            THEN T2.EDATE ELSE '".$date."' END))) OR ";
                            //留学(1)・休学(2)者
        $query .= "         EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                 WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                       S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                       CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                            THEN T2.EDATE ELSE '".$date."' END  ";
        $query .= "                       BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //対象者リスト
    function getSchno($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }
}
?>
