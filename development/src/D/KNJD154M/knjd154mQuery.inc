<?php

require_once('for_php7.php');

class knjd154mQuery extends Query {
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
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->semester == '9') {
            $query .= "    AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= "    AND T1.SEMESTER = '".$model->semester."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3     = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校校種取得
    function getSchoolkindQuery($grade) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE = '".$grade."' ";

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

    //生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     (CASE WHEN S1.SEND_ADDR1 IS NULL THEN '　' ELSE '☆' END) || T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_SEND_ADDRESS_DAT S1 ON S1.SCHREGNO = T1.SCHREGNO AND S1.DIV = '1' ";
        if ($model->field["RYOUSEI_ONLY"] == "1") {
            $query .= " INNER JOIN SCHREG_DOMITORY_HIST_DAT S2 ";
            $query .= "   ON S2.SCHREGNO = T1.SCHREGNO";
            $query .= "  AND S2.DOMI_ENTDAY <= '".CTRL_DATE."' ";
            $query .= "  AND (S2.DOMI_OUTDAY IS NULL OR S2.DOMI_OUTDAY >= '".CTRL_DATE."') ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."' AND ";
        if ($model->semester == '9') {
            $query .= "     T1.SEMESTER         = '". CTRL_SEMESTER ."' AND ";
        } else {
            $query .= "     T1.SEMESTER         = '".$model->semester."' AND ";
        }
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->hrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    function getTest($model, $searchBeforeSems = false) {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "         AND T2.CLASSCD      = '00' ";
        $query .= "         AND T2.SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "         AND T2.CURRICULUM_CD  = '00' ";
        $query .= "         AND T2.SUBCLASSCD  = '000000' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        if ($searchBeforeSems) {
            $query .= "     T1.SEMESTER <= '{$model->semester}' AND ";
        } else {
            $query .= "     T1.SEMESTER = '{$model->semester}' AND ";
        }
        $query .= "     T1.TESTKINDCD <> '99' AND ";
        $query .= "     T1.SCORE_DIV <> '09' ";

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
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$model->semester}' AND ";
        $query .= "     SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$model->test_cd}' ";

        return $query;
    }
}
?>
