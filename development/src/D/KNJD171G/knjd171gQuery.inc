<?php

require_once('for_php7.php');

class knjd171gQuery extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'H' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER    = '".$model->field["SEMESTER"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3, T1.SUBTR_CD1, T1.SUBTR_CD2, T1.SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model) {
        $date = str_replace("/","-",$model->field["DATE"]);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     ( ";
                            //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                            //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "         EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                 WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                     ((S1.GRD_DIV IN ('2','3') AND ";
        $query .= "                       S1.GRD_DATE < CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                                          THEN T2.EDATE ";
        $query .= "                                          ELSE '$date' END) OR ";
        $query .= "                      (S1.ENT_DIV IN ('4','5') AND ";
        $query .= "                       S1.ENT_DATE > CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                                          THEN T2.EDATE ";
        $query .= "                                          ELSE '".$date."' END))) OR ";
                            //留学(1)・休学(2)者
        $query .= "         EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                 WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                       S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                       CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                            THEN T2.EDATE ";
        $query .= "                            ELSE '".$date."' END ";
        $query .= "                       BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ";
        $query .= "     ) ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
