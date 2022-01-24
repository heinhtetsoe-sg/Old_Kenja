<?php

require_once('for_php7.php');

class knjd672fQuery extends Query {
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

    //担当取得
    function getTantou($model) {
        $query  = " SELECT ";
        $query .= "     T2.STAFFCD AS VALUE, ";
        $query .= "     T2.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T2 ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST T3 ON T3.YEAR = T2.YEAR AND T3.STAFFCD = T2.STAFFCD AND T3.STAFF_SEQ = '005' AND T3.FIELD1 = '1052' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST T4 ON T4.YEAR = T2.YEAR AND T4.STAFFCD = T2.STAFFCD AND T4.STAFF_SEQ = '006' AND T4.FIELD1 = '1052' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST T5 ON T5.YEAR = T2.YEAR AND T5.STAFFCD = T2.STAFFCD AND T5.STAFF_SEQ = '007' AND T5.FIELD1 = '1052' ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    (T3.STAFFCD IS NOT NULL OR ";
        $query .= "     T4.STAFFCD IS NOT NULL OR ";
        $query .= "     T5.STAFFCD IS NOT NULL) ";
        $query .= " ORDER BY ";
        $query .= "     T2.STAFFCD ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'J' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER    = '".$model->field["SEMESTER"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3  = '".STAFFCD."') ";
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
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_DAT T1, ";
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
        $query .= "     SCHREG_REGD_FI_DAT T1, ";
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

    //進学希望一覧取得
    function getShingaku() {
        $query  = " SELECT ";
        $query .= "     T1.QUESTIONNAIRECD AS VALUE, ";
        $query .= "     T1.QUESTIONNAIRECD || '　' || T1.QUESTIONNAIRENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.QUESTIONNAIRECD IN ( ";
        $query .= "         SELECT ";
        $query .= "             T2.QUESTIONNAIRECD ";
        $query .= "         FROM ";
        $query .= "             COURSE_HOPE_DAT T2 ";
        $query .= "         WHERE ";
        $query .= "             T2.COURSE_KIND = '1' AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "         GROUP BY ";
        $query .= "             T2.QUESTIONNAIRECD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
