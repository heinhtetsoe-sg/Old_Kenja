<?php

require_once('for_php7.php');

class knjd186iQuery extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER IN ('1', '9') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //MAX学期を取得
    function getMaxSemester() {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    //学年一覧取得
    function getIBGrade() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR      = T1.YEAR ";
        $query .= "                            AND L1.NAMECD1   = 'A034' ";
        $query .= "                            AND L1.NAME1 <> 'M' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE BETWEEN L1.NAME2 AND L1.NAME3 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //IBコース一覧取得
    function getIBPrgCourse() {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A034' AND ";
        $query .= "     NAME1 <> 'M' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //IB科目一覧取得
    function getIbSubclass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD AS VALUE, ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD  || ':' || T1.IBSUBCLASSNAME_ENG AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_IBSUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN IBSUBCLASS_REPLACE_DAT L1 ";
        $query .= "                      ON L1.IBYEAR           = T1.IBYEAR ";
        $query .= "                     AND L1.IBCLASSCD        = T1.IBCLASSCD ";
        $query .= "                     AND L1.IBPRG_COURSE     = T1.IBPRG_COURSE ";
        $query .= "                     AND L1.IBCURRICULUM_CD  = T1.IBCURRICULUM_CD ";
        $query .= "                     AND L1.IBSUBCLASSCD     = T1.IBSUBCLASSCD ";
        $query .= "     LEFT JOIN CHAIR_DAT L2 ";
        $query .= "                      ON L2.YEAR             = L1.IBYEAR ";
        $query .= "                     AND L2.SEMESTER         = '".CTRL_SEMESTER."' ";
        $query .= "                     AND L2.CLASSCD          = L1.CLASSCD ";
        $query .= "                     AND L2.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        $query .= "                     AND L2.CURRICULUM_CD    = L1.CURRICULUM_CD ";
        $query .= "                     AND L2.SUBCLASSCD       = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.IBPRG_COURSE = '".$model->field["IBPRG_COURSE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND L2.CHAIRCD IN ";
            $query .= "            (SELECT ";
            $query .= "                 C1.CHAIRCD ";
            $query .= "             FROM ";
            $query .= "                 CHAIR_STF_DAT C1 ";
            $query .= "             WHERE ";
            $query .= "                 L2.YEAR     = C1.YEAR AND ";
            $query .= "                 L2.SEMESTER = C1.SEMESTER AND ";
            $query .= "                 C1.STAFFCD  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目一覧取得
    function getSubclass($model, $div="") {
        if ($div == "CNT") {
            $query  = " WITH CNT_T AS ( ";
        } else {
            $query  = "";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "              ON T1.CLASSCD          = L1.CLASSCD ";
        $query .= "             AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        $query .= "             AND T1.CURRICULUM_CD    = L1.CURRICULUM_CD ";
        $query .= "             AND T1.SUBCLASSCD       = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR   = '".CTRL_YEAR."' AND ";
        $query .= "     T1.IBGRADE  = '".$model->field["GRADE"]."' AND ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '".$model->field["IBSUBCLASSCD"]."' AND ";
        $query .= "     VALUE(T1.CLASSCD, '')       != '' AND ";
        $query .= "     VALUE(T1.SCHOOL_KIND, '')   != '' AND ";
        $query .= "     VALUE(T1.CURRICULUM_CD, '') != '' AND ";
        $query .= "     VALUE(T1.SUBCLASSCD, '')    != '' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        if ($div == "CNT") {
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     CNT_T ";
        }

        return $query;
    }

    //講座一覧取得
    function getChair($model, $subclasscd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIRCD AS VALUE, ";
        $query .= "     CHAIRCD || ':' || CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "     SEMESTER = '". CTRL_SEMESTER ."' AND ";
        } else {
            $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        }
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全講座取得
    function getAllChair($model) {
        $query  = " WITH SUB_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         IBSUBCLASS_UNIT_DAT ";
        $query .= "     WHERE ";
        $query .= "         IBYEAR  = '".CTRL_YEAR."' AND ";
        $query .= "         IBGRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "         IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD = '".$model->field["IBSUBCLASSCD"]."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     L1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SUB_T T1 ";
        $query .= "     INNER JOIN CHAIR_DAT L1 ";
        $query .= "              ON L1.YEAR             = '".CTRL_YEAR."' ";
        $query .= "             AND T1.CLASSCD          = L1.CLASSCD ";
        $query .= "             AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        $query .= "             AND T1.CURRICULUM_CD    = L1.CURRICULUM_CD ";
        $query .= "             AND T1.SUBCLASSCD       = L1.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     L1.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     L1.CHAIRCD ";

        return $query;
    }

    //生徒取得
    function getStudent($model, $chairInState) {
        $query  = " WITH SCH_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "         SEMESTER = '". CTRL_SEMESTER ."' AND ";
        } else {
            $query .= "         SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        }
        $query .= "         CHAIRCD IN (".$chairInState.") ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || '-' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T3.HR_NAMEABBV || '-' || T2.ATTENDNO || '　' || T1.SCHREGNO || '　' || T1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "              ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND T2.YEAR     = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "             AND T2.SEMESTER = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= "             AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "              ON T3.YEAR     = T2.YEAR ";
        $query .= "             AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T3.GRADE    = T2.GRADE ";
        $query .= "             AND T3.HR_CLASS = T2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) AND ";
        $query .= "     T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
