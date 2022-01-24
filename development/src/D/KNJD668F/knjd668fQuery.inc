<?php

require_once('for_php7.php');

class knjd668fQuery extends Query {
    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($semester, $model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($semester == "9") {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$semester."' ";
        }
        $query .= "     AND T1.RECORD_DIV = '1' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
            $query .= "         OR (T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校校種取得
    function getSchoolKind($grade)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$grade."' ";

        return $query;
    }

    //テスト取得
    function getTest($semester, $model)
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$semester."' ";
        $query .= " AND T1.SCORE_DIV IN ( '01', '08' ) ";
        $query .= " AND T1.CLASSCD = '00' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= " AND T1.CURRICULUM_CD = '00' ";
        $query .= " AND T1.SUBCLASSCD = '000000' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象科目
    function getSubclass($model, $seme)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REG ON T1.YEAR = REG.YEAR ";
        if ($model->field["SEMESTER"] !== '9') {
            $query .= "     AND REG.SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query .= "     AND REG.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= "           AND REG.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "           AND T1.SCHREGNO = REG.SCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.CLASSCD = T1.CLASSCD ";
        $query .= "                              AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "                              AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "                              AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR           = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER       = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.TESTKINDCD     = '".substr($model->field["TESTCD"], 0, 2)."' AND ";
        $query .= "     T1.TESTITEMCD     = '".substr($model->field["TESTCD"], 2, 2)."' AND ";
        $query .= "     T1.SCORE_DIV      = '".substr($model->field["TESTCD"], 4, 2)."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '99-$model->schoolkind-3-333333' AS VALUE, ";
        $query .= "     '333333:３教科合計' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '99-$model->schoolkind-5-555555' AS VALUE, ";
        $query .= "     '555555:５教科合計' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '99-$model->schoolkind-9-999999' AS VALUE, ";
        $query .= "     '999999:総合計' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
