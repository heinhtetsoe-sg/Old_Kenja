<?php

require_once('for_php7.php');

class knjd187tQuery extends Query
{
    //学期
    public function getSemester()
    {
        $query  = " SELECT";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //考査種別
    public function getTestcd($model)
    {
        $query  = " SELECT";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND (TESTKINDCD = '01' AND SCORE_DIV = '01' "; // 中間素点
        $query .= "       OR TESTKINDCD = '99' AND SCORE_DIV = '08' "; // 学期評価
        $query .= "      ) ";
        $query .= " ORDER BY ";
        $query .= "     TESTKINDCD, TESTITEMCD, SCORE_DIV ";

        return $query;
    }

    //年組取得
    public function getSelectHrclass($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '" .CTRL_YEAR. "' ";
        $query .= " AND SEMESTER = '{$seme}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
