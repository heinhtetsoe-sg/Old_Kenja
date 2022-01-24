<?php

require_once('for_php7.php');

class knjj192Query extends Query {

    //学期
    function getSemester($model) {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->year}' ";
        $query .= "    AND SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //学年
    function getRegdGdat($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス
    function getRegdHdat($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス
    function getFirstGh($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS GRADEHR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND GRADE || '-' || HR_CLASS = '".$model->selectdata[0]."' ";

        return $query;
    }

    //ＣＳＶ出力データ
    function getDownloadCsv($model) {
        $query  = " SELECT ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS AS GRADEHR, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     HDAT.HR_CLASS_NAME2, ";
        $query .= "     STAFF.STAFFNAME, ";
        $query .= "     STAFF.STAFFTELNO, ";
        $query .= "     BRANCH.GUARD_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     BASED.BASE_REMARK1 AS FAMILY_NO, ";
        $query .= "     FINSCHOOL.FINSCHOOL_NAME, ";
        $query .= "     BRANCH.GUARD_ZIPCD, ";
        $query .= "     VALUE(BRANCH.GUARD_ADDR1, '') AS GUARD_ADDR1, ";
        $query .= "     VALUE(BRANCH.GUARD_ADDR2, '') AS GUARD_ADDR2, ";
        $query .= "     BRANCH.GUARD_TELNO, ";
        $query .= "     BRANCHM.BRANCHNAME, ";
        $query .= "     J007.NAME1 AS POSITION_NAME, ";
        $query .= "     J008.NAME1 AS JYUKYO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     LEFT JOIN STAFF_MST STAFF ON HDAT.TR_CD1 = STAFF.STAFFCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON HDAT.YEAR = REGD.YEAR ";
        $query .= "          AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "          AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON BASE.FINSCHOOLCD = FINSCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST BASED ON BASE.SCHREGNO = BASED.SCHREGNO ";
        $query .= "          AND BASED.BASE_SEQ = '009' ";
        $query .= "     LEFT JOIN SCHREG_BRANCH_DAT BRANCH ON HDAT.YEAR = BRANCH.YEAR ";
        $query .= "          AND REGD.SCHREGNO = BRANCH.SCHREGNO ";
        $query .= "     LEFT JOIN BRANCH_MST BRANCHM ON BRANCH.BRANCHCD = BRANCHM.BRANCHCD ";
        $query .= "     LEFT JOIN NAME_MST J007 ON J007.NAMECD1 = 'J007' ";
        $query .= "          AND BRANCH.BRANCH_POSITION = J007.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST J008 ON J008.NAMECD1 = 'J008' ";
        $query .= "          AND BRANCH.RESIDENTCD = J008.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND HDAT.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND HDAT.GRADE || '-' || HDAT.HR_CLASS IN ('".str_replace(",", "','", implode(",", $model->selectdata))."') ";
        $query .= "     AND NOT EXISTS(SELECT ";
        $query .= "                        'x' ";
        $query .= "                    FROM ";
        $query .= "                        SCHREG_TRANSFER_DAT TRANS ";
        $query .= "                    WHERE ";
        $query .= "                        TRANS.TRANSFERCD IN ('1', '2') ";
        $query .= "                        AND '".CTRL_DATE."' BETWEEN TRANS.TRANSFER_SDATE AND VALUE(TRANS.TRANSFER_EDATE, '9999-12-31') ";
        $query .= "                        AND BASE.SCHREGNO = TRANS.SCHREGNO ";
        $query .= "                   ) ";
        $query .= "     AND NOT EXISTS(SELECT ";
        $query .= "                        'x' ";
        $query .= "                    FROM ";
        $query .= "                        SCHREG_BASE_MST BASE2 ";
        $query .= "                    WHERE ";
        $query .= "                            BASE2.GRD_DIV IN ('2', '3', '6', '7') "; //2:退学 3:転学 6:除籍 7:転籍
        $query .= "                        AND value(BASE2.GRD_DATE, '9999-12-31') <= '".CTRL_DATE."' ";
        $query .= "                        AND BASE2.SCHREGNO =  REGD.SCHREGNO ";
        $query .= "                   ) ";
        $query .= " ORDER BY ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }
    //ＣＳＶ出力データ(本校在籍兄弟姉妹)
    function getFamilyCsv($familyNo) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     VALUE(BASE.GRD_DIV, '') AS GRD_DIV, ";
        $query .= "     VALUE(BASE.GRD_TERM, '') AS GRD_TERM, ";
        $query .= "     BASE.NAME, ";
        $query .= "     HDAT.HR_NAME ";
        $query .= " FROM ";
        $query .= "     FAMILY_DAT FAMILY ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON FAMILY.RELA_SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                   AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                                   AND BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "                                    AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         FAMILY.FAMILY_NO = '".$familyNo."' ";
        $query .= "     AND FAMILY.RELA_SCHREGNO IS NOT NULL ";
        $query .= " ), GRD_REGD AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD2.YEAR, ";
        $query .= "     REGD2.SCHREGNO, ";
        $query .= "     MAX(REGD2.SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         MAIN_T.SCHREGNO, ";
        $query .= "         MAX(YEAR) AS YEAR ";
        $query .= "     FROM ";
        $query .= "         MAIN_T ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT REGD ON MAIN_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         MAIN_T.SCHREGNO ";
        $query .= "     ) MAXYEAR ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD2 ON MAXYEAR.SCHREGNO = REGD2.SCHREGNO ";
        $query .= "                                     AND MAXYEAR.YEAR     = REGD2.YEAR ";
        $query .= " GROUP BY ";
        $query .= "     REGD2.YEAR, ";
        $query .= "     REGD2.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.SCHREGNO, ";
        $query .= "     MAIN_T.GRD_DIV, ";
        $query .= "     MAIN_T.GRD_TERM, ";
        $query .= "     MAIN_T.NAME, ";
        $query .= "     MAIN_T.HR_NAME ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     MAIN_T.GRD_DIV = '' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.SCHREGNO, ";
        $query .= "     MAIN_T.GRD_DIV, ";
        $query .= "     MAIN_T.GRD_TERM, ";
        $query .= "     MAIN_T.NAME, ";
        $query .= "     HDAT.HR_NAME ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= "     INNER JOIN GRD_REGD ON MAIN_T.SCHREGNO = GRD_REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON GRD_REGD.YEAR     = REGD.YEAR ";
        $query .= "                                   AND GRD_REGD.SEMESTER = REGD.SEMESTER ";
        $query .= "                                   AND GRD_REGD.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "                                    AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     MAIN_T.GRD_DIV != '' ";

        return $query;
    }

}
?>
