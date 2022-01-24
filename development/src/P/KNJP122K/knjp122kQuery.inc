<?php

require_once('for_php7.php');
class knjp122kQuery extends Query
{

    //クラス取得
    public function getClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || HR_CLASS AS CD, ";
        $query .= "     HR_NAME AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE    = '".$model->grade."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE || HR_CLASS ";

        return $query;
    }

    //学年取得
    public function getGrade()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS CD, ";
        $query .= "     GRADE AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //学年またはクラス名
    public function getGradeHrName($model, $val)
    {
        if ($model->output == "1") {
            $query  = " SELECT ";
            $query .= "     GRADE || '学年' AS NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND GRADE    = '".$val."' ";
        } else {
            $query  = " SELECT ";
            $query .= "     HR_NAME AS NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND GRADE || HR_CLASS = '".$val."' ";
        }

        return $query;
    }

    //CSV
    public function getCsvData($model, $val)
    {
        $query  = "";
        if ($model->output == "1" && $model->kojinBetsu != "1") {
            $query .= " WITH MAIN_T AS ( ";
        }
        $query .= " SELECT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD_H.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     SUM(VALUE(DUE_M.MONEY_DUE, 0)) AS MONEY_DUE, ";
        $query .= "     SUM(VALUE(PAID_M.PAID_MONEY, 0)) AS NYUKIN, ";
        $query .= "     SUM(VALUE(PAID_M.REPAY_MONEY, 0)) AS SIEN_KANPU, ";
        $query .= "     LISTAGG(CAST(PAID_M.REMARK AS VARCHAR(1000)), '、') WITHIN GROUP(ORDER BY int(PAID_M.EXPENSE_M_CD)) AS REMARK, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD.YEAR     = REGD_H.YEAR ";
        $query .= "                                      AND REGD.SEMESTER = REGD_H.SEMESTER ";
        $query .= "                                      AND REGD.GRADE    = REGD_H.GRADE ";
        $query .= "                                      AND REGD.HR_CLASS = REGD_H.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT DUE_M ON REGD.YEAR        = DUE_M.YEAR ";
        $query .= "                                    AND REGD.SCHREGNO    = DUE_M.SCHREGNO ";
        $query .= "                                    AND DUE_M.EXPENSE_M_CD < '20' ";
        $query .= "     LEFT JOIN MONEY_PAID_M_DAT PAID_M ON PAID_M.YEAR         = REGD.YEAR ";
        $query .= "                                      AND PAID_M.SCHREGNO     = REGD.SCHREGNO ";
        $query .= "                                      AND PAID_M.EXPENSE_M_CD = DUE_M.EXPENSE_M_CD ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->output == "1") {
            $query .= "     AND REGD.GRADE = '".$val."' ";
        } else {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$val."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD_H.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";
        if ($model->output == "1" && $model->kojinBetsu != "1") {
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     COUNT(*) AS CNT, ";
            $query .= "     SUM(VALUE(MONEY_DUE, 0)) AS MONEY_DUE, ";
            $query .= "     SUM(VALUE(NYUKIN, 0)) AS NYUKIN, ";
            $query .= "     SUM(VALUE(SIEN_KANPU, 0)) AS SIEN_KANPU ";
            $query .= " FROM ";
            $query .= "     MAIN_T ";
            $query .= " GROUP BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS ";
            $query .= " ORDER BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS ";
        }

        return $query;
    }
}
