<?php

require_once('for_php7.php');

require_once("PrimarySchoolProcess.php");

class knjd126dQuery extends Query {

    /* 年度 */
    function getYear() {
        $query  = " WITH CTL_YT (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.IBYEAR AS VALUE, ";
        $query .= "     T1.IBYEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNITPLAN_DAT T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.LABEL ";
        $query .= " FROM ";
        $query .= "     CTL_YT T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    /* 学年 */
    function getGrade($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T2.NAMECD1 = 'A034' ";
        $query .= "     AND T2.NAME1 != 'M' ";
        $query .= "     AND T1.GRADE BETWEEN T2.NAME2 AND T2.NAME3 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    /* IBコース */
    function getIbCourse() {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A034' ";
        $query .= "     AND NAME1 != 'M' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    /* IB科目 */
    function getIbSubclass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD AS VALUE, ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD  || ':' || T1.IBSUBCLASSNAME_ENG AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_IBSUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN IBSUBCLASS_REPLACE_DAT L1 ON L1.IBYEAR = T1.IBYEAR ";
        $query .= "                                        AND L1.IBCLASSCD = T1.IBCLASSCD ";
        $query .= "                                        AND L1.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                                        AND L1.IBCURRICULUM_CD = T1.IBCURRICULUM_CD ";
        $query .= "                                        AND L1.IBSUBCLASSCD = T1.IBSUBCLASSCD ";
        $query .= "     LEFT JOIN CHAIR_DAT L2 ON L2.YEAR = L1.IBYEAR ";
        $query .= "                           AND L2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                           AND L2.CLASSCD  = L1.CLASSCD ";
        $query .= "                           AND L2.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "                           AND L2.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "                           AND L2.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBPRG_COURSE = '{$model->field["IBPRG_COURSE"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND L2.CHAIRCD IN ";
            $query .= "             (SELECT  ";
            $query .= "                  C1.CHAIRCD ";
            $query .= "              FROM  ";
            $query .= "                  CHAIR_STF_DAT C1  ";
            $query .= "              WHERE  ";
            $query .= "                  L2.YEAR = C1.YEAR  ";
            $query .= "              AND L2.SEMESTER = C1.SEMESTER ";
            $query .= "              AND C1.STAFFCD = '".STAFFCD."')  ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    /* ADMIN_CONTROL_DAT */
    function getAdminControlDat($model) {
        $query .= " SELECT ";
        $query .= "     CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["IBYEAR"]}' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND CONTROL_FLG = '4' ";
        return $query;
    }

    /* 名称マスタ */
    function getNameMst($nameCd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$nameCd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    /* 科目 */
    function getSubclass($model, $div = "") {
        if ($div == "CNT") {
            $query  = " WITH CNT_T AS ( ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBGRADE = '{$model->field["IBGRADE"]}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
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

    /* 講座 */
    function getChair($model, $subclassCd) {

        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     MAX(T1.CHAIRCD || ':' || T1.CHAIRNAME) AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$subclassCd}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    /* 全講座 */
    function getAllChair($model) {

        $query  = " WITH SUB_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBGRADE = '{$model->field["IBGRADE"]}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     L1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SUB_T T1 ";
        $query .= "     INNER JOIN CHAIR_DAT L1 ON L1.YEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "           AND T1.CLASSCD = L1.CLASSCD ";
        $query .= "           AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "           AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "           AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     L1.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     L1.CHAIRCD ";
        return $query;
    }

    /* タイトル(横) */
    function getIbUnit($model, $subInstate) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.IBSEQ, ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L2.SEMESTERNAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS KEY_SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     '0000' AS VIEWCD, ";
        $query .= "     L3.EXECUTED, ";
        $query .= "     T1.LINK_NO ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SEMESTER_MST L2 ON T1.IBYEAR = L2.YEAR ";
        $query .= "          AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "     LEFT JOIN IBSUBCLASS_CHKFIN_DAT L3 ON T1.IBYEAR = L3.IBYEAR ";
        $query .= "          AND T1.IBGRADE = L3.IBGRADE ";
        $query .= "          AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = L3.IBCLASSCD || '-' || L3.IBPRG_COURSE || '-' || L3.IBCURRICULUM_CD || '-' || L3.IBSUBCLASSCD ";
        $query .= "          AND T1.IBSEQ = L3.IBSEQ ";
        $query .= "          AND L3.VIEWCD = '0000' ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBGRADE = '{$model->field["IBGRADE"]}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        if ($subInstate) {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ({$subInstate}) ";
        }
        $query .= "     AND T1.YEAR IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.IBSEQ, ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTERNAME, ";
        $query .= "     T1.KEY_SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.EXECUTED, ";
        $query .= "     T1.LINK_NO ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.IBSEQ, ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L2.SEMESTERNAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS KEY_SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     '0000' AS VIEWCD, ";
        $query .= "     L3.EXECUTED, ";
        $query .= "     T1.LINK_NO ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SEMESTER_MST L2 ON T1.IBYEAR = L2.YEAR ";
        $query .= "          AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "     LEFT JOIN IBSUBCLASS_CHKFIN_DAT L3 ON T1.IBYEAR = L3.IBYEAR ";
        $query .= "          AND T1.IBGRADE = L3.IBGRADE ";
        $query .= "          AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = L3.IBCLASSCD || '-' || L3.IBPRG_COURSE || '-' || L3.IBCURRICULUM_CD || '-' || L3.IBSUBCLASSCD ";
        $query .= "          AND T1.IBSEQ = L3.IBSEQ ";
        $query .= "          AND L3.VIEWCD = '0000', ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     MAIN_T.LINK_NO = T1.LINK_NO ";
        $query .= "     AND MAIN_T.IBYEAR || MAIN_T.KEY_SUBCLASSCD != T1.IBYEAR || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        if ($subInstate) {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ({$subInstate}) ";
        }
        $query .= "     AND T1.YEAR IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     IBSEQ, ";
        $query .= "     VIEWCD ";

        return $query;
    }

    /* タイトル(縦) */
    function getIbViewName($model) {
        $query  = " SELECT ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     T1.IBEVAL_DIV1, ";
        $query .= "     Z035.NAME1 AS Z035_NAME, ";
        $query .= "     T1.IBEVAL_DIV2, ";
        $query .= "     Z036.NAME1 AS Z036_NAME, ";
        $query .= "     Z037.NAME1 AS Z037_NAME, ";
        $query .= "     Z038.NAME1 AS Z038_NAME, ";
        $query .= "     T1.IBEVAL_MARK, ";
        $query .= "     T1.IBEVAL_NAME, ";
        $query .= "     T1.IBEVAL_ABBV, ";
        $query .= "     T1.IBSORT, ";
        $query .= "     T1.IBPERFECT ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST T1 ";
        $query .= "     LEFT JOIN NAME_MST Z035 ON Z035.NAMECD1 = 'Z035' ";
        $query .= "          AND T1.IBEVAL_DIV1 = Z035.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST Z036 ON Z036.NAMECD1 = 'Z036' ";
        $query .= "          AND T1.IBEVAL_DIV2 = Z036.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST Z037 ON Z037.NAMECD1 = 'Z037' ";
        $query .= "          AND T1.IBEVAL_DIV2 = Z037.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST Z038 ON Z038.NAMECD1 = 'Z038' ";
        $query .= "          AND T1.IBEVAL_DIV2 = Z038.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBGRADE = '{$model->field["IBGRADE"]}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        $query .= "     AND T1.IBEVAL_DIV1 = '{$model->field["IBEVAL_DIV1"]}' ";
        $query .= "     AND T1.IBEVAL_DIV2 = '{$model->field["IBEVAL_DIV2"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     T1.IBEVAL_DIV1, ";
        $query .= "     T1.IBEVAL_DIV2, ";
        $query .= "     T1.IBSORT, ";
        $query .= "     T1.IBEVAL_MARK ";

        return $query;
    }

    /* IBSUBCLASS_UNITPLAN_DAT(入力可) */
    function getIbsubclassUnitplanDat($model) {

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNITPLAN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBGRADE = '{$model->field["IBGRADE"]}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        $query .= "     AND T1.IBEVAL_DIV1 = '{$model->field["IBEVAL_DIV1"]}' ";
        $query .= "     AND T1.IBEVAL_DIV2 = '{$model->field["IBEVAL_DIV2"]}' ";
        return $query;
    }

    /* SEMESTER_MST(IBGrade入力不可) */
    function getMaxSemester($model) {

        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS MAX_SEM ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.SEMESTER < '9' ";
        return $query;
    }

    /* IBSUBCLASS_SELECT_DAT(IBGrade入力不可) */
    function getIbsubclassSelectDat($model) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_SELECT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBGRADE = '{$model->field["IBGRADE"]}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        return $query;
    }

    /* 生徒取得 */
    function getStudent($model, $chairInState) {

        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.CHAIRCD IN ({$chairInState}) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD_H.HR_NAMEABBV, ";
        $query .= "     REGD.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "          AND REGD.YEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = REGD.YEAR ";
        $query .= "          AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND REGD_H.GRADE = REGD.GRADE ";
        $query .= "          AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO IN (SELECT I1.SCHREGNO FROM SCH_T I1) ";
        $query .= "     AND REGD.GRADE = '{$model->field["IBGRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    /* Grade */
    function getGradeVal($model, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd) {

        $query  = " SELECT ";
        $query .= "     T1.GRADE ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_GRADE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$ibYear}' ";
        $query .= "     AND T1.IBGRADE = '{$ibGrade}' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= "     AND T1.IBEVAL_DIV1 = '{$div1}' ";
        $query .= "     AND T1.IBEVAL_DIV2 = '{$div2}' ";
        $query .= "     AND T1.IBEVAL_MARK = '{$mark}' ";
        $query .= "     AND T1.VIEWCD = '{$viewCd}' ";
        return $query;
    }

    /* 得点 */
    function getScoreAll($model, $schregNo) {

        $query  = " SELECT ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.IBSEQ, ";
        $query .= "     T1.IBEVAL_DIV1, ";
        $query .= "     T1.IBEVAL_DIV2, ";
        $query .= "     T1.IBEVAL_MARK, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.SCORE ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        return $query;
    }

    /* 満点 */
    function getPerfectAll($model) {

        $query .= " SELECT ";
        $query .= "     T1.IBEVAL_DIV1, ";
        $query .= "     T1.IBEVAL_DIV2, ";
        $query .= "     T1.IBEVAL_MARK, ";
        $query .= "     T1.IBPERFECT ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBGRADE = '{$model->field["IBGRADE"]}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        return $query;
    }

    /* 得点 */
    function getScore($model, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $seq, $viewCd) {

        $query  = " SELECT ";
        $query .= "     T1.SCORE ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$ibYear}' ";
        $query .= "     AND T1.IBGRADE = '{$ibGrade}' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        $query .= "     AND T1.IBSEQ = {$seq} ";
        $query .= "     AND T1.IBEVAL_DIV1 = '{$div1}' ";
        $query .= "     AND T1.IBEVAL_DIV2 = '{$div2}' ";
        $query .= "     AND T1.IBEVAL_MARK = '{$mark}' ";
        $query .= "     AND T1.VIEWCD = '{$viewCd}' ";
        return $query;
    }

    /* 満点 */
    function getPerfect($model, $div1, $div2, $mark, $seq) {

        $query .= " SELECT ";
        $query .= "     T1.IBPERFECT ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBGRADE = '{$model->field["IBGRADE"]}' ";
        $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        $query .= "     AND T1.IBEVAL_DIV1 = '{$div1}' ";
        $query .= "     AND T1.IBEVAL_DIV2 = '{$div2}' ";
        $query .= "     AND T1.IBEVAL_MARK = '{$mark}' ";
        return $query;
    }

    /* IBSUBCLASS_SCORE_DAT削除 */
    function getScoreDeleteSql($model, $schregNo, $ibYear, $ibGrade, $div1, $div2, $seq, $viewCd) {

        $query  = "   DELETE FROM IBSUBCLASS_SCORE_DAT ";
        $query .= "   WHERE ";
        $query .= "     IBYEAR = '{$ibYear}' ";
        $query .= "     AND IBGRADE = '{$ibGrade}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";
        $query .= "     AND IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        $query .= "     AND IBSEQ = {$seq} ";
        $query .= "     AND IBEVAL_DIV1 = '{$div1}' ";
        $query .= "     AND IBEVAL_DIV2 = '{$div2}' ";
        $query .= "     AND VIEWCD = '{$viewCd}' ";

        return $query;
    }

    /* IBSUBCLASS_SCORE_DAT新規 */
    function getScoreInsertSql($model, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $seq, $viewCd, $score) {

        list($classCd, $prgCourse, $curriculum, $subClassCd) = preg_split("/-/", $model->field["IBSUBCLASSCD"]);

        $data["IBYEAR"][TEXT]           = $ibYear;
        $data["IBGRADE"][TEXT]          = $ibGrade;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["IBCLASSCD"][TEXT]        = $classCd;
        $data["IBPRG_COURSE"][TEXT]     = $prgCourse;
        $data["IBCURRICULUM_CD"][TEXT]  = $curriculum;
        $data["IBSUBCLASSCD"][TEXT]     = $subClassCd;
        $data["IBSEQ"][NUMBER]          = $seq;
        $data["IBEVAL_DIV1"][TEXT]      = $div1;
        $data["IBEVAL_DIV2"][TEXT]      = $div2;
        $data["IBEVAL_MARK"][TEXT]      = $mark;
        $data["VIEWCD"][TEXT]           = $viewCd;
        $data["SCORE"][NUMBER]          = $score;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "IBSUBCLASS_SCORE_DAT");

        return $query;
    }

    /* IBSUBCLASS_GRADE_DAT削除 */
    function getGradeDeleteSql($model, $schregNo, $ibYear, $ibGrade, $semester, $viewCd) {

        $query  = "   DELETE FROM IBSUBCLASS_GRADE_DAT ";
        $query .= "   WHERE ";
        $query .= "     IBYEAR = '{$ibYear}' ";
        $query .= "     AND IBGRADE = '{$ibGrade}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";
        $query .= "     AND IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND VIEWCD = '{$viewCd}' ";

        return $query;
    }

    /* IBSUBCLASS_GRADE_DAT新規 */
    function getGradeInsertSql($model, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $grade) {

        list($classCd, $prgCourse, $curriculum, $subClassCd) = preg_split("/-/", $model->field["IBSUBCLASSCD"]);

        $data["IBYEAR"][TEXT]           = $ibYear;
        $data["IBGRADE"][TEXT]          = $ibGrade;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["IBCLASSCD"][TEXT]        = $classCd;
        $data["IBPRG_COURSE"][TEXT]     = $prgCourse;
        $data["IBCURRICULUM_CD"][TEXT]  = $curriculum;
        $data["IBSUBCLASSCD"][TEXT]     = $subClassCd;
        $data["SEMESTER"][TEXT]         = $semester;
        $data["IBEVAL_DIV1"][TEXT]      = $div1;
        $data["IBEVAL_DIV2"][TEXT]      = $div2;
        $data["IBEVAL_MARK"][TEXT]      = $mark;
        $data["VIEWCD"][TEXT]           = $viewCd;
        $data["GRADE"][NUMBER]          = $grade;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "IBSUBCLASS_GRADE_DAT");

        return $query;
    }

    /* IBSUBCLASS_CHKFIN_DAT削除 */
    function getChkFinDeleteSql($model, $schregNo, $ibYear, $ibGrade, $seq, $viewCd) {

        $query  = "   DELETE FROM IBSUBCLASS_CHKFIN_DAT ";
        $query .= "   WHERE ";
        $query .= "     IBYEAR = '{$ibYear}' ";
        $query .= "     AND IBGRADE = '{$ibGrade}' ";
        $query .= "     AND IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        $query .= "     AND IBSEQ = {$seq} ";
        $query .= "     AND VIEWCD = '{$viewCd}' ";

        return $query;
    }

    /* IBSUBCLASS_CHKFIN_DAT新規 */
    function getChkFinInsertSql($model, $ibYear, $ibGrade, $seq, $viewCd, $chkFin) {

        list($classCd, $prgCourse, $curriculum, $subClassCd) = preg_split("/-/", $model->field["IBSUBCLASSCD"]);

        $data["IBYEAR"][TEXT]           = $ibYear;
        $data["IBGRADE"][TEXT]          = $ibGrade;
        $data["IBCLASSCD"][TEXT]        = $classCd;
        $data["IBPRG_COURSE"][TEXT]     = $prgCourse;
        $data["IBCURRICULUM_CD"][TEXT]  = $curriculum;
        $data["IBSUBCLASSCD"][TEXT]     = $subClassCd;
        $data["IBSEQ"][NUMBER]          = $seq;
        $data["VIEWCD"][TEXT]           = $viewCd;
        $data["EXECUTED"][TEXT]         = $chkFin;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "IBSUBCLASS_CHKFIN_DAT");

        return $query;
    }
}
?>
