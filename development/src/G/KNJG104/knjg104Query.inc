<?php

require_once('for_php7.php');

class knjg104Query extends Query {
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
        $tablename = ($model->field["HR_CLASS_TYPE"] == "2") ? "SCHREG_REGD_FI_HDAT" : "SCHREG_REGD_HDAT";

        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $tablename;
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudent($model, $schregno="") {
        $tablename = ($model->field["HR_CLASS_TYPE"] == "2") ? "SCHREG_REGD_FI_DAT" : "SCHREG_REGD_DAT";
        $tablename2 = ($model->field["HR_CLASS_TYPE"] == "2") ? "SCHREG_REGD_FI_HDAT" : "SCHREG_REGD_HDAT";

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS || '-' || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || ' ' || L1.HR_NAME || ' ' || T1.ATTENDNO ||  '番 ' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     {$tablename} T1 ";
        $query .= "     LEFT JOIN {$tablename2} L1 ON T1.YEAR = L1.YEAR ";
        $query .= "     AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "     AND T1.GRADE = L1.GRADE ";
        $query .= "     AND T1.HR_CLASS = L1.HR_CLASS, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($schregno) {
            $query .= "    AND T1.SCHREGNO IN ('".implode("','", $schregno)."') ";
        } else {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN (SELECT ";
                $query .= "                         SCHOOL_KIND ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT ";
                $query .= "                     WHERE ";
                $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
                $query .= "                         GRADE   = '". substr($model->field["GRADE_HR_CLASS"],0,2)."' ";
                $query .= "                     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '". substr($model->field["GRADE_HR_CLASS"],0,2)."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
