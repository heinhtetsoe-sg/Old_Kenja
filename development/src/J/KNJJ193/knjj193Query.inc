<?php

require_once('for_php7.php');

class knjj193Query extends Query {
    //学期取得
    function getSemester($model) {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //学年取得
    function getRegdGdat($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス取得
    function getRegdHdat($model, $grade="") {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";
        if ($grade) {
            $query .= " AND GRADE       = '".$grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //個人取得
    function getSchList($model) {
        $query  = "";
        if ($model->field["TYOUSHI_FLG"] == "1") {
            $query .= knjj193Query::getOmitSchList($model);
        }

        $query .= " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '_' || T1.SCHREGNO AS VALUE, ";
        $query .= "     S2.HR_NAME || '　' || T1.ATTENDNO || '番　' || S1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST S1 ";
        $query .= "          ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT S2 ";
        $query .= "          ON T1.YEAR     = S2.YEAR ";
        $query .= "         AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "         AND T1.GRADE    = S2.GRADE ";
        $query .= "         AND T1.HR_CLASS = S2.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR = GDAT.YEAR ";
        $query .= "          AND T1.GRADE = GDAT.GRADE ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON T1.SCHREGNO = ENTGRD.SCHREGNO ";
        $query .= "          AND GDAT.SCHOOL_KIND = ENTGRD.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        if ($model->field["TYOUSHI_FLG"] == "1") {
            $query .= " AND NOT EXISTS (SELECT ";
            $query .= "                     'X' ";
            $query .= "                 FROM ";
            $query .= "                     OMIT_LIST E1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.SCHREGNO = E1.RELA_SCHREGNO ";
            $query .= "                 ) ";
        }
        if ($model->field["IDOU_FLG"] != "1") {
            $query .= " AND (ENTGRD.GRD_DIV IS NULL ";
            $query .= "   OR ENTGRD.GRD_DIV = '4' ";
            $query .= "   OR ENTGRD.GRD_DIV IS NOT NULL AND '".str_replace('/', '-', $model->field["IDOU_DATE"])."' <= ENTGRD.GRD_DATE ";
            $query .= "     ) ";
            $query .= " AND NOT EXISTS (SELECT ";
            $query .= "                     'X' ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_TRANSFER_DAT TR1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.SCHREGNO = TR1.SCHREGNO ";
            $query .= "                     AND '".str_replace('/', '-', $model->field["IDOU_DATE"])."' BETWEEN TR1.TRANSFER_SDATE AND TR1.TRANSFER_EDATE ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //支部取得
    function getBranchMst($model) {
        $query  = " SELECT ";
        $query .= "     BRANCHNAME AS LABEL, ";
        $query .= "     BRANCHCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     BRANCH_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //寮生、下宿生取得
    function getEnvirSchList($model, $cd) {
        $query  = "";
        if ($model->field["TYOUSHI_FLG"] == "1") {
            $query .= knjj193Query::getOmitSchList($model);
        }

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '_' || T1.SCHREGNO AS VALUE, ";
        $query .= "     S2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     S1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST S1 ";
        $query .= "          ON T1.SCHREGNO     = S1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT S2 ";
        $query .= "          ON T1.YEAR         = S2.YEAR ";
        $query .= "         AND T1.SEMESTER     = S2.SEMESTER ";
        $query .= "         AND T1.GRADE        = S2.GRADE ";
        $query .= "         AND T1.HR_CLASS     = S2.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT S3 ";
        $query .= "          ON T1.YEAR         = S3.YEAR ";
        $query .= "         AND T1.GRADE        = S3.GRADE ";
        $query .= "     INNER JOIN V_NAME_MST N1 ";
        $query .= "          ON T1.YEAR         = N1.YEAR ";
        $query .= "         AND N1.NAMECD1      = 'J008' ";
        $query .= "         AND N1.NAMESPARE1 IN ('".implode(explode(',', $cd),"','")."') ";
        $query .= "     INNER JOIN SCHREG_BRANCH_DAT S4 ";
        $query .= "          ON S4.SCHOOLCD     = '000000000000' ";
        $query .= "         AND S3.SCHOOL_KIND  = S4.SCHOOL_KIND ";
        $query .= "         AND T1.YEAR         = S4.YEAR ";
        $query .= "         AND T1.SCHREGNO     = S4.SCHREGNO ";
        $query .= "         AND S4.RESIDENTCD   = N1.NAMECD2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR = GDAT.YEAR ";
        $query .= "          AND T1.GRADE = GDAT.GRADE ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON T1.SCHREGNO = ENTGRD.SCHREGNO ";
        $query .= "          AND GDAT.SCHOOL_KIND = ENTGRD.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->field["TYOUSHI_FLG"] == "1") {
            $query .= " AND NOT EXISTS (SELECT ";
            $query .= "                     'X' ";
            $query .= "                 FROM ";
            $query .= "                     OMIT_LIST E1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.SCHREGNO = E1.RELA_SCHREGNO ";
            $query .= "                 ) ";
        }
        if ($model->field["IDOU_FLG"] != "1") {
            $query .= " AND (ENTGRD.GRD_DIV IS NULL OR ENTGRD.GRD_DIV = '4' OR ";
            $query .= "      ENTGRD.GRD_DIV IS NOT NULL AND '".str_replace('/', '-', $model->field["IDOU_DATE"])."' <= ENTGRD.GRD_DATE ";
            $query .= "     ) ";
            $query .= " AND NOT EXISTS (SELECT ";
            $query .= "                     'X' ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_TRANSFER_DAT TR1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.SCHREGNO = TR1.SCHREGNO ";
            $query .= "                     AND '".str_replace('/', '-', $model->field["IDOU_DATE"])."' BETWEEN TR1.TRANSFER_SDATE AND TR1.TRANSFER_EDATE ";
            $query .= "                 ) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND S3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //長子以外の家族取得
    function getOmitSchList($model) {
        $query  = " WITH TAISYOU_FAMILY AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.FAMILY_NO ";
        $query .= "     FROM ";
        $query .= "         FAMILY_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT S1 ";
        $query .= "              ON S1.YEAR             = '".CTRL_YEAR."' ";
        $query .= "             AND S1.SEMESTER         = '".$model->field["SEMESTER"]."' ";
        $query .= "             AND T1.RELA_SCHREGNO    = S1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         VALUE(T1.TYOUSHI_FLG, '0') = '1' ";
        $query .= " ), OMIT_LIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.RELA_SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         TAISYOU_FAMILY T1, ";
        $query .= "         FAMILY_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.FAMILY_NO = T2.FAMILY_NO AND ";
        $query .= "         VALUE(T2.TYOUSHI_FLG, '0') <> '1' AND ";
        $query .= "         T2.RELA_SCHREGNO IS NOT NULL ";
        $query .= " ) ";

        return $query;
    }
}
?>
