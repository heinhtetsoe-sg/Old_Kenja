<?php

require_once('for_php7.php');

class knjf015Query extends Query {
    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
//        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR = T1.YEAR ";
            $query .= "                         AND L1.GRADE = T1.GRADE ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY VALUE";
//        }
//        //参照・更新可（制限付き）
//        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
//            $query  = " SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
//            $query .= " FROM SCHREG_REGD_HDAT ";
//            $query .= " LEFT JOIN ";
//            $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR = T1.YEAR ";
//            $query .= "                         AND L1.GRADE = T1.GRADE ";
//            $query .= " WHERE ";
//            $query .= "         YEAR       = '".$model->control["年度"]."' ";
//            $query .= "     AND SEMESTER   = '".$model->field["SEMESTER"]."' ";
//            $query .= "     AND (  TR_CD1  = '".STAFFCD."' ";
//            $query .= "         OR TR_CD2  = '".STAFFCD."' ";
//            $query .= "         OR TR_CD3  = '".STAFFCD."' ";
//            $query .= "         OR SUBTR_CD1  = '".STAFFCD."' ";
//            $query .= "         OR SUBTR_CD2  = '".STAFFCD."' ";
//            $query .= "         OR SUBTR_CD3  = '".STAFFCD."') ";
//            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
//                $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
//            }
//            $query .= " ORDER BY VALUE";
//        }

        return $query;
    }

    //種類
    function getMonth($model) {
        $query  = " WITH T_COMMON (VALUE,LABEL,SORT) AS ( ";
        $query .= "     VALUES('04','4月', 1) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('05','5月', 2) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('06','6月', 3) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('07','7月', 4) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('08','8月', 5) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('09','9月', 6) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('10','10月', 7) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('11','11月', 8) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('12','12月', 9) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('01','1月', 10) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('02','2月', 11) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('03','3月', 12) ";
        $query .= "  ), SORT_MIN_MAX AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(T2.SORT) AS SORT_MIN ";
        $query .= "       , MAX(T3.SORT) AS SORT_MAX ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         , T_COMMON T2 ";
        $query .= "         , T_COMMON T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND T2.VALUE = RIGHT('00' || RTRIM(CAST(MONTH(T1.SDATE) AS CHAR(2))),2) ";
        $query .= "         AND T3.VALUE = RIGHT('00' || RTRIM(CAST(MONTH(T1.EDATE) AS CHAR(2))),2) ";
        $query .= "  ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     T_COMMON T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SORT BETWEEN (SELECT SORT_MIN FROM SORT_MIN_MAX) AND (SELECT SORT_MAX FROM SORT_MIN_MAX) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SORT ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }
}
?>
