<?php

require_once('for_php7.php');

class knje461Query extends Query {

    //年組取得
    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR         = T1.YEAR ";
        $query .= "         AND G1.GRADE        = T1.GRADE ";
        $query .= "         AND G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.GRADE    = T1.GRADE ";
        $query .= "         AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "          ON L1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
        $query .= "          ON L2.SCHREGNO = T3.SCHREGNO ";
        $query .= "         AND L2.YEAR     = T1.YEAR ";
        $query .= "         AND L2.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        //更新・参照可（制限付き）
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= " AND (T1.TR_CD1      = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD2      = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD3      = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD1   = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD2   = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD3   = '". STAFFCD ."') ";
        }
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 IN ('1', '2')) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実クラス取得
    function getGhr($model, $ghrCd = '') {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GHR_NAME AS LABEL, ";
        $query .= "     T1.GHR_CD AS VALUE, ";
        $query .= "     MAX(T4.SCHOOL_KIND) AS SCHOOL_KIND, ";
        $query .= "     CASE WHEN MIN(T4.SCHOOL_KIND) = MAX(T4.SCHOOL_KIND) THEN 1 END AS SCHOOL_KIND_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T2 ";
        $query .= "          ON T2.YEAR     = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.GHR_CD   = T1.GHR_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T4 ";
        $query .= "          ON T4.YEAR     = T3.YEAR ";
        $query .= "         AND T4.GRADE    = T3.GRADE ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "          ON L1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
        $query .= "          ON L2.SCHREGNO = L1.SCHREGNO ";
        $query .= "         AND L2.YEAR     = T1.YEAR ";
        $query .= "         AND L2.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        if ($ghrCd) {
            $query .= " AND T1.GHR_CD = '". $ghrCd."' ";
        }
        //更新・参照可（制限付き）
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= " AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 IN ('1', '2')) ";
        $query .= " GROUP BY ";
        $query .= "     T1.GHR_CD, T1.GHR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名取得の取得(テストコンボで使う)
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //校種
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NMA023.NAMECD2, ";
        $query .= "     NMA023.ABBV1 AS LABEL, ";
        $query .= "     NMA023.NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST NMA023 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = NMA023.YEAR ";
        $query .= "       AND T2.SCHOOL_KIND = NMA023.NAME1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "       AND T3.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     NMA023.YEAR                 = '".CTRL_YEAR."' ";
        $query .= " AND NMA023.NAMECD1              = 'A023' ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= " UNION ";
            $query .= " SELECT DISTINCT ";
            $query .= "     NMA023.NAMECD2, ";
            $query .= "     NMA023.ABBV1 AS LABEL, ";
            $query .= "     NMA023.NAME1 AS VALUE ";
            $query .= " FROM ";
            $query .= "     V_NAME_MST NMA023 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = NMA023.YEAR ";
            $query .= "       AND T2.SCHOOL_KIND = NMA023.NAME1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
            $query .= "       AND T3.GRADE = T2.GRADE ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO ";
            $query .= "       AND T4.YEAR = T3.YEAR ";
            $query .= "       AND T4.SEMESTER = T3.SEMESTER ";
            $query .= " WHERE ";
            $query .= "     NMA023.YEAR                 = '".CTRL_YEAR."' ";
            $query .= " AND NMA023.NAMECD1              = 'A023' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            //実クラス
            $query  = " WITH MXSCHREG AS ( ";
            $query .= " SELECT ";
            $query .= "     YEAR, ";
            $query .= "     MAX(SEMESTER) AS SEMESTER, ";
            $query .= "     SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= " GROUP BY ";
            $query .= "     YEAR, ";
            $query .= "     SCHREGNO ";
            $query .= " ) ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T4.GHR_NAME || T1.GHR_ATTENDNO || '番　' || T1.SCHREGNO || '　' || L1.NAME_SHOW AS LABEL, ";
            $query .= "     '1-' || T1.GHR_CD || T1.GHR_ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_DAT T1 ";
            $query .= "     INNER JOIN MXSCHREG T1WK ";
            $query .= "          ON T1WK.YEAR = T1.YEAR ";
            $query .= "         AND T1WK.SEMESTER = T1.SEMESTER ";
            $query .= "         AND T1WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "     INNER JOIN SCHREG_BASE_MST L1 ";
            $query .= "          ON L1.SCHREGNO     = T1.SCHREGNO ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
            $query .= "          ON T2.SCHREGNO     = T1.SCHREGNO ";
            $query .= "         AND T2.YEAR         = T1.YEAR ";
            $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "          ON T3.YEAR         = T2.YEAR ";
            $query .= "         AND T3.GRADE        = T2.GRADE ";
            $query .= "         AND T3.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT T4 ";
            $query .= "          ON T4.YEAR         = T1.YEAR ";
            $query .= "         AND T4.SEMESTER     = T1.SEMESTER ";
            $query .= "         AND T4.GHR_CD       = T1.GHR_CD ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
            $query .= "          ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND L2.YEAR     = T1.YEAR ";
            $query .= "         AND L2.BASE_SEQ = '004' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
            if ($model->field["GHR_CD"]) {
                $query .= " AND T1.GHR_CD = '".$model->field["GHR_CD"]."' ";
            }
        } else {
            //法定クラス
            $query  = " WITH MXSCHREG AS ( ";
            $query .= " SELECT ";
            $query .= "     YEAR, ";
            $query .= "     MAX(SEMESTER) AS SEMESTER, ";
            $query .= "     SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= " GROUP BY ";
            $query .= "     YEAR, ";
            $query .= "     SCHREGNO ";
            $query .= " ) ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T4.HR_NAME || T1.ATTENDNO || '番　' || T1.SCHREGNO || '　' || L1.NAME_SHOW AS LABEL, ";
            $query .= "     '2-' || T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN MXSCHREG T1WK ";
            $query .= "          ON T1WK.YEAR = T1.YEAR ";
            $query .= "         AND T1WK.SEMESTER = T1.SEMESTER ";
            $query .= "         AND T1WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "     INNER JOIN SCHREG_BASE_MST L1 ";
            $query .= "          ON L1.SCHREGNO     = T1.SCHREGNO ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "          ON T3.YEAR         = T1.YEAR ";
            $query .= "         AND T3.GRADE        = T1.GRADE ";
            $query .= "         AND T3.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ";
            $query .= "          ON T4.YEAR         = T1.YEAR ";
            $query .= "         AND T4.SEMESTER     = T1.SEMESTER ";
            $query .= "         AND T4.GRADE        = T1.GRADE ";
            $query .= "         AND T4.HR_CLASS     = T1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
            $query .= "          ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND L2.YEAR     = T1.YEAR ";
            $query .= "         AND L2.BASE_SEQ = '004' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
            if ($model->field["GHR_CD"]) {
                $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GHR_CD"]."' ";
            }
        }
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 IN ('1', '2')) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得(計画データのある生徒が対象条件)
    function getSchnoIdou($model,$semester) {
        $query  = " WITH MXSCHREG AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS SEMESTER, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            $query .= "     SCHREG_REGD_GHR_DAT ";
        } else {
            $query .= "     SCHREG_REGD_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            $query .= "     T1.GHR_ATTENDNO ";
        } else {
            $query .= "     T1.ATTENDNO ";
        }
        $query .= " FROM ";
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            $query .= "     SCHREG_REGD_GHR_DAT T1 ";
        } else {
            $query .= "     SCHREG_REGD_DAT T1 ";
        }
        $query .= "     INNER JOIN MXSCHREG T5 ";
        $query .= "       ON T5.YEAR = T1.YEAR ";
        $query .= "      AND T5.SEMESTER = T1.SEMESTER ";
        $query .= "      AND T5.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["HUKUSIKI_RADIO"] == "1") {
            $query .= "  INNER JOIN SCHREG_REGD_GDAT T3";
            $query .= "    ON T3.YEAR = T1.YEAR ";
            $query .= "   AND T3.GRADE = T1.GRADE ";
            $query .= "   AND T3.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T4 ";
        $query .= "        ON T4.YEAR = T1.YEAR ";
        $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T4.SCHREGNO IS NULL ";
        if ($model->field["HUKUSIKI_RADIO"] == "1") {
            $query .= "   AND  T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GHR_CD"]."' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            $query .= "     T1.GHR_ATTENDNO, ";
        } else {
            $query .= "     T1.ATTENDNO, ";
        }
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

}
?>
