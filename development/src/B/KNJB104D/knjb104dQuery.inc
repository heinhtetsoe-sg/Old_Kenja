<?php

require_once('for_php7.php');

class knjb104dQuery extends Query
{

    //学期
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "   AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    public function getTest($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR      = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER  = '".$model->field["SEMESTER"]."' ";
        $query .= "   AND T1.SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ
    public function getNameMst($model, $namecd1)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR    = '".CTRL_YEAR."' ";
        $query .= "   AND NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //座席表一覧
    public function getListQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T4.CHAIRCD || ':' || T4.CHAIRNAME AS CHAIRNAME, ";
        $query .= "     T5.SUBCLASSNAME AS SUBCLASSNAME, ";
        //年組名称を","区切りで集約
        $query .= "     LISTAGG(T7.HR_NAME, ',') WITHIN GROUP(ORDER BY T1.EXECUTEDATE, T7.GRADE, T7.HR_CLASS) AS HR_NAME, ";
        $query .= "     T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T4.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST T1 ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        $query .= "             ON T2.YEAR       = T1.YEAR ";
        $query .= "            AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "            AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "            AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "            AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "     INNER JOIN CHAIR_DAT T4 ";
        $query .= "             ON T4.YEAR     = T1.YEAR ";
        $query .= "            AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "            AND T4.CHAIRCD  = T1.CHAIRCD ";
        $query .= "     INNER JOIN SUBCLASS_MST T5 ";
        $query .= "             ON T5.CLASSCD       = T4.CLASSCD ";
        $query .= "            AND T5.SCHOOL_KIND   = T4.SCHOOL_KIND ";
        $query .= "            AND T5.CURRICULUM_CD = T4.CURRICULUM_CD ";
        $query .= "            AND T5.SUBCLASSCD    = T4.SUBCLASSCD ";
        $query .= "     INNER JOIN CHAIR_CLS_DAT AS T6 ";
        $query .= "             ON T6.YEAR     = T1.YEAR ";
        $query .= "            AND T6.SEMESTER = T1.SEMESTER ";
        $query .= "            AND T6.CHAIRCD  = T1.CHAIRCD ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T7 ";
        $query .= "             ON T7.YEAR     = T1.YEAR ";
        $query .= "            AND T7.SEMESTER = T1.SEMESTER ";
        $query .= "            AND T7.GRADE    = T6.TRGTGRADE ";
        $query .= "            AND T7.HR_CLASS = T6.TRGTCLASS ";
        $query .= "            AND T7.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     INNER JOIN CHAIR_TEST_FAC_DAT FAC_DAT ";
        $query .= "             ON FAC_DAT.YEAR     = T1.YEAR ";
        $query .= "            AND FAC_DAT.SEMESTER = T1.SEMESTER ";
        $query .= "            AND FAC_DAT.CHAIRCD  = T1.CHAIRCD ";
        $query .= "     INNER JOIN (SELECT DISTINCT EXECUTEDATE, PERIODCD, CHAIRCD ";
        $query .= "                   FROM CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT ";
        $query .= "                ) FAC_SCH ";
        $query .= "             ON FAC_SCH.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "            AND FAC_SCH.PERIODCD    = T1.PERIODCD ";
        $query .= "            AND FAC_SCH.CHAIRCD     = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->field["EXECUTEDATE"] != "") {
            $query .= "     AND T1.EXECUTEDATE = '".str_replace('/', '-', $model->field["EXECUTEDATE"])."'";
        }
        if ($model->field["PERIODCD"] != "ALL") {
            $query .= "     AND T1.PERIODCD = '".$model->field["PERIODCD"]."'";
        }
        $query .= " GROUP BY ";
        $query .= "     T5.CLASSCD, ";
        $query .= "     T5.SCHOOL_KIND, ";
        $query .= "     T5.CURRICULUM_CD, ";
        $query .= "     T5.SUBCLASSCD, ";
        $query .= "     T5.SUBCLASSNAME, ";
        $query .= "     T4.CHAIRCD, ";
        $query .= "     T4.CHAIRNAME ";
        $query .= " ORDER BY ";
        $query .= "     CHAIRCD ";
        return $query;
    }
}
