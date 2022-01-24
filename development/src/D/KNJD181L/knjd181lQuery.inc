<?php

require_once('for_php7.php');

class knjd181lQuery extends Query {
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

    //出欠を出力しない学期
    function getAttendSemester() {
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
    function getAuth($model) {
        $semester = $model->field["SEMESTER"];
        if ($model->field["HR_CLASS_TYPE"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            if ($semester == "9") {
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND T1.SEMESTER = '".$semester."' ";
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
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'J' ";
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
                $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.GHR_CD = T1.GHR_CD ";
                $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T3.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR = REGDG.YEAR AND REGD.GRADE = REGDG.GRADE AND REGDG.SCHOOL_KIND = 'J' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
            if ($model->semester == "9") {
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND T1.SEMESTER = '".$semester."' ";
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
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'J' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
            if ($model->semester == "9") {
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND T1.SEMESTER = '".$semester."' ";
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

    //対象外の生徒取得
    function getSchnoIdou($model, $year, $semester, $grade_hr_class, $date) {
        $where  = "       AND ( ";
                           //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                           //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $where .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $where .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $where .= "                    ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $where .= "                         CASE WHEN SEME.EDATE < '$date' ";
        $where .= "                              THEN SEME.EDATE ELSE '$date' END) OR ";
        $where .= "                     (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $where .= "                         CASE WHEN SEME.EDATE < '$date' ";
        $where .= "                              THEN SEME.EDATE ELSE '$date' END)) ) OR ";
                           //留学(1)・休学(2)者
        $where .= "        EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $where .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $where .= "                      S1.TRANSFERCD IN ('1','2') AND  ";
        $where .= "                      CASE WHEN SEME.EDATE < '$date' ";
        $where .= "                           THEN SEME.EDATE ELSE '$date' END  ";
        $where .= "                      BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ) ";
        $query  = knjd181lQuery::getSchno($model, $year, $semester, $grade_hr_class, $where);

        return $query;
    }

    //対象者リスト
    function getSchno($model, $year, $semester, $grade_hr_class, $where = "") {
        if ($model->field["HR_CLASS_TYPE"] == '1') {
            $query  = "SELECT T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
            $query .= "       (CASE WHEN S1.SEND_ADDR1 IS NULL THEN '　' ELSE '☆' END) || T2.SCHREGNO || '　' || T2.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
            $query .= "       , T1.SCHREGNO, T2.ATTENDNO AS ATTENDNO,T1.NAME_SHOW ";
            $query .= "FROM SCHREG_BASE_MST T1 ";
            $query .= "INNER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T2.YEAR AND T3.GRADE = T2.GRADE ";
        } else {
            $query  = "SELECT T2.GHR_CD || T2.GHR_ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
            $query .= "       (CASE WHEN S1.SEND_ADDR1 IS NULL THEN '　' ELSE '☆' END) || T2.SCHREGNO || '　' || T2.GHR_ATTENDNO || '番' || '　' || T1.NAME_SHOW AS LABEL ";
            $query .= "       , T1.SCHREGNO, T2.GHR_ATTENDNO AS ATTENDNO,T1.NAME_SHOW ";
            $query .= "FROM SCHREG_BASE_MST T1 ";
            $query .= "INNER JOIN SCHREG_REGD_GHR_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T2.SCHREGNO AND REGD.YEAR = T2.YEAR AND REGD.SEMESTER = T2.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR = REGDG.YEAR AND REGD.GRADE = REGDG.GRADE ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_SEND_ADDRESS_DAT S1 ON S1.SCHREGNO = T1.SCHREGNO AND S1.DIV = '1' ";
        $query .= "     LEFT JOIN ";
        $query .= "         SEMESTER_MST SEME ON SEME.YEAR = T2.YEAR AND SEME.SEMESTER = T2.SEMESTER ";
        $query .= "WHERE  T2.YEAR ='" .$year."' ";
        if ($semester == '9') {
            $query .= "     AND T2.SEMESTER         = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= "     AND T2.SEMESTER         = '".$semester."' ";
        }
        $query .= "     AND REGDG.SCHOOL_KIND = 'J' ";
        $query .= $where;
        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= "       AND T3.SCHOOL_KIND || '-' || T2.HR_CLASS ='" .$grade_hr_class."' ";
            $query .= "ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO";
        } else if ($model->field["HR_CLASS_TYPE"] == "2") {
            $query .= "       AND T2.GHR_CD = '" .$grade_hr_class."' ";
            $query .= "ORDER BY T2.GHR_CD, T2.GHR_ATTENDNO";
        } else {
            $query .= "       AND T2.GRADE || T2.HR_CLASS ='" .$grade_hr_class."' ";
            $query .= "ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO";
        }
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            return $query;
        }
        return "";
    }

}
?>
