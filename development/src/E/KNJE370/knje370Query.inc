<?php

require_once('for_php7.php');


class knje370Query extends Query {
    //年度取得
    function getYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR AS VALUE, ";
        $query .= "     T1.YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT T2 ON  T1.YEAR     = T2.YEAR ";
        $query .= "                         AND T1.GRADE    = T2.GRADE ";
        $query .= "                         AND T2.SCHOOL_KIND IN ('H') ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //MAX学期取得
    function getMaxSemester($model) {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //既卒生有無チェック
    function checkGradCnt($model, $semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(T1.SCHREGNO) ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT E1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR     = E1.YEAR AND ";
        $query .= "                     E1.SEMESTER = '".$semester."' AND ";
        $query .= "                     T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                 ) ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $semester, $kisotsu) {
        $query  = " SELECT ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT T2 ON  T1.YEAR = T2.YEAR ";
        $query .= "                         AND T1.GRADE = T2.GRADE ";
        $query .= "                         AND T2.SCHOOL_KIND IN ('H') ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
        } else if ($kisotsu > 0) {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     '既卒生' AS LABEL, ";
            $query .= "     'ZZZZZ' AS VALUE ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudent($model, $semester) {
        $query  = " SELECT ";
        $query .= "     '0-' || REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGD_H.HR_NAME || '　' || REGD.ATTENDNO || '番　' || BASE.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = REGD.YEAR ";
        $query .= "           AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND REGD_H.GRADE    = REGD.GRADE ";
        $query .= "           AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "     AND REGD.SEMESTER = '".$semester."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->field["GRADE_HR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //既卒生取得
    function getGradStudent($model, $semester) {
        $senkou_kind = ($model->field["OUT_DIV"] == "1") ? "0" : "1";

        $query  = " SELECT DISTINCT ";
        $query .= "     '1-ZZZZZZZZ-' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || L1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SENKOU_KIND  = '".$senkou_kind."' AND ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT E1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR     = E1.YEAR AND ";
        $query .= "                     E1.SEMESTER = '".$semester."' AND ";
        $query .= "                     T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudentLeft($model, $semester) {
        $senkou_kind = ($model->field["OUT_DIV"] == "1") ? "0" : "1";

        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         '0-' || REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
        $query .= "         REGD_H.HR_NAME || '　' || REGD.ATTENDNO || '番　' || BASE.NAME_SHOW AS LABEL, ";
        $query .= "         REGD.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = REGD.YEAR ";
        $query .= "               AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "               AND REGD_H.GRADE    = REGD.GRADE ";
        $query .= "               AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "         AND REGD.SEMESTER = '".$semester."' ";
        $query .= "     UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         '1-ZZZZZZZZ-' || T1.SCHREGNO AS VALUE, ";
        $query .= "         T1.SCHREGNO || '　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SENKOU_KIND  = '".$senkou_kind."' AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT E1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = E1.YEAR AND ";
        $query .= "                         E1.SEMESTER = '".$semester."' AND ";
        $query .= "                         T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                     ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO IN ('{$model->selectdata}') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象（合否・進路先）取得
    function getGouhi($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD1 || '-' || NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E005' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        if ($model->field["OUT_DIV"] == "2") {
            $query .= "     VALUE(NAME2, NAME1) AS LABEL, ";
        } else {
            $query .= "     NAME1 AS LABEL, ";
        }
        $query .= "     NAMECD1 || '-' || NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E006' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種別（設置区分）取得
    function getKubun()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD1 || '-' || NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 in ('E012') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ
    function getNameMst($model, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        if ($model->field["OUT_DIV"] == "2") {
            $query .= "     VALUE(NAME2, NAME1) AS NAME1 ";
        } else {
            $query .= "     NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' AND NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    //名称マスタ
    function getNameMst2($namecd1, $namespare = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1      AS LABEL, ";
        $query .= "     NAMECD2    AS VALUE, ";
        $query .= "     NAMESPARE2 AS NAMESPARE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namespare != ""){
            $query .= "     AND NAMESPARE2 = '".$namespare."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //NAMESPARE取得
    function getNamespare($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMESPARE2 IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     NAMESPARE2 ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE2 ";
        return $query;
    }

    //進学
    function getPrintSingaku($model, $year, $semester, $senkouKind, $gouhiCd1, $gouhiCd2, $kubunarry, $selectdata, $mix = "")
    {
        $query  = "";
        $query .= " WITH EXAM_CALENDAR_FORM AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, FORM_CD, MAX(PROGRAM_NAME) AS PROGRAM_NAME, MAX(FORM_NAME) AS FORM_NAME ";
        $query .= "     FROM ";
        $query .= "         COLLEGE_EXAM_CALENDAR T1 ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, FORM_CD  ";
        $query .= " ), EXAM_CALENDAR_PROGRAM AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, MAX(PROGRAM_NAME) AS PROGRAM_NAME ";
        $query .= "     FROM ";
        $query .= "         EXAM_CALENDAR_FORM T1 ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD  ";
        $query .= " ), SCHREG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         S2.HR_NAME, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         S1.SEX ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT S2   ON S2.YEAR     = T1.YEAR ";
        $query .= "                                         AND S2.SEMESTER = T1.SEMESTER ";
        $query .= "                                         AND S2.GRADE    = T1.GRADE ";
        $query .= "                                         AND S2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '{$year}' AND ";
        $query .= "         T1.SEMESTER = '{$semester}' ";
        if ($model->field["DATA_DIV"] == "1") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS IN ('{$selectdata}') ";
        } else {
            $query .= "     AND T1.SCHREGNO IN ('{$selectdata}') ";
        }
        $query .= "     UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         'ZZ-' || VALUE(FISCALYEAR(CASE WHEN S2.GRD_DATE IS NOT NULL THEN S2.GRD_DATE ELSE S1.GRD_DATE END),'') || '-' || VALUE(S2.GRD_GRADE,'') AS GRADE, ";
        $query .= "         'ZZZ-' || VALUE(S2.GRD_HR_CLASS,'') AS HR_CLASS, ";
        $query .= "         'ZZZ-' || VALUE(S2.GRD_ATTENDNO,'') AS ATTENDNO, ";
        $query .= "         '既卒生' AS HR_NAME, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         S1.SEX ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN GRD_BASE_MST S2 ON S2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$year}' AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT E1 ";
        $query .= "                     WHERE ";
        $query .= "                         E1.YEAR     = T1.YEAR AND ";
        $query .= "                         E1.SEMESTER = '{$semester}' AND ";
        $query .= "                         E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                     ) ";
        if ($model->field["DATA_DIV"] == "1") {
            $query .= "     AND 'ZZZZZ' IN ('{$selectdata}') ";
        } else {
            $query .= "     AND T1.SCHREGNO IN ('{$selectdata}') ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     I1.GRADE, ";
        $query .= "     I1.HR_CLASS, ";
        $query .= "     I1.ATTENDNO, ";
        $query .= "     I1.HR_NAME, ";
        $query .= "     I1.NAME, ";
        $query .= "     I1.NAME_KANA, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     T1.SENKOU_KIND, ";
        $query .= "     T1.STAT_CD, ";
        $query .= "     L1.SCHOOL_NAME as STAT_NAME, ";
        $query .= "     T1.SCHOOL_GROUP, ";
        $query .= "     E012.NAME1 as SCHOOL_GROUP_NAME, ";
        $query .= "     T1.FACULTYCD, ";
        $query .= "     L2.FACULTYNAME, ";
        $query .= "     T1.DEPARTMENTCD, ";
        $query .= "     L3.DEPARTMENTNAME, ";
        $query .= "     T1.PREF_CD, ";
        $query .= "     L4.PREF_NAME, ";
        $query .= "     T1.HOWTOEXAM, ";
        $query .= "     E002.NAME1 as HOWTOEXAM_NAME, ";
        $query .= "     T1.DECISION, ";
        $query .= "     E005.NAME1 as DECISION_NAME, ";
        $query .= "     T1.PLANSTAT, ";
        $query .= "     AFT_GRAD_D.REMARK9 AS EXAMNO, ";
        $query .= "     E006.NAME1 as PLANSTAT_NAME, ";
        $query .= "     ECF.FORM_NAME, ";
        $query .= "     ECP.PROGRAM_NAME, ";
        $query .= "     CASE WHEN AFT_GRAD_D5.REMARK1 = '1' THEN '〇' ELSE '' END AS CHKMARK ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG I1 ON I1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ON T1.YEAR = AFT_GRAD_D.YEAR ";
        $query .= "                                        AND T1.SEQ = AFT_GRAD_D.SEQ ";
        $query .= "                                        AND AFT_GRAD_D.DETAIL_SEQ = 1 ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST L2 ON L2.SCHOOL_CD = T1.STAT_CD ";
        $query .= "                                     AND L2.FACULTYCD = T1.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ON L3.SCHOOL_CD    = T1.STAT_CD ";
        $query .= "                                        AND L3.FACULTYCD    = T1.FACULTYCD ";
        $query .= "                                        AND L3.DEPARTMENTCD = T1.DEPARTMENTCD ";
        $query .= "     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ";
        $query .= "     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ";
        $query .= "     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ";
        $query .= "     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ";
        $query .= "     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = T1.SCHOOL_GROUP ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = I1.SEX ";
        $query .= "     LEFT JOIN EXAM_CALENDAR_PROGRAM ECP ON ECP.YEAR = T1.YEAR AND ECP.SCHOOL_CD = T1.STAT_CD AND ECP.FACULTYCD = T1.FACULTYCD AND ECP.DEPARTMENTCD = T1.DEPARTMENTCD ";
        $query .= "         AND ECP.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECP.PROGRAM_CD = AFT_GRAD_D.REMARK2 ";
        $query .= "     LEFT JOIN EXAM_CALENDAR_FORM ECF ON ECF.YEAR = T1.YEAR AND ECF.SCHOOL_CD = T1.STAT_CD AND ECF.FACULTYCD = T1.FACULTYCD AND ECF.DEPARTMENTCD = T1.DEPARTMENTCD ";
        $query .= "         AND ECF.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECF.PROGRAM_CD = AFT_GRAD_D.REMARK2 AND ECF.FORM_CD = AFT_GRAD_D.REMARK3 ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D5 ON AFT_GRAD_D5.YEAR = T1.YEAR ";
        $query .= "          AND AFT_GRAD_D5.SEQ = T1.SEQ ";
        $query .= "          AND AFT_GRAD_D5.DETAIL_SEQ = 5 ";
        if($mix == "MIX"){
            $query .= "     LEFT JOIN NAME_MST NM ";
            $query .= "            ON NM.NAMECD1    = '{$gouhiCd1}' ";
            $query .= "           AND NM.NAMESPARE2 = '{$gouhiCd2}' ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SENKOU_KIND  = '{$senkouKind}' ";

        $querywk = " AND ( ";
        $qconnectwk = "";
        foreach($kubunarry as $kbn) {
            list($kubunCd1, $kubunCd2) = explode("-", $kbn);
            if (get_count($kubunarry) == 1 && $kbn == "E000-ALL") {
                $querywk = "";
                continue;
            }
            if ($kubunCd1 == "E012") {
                $querywk .= $qconnectwk." T1.SCHOOL_GROUP = '{$kubunCd2}' ";
            }
            $qconnectwk = " OR ";
        }
        $query .= $qconnectwk == "" ? "" : $querywk." ) ";

        if ($gouhiCd1 == "E005") {
            if($mix == "MIX"){
                $query .= " AND T1.DECISION     = NM.NAMECD2 ";
            } else {
                $query .= " AND T1.DECISION     = '{$gouhiCd2}' ";
            }
        }
        if ($gouhiCd1 == "E006") {
            $query .= " AND T1.PLANSTAT     = '{$gouhiCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     I1.GRADE, ";
        $query .= "     I1.HR_CLASS, ";
        $query .= "     I1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEQ ";
        return $query;
    }

    //就職
    function getPrintShushoku($model, $year, $semester, $senkouKind, $gouhiCd1, $gouhiCd2, $selectdata, $mix = "")
    {
        $query  = " WITH SCHREG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         S2.HR_NAME, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         S1.SEX ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT S2   ON S2.YEAR     = T1.YEAR ";
        $query .= "                                         AND S2.SEMESTER = T1.SEMESTER ";
        $query .= "                                         AND S2.GRADE    = T1.GRADE ";
        $query .= "                                         AND S2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '{$year}' AND ";
        $query .= "         T1.SEMESTER = '{$semester}' ";
        if ($model->field["DATA_DIV"] == "1") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS IN ('{$selectdata}') ";
        } else {
            $query .= "     AND T1.SCHREGNO IN ('{$selectdata}') ";
        }
        $query .= "     UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         'ZZ-' || VALUE(FISCALYEAR(CASE WHEN S2.GRD_DATE IS NOT NULL THEN S2.GRD_DATE ELSE S1.GRD_DATE END),'') || '-' || VALUE(S2.GRD_GRADE,'') AS GRADE, ";
        $query .= "         'ZZZ-' || VALUE(S2.GRD_HR_CLASS,'') AS HR_CLASS, ";
        $query .= "         'ZZZ-' || VALUE(S2.GRD_ATTENDNO,'') AS ATTENDNO, ";
        $query .= "         '既卒生' AS HR_NAME, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         S1.SEX ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN GRD_BASE_MST S2 ON S2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$year}' AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT E1 ";
        $query .= "                     WHERE ";
        $query .= "                         E1.YEAR     = T1.YEAR AND ";
        $query .= "                         E1.SEMESTER = '{$semester}' AND ";
        $query .= "                         E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                     ) ";
        if ($model->field["DATA_DIV"] == "1") {
            $query .= "     AND 'ZZZZZ' IN ('{$selectdata}') ";
        } else {
            $query .= "     AND T1.SCHREGNO IN ('{$selectdata}') ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     I1.GRADE, ";
        $query .= "     I1.HR_CLASS, ";
        $query .= "     I1.ATTENDNO, ";
        $query .= "     I1.HR_NAME, ";
        $query .= "     I1.NAME, ";
        $query .= "     I1.NAME_KANA, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     T1.SENKOU_KIND, ";
        $query .= "     T1.STAT_CD, ";
        $query .= "     L1.COMPANY_NAME as STAT_NAME, ";
        $query .= "     L1.INDUSTRY_LCD, ";
        $query .= "     L2.INDUSTRY_LNAME, ";
        $query .= "     T1.PREF_CD, ";
        $query .= "     L4.PREF_NAME, ";
        $query .= "     T1.HOWTOEXAM, ";
        $query .= "     E002.NAME1 as HOWTOEXAM_NAME, ";
        $query .= "     T1.DECISION, ";
        $query .= "     E005.NAME1 as DECISION_NAME, ";
        $query .= "     T1.PLANSTAT, ";
        $query .= "     VALUE(E006.NAME2,E006.NAME1) as PLANSTAT_NAME ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG I1 ON I1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN COMPANY_MST L1 ON L1.COMPANY_CD = T1.STAT_CD ";
        $query .= "     LEFT JOIN INDUSTRY_L_MST L2 ON L2.INDUSTRY_LCD = L1.INDUSTRY_LCD ";
        $query .= "     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ";
        $query .= "     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ";
        $query .= "     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ";
        $query .= "     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = I1.SEX ";
        if($mix == "MIX"){
            $query .= "     LEFT JOIN NAME_MST NM ";
            $query .= "            ON NM.NAMECD1    = '{$gouhiCd1}' ";
            $query .= "           AND NM.NAMESPARE2 = '{$gouhiCd2}' ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SENKOU_KIND  = '{$senkouKind}' ";
        if ($gouhiCd1 == "E005") {
            if($mix == "MIX"){
                $query .= " AND T1.DECISION     = NM.NAMECD2 ";
            } else {
                $query .= " AND T1.DECISION     = '{$gouhiCd2}' ";
            }
        }
        if ($gouhiCd1 == "E006") {
            $query .= " AND T1.PLANSTAT     = '{$gouhiCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     I1.GRADE, ";
        $query .= "     I1.HR_CLASS, ";
        $query .= "     I1.ATTENDNO, ";
        $query .= "     T1.SEQ ";
        return $query;
    }

    //学期開始日取得
    function getSdate($model) {
        $query  = " SELECT ";
        $query .= "     SDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' ";

        return $query;
    }

}
?>
