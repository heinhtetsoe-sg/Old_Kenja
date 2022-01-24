<?php

require_once('for_php7.php');

class knje374Query extends Query {

    //学校種別取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   NAMECD2 ";

        return $query;
    }

    //年度取得（学校マスタ）
    function getSchoolYear()
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //出身学校一覧取得（在校生）
    function getFinschoolUngrd($model)
    {
        $query  = " WITH FINSCHOOL AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.FINSCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     NAME_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T3.NAMECD1 = 'A023' AND ";
        $query .= "     T3.NAME1 = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     T1.GRADE BETWEEN T3.NAME2 AND T3.NAME3 AND ";
        $query .= "     T2.GRD_DATE IS NULL AND ";
        $query .= "     T2.FINSCHOOLCD IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.FINSCHOOLCD AS VALUE, ";
        $query .= "     T1.FINSCHOOLCD || '　' || T2.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL T1, ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOLCD = T2.FINSCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校一覧取得（卒業生）
    function getFinschoolGrd($model)
    {
        $query  = " WITH FINSCHOOL AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.FINSCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     NAME_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR >= '".$model->field["GRD_YEAR"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T3.NAMECD1 = 'A023' AND ";
        $query .= "     T3.NAME1 = 'H' AND ";
        $query .= "     T1.GRADE BETWEEN T3.NAME2 AND T3.NAME3 AND ";
        $query .= "     T2.GRD_DATE IS NOT NULL AND ";
        $query .= "     T2.FINSCHOOLCD IS NOT NULL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     FINSCHOOLCD ";
        $query .= " FROM ";
        $query .= "     GRD_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     GRD_DATE IS NOT NULL AND ";
        $query .= "     GRD_DATE >= '".$model->field["GRD_YEAR"]."' || '-04-01' AND ";
        $query .= "     FINSCHOOLCD IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.FINSCHOOLCD AS VALUE, ";
        $query .= "     T1.FINSCHOOLCD || '　' || T2.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL T1, ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOLCD = T2.FINSCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校区分
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='Z010' AND ";
        $query .= "     NAMECD2='00' ";

        return $query;
    }

    //テストコンボ
    function getTestkind($model, $isKindai, $semester) {
        $query  = " WITH T_TESTKIND(LABEL, VALUE, SORT) AS ( ";
        if ($isKindai) {
            if ($semester != "9") {
                if ($semester != "3") {
                    $query .= " VALUES('0101　中間成績平均', '0101', '01') ";
                    $query .= " UNION ";
                    $query .= " VALUES('0201　期末成績平均', '0201', '02') ";
                    $query .= " UNION ";
                    $query .= " VALUES('0000　評価成績平均', '0000', '99') ";
                } else {
                    $query .= " VALUES('0201　期末成績平均', '0201', '02') ";
                    $query .= " UNION ";
                    $query .= " VALUES('0000　評価成績平均', '0000', '99') ";
                }
            } else {
                $query .= " VALUES('0000　評価成績平均', '0000', '99') ";
            }
        } else {
            if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= " SELECT ";
                $query .= "    TESTKINDCD || TESTITEMCD || SCORE_DIV || '　' || TESTITEMNAME AS LABEL, ";
                $query .= "    TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
                $query .= "    TESTKINDCD || TESTITEMCD || SCORE_DIV AS SORT ";
                $query .= " FROM ";
                $query .= "    TESTITEM_MST_COUNTFLG_NEW_SDIV ";
            } else {
                $query .= " SELECT ";
                $query .= "    TESTKINDCD || TESTITEMCD || '　' || TESTITEMNAME AS LABEL, ";
                $query .= "    TESTKINDCD || TESTITEMCD AS VALUE, ";
                $query .= "    TESTKINDCD || TESTITEMCD AS SORT ";
                $query .= " FROM ";
                $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
            }
            $query .= " WHERE ";
            if ($model->field["STUDENT"] == '2') {
                $query .= "     YEAR = '".$model->field["GRD_YEAR"]."' ";
            } else {
                $query .= "     YEAR = '".CTRL_YEAR."' ";
            }
            $query .= "     AND SEMESTER = '".$semester."' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " from ";
        $query .= "     T_TESTKIND ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }
}
?>
