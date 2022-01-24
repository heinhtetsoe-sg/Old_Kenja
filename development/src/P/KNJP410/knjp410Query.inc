<?php

require_once('for_php7.php');

class knjp410Query extends Query
{

    //クラス取得
    public function getClass($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE || HR_CLASS AS CD, ";
        $query .= "    HR_NAME AS NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND GRADE = '".$model->grade."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE || HR_CLASS ";

        return $query;
    }

    //学年取得
    public function getGrade()
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS CD, ";
        $query .= "    GRADE AS NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE ";

        return $query;
    }

    //中高判定用
    public function getJorH()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLNAME1 LIKE '%近畿%中学%' ";

        return $query;
    }

    //学年またはクラス名
    public function getGradeHrName($model, $val)
    {
        if ($model->output == "1") {
            $query  = "SELECT ";
            $query .= "    GRADE || '学年' AS NAME ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "    AND GRADE = '".$val."' ";
        } else {
            $query  = "SELECT ";
            $query .= "    HR_NAME AS NAME ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "    AND GRADE || HR_CLASS = '".$val."' ";
        }

        return $query;
    }

    //CSV
    public function getCsvData($model, $val)
    {
        if ($model->output == "1") {
            $query  = " WITH MAIN_T AS ( ";
        }
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     AUTHORIZE.PASSNO, ";
        $query .= "     SUM(VALUE(DUE.MONEY_DUE, 0)) AS MONEY_DUE, ";
        $query .= "     CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "          THEN VALUE(COUNTRY_D.REDUCTIONMONEY_1, 0) ";
        $query .= "          ELSE VALUE(COUNTRY_D.REDUCTIONMONEY_1, 0) * 3 ";
        $query .= "     END AS REDUCTIONMONEY_C1, ";
        $query .= "     CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "          THEN VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_1, 0) ";
        $query .= "          ELSE VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_1, 0) * 3 ";
        $query .= "     END AS REDUCTION_ADD_MONEY_1, ";
        $query .= "     CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "          THEN VALUE(COUNTRY_D.REDUCTIONMONEY_2, 0) ";
        $query .= "          ELSE VALUE(COUNTRY_D.REDUCTIONMONEY_2, 0) * 9 ";
        $query .= "     END AS REDUCTIONMONEY_C2, ";
        $query .= "     CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "          THEN VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_2, 0) ";
        $query .= "          ELSE VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_2, 0) * 9 ";
        $query .= "     END AS REDUCTION_ADD_MONEY_2, ";
        $query .= "     VALUE(REDUCTION_D.REDUCTIONMONEY_1, 0) + VALUE(REDUCTION_D.REDUCTIONMONEY_2, 0) AS REDUCTIONMONEY_B, ";
        $query .= "     VALUE(ADJUSTMENT.TOTAL_ADJUSTMENT_MONEY, 0) AS TOTAL_ADJUSTMENT_MONEY, ";
        $query .= "     VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) AS TOTAL_BURDEN_CHARGE, ";
        $query .= "     SUM(VALUE(DUE.MONEY_DUE, 0)) -  ";
        $query .= "        (CASE WHEN COUNTRY_D.OFFSET_FLG = '1' ";
        $query .= "              THEN VALUE(COUNTRY_D.REDUCTIONMONEY_1, 0) * 3 + VALUE(COUNTRY_D.REDUCTIONMONEY_2, 0) * 9 + ";
        $query .= "                    VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_1, 0) * 3 + VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_2, 0) * 9 ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "         + VALUE(REDUCTION_D.REDUCTIONMONEY_1, 0) + VALUE(REDUCTION_D.REDUCTIONMONEY_2, 0) + ";
        $query .= "         VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) - VALUE(ADJUSTMENT.TOTAL_ADJUSTMENT_MONEY, 0) ";
        $query .= "         ) - SUM(VALUE(MONEY_PAID.PAID_MONEY, 0)) AS HIKIOTOSI, ";
        $query .= "     SUM(VALUE(MONEY_PAID.PAID_MONEY, 0)) AS PAID_MONEY, ";
        $query .= "     SUM(VALUE(SASIHIKI.PAID_MONEY, 0)) AS SASIHIKI, ";
        $query .= "     SUM(VALUE(MONEY_PAID.REPAY_MONEY, 0)) AS REPAY_MONEY, ";
        $query .= "     REGISTBANK.ACCOUNTNAME, ";
        $query .= "     BASE.GRD_DATE, ";
        $query .= "     GRD_NAME.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST GRD_NAME ON GRD_NAME.NAMECD1 = 'A003' ";
        $query .= "          AND BASE.GRD_DIV = GRD_NAME.NAMECD2 ";
        $query .= "          AND GRD_NAME.NAMECD2 IN ('2', '3') ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT AUTHORIZE ON T1.SCHREGNO = AUTHORIZE.SCHREGNO ";
        $query .= "          AND AUTHORIZE.DATA_DIV = '1' ";
        $query .= "          AND AUTHORIZE.DATA_DIV_SUB = '1' ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT DUE ON T1.YEAR = DUE.YEAR ";
        $query .= "          AND T1.SCHREGNO = DUE.SCHREGNO ";
        $query .= "          AND DUE.EXPENSE_M_CD < '20' ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_DAT COUNTRY_D ON T1.YEAR = COUNTRY_D.YEAR ";
        $query .= "          AND T1.SCHREGNO = COUNTRY_D.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_DAT REDUCTION_D ON T1.YEAR = REDUCTION_D.YEAR ";
        $query .= "          AND T1.SCHREGNO = REDUCTION_D.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_ADJUSTMENT_DAT ADJUSTMENT ON T1.YEAR = ADJUSTMENT.YEAR ";
        $query .= "          AND T1.SCHREGNO = ADJUSTMENT.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT BURDEN ON T1.YEAR = BURDEN.YEAR ";
        $query .= "          AND T1.SCHREGNO = BURDEN.SCHREGNO ";
        $query .= "     LEFT JOIN MONEY_PAID_M_DAT SASIHIKI ON T1.YEAR = SASIHIKI.YEAR ";
        $query .= "          AND T1.SCHREGNO = SASIHIKI.SCHREGNO ";
        $query .= "          AND DUE.EXPENSE_M_CD = SASIHIKI.EXPENSE_M_CD ";
        $query .= "     LEFT JOIN MONEY_PAID_M_DAT MONEY_PAID ON T1.YEAR = MONEY_PAID.YEAR ";
        $query .= "          AND T1.SCHREGNO = MONEY_PAID.SCHREGNO ";
        $query .= "          AND DUE.EXPENSE_M_CD = MONEY_PAID.EXPENSE_M_CD ";
        $query .= "          AND MONEY_PAID.PAID_MONEY_DIV <> '01' ";
        $query .= "     LEFT JOIN REGISTBANK_DAT REGISTBANK ON T1.SCHREGNO = REGISTBANK.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST RARE ON RARE.NAMECD1 = 'G216' ";
        $query .= "          AND RARE.NAMECD2 = COUNTRY_D.REDUC_RARE_CASE_CD_1 ";
        $query .= "     LEFT JOIN NAME_MST RARE2 ON RARE2.NAMECD1 = 'G216' ";
        $query .= "          AND RARE2.NAMECD2 = COUNTRY_D.REDUC_RARE_CASE_CD_2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->output == "1") {
            $query .= "     AND T1.GRADE = '".$val."' ";
        } else {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$val."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     AUTHORIZE.PASSNO, ";
        $query .= "     RARE.NAMESPARE1, ";
        $query .= "     COUNTRY_D.REDUCTIONMONEY_1, ";
        $query .= "     RARE2.NAMESPARE1, ";
        $query .= "     COUNTRY_D.REDUCTIONMONEY_2, ";
        $query .= "     COUNTRY_D.REDUCTION_ADD_MONEY_1, ";
        $query .= "     COUNTRY_D.REDUCTION_ADD_MONEY_2, ";
        $query .= "     COUNTRY_D.OFFSET_FLG, ";
        $query .= "     REDUCTION_D.REDUCTIONMONEY_1, ";
        $query .= "     REDUCTION_D.REDUCTIONMONEY_2, ";
        $query .= "     ADJUSTMENT.TOTAL_ADJUSTMENT_MONEY, ";
        $query .= "     BURDEN.TOTAL_BURDEN_CHARGE, ";
        $query .= "     REGISTBANK.ACCOUNTNAME, ";
        $query .= "     BASE.GRD_DATE, ";
        $query .= "     GRD_NAME.NAME1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        if ($model->output == "1") {
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     COUNT(*) AS CNT, ";
            $query .= "     SUM(VALUE(MONEY_DUE, 0)) AS MONEY_DUE, ";
            $query .= "     SUM(VALUE(REDUCTIONMONEY_C1, 0)) AS REDUCTIONMONEY_C1, ";
            $query .= "     SUM(VALUE(REDUCTION_ADD_MONEY_1, 0)) AS REDUCTION_ADD_MONEY_1, ";
            $query .= "     SUM(VALUE(REDUCTIONMONEY_C2, 0)) AS REDUCTIONMONEY_C2, ";
            $query .= "     SUM(VALUE(REDUCTION_ADD_MONEY_2, 0)) AS REDUCTION_ADD_MONEY_2, ";
            $query .= "     SUM(VALUE(REDUCTIONMONEY_B, 0)) AS REDUCTIONMONEY_B, ";
            $query .= "     SUM(VALUE(TOTAL_ADJUSTMENT_MONEY, 0)) AS TOTAL_ADJUSTMENT_MONEY, ";
            $query .= "     SUM(VALUE(TOTAL_BURDEN_CHARGE, 0)) AS TOTAL_BURDEN_CHARGE, ";
            $query .= "     SUM(VALUE(HIKIOTOSI, 0)) AS HIKIOTOSI, ";
            $query .= "     SUM(VALUE(PAID_MONEY, 0)) AS PAID_MONEY, ";
            $query .= "     SUM(VALUE(SASIHIKI, 0)) AS SASIHIKI, ";
            $query .= "     SUM(VALUE(REPAY_MONEY, 0)) AS REPAY_MONEY ";
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
