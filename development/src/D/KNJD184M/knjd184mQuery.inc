<?php

require_once('for_php7.php');

class knjd184mQuery extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    function getTest($model, $semester, $grade) {
        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT L1 ";
        $query .= "              ON L1.YEAR          = T1.YEAR ";
        $query .= "             AND L1.SEMESTER      = T1.SEMESTER ";
        $query .= "             AND L1.TESTKINDCD    = T1.TESTKINDCD ";
        $query .= "             AND L1.TESTITEMCD    = T1.TESTITEMCD ";
        $query .= "             AND L1.SCORE_DIV     = T1.SCORE_DIV ";
        $query .= "             AND L1.CLASSCD       = '00' ";
        $query .= "             AND L1.CURRICULUM_CD = '00' ";
        $query .= "             AND L1.SUBCLASSCD    = '000000' ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ";
        $query .= "              ON L2.YEAR        = T1.YEAR ";
        $query .= "             AND L2.GRADE       = '".$grade."' ";
        $query .= "             AND L2.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model, $semester, $gradeHr = "") {
        $query  = " SELECT ";
        if (!$gradeHr || ($model->field["DISP"] == "1" && $gradeHr)) {
            $query .= "     REGDH.GRADE || REGDH.HR_CLASS AS VALUE, ";
            $query .= "     REGDH.HR_NAME AS LABEL, ";
        }
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ";
        $query .= "           AND REGDH.GRADE = GDAT.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "         REGDH.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND REGDH.SEMESTER    = '".$semester."' ";
        $query .= "     AND GDAT.SCHOOL_KIND  = 'J' "; // 中学固定
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3) ";
        }
        if ($gradeHr) {
            if ($model->field["DISP"] == "1") {
                $query .= "     AND REGDH.GRADE = '{$gradeHr}' ";
            } else {
                $query .= "     AND REGDH.GRADE || REGDH.HR_CLASS = '{$gradeHr}' ";
            }
        }
        if (!$gradeHr || ($model->field["DISP"] == "1" && $gradeHr)) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model,$semester) {
        $date = str_replace("/","-",$model->field["DATE"]);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ( ";
                        //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                        //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "     EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "             WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                 ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END) OR ";
        $query .= "                  (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ) OR ";
                        //留学(1)・休学(2)者
        $query .= "     EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "             WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                   S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                   CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                        THEN T2.EDATE ELSE '".$date."' END ";
        $query .= "                        BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //対象生徒
    function getStudent($model, $semester) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$semester."' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.SCHREGNO ";

        return $query;
    }

    //設定の初期値取得
    function getHreportConditionDat($model) {
        $query  = " SELECT ";
        $query .= "     *";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     GRADE       = '".sprintf("%02d", "")."' AND ";
        $query .= "     COURSECD    = '".sprintf("%01d", "")."' AND ";
        $query .= "     MAJORCD     = '".sprintf("%03d", "")."' AND ";
        $query .= "     COURSECODE  = '".sprintf("%04d", "")."' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }
}
?>
