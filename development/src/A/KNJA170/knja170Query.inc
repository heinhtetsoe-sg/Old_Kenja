<?php

require_once('for_php7.php');

class knja170Query extends Query {

    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA170' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($ga, $model)
    {
        //参照・更新可
        if ($model->auth == DEF_REFERABLE || $model->auth == DEF_UPDATABLE){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SEMESTER='".$ga ."'";
        }
        //参照・更新可（制限付き）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."' ";
            $query .= "AND SEMESTER='".$ga ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //CSV取得
    function getCsvData($model)
    {
        $sdate = str_replace("/","-",$model->control["学期開始日付"][$model->field["OUTPUT"]]);
        $edate = str_replace("/","-",$model->control["学期終了日付"][$model->field["OUTPUT"]]);

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     L4.STAFFNAME_SHOW, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     VALUE(T2.NAME_KANA,'') AS NAME_KANA, ";
        $query .= "     VALUE(CHAR(T2.BIRTHDAY),'') AS BIRTHDAY, ";
        $query .= "     VALUE(L3.ZIPCD,'') AS ZIPCD, ";
        $query .= "     VALUE(L3.ADDR1,'') AS ADDR1, ";
        $query .= "     VALUE(L3.ADDR2,'') AS ADDR2, ";
        $query .= "     VALUE(L3.TELNO,'') AS TEL, ";
        $query .= "     VALUE(L1.GUARD_NAME,'') AS GUARD_NAME, ";
        $query .= "     VALUE(L1.GUARD_KANA,'') AS GUARD_KANA, ";
        $query .= "     VALUE(L1.GUARD_ZIPCD,'') AS GUARD_ZIPCD, ";
        $query .= "     VALUE(L1.GUARD_ADDR1,'') AS GUARD_ADDR1, ";
        $query .= "     VALUE(L1.GUARD_ADDR2,'') AS GUARD_ADDR2, ";
        $query .= "     VALUE(L1.GUARD_TELNO,'') AS GUARD_TEL, ";
        $query .= "     VALUE(T2.EMERGENCYTELNO,'') AS E_TEL, ";
        $query .= "     VALUE(L2.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2  ON T1.SCHREGNO   = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR       = T3.YEAR ";
        $query .= "                                   AND T1.SEMESTER   = T3.SEMESTER ";
        $query .= "                                   AND T1.GRADE      = T3.GRADE ";
        $query .= "                                   AND T1.HR_CLASS   = T3.HR_CLASS ";
        $query .= "     LEFT JOIN GUARDIAN_DAT L1      ON T2.SCHREGNO   = L1.SCHREGNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L2     ON T2.FINSCHOOLCD = L2.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     SCHREGNO, ";
        $query .= "                     ZIPCD, ";
        $query .= "                     ADDR1, ";
        $query .= "                     ADDR2, ";
        $query .= "                     TELNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_ADDRESS_DAT W1 ";
        $query .= "                 WHERE ";
        $query .= "                     (W1.SCHREGNO,W1.ISSUEDATE) IN ( SELECT ";
        $query .= "                                                         SCHREGNO, ";
        $query .= "                                                         MAX(ISSUEDATE) ";
        $query .= "                                                     FROM ";
        $query .= "                                                         SCHREG_ADDRESS_DAT W2 ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         W2.ISSUEDATE <= '".$sdate."' AND ";
        $query .= "                                                         (W2.EXPIREDATE IS NULL OR W2.EXPIREDATE >= '".$edate."') ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         SCHREGNO ";
        $query .= "                                                   ) ";
        $query .= "               ) L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN STAFF_MST L4      ON T3.TR_CD1   = L4.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["OUTPUT"]."' AND ";
        $query .= "     T1.SCHREGNO IN ('".implode(explode(',', $model->selectdata),"','")."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
