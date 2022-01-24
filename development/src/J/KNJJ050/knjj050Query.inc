<?php

require_once('for_php7.php');


class knjj050Query extends Query {

    function getHr_Class_alp($model, $year,$semester,$auth_user,$staffcd,$auth)
    {
        $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR='" .$year ."' ";
        $query .= "AND SEMESTER='".$semester ."' ";
        if ($auth == "1") {
        //参照・更新可（制限付き）
            if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT) {
                $query .= " AND (TR_CD1 = '" .$staffcd ."' ";
                $query .= "    OR TR_CD2 = '" .$staffcd ."' ";
                $query .= "    OR TR_CD3 = '" .$staffcd ."' ";
                $query .= " OR SUBTR_CD1 = '" .$staffcd ."' ";
                $query .= "    OR SUBTR_CD2 = '" .$staffcd ."' ";
                $query .= "    OR SUBTR_CD3 = '" .$staffcd ."') ";
            }
        }

        return $query;
    }

    //ＣＳＶ作成
    function selectCsvQuery($model) {

        $setFromDate = str_replace("/", "-", $model->field["FROM_DATE"]);
        $setToDate = str_replace("/", "-", $model->field["TO_DATE"]);

        $query .= " WITH SCHINFO AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".implode("','", explode(',', $model->selectdata))."') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAMEABBV, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     C1.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     N1.NAME1 AS EXECUTIVECD, ";
        $query .= "     T1.REMARK, ";
        $query .= "     G1.GUARD_NAME, ";
        $query .= "     VALUE(G1.GUARD_ADDR1,'') || VALUE(G1.GUARD_ADDR2,'') AS GUARD_ADDR, ";
        $query .= "     G1.GUARD_TELNO, ";
        $query .= "     H1.DETAIL_DATE, ";
        $query .= "     H1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     INNER JOIN CLUB_MST C1 ON T1.CLUBCD = C1.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND C1.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= "     INNER JOIN CLUB_YDAT CY ON '".CTRL_YEAR."' = CY.YEAR ";
        $query .= "         AND C1.CLUBCD = CY.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND C1.SCHOOLCD    = CY.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = CY.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'J001' AND T1.EXECUTIVECD = N1.NAMECD2 ";
        $query .= "     LEFT JOIN GUARDIAN_DAT G1 ON T1.SCHREGNO = G1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_CLUB_HDETAIL_DAT H1 ON T1.SCHREGNO = H1.SCHREGNO AND T1.CLUBCD = H1.CLUBCD AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "  H1.SCHOOLCD    = T1.SCHOOLCD AND ";
            $query .= "  H1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        }
        $query .= "         H1.DETAIL_DATE BETWEEN T1.SDATE AND CASE WHEN T1.EDATE IS NULL THEN '9999-12-31' ELSE T1.EDATE END, ";
        $query .= "     SCHINFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        if ($model->Properties["useClubMultiSchoolKind"]) {
            $query .= "     AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["taibusya_nozoku"] == "on") {
            $query .= "    AND (T1.EDATE IS NULL OR ";
            $query .= "         T1.EDATE > '{$setToDate}') ";
        }
        $query .= "     AND ((T1.SDATE BETWEEN '{$setFromDate}' AND '{$setToDate}') ";
        $query .= "           OR ";
        $query .= "          (VALUE(T1.EDATE, '9999-12-31') BETWEEN '{$setFromDate}' AND '{$setToDate}') ";
        $query .= "           OR ";
        $query .= "          (T1.SDATE <= '{$setFromDate}' AND VALUE(T1.EDATE, '9999-12-31') >= '{$setToDate}') ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T1.SDATE ";

        return $query;
    }
}
?>
