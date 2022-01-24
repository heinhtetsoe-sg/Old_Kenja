<?php

require_once('for_php7.php');

class knja143nQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN ";
            $query .= "         SCHREG_REGD_GDAT T2 ON  T1.YEAR        = T2.YEAR ";
            $query .= "                             AND T1.GRADE       = T2.GRADE ";
            $query .= "                             AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1    = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2    = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3    = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getSchno($model, $year, $semester) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.HR_NAME || '　' || T1.ATTENDNO || '番' || '　' || T2.NAME AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR     = T1.YEAR ";
        $query .= "                                  AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                                  AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".$year."' ";
        $query .= "     AND T1.SEMESTER   = '".$semester."' ";
        $query .= "     AND T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
    
    function getStudentInfo($model, $flg) {
        $search_scregno = $model->SELECTED_DATA;
        $search_scregno = str_replace(",", "','", $search_scregno);
        $search_scregno = "'".$search_scregno."'";

        $query  = "WITH LASTYEARDAT AS (";
        $query .= " SELECT ";
        $query .= " L1.YEAR AS YEAR, ";
        $query .= " L1.SCHREGNO AS SCHREGNO, ";
        $query .= " L1.SEMESTER AS SEMESTER, ";
        $query .= " L2.HR_NAMEABBV AS G_OLD_HR_CLASS_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT L1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ";
        $query .= "       ON  L2.YEAR = L1.YEAR ";
        $query .= "       AND L2.SEMESTER = L1.SEMESTER ";
        $query .= "       AND L2.GRADE = L1.GRADE ";
        $query .= "       AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '".(intval(CTRL_YEAR) - 1)."' ";
        $query .= "      AND L1.SCHREGNO IN (" . $search_scregno . ") ";
        $query .= "      AND L1.SEMESTER =  ( ";
        $query .= "              SELECT MAX(LX.SEMESTER) ";
        $query .= "              FROM SCHREG_REGD_DAT AS LX ";
        $query .= "              WHERE LX.YEAR = L1.YEAR AND LX.SCHREGNO = L1.SCHREGNO ) ";
        $query  .= ")  ";
        $query  .= " SELECT ";
        if ($flg === "COUNT") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     T1.SCHREGNO AS SCHREGNO, ";
            $query .= "     CASE WHEN T8.SCHOOL_KIND = 'J' THEN '中学' ELSE (CASE WHEN T8.SCHOOL_KIND = 'H' THEN '高校' ELSE '' END) END AS SCHOOL_KIND, ";
            $query .= "     T1.GRADE AS GRADE, ";
            $query .= "     T2.HR_CLASS_NAME1 AS HR_CLASS_NAME, ";
            $query .= "     T3.NAME AS NAME, ";
            $query .= "     CASE WHEN T4.ADDR2 IS NULL THEN T4.ADDR1 ELSE T4.ADDR1 || T4.ADDR2 END AS ADDR, ";
            $query .= "     T3.BIRTHDAY AS BIRTHDAY, ";
            $query .= "     CASE WHEN T5.FLG_1 = '1' THEN T6_1_JYOSYA.LINE_NAME ELSE '' END AS ROSEN_1, ";
            $query .= "     CASE WHEN T5.FLG_1 = '1' THEN T6_1_JYOSYA.STATION_NAME ELSE '' END AS JOSYA_1, ";
            $query .= "     CASE WHEN T5.FLG_1 = '1' THEN T6_1_GESYA.STATION_NAME ELSE '' END AS GESYA_1, ";
            $query .= "     CASE WHEN T5.FLG_2 = '1' THEN T6_2_JYOSYA.LINE_NAME ELSE '' END AS ROSEN_2, ";
            $query .= "     CASE WHEN T5.FLG_2 = '1' THEN T6_2_JYOSYA.STATION_NAME ELSE '' END AS JOSYA_2, ";
            $query .= "     CASE WHEN T5.FLG_2 = '1' THEN T6_2_GESYA.STATION_NAME ELSE '' END AS GESYA_2, ";
            $query .= "     CASE WHEN T5.FLG_3 = '1' THEN T6_3_JYOSYA.LINE_NAME ELSE '' END AS ROSEN_3, ";
            $query .= "     CASE WHEN T5.FLG_3 = '1' THEN T6_3_JYOSYA.STATION_NAME ELSE '' END AS JOSYA_3, ";
            $query .= "     CASE WHEN T5.FLG_3 = '1' THEN T6_3_GESYA.STATION_NAME ELSE '' END AS GESYA_3, ";
            $query .= "     T7.G_OLD_HR_CLASS_NAME1 AS G_OLD_HR_CLASS_NAME1 ";
        }
        
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "       ON  T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "       ON  T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_ADDRESS_DAT T4 ";
        $query .= "       ON  T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "       AND  DATE('".CTRL_DATE."') >= T4.ISSUEDATE ";
        $query .= "       AND  DATE('".CTRL_DATE."') <= T4.EXPIREDATE ";
        $query .= "     LEFT JOIN SCHREG_ENVIR_DAT T5 ";
        $query .= "       ON  T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN STATION_NETMST T6_1_JYOSYA ";
        $query .= "       ON   T6_1_JYOSYA.LINE_CD = T5.ROSEN_1 ";
        $query .= "       AND  T6_1_JYOSYA.STATION_CD = T5.JOSYA_1 ";
        $query .= "     LEFT JOIN STATION_NETMST T6_1_GESYA ";
        $query .= "       ON  T6_1_GESYA.LINE_CD = T5.ROSEN_1 ";
        $query .= "       AND  T6_1_GESYA.STATION_CD = T5.GESYA_1 ";
        $query .= "     LEFT JOIN STATION_NETMST T6_2_JYOSYA ";
        $query .= "       ON  T6_2_JYOSYA.LINE_CD = T5.ROSEN_2 ";
        $query .= "       AND  T6_2_JYOSYA.STATION_CD = T5.JOSYA_2 ";
        $query .= "     LEFT JOIN STATION_NETMST T6_2_GESYA ";
        $query .= "       ON  T6_2_GESYA.LINE_CD = T5.ROSEN_2 ";
        $query .= "       AND  T6_2_GESYA.STATION_CD = T5.GESYA_2 ";
        $query .= "     LEFT JOIN STATION_NETMST T6_3_JYOSYA ";
        $query .= "       ON  T6_3_JYOSYA.LINE_CD = T5.ROSEN_3 ";
        $query .= "       AND  T6_3_JYOSYA.STATION_CD = T5.JOSYA_3 ";
        $query .= "     LEFT JOIN STATION_NETMST T6_3_GESYA ";
        $query .= "       ON  T6_3_GESYA.LINE_CD = T5.ROSEN_3 ";
        $query .= "       AND  T6_3_GESYA.STATION_CD = T5.GESYA_3 ";
        $query .= "     LEFT JOIN LASTYEARDAT T7 ";
        $query .= "       ON  INT(T7.YEAR) = INT(T1.YEAR)-1 ";
        $query .= "       AND T7.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T8 ";
        $query .= "       ON  T8.YEAR = T1.YEAR ";
        $query .= "       AND T8.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND T1.SCHREGNO IN (".$search_scregno.") ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '" . $model->field["GRADE_HR_CLASS"] . "' ";
        if ($flg !== "COUNT") {
            $query .= " ORDER BY ";
            $query .= "       T1.SCHREGNO";
        }
        
        return $query;
    }
}
?>
