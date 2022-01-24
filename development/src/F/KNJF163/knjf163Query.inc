<?php

require_once('for_php7.php');

class knjf163Query extends Query {
    //年組取得（権限チェック）
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '". CTRL_YEAR ."'";
        $query .= "     AND SEMESTER = '". CTRL_SEMESTER ."' ";
        $query .= "     AND GRADE    = '".$model->field["GRADE"] ."'";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "    GRADE_NAME1 AS LABEL, ";
        $query .= "    GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //V_NAME_MST
    function getNameMst($namecd1) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";

        return $query;
    }

    //CSV出力
    function getCsvQuery($grade_hr_class) {
        $year = CTRL_YEAR;
        $semester = CTRL_SEMESTER;

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     L1.INSURED_NAME, ";
        $query .= "     L1.INSURED_MARK, ";
        $query .= "     L1.INSURED_NO, ";
        $query .= "     L1.INSURANCE_NAME, ";
        $query .= "     L1.INSURANCE_NO, ";
        $query .= "     L1.VALID_DATE, ";
        $query .= "     L1.AUTHORIZE_DATE, ";
        $query .= "     L1.RELATIONSHIP, ";
        $query .= "     L2.RELANAME AS FATHER_RELANAME, ";
        $query .= "     L2.RELAKANA AS FATHER_RELAKANA, ";
        $query .= "     L3.RELANAME AS MOTHER_RELANAME, ";
        $query .= "     L3.RELAKANA AS MOTHER_RELAKANA, ";
        $query .= "     T2.EMERGENCYNAME, ";
        $query .= "     T2.EMERGENCYRELA_NAME, ";
        $query .= "     T2.EMERGENCYCALL, ";
        $query .= "     T2.EMERGENCYTELNO, ";
        $query .= "     T2.EMERGENCYNAME2, ";
        $query .= "     T2.EMERGENCYRELA_NAME2, ";
        $query .= "     T2.EMERGENCYCALL2, ";
        $query .= "     T2.EMERGENCYTELNO2, ";
        $query .= "     L1.ATTENTION ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HEALTH_NURSE_ENT_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_RELA_DAT L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                 AND L2.RELATIONSHIP = '01' ";
        $query .= "     LEFT JOIN SCHREG_RELA_DAT L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                 AND L3.RELATIONSHIP = '02' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$semester}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS='{$grade_hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
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
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '".$model->field["GRADE"]."' ";
            $query .= "                     ) ";
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
            $query .= "                         GRADE   = '".$model->field["GRADE"]."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
