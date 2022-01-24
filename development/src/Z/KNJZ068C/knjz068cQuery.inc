<?php

require_once('for_php7.php');

require_once("PrimarySchoolProcess.php");

class knjz068cQuery extends Query {

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
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                            AND L1.NAMECD1 = 'A034' ";
        $query .= "                            AND L1.NAME1 = 'M' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["IBYEAR"]}' ";
        $query .= " AND T1.GRADE BETWEEN L1.NAME2 AND L1.NAME3 ";
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
        $query .= "     AND NAME1 = 'M' ";
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
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND T1.IBPRG_COURSE = '{$model->field["IBPRG_COURSE"]}' ";
        $query .= "     AND L1.IBGRADE = '".$model->field["IBGRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND T1.IBCLASSCD || T1.IBPRG_COURSE IN ";
            $query .= "                             (SELECT  ";
            $query .= "                                  ST.FIELD2 || ST.FIELD3 ";
            $query .= "                              FROM  ";
            $query .= "                                  STAFF_DETAIL_MST ST  ";
            $query .= "                              WHERE  ";
            $query .= "                                  T1.IBYEAR = ST.YEAR  ";
            $query .= "                                  AND ST.STAFFCD = '".STAFFCD."'  ";
            $query .= "                                  AND ST.STAFF_SEQ IN ('005', '006', '007')  ";
            $query .= "                                  AND ST.FIELD1 = '1050')  ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    /* タイトル(横) */
    function getIbUnit($model, $listrow="") {
        $query  = " SELECT ";
        if ($listrow["IBYEAR"]) {
            $query .= "     T1.IBYEAR, ";
            $query .= "     T1.IBGRADE, ";
            $query .= "     T1.IBCLASSCD, ";
            $query .= "     T1.IBPRG_COURSE, ";
            $query .= "     T1.IBCURRICULUM_CD, ";
            $query .= "     T1.IBSUBCLASSCD, ";
        }
        $query .= "     T1.IBSEQ, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L2.SEMESTERNAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS KEY_SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     L4.SHOWORDER, ";
        $query .= "     L3.VIEWCD, ";
        $query .= "     L4.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SEMESTER_MST L2 ON T1.IBYEAR = L2.YEAR ";
        $query .= "          AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_YDAT L3 ON T1.IBYEAR = L3.YEAR ";
        $query .= "          AND T1.IBGRADE = L3.GRADE ";
        $query .= "          AND T1.CLASSCD = L3.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = L3.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = L3.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = L3.SUBCLASSCD ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST L4 ON T1.IBGRADE = L4.GRADE ";
        $query .= "          AND T1.CLASSCD = L3.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = L4.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = L4.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = L4.SUBCLASSCD ";
        $query .= "          AND L3.VIEWCD = L4.VIEWCD ";
        if ($listrow["IBYEAR"]) {
            $query .= " WHERE ";
            $query .= "     T1.IBYEAR = '{$listrow["IBYEAR"]}' AND ";
            $query .= "     T1.IBGRADE = '{$listrow["IBGRADE"]}' AND ";
            $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$listrow["IBSUBCLASSCD_SET"]}' AND ";
            $query .= "     T1.YEAR IS NOT NULL ";
        } else {
            $query .= " WHERE ";
            $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' AND ";
            $query .= "     T1.IBGRADE = '{$model->field["IBGRADE"]}' AND ";
            $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' AND ";
            $query .= "     T1.YEAR IS NOT NULL ";
        }
        $query .= " ORDER BY ";
        if ($listrow["IBYEAR"]) {
            $query .= " T1.IBYEAR, ";
            $query .= " T1.IBGRADE, ";
            $query .= " T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD, ";
        }
        $query .= "     T1.IBSEQ, ";
        $query .= "     VALUE(L4.SHOWORDER, 99), ";
        $query .= "     L3.VIEWCD ";

        return $query;
    }

    /* タイトル(縦) */
    function getIbViewName($model, $listrow="") {
        $query  = " SELECT ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     L1.IBSUBCLASSNAME_ENG, ";
        $query .= "     T1.IBEVAL_DIV1, ";
        $query .= "     T1.IBEVAL_DIV2, ";
        $query .= "     T1.IBEVAL_MARK, ";
        $query .= "     T1.IBEVAL_NAME, ";
        $query .= "     T1.IBEVAL_ABBV, ";
        $query .= "     T1.IBSORT, ";
        $query .= "     T1.IBPERFECT ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST T1 ";
        $query .= "     LEFT JOIN V_IBSUBCLASS_MST L1 ON T1.IBYEAR = L1.IBYEAR ";
        $query .= "          AND T1.IBCLASSCD       = L1.IBCLASSCD ";
        $query .= "          AND T1.IBPRG_COURSE    = L1.IBPRG_COURSE ";
        $query .= "          AND T1.IBCURRICULUM_CD = L1.IBCURRICULUM_CD ";
        $query .= "          AND T1.IBSUBCLASSCD    = L1.IBSUBCLASSCD ";
        if ($listrow["IBYEAR"]) {
            $query .= " WHERE ";
            $query .= "     T1.IBYEAR = '{$listrow["IBYEAR"]}' ";
            $query .= "     AND T1.IBGRADE = '{$listrow["IBGRADE"]}' ";
            $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$listrow["IBSUBCLASSCD_SET"]}' ";
        } else {
            $query .= " WHERE ";
            $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
            $query .= "     AND T1.IBGRADE = '{$model->field["IBGRADE"]}' ";
            $query .= "     AND T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        if ($listrow["IBYEAR"]) {
            $query .= "     T1.IBYEAR, ";
            $query .= "     T1.IBGRADE, ";
        }
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

    /* IBSUBCLASS_UNITPLAN_DAT存在 */
    function getUnitPlanCnt($model, $div1, $div2, $mark, $seq, $viewCd, $setibgrade="", $setibsubclasscd="") {

        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNITPLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '{$model->field["IBYEAR"]}' ";
        if ($setibgrade) {
            $query .= "     AND IBGRADE = '{$setibgrade}' ";
        } else {
            $query .= "     AND IBGRADE = '{$model->field["IBGRADE"]}' ";
        }
        if ($setibsubclasscd) {
            $query .= "     AND IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD = '{$setibsubclasscd}' ";
        } else {
            $query .= "     AND IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";
        }
        $query .= "     AND IBSEQ = {$seq} ";
        $query .= "     AND IBEVAL_DIV1 = '{$div1}' ";
        $query .= "     AND IBEVAL_DIV2 = '{$div2}' ";
        $query .= "     AND IBEVAL_MARK = '{$mark}' ";
        $query .= "     AND VIEWCD = '{$viewCd}' ";
        return $query;
    }

    /* IBSUBCLASS_UNITPLAN_DAT削除 */
    function getDeleteSql($model) {

        $query  = "   DELETE FROM IBSUBCLASS_UNITPLAN_DAT ";
        $query .= "   WHERE ";
        $query .= "     IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= "     AND IBGRADE = '{$model->field["IBGRADE"]}' ";
        $query .= "     AND IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD = '{$model->field["IBSUBCLASSCD"]}' ";

        return $query;
    }

    /* IBSUBCLASS_UNITPLAN_DAT新規 */
    function getInsertSql($model, $div1, $div2, $mark, $seq, $viewCd) {

        list($classCd, $prgCourse, $curriculum, $subClassCd) = preg_split("/-/", $model->field["IBSUBCLASSCD"]);

        $data["IBYEAR"][TEXT]           = $model->field["IBYEAR"];
        $data["IBGRADE"][TEXT]          = $model->field["IBGRADE"];
        $data["IBCLASSCD"][TEXT]        = $classCd;
        $data["IBPRG_COURSE"][TEXT]     = $prgCourse;
        $data["IBCURRICULUM_CD"][TEXT]  = $curriculum;
        $data["IBSUBCLASSCD"][TEXT]     = $subClassCd;
        $data["IBSEQ"][NUMBER]          = $seq;
        $data["IBEVAL_DIV1"][TEXT]      = $div1;
        $data["IBEVAL_DIV2"][TEXT]      = $div2;
        $data["IBEVAL_MARK"][TEXT]      = $mark;
        $data["VIEWCD"][TEXT]           = $viewCd;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "IBSUBCLASS_UNITPLAN_DAT");

        return $query;
    }

    //CSV関連
    function getIbUnitDatList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD AS IBSUBCLASSCD_SET ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT T1  ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->field["IBYEAR"]}' ";
        $query .= " AND T1.YEAR IS NOT NULL ";
        $query .= " AND T1.IBPRG_COURSE = 'M' ";
        $query .= " ORDER BY ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBSUBCLASSCD ";

        return $query;
    }
        
}
?>
