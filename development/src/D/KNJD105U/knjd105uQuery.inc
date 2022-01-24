<?php

require_once('for_php7.php');

class knjd105uQuery extends Query {
    //学期取得
    function getSemester($model) {
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

    //課程学科取得
    function getCourseMajor($model) {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || T1.COURSENAME || T1.MAJORNAME AS LABEL, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON NAMECD1 = 'A043' ";
        $query .= "         AND T1.COURSECD || T1.MAJORCD = L1.NAME2 || L1.NAME3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     VALUE(L1.NAME1, 'H') = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "          ON T2.YEAR  = T1.YEAR ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " INNER JOIN SCHREG_REGD_DAT T3 ";
            $query .= "      ON T3.YEAR     = T1.YEAR ";
            $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "     AND T3.GRADE    = T1.GRADE ";
            $query .= "     AND T3.HR_CLASS = T1.HR_CLASS ";
            $query .= "     AND T3.COURSECD || '-' || T3.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND '".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3, T1.SUBTR_CD1, T1.SUBTR_CD2, T1.SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    function getTest($model) {
        $grade = $hr_class = array();
        if ($model->field["HR_CLASS"]) list($grade, $hr_class) = explode('-', $model->field["HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.SCORE_DIV   != '09' AND ";
        $query .= "     T1.TESTKINDCD  != '99' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.GRADE    = '00' ";
            $query .= " AND T1.COURSECD || '-' || MAJORCD = '".$model->field["MAJOR"]."' ";
            $query .= " AND T1.SCHOOL_KIND IN ( SELECT ";
            $query .= "                             SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_GDAT ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                             GRADE   = '".$grade."') ";
            if ($model->Properties["useSchool_KindField"] == '1') {
                $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model) {
        $date = str_replace("/", "-", CTRL_DATE);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["HR_CLASS"]."' AND ";
        $query .= "     ( ";
                            //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                            //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "         EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                 WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                     ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                          CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                               THEN T2.EDATE ELSE '".$date."' END) OR ";
        $query .= "                      (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                          CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                               THEN T2.EDATE ELSE '".$date."' END)) ) OR ";
                            //留学(1)・休学(2)者
        $query .= "         EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                 WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                       S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                       CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                            THEN T2.EDATE ELSE '".$date."' END  ";
        $query .= "                       BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ";
        $query .= "     ) ";

        return $query;
    }

    //対象生徒
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T2.GRADE || '-' || T2.HR_CLASS = '".$model->field["HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T2.COURSECD || '-' || T2.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //SCHREG_REGD_GDAT
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE    = '".substr($model->field["HR_CLASS"], 0, 2)."' ";
        return $query;
    }
}
?>
