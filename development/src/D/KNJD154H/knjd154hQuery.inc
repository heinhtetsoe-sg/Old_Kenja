<?php

require_once('for_php7.php');

class knjd154hQuery extends Query {
    //学期を取得
    function getSemester() {
        $query .= " SELECT ";
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

    //年組取得(権限チェック)
    function getAuth($model) {
        //参照・更新可
        if ($model->field["HR_CLASS_TYPE"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            if ($model->semester == "9") {
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
            }
            $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR     = M1.YEAR AND ";
            $query .= "                                     T1.SEMESTER = M1.SEMESTER AND ";
            $query .= "                                     T1.HR_CLASS = M1.HR_CLASS AND ";
            $query .= "                                     T1.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
            $query .= "                                     T1.STAFFCD  = M1.STAFFCD ";
            $query .= "                                 ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_CLASS_NAME1, ";
            $query .= "     T1.SCHOOL_KIND ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND DESC, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     LABEL ";
        } else if($model->field["HR_CLASS_TYPE"] == "2") {
            $query  = " SELECT DISTINCT ";
            if ($model->Properties["useFi_Hrclass"] != '1' && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     T1.GHR_CD AS VALUE, ";
                $query .= "     T1.GHR_NAME AS LABEL ";
            } else {
                $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
                $query .= "     T1.HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_FI_HDAT T1 ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
            if ($model->semester == "9") {
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND IN ('H', 'A') ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
            if ($model->semester == "9") {
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            return $query;
        }
        return "";
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

    //生徒取得
    function getStudent($model) {
        if ($model->field["HR_CLASS_TYPE"] == '1') {
            $query  = "SELECT T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
            $query .= "       (CASE WHEN S1.SEND_ADDR1 IS NULL THEN '　' ELSE '☆' END) || T2.SCHREGNO || '　' || T2.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
            $query .= "FROM SCHREG_BASE_MST T1 ";
            $query .= "INNER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T2.YEAR AND T3.GRADE = T2.GRADE ";
        } else {
            $query  = "SELECT T2.GHR_CD || T2.GHR_ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
            $query .= "       (CASE WHEN S1.SEND_ADDR1 IS NULL THEN '　' ELSE '☆' END) || T2.SCHREGNO || '　' || T2.GHR_ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
            $query .= "FROM SCHREG_BASE_MST T1 ";
            $query .= "INNER JOIN SCHREG_REGD_GHR_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_SEND_ADDRESS_DAT S1 ON S1.SCHREGNO = T1.SCHREGNO AND S1.DIV = '1' ";
        $query .= "WHERE  T2.YEAR ='" .CTRL_YEAR ."' ";
        if ($model->semester == '9') {
            $query .= "     AND T2.SEMESTER         = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= "     AND T2.SEMESTER         = '".$model->semester."' ";
        }
        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= "       AND T3.SCHOOL_KIND || '-' || T2.HR_CLASS ='" .$model->hrClass ."' ";
            $query .= "ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO";
        } else if ($model->field["HR_CLASS_TYPE"] == "2") {
            $query .= "       AND T2.GHR_CD = '" .$model->hrClass ."' ";
            $query .= "ORDER BY T2.GHR_CD, T2.GHR_ATTENDNO";
        } else {
            $query .= "       AND T2.GRADE || T2.HR_CLASS ='" .$model->hrClass ."' ";
            $query .= "ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO";
        }
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            return $query;
        }
        return $query;
    }

    function getTest($semester) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD <> '9900' ";

        return $query;
    }
    //学期詳細マスタ(日付の取得)
    function getSemesterDetailMst($semesterDetail) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTER_DETAIL, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     VALUE(SDATE, '9999-12-31') AS SDATE, ";
        $query .= "     VALUE(EDATE, '9999-12-31') AS EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER_DETAIL = '{$semesterDetail}' ";

        return $query;
    }

    function get_semester_detail($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$model->semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$model->test_cd}' ";

        return $query;
    }
}
?>
