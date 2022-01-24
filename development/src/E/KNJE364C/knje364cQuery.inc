<?php

require_once('for_php7.php');

class knje364cQuery extends Query {

    //アンケート一覧取得
    function getQuestionnaireList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.QUESTIONNAIRECD AS VALUE, ";
        $query .= "     T1.QUESTIONNAIRECD || '：' || T2.QUESTIONNAIRENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT T1 ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST T2 ON T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //名称マスタより校種取得
    function getVNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //年組取得
    function getListToList($model, $left=false) {
        $query  = " SELECT ";
        $query .= "     HDAT.GRADE || '-' || HDAT.HR_CLASS AS VALUE ";
        $query .= "     , HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON GDAT.YEAR = HDAT.YEAR ";
        $query .= "         AND GDAT.GRADE = HDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "     HDAT.YEAR = '".$model->year."' ";
        $query .= "     AND HDAT.SEMESTER = '".$model->semester."' ";
        $query .= "     AND GDAT.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        if ($left) {
            $query .= "     AND HDAT.GRADE || '-' || HDAT.HR_CLASS IN ('".str_replace(",", "','", $model->selectdata)."') ";
        } else {
            $query .= "     AND HDAT.GRADE || '-' || HDAT.HR_CLASS NOT IN ('".str_replace(",", "','", $model->selectdata)."') ";
        }

        return $query;
    }

    //年度内期間取得
    function getSemesterMst()
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '9' ";

        return $query;
    }

    function getVNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= "     , NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    function getFacultyGroupMst() {
        $query  = " SELECT ";
        $query .= "     FACULTY_GROUP ";
        $query .= "     , FACULTY_GROUPNAME ";
        $query .= " FROM ";
        $query .= "     FACULTY_GROUP_MST ";
        $query .= " ORDER BY ";
        $query .= "     FACULTY_GROUP ";

        return $query;
    }

    function getDepartmentGroupMst() {
        $query  = " SELECT ";
        $query .= "     DEPARTMENT_GROUP ";
        $query .= "     , DEPARTMENT_GROUPNAME ";
        $query .= " FROM ";
        $query .= "     DEPARTMENT_GROUP_MST ";
        $query .= " ORDER BY ";
        $query .= "     DEPARTMENT_GROUP ";

        return $query;
    }

    function getHrClassName($model, $gradeHrClass) {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND GRADE || '-' || HR_CLASS = '".$gradeHrClass."' ";

        return $query;
    }

    function getCsvDat($model, $gradeHrClass) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO ";
        $query .= "     , REGD.ATTENDNO ";
        $query .= "     , BASE.NAME ";
        $query .= "     , BASE.SEX ";
        $query .= "     , HOPE.SEQ ";
        $query .= "     , HOPE.SCHOOL_GROUP1 ";
        $query .= "     , HOPE.FACULTY_GROUP1 ";
        $query .= "     , HOPE.DEPARTMENT_GROUP1 ";
        $query .= "     , HOPE.SCHOOL_CD1 ";
        $query .= "     , HOPE.FACULTYCD1 ";
        $query .= "     , HOPE.DEPARTMENTCD1 ";
        $query .= "     , HOPE.SCHOOL_CD2 ";
        $query .= "     , HOPE.FACULTYCD2 ";
        $query .= "     , HOPE.DEPARTMENTCD2 ";
        $query .= "     , COL1.SCHOOL_NAME AS SCHOOL_NAME1 ";
        $query .= "     , SC_GRP.NAME1 AS SCHOOL_GROUP_NAME1 ";
        $query .= "     , FA_GRP.FACULTY_GROUPNAME AS FACULTY_GROUPNAME1 ";
        $query .= "     , COL2.SCHOOL_NAME AS SCHOOL_NAME2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ENTRYDATE ";
        $query .= "             , SCHREGNO ";
        $query .= "             , COURSE_KIND ";
        $query .= "             , QUESTIONNAIRECD ";
        $query .= "             , YEAR ";
        $query .= "             , MAX(SEQ) AS SEQ ";
        $query .= "         FROM ";
        $query .= "             COURSE_HOPE_DAT ";
        $query .= "         GROUP BY ";
        $query .= "             ENTRYDATE ";
        $query .= "             , SCHREGNO ";
        $query .= "             , COURSE_KIND ";
        $query .= "             , QUESTIONNAIRECD ";
        $query .= "             , YEAR ";
        $query .= "     ) MAX_SEQ ";
        $query .= "         ON REGD.SCHREGNO = MAX_SEQ.SCHREGNO ";
        $query .= "     INNER JOIN COURSE_HOPE_DAT HOPE ";
        $query .= "         ON HOPE.ENTRYDATE = MAX_SEQ.ENTRYDATE ";
        $query .= "         AND HOPE.SEQ = MAX_SEQ.SEQ ";
        $query .= "         AND HOPE.SCHREGNO = MAX_SEQ.SCHREGNO ";
        $query .= "         AND HOPE.COURSE_KIND = MAX_SEQ.COURSE_KIND ";
        $query .= "         AND HOPE.QUESTIONNAIRECD = MAX_SEQ.QUESTIONNAIRECD ";
        $query .= "     LEFT JOIN COLLEGE_MST COL1 ";
        $query .= "         ON COL1.SCHOOL_CD = HOPE.SCHOOL_CD1 ";
        $query .= "     LEFT JOIN V_NAME_MST SC_GRP ";
        $query .= "         ON SC_GRP.YEAR = '".$model->year."' ";
        $query .= "         AND SC_GRP.NAMECD1 = 'E012' ";
        $query .= "         AND SC_GRP.NAMECD2 = HOPE.SCHOOL_GROUP1 ";
        $query .= "     LEFT JOIN FACULTY_GROUP_MST FA_GRP ";
        $query .= "         ON FA_GRP.FACULTY_GROUP = HOPE.FACULTYCD1 ";
        $query .= "     LEFT JOIN COLLEGE_MST COL2 ";
        $query .= "         ON COL2.SCHOOL_CD = HOPE.SCHOOL_CD2 ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".$model->year."' ";
        $query .= "     AND REGD.SEMESTER = '".$model->semester."' ";
        $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '".$gradeHrClass."' ";
        $query .= "     AND HOPE.ENTRYDATE = '".str_replace("/", "-", $model->field["ENTRYDATE"])."' ";
        $query .= "     AND HOPE.COURSE_KIND = '".$model->field["COURSE_KIND"]."' ";
        $query .= "     AND HOPE.QUESTIONNAIRECD = '".$model->field["QUESTIONNAIRECD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    function getCourseHopeDetailDat($model, $seq, $schregno, $hopeNum) {
        $query  = " SELECT ";
        $query .= "     HOPE_DE.HOPE_NUM ";
        $query .= "     , HOPE_DE.SCHOOL_GROUP ";
        $query .= "     , HOPE_DE.FACULTY_GROUP ";
        $query .= "     , HOPE_DE.DEPARTMENT_GROUP ";
        $query .= "     , HOPE_DE.SCHOOL_CD ";
        $query .= "     , HOPE_DE.FACULTYCD ";
        $query .= "     , HOPE_DE.DEPARTMENTCD ";
        $query .= "     , COL.SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DETAIL_DAT HOPE_DE ";
        $query .= "     LEFT JOIN COLLEGE_MST COL ";
        $query .= "         ON COL.SCHOOL_CD = HOPE_DE.SCHOOL_CD ";
        $query .= " WHERE ";
        $query .= "     HOPE_DE.ENTRYDATE = '".str_replace("/", "-", $model->field["ENTRYDATE"])."' ";
        $query .= "     AND HOPE_DE.SEQ = '".$seq."' ";
        $query .= "     AND HOPE_DE.SCHREGNO = '".$schregno."' ";
        $query .= "     AND HOPE_DE.HOPE_NUM = '".$hopeNum."' ";
        $query .= " ORDER BY ";
        $query .= "     HOPE_NUM ";

        return $query;
    }

}
?>
