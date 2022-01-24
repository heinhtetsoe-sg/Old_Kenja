<?php

require_once('for_php7.php');

class knjp412Query extends Query {

    //学年取得
    function getGrade() {

        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //中高判定用
    function GetJorH() {
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
    function getGradeHrName($model, $val) {
        if ($model->grade == "") {
            $query  = " SELECT ";
            $query .= "     GRADE_NAME1 AS NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND GRADE = '".$val."' ";
            $query .= " ORDER BY ";
            $query .= "     GRADE_CD ";
        } else {
            $query  = "SELECT ";
            $query .= "    HR_NAME AS NAME ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "    AND GRADE = '".$grade."' ";
            $query .= " ORDER BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS ";
        }

        return $query;
    }

    //CSV
    function getCsvData($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     REGD_H.HR_NAME, ";
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
        $query .= "              THEN ";
        $query .= "                     CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "                          THEN VALUE(COUNTRY_D.REDUCTIONMONEY_1, 0) ";
        $query .= "                          ELSE VALUE(COUNTRY_D.REDUCTIONMONEY_1, 0) * 3 ";
        $query .= "                     END ";
        $query .= "                     + ";
        $query .= "                     CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "                          THEN VALUE(COUNTRY_D.REDUCTIONMONEY_2, 0) ";
        $query .= "                          ELSE VALUE(COUNTRY_D.REDUCTIONMONEY_2, 0) * 9 ";
        $query .= "                     END ";
        $query .= "                     + ";
        $query .= "                     CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "                          THEN VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_1, 0) ";
        $query .= "                          ELSE VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_1, 0) * 3 ";
        $query .= "                     END ";
        $query .= "                     + ";
        $query .= "                     CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "                          THEN VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_2, 0) ";
        $query .= "                          ELSE VALUE(COUNTRY_D.REDUCTION_ADD_MONEY_2, 0) * 9 ";
        $query .= "                     END ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "         + ";
        $query .= "         VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) - VALUE(ADJUSTMENT.TOTAL_ADJUSTMENT_MONEY, 0) ";
        $query .= "         ) - SUM(VALUE(MONEY_PAID.PAID_MONEY, 0)) AS HIKIOTOSI, ";
        $query .= "     SUM(VALUE(MONEY_PAID.PAID_MONEY, 0)) AS PAID_MONEY, ";
        $query .= "     SUM(VALUE(SASIHIKI.PAID_MONEY, 0)) AS SASIHIKI, ";
        $query .= "     SUM(VALUE(MONEY_PAID.REPAY_MONEY, 0)) AS REPAY_MONEY, ";
        $query .= "     REGISTBANK.ACCOUNTNAME, ";
        $query .= "     BASE.GRD_DATE, ";
        $query .= "     GRANT01.GRANT_MONEY AS SIJOGEN, ";
        $query .= "     GRANT02.GRANT_MONEY AS SYOUGAKU, ";
        $query .= "     GRANT03.GRANT_MONEY AS RYUUGAKU, ";
        $query .= "     GRD_NAME.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON T1.YEAR = REGD_H.YEAR ";
        $query .= "          AND T1.SEMESTER = REGD_H.SEMESTER ";
        $query .= "          AND T1.GRADE = REGD_H.GRADE ";
        $query .= "          AND T1.HR_CLASS = REGD_H.HR_CLASS ";
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
        $query .= "     LEFT JOIN SCHREG_GRANT_DAT GRANT01 ON T1.YEAR = GRANT01.YEAR ";
        $query .= "          AND T1.SCHREGNO = GRANT01.SCHREGNO ";
        $query .= "          AND GRANT01.GRANTCD = '01' ";
        $query .= "     LEFT JOIN SCHREG_GRANT_DAT GRANT02 ON T1.YEAR = GRANT02.YEAR ";
        $query .= "          AND T1.SCHREGNO = GRANT02.SCHREGNO ";
        $query .= "          AND GRANT02.GRANTCD = '02' ";
        $query .= "     LEFT JOIN SCHREG_GRANT_DAT GRANT03 ON T1.YEAR = GRANT03.YEAR ";
        $query .= "          AND T1.SCHREGNO = GRANT03.SCHREGNO ";
        $query .= "          AND GRANT03.GRANTCD = '03' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->grade != "") {
            $query .= "     AND T1.GRADE = '{$model->grade}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     REGD_H.HR_NAME, ";
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
        $query .= "     GRANT01.GRANT_MONEY, ";
        $query .= "     GRANT02.GRANT_MONEY, ";
        $query .= "     GRANT03.GRANT_MONEY, ";
        $query .= "     GRD_NAME.NAME1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     GRADE, ";
        if ($model->grade != "") {
            $query .= "     HR_CLASS, ";
            $query .= "     HR_NAME, ";
        }
        $query .= "     MONEY_DUE, ";
        $query .= "     SUM(VALUE(MONEY_DUE, 0)) AS SUM_MONEY_DUE, ";
        $query .= "     COUNT(*) AS CNT, ";
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
        $query .= "     SUM(VALUE(SIJOGEN, 0)) AS SIJOGEN, ";
        $query .= "     SUM(VALUE(SYOUGAKU, 0)) AS SYOUGAKU, ";
        $query .= "     SUM(VALUE(RYUUGAKU, 0)) AS RYUUGAKU, ";
        $query .= "     SUM(VALUE(REPAY_MONEY, 0)) AS REPAY_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " GROUP BY ";
        $query .= "     GRADE, ";
        if ($model->grade != "") {
            $query .= "     HR_CLASS, ";
            $query .= "     HR_NAME, ";
        }
        $query .= "     MONEY_DUE ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        if ($model->grade != "") {
            $query .= "     HR_CLASS, ";
        }
        $query .= "     MONEY_DUE DESC ";

        return $query;
    }
}
?>
