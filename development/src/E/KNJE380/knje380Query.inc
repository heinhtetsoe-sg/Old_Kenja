<?php

require_once('for_php7.php');


class knje380Query extends Query {

    //校種の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //アンケート一覧取得
    function getQuestionnaireList()
    {
        $query  = " SELECT ";
        $query .= "     T1.QUESTIONNAIRECD AS VALUE, ";
        $query .= "     T1.QUESTIONNAIRECD || '：' || T2.QUESTIONNAIRENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT T1 ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST T2 ON T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年データ
    function getGrade($model, $grade="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE ";
        if ($grade) {
            $query .= " AND T1.GRADE    = '".$grade."' ";
        }
        //参照・更新可（制限付き）
        if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT) {
            $query .= " AND (  T1.TR_CD1    = '" .STAFFCD ."' ";
            $query .= "     OR T1.TR_CD2    = '" .STAFFCD ."' ";
            $query .= "     OR T1.TR_CD3    = '" .STAFFCD ."' ";
            $query .= "     OR T1.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "     OR T1.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR T1.SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= "     AND T2.SCHOOL_KIND = '".$model->field["A023_SCHOOL_KIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラスデータ
    function getRegdDat($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学科データ
    function getMajor($model)
    {
        $query .= " WITH ALL_T(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('9999', '全学科') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ALL_T ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE, ";
        $query .= "     I1.COURSENAME || ' ' || I2.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN COURSE_MST I1 ON T1.COURSECD = I1.COURSECD ";
        $query .= "     INNER JOIN MAJOR_MST I2 ON T1.COURSECD = I2.COURSECD ";
        $query .= "           AND T1.MAJORCD = I2.MAJORCD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR         = T1.YEAR ";
        $query .= "         AND G1.GRADE        = T1.GRADE ";
        $query .= "         AND G1.SCHOOL_KIND  = '".$model->field["A023_SCHOOL_KIND"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["KUBUN"] == "2") {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.COURSECD || T1.MAJORCD, ";
        $query .= "     I1.COURSENAME || ' ' || I2.MAJORNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校マスタ
    function getSchoolMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["A023_SCHOOL_KIND"]."' ";
        }

        return $query;
    }

    //学科名
    function getMajorName($majorCd)
    {
        $query  = " SELECT ";
        $query .= "     L1.COURSENAME || ' ' || T1.MAJORNAME AS MAJORNAME ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST T1 ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON T1.COURSECD = L1.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.COURSECD || T1.MAJORCD = '".$majorCd."' ";

        return $query;
    }

    function getStudentSql($model, $selected, $majorCd) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     HR.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.SEX AS SEX_CD, ";
        $query .= "     N1.NAME2 AS SEX, ";
        $query .= "     BASE.GRD_DATE, ";
        $query .= "     BASE.GRD_DIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     COURSE.COURSENAME, ";
        $query .= "     MAJOR.MAJORNAME, ";
        $query .= "     COURSEC.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT AS HR ON T1.YEAR = HR.YEAR ";
        $query .= "          AND T1.SEMESTER = HR.SEMESTER ";
        $query .= "          AND T1.GRADE = HR.GRADE ";
        $query .= "          AND T1.HR_CLASS = HR.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST AS BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST AS N1 ON N1.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = N1.NAMECD2 ";
        $query .= "     LEFT JOIN COURSECODE_MST AS COURSEC ON T1.COURSECODE = COURSEC.COURSECODE ";
        $query .= "     LEFT JOIN MAJOR_MST AS MAJOR ON T1.COURSECD = MAJOR.COURSECD ";
        $query .= "          AND T1.MAJORCD = MAJOR.MAJORCD ";
        $query .= "     LEFT JOIN COURSE_MST AS COURSE ON T1.COURSECD = COURSE.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["KUBUN"] == "1") {
            $query .= "     AND T1.GRADE = '".$selected."' ";
        } else {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$selected."' ";
        }
        if ($majorCd != "9999") {
            $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$majorCd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    function getAftSql($schregNo, $senkou) {
        $query  = " SELECT ";
        $query .= "     T1.STAT_KIND, ";
        $query .= "     T1.SENKOU_KIND, ";
        $query .= "     T1.STAT_CD, ";
        $query .= "     T1.STAT_NAME, ";
        $query .= "     T1.SCHOOL_GROUP, ";
        $query .= "     T1.JOBTYPE_LCD, ";
        $query .= "     JOB_L.JOBTYPE_LNAME, ";
        $query .= "     T1.JOBTYPE_MCD, ";
        $query .= "     JOB_M.JOBTYPE_MNAME, ";
        $query .= "     T1.JOBTYPE_SCD, ";
        $query .= "     JOB_S.JOBTYPE_SNAME, ";
        $query .= "     T1.SCHOOL_SORT, ";
        $query .= "     T1.PREF_CD, ";
        $query .= "     T1.CITY_CD, ";
        $query .= "     T1.DECISION, ";
        $query .= "     T1.PLANSTAT, ";
        $query .= "     T1.INTRODUCTION_DIV, ";
        $query .= "     T1.STAT_DATE1, ";
        $query .= "     T1.STAT_STIME, ";
        $query .= "     T1.STAT_ETIME, ";
        $query .= "     T1.AREA_NAME, ";
        $query .= "     T1.STAT_DATE2, ";
        $query .= "     T1.CONTENTEXAM, ";
        $query .= "     T1.REASONEXAM, ";
        $query .= "     T1.THINKEXAM, ";
        $query .= "     T1.JOB_DATE1, ";
        $query .= "     T1.JOB_STIME, ";
        $query .= "     T1.JOB_ETIME, ";
        $query .= "     T1.JOB_REMARK, ";
        $query .= "     T1.JOB_CONTENT, ";
        $query .= "     T1.JOB_THINK, ";
        $query .= "     T1.JOBEX_DATE1, ";
        $query .= "     T1.JOBEX_STIME, ";
        $query .= "     T1.JOBEX_ETIME, ";
        $query .= "     T1.JOBEX_REMARK, ";
        $query .= "     T1.JOBEX_CONTENT, ";
        $query .= "     T1.JOBEX_THINK, ";
        $query .= "     COLL_M.SCHOOL_CD, ";
        $query .= "     COLL_M.SCHOOL_NAME, ";
        $query .= "     T1.FACULTYCD, ";
        $query .= "     FACU.FACULTYNAME, ";
        $query .= "     T1.DEPARTMENTCD, ";
        $query .= "     DEPAR.DEPARTMENTNAME, ";
        $query .= "     COLL_M.TELNO AS SCHOOL_TELNO, ";
        $query .= "     COMPANY.COMPANY_CD, ";
        $query .= "     COMPANY.COMPANY_NAME, ";
        $query .= "     COMPANY.SHUSHOKU_ADDR, ";
        $query .= "     COMPANY.SHIHONKIN, ";
        $query .= "     COMPANY.SONINZU, ";
        $query .= "     COMPANY.TONINZU, ";
        $query .= "     COMPANY.INDUSTRY_LCD, ";
        $query .= "     INDU_L.INDUSTRY_LNAME, ";
        $query .= "     COMPANY.INDUSTRY_MCD, ";
        $query .= "     INDU_M.INDUSTRY_MNAME, ";
        $query .= "     COMPANY.COMPANY_SORT, ";
        $query .= "     COMPANY.TARGET_SEX, ";
        $query .= "     COMPANY.ZIPCD, ";
        $query .= "     COMPANY.ADDR1, ";
        $query .= "     COMPANY.ADDR2, ";
        $query .= "     COMPANY.TELNO AS COMPANY_TELNO, ";
        $query .= "     COMPANY.REMARK ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST JOB_L ON T1.JOBTYPE_LCD = JOB_L.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN JOBTYPE_M_MST JOB_M ON T1.JOBTYPE_LCD = JOB_M.JOBTYPE_LCD ";
        $query .= "          AND T1.JOBTYPE_MCD = JOB_M.JOBTYPE_MCD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST JOB_S ON T1.JOBTYPE_LCD = JOB_S.JOBTYPE_LCD ";
        $query .= "          AND T1.JOBTYPE_MCD = JOB_S.JOBTYPE_MCD ";
        $query .= "          AND T1.JOBTYPE_SCD = JOB_S.JOBTYPE_SCD ";
        $query .= "     LEFT JOIN COLLEGE_MST COLL_M ON T1.STAT_CD = COLL_M.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST FACU ON T1.STAT_CD = FACU.SCHOOL_CD ";
        $query .= "          AND T1.FACULTYCD = FACU.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST DEPAR ON T1.STAT_CD = DEPAR.SCHOOL_CD ";
        $query .= "          AND T1.FACULTYCD = DEPAR.FACULTYCD ";
        $query .= "          AND T1.DEPARTMENTCD = DEPAR.DEPARTMENTCD ";
        $query .= "     LEFT JOIN COMPANY_MST COMPANY ON T1.STAT_CD = COMPANY.COMPANY_CD ";
        $query .= "     LEFT JOIN INDUSTRY_L_MST INDU_L ON COMPANY.INDUSTRY_LCD = INDU_L.INDUSTRY_LCD ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST INDU_M ON COMPANY.INDUSTRY_LCD = INDU_M.INDUSTRY_LCD ";
        $query .= "          AND COMPANY.INDUSTRY_MCD = INDU_M.INDUSTRY_MCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.SENKOU_KIND = '".$senkou."' ";
        $query .= "     AND T1.PLANSTAT = '1' ";

        return $query;
    }

    function getHopeSql($model, $schregNo) {
        $query  = " WITH HOPE_MAX_DATE AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(T1.ENTRYDATE) AS ENTRYDATE, ";
        $query .= "     MAX(T1.SCHREGNO) AS SCHREGNO, ";
        $query .= "     MAX(T1.QUESTIONNAIRECD) AS QUESTIONNAIRECD, ";
        $query .= "     MAX(T1.YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.QUESTIONNAIRECD = '".$model->field["QUESTIONNAIRECD"]."' ";
        $query .= " ), HOPE_MAX AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(T1.ENTRYDATE) AS ENTRYDATE, ";
        $query .= "     MAX(T1.SEQ) AS SEQ, ";
        $query .= "     MAX(T1.QUESTIONNAIRECD) AS QUESTIONNAIRECD, ";
        $query .= "     MAX(T1.SCHREGNO) AS SCHREGNO ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT T1, ";
        $query .= "     HOPE_MAX_DATE T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.ENTRYDATE = T2.ENTRYDATE ";
        $query .= "     AND T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT T1, ";
        $query .= "     HOPE_MAX T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTRYDATE = T2.ENTRYDATE ";
        $query .= "     AND T1.SEQ = T2.SEQ ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";

        return $query;
    }

    function getIndustrySql() {
        $query  = " SELECT ";
        $query .= "     MST_L.INDUSTRY_LCD, ";
        $query .= "     MST_L.INDUSTRY_LNAME, ";
        $query .= "     MST_L.L_GROUPCD, ";
        $query .= "     GRP_L.L_GROUPNAME, ";
        $query .= "     MST_M.INDUSTRY_MCD, ";
        $query .= "     MST_M.INDUSTRY_MNAME, ";
        $query .= "     MST_M.M_GROUPCD, ";
        $query .= "     MST_M.NO_OUTPUT, ";
        $query .= "     GRP_M.M_GROUPNAME ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_L_MST MST_L ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST MST_M ON MST_L.INDUSTRY_LCD = MST_M.INDUSTRY_LCD ";
        $query .= "     LEFT JOIN INDUSTRY_LGROUP_MST GRP_L ON MST_L.L_GROUPCD = GRP_L.L_GROUPCD ";
        $query .= "     LEFT JOIN INDUSTRY_MGROUP_MST GRP_M ON MST_M.M_GROUPCD = GRP_M.M_GROUPCD ";
        $query .= " ORDER BY ";
        $query .= "     MST_L.INDUSTRY_LCD, ";
        $query .= "     VALUE(GRP_M.M_GROUPCD, '0'), ";
        $query .= "     MST_M.INDUSTRY_MCD ";

        return $query;
    }

    function getPercentSql($kei, $soukei) {
        $query  = " VALUES(INT(ROUND(FLOAT(".$kei.") / FLOAT(".$soukei.") * 100, 0))) ";

        return $query;
    }

}
?>
