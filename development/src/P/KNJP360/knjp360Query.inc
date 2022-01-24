<?php

require_once('for_php7.php');
class knjp360Query extends Query {

    //クラス
    function GetReduc_rare_case_cd(){

        $query  = "SELECT ";
        $query .= "    NAMECD2 AS CD, ";
        $query .= "    NAME1 AS NAME ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1 = 'G211' ";
        $query .= "ORDER BY ";
        $query .= "    NAMECD1 ";

        return $query;
    }

    //クラス
    function GetClasscd($model){

        $query  = "SELECT ";
        $query .= "    GRADE || HR_CLASS AS CD, ";
        $query .= "    HR_NAME AS NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE,HR_CLASS ";

        return $query;
    }

    //府県
    function getCsvQueryFuken($model){

        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.HR_CLASS, ";
        $query .= "     t1.GRADE || t1.HR_CLASS AS CHANGEPAGE, ";
        $query .= "     t2.HR_NAME, ";
        $query .= "     t1.ATTENDNO, ";
        $query .= "     t9.PASSNO, ";
        $query .= "     t5.NAME, ";
        $query .= "     t7.GUARANTOR_NAME, ";
        $query .= "     t3.REDUC_RARE_CASE_CD, ";
        $query .= "     t3.REDUC_DEC_FLG_1, ";
        $query .= "     t3.REDUC_INCOME_1, ";
        $query .= "     t10.NAME1 AS REDUC_RANK_1_NAME, ";
        $query .= "     t3.REDUCTIONMONEY_1, ";
        $query .= "     t3.REDUC_INCOME_2, ";
        $query .= "     t11.NAME1 AS REDUC_RANK_2_NAME, ";
        $query .= "     t3.REDUCTIONMONEY_2, ";
        $query .= "     VALUE(t3.REDUCTIONMONEY_1,0) + VALUE(t3.REDUCTIONMONEY_2,0) AS REDUCTIONMONEY_TOTAL, ";
        $query .= "     t4.MONEY_DUE, ";
        $query .= "     t8.PREF, ";
        $query .= "     t3.REDUC_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t2 ON t2.GRADE || t2.HR_CLASS = t1.GRADE || t1.HR_CLASS ";
        $query .= "         AND t2.YEAR = t1.YEAR ";
        $query .= "         AND t2.SEMESTER = t1.SEMESTER ";
        $query .= "     LEFT JOIN REDUCTION_DAT t3 ON t3.SCHREGNO = t1.SCHREGNO ";
        $query .= "         AND t3.YEAR = t1.YEAR ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT t4 ON t4.YEAR = t1.YEAR ";
        $query .= "         AND t4.SCHREGNO = t1.SCHREGNO ";
        $query .= "         AND t4.EXPENSE_M_CD = '13' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t5 ON t5.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT t7 ON t7.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ZIP.NEW_ZIPCD, ";
        $query .= "             MAX(ZIP.PREF) AS PREF ";
        $query .= "         FROM ";
        $query .= "             ZIPCD_MST ZIP ";
        $query .= "         GROUP BY ZIP.NEW_ZIPCD ";
        $query .= "     ) t8 ON t7.GUARANTOR_ZIPCD = t8.NEW_ZIPCD ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT t9 ON t9.SCHREGNO = t1.SCHREGNO ";
        $query .= "          AND t9.DATA_DIV = '1' ";
        $query .= "          AND t9.DATA_DIV_SUB = '1' ";
        $query .= "     LEFT JOIN NAME_MST t10 ON t10.NAMECD1 = 'G213' ";
        $query .= "          AND t10.NAMECD2 = T3.REDUC_RANK_1 ";
        $query .= "     LEFT JOIN NAME_MST t11 ON t11.NAMECD1 = 'G213' ";
        $query .= "          AND t11.NAMECD2 = T3.REDUC_RANK_2 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND t1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND t1.GRADE || t1.HR_CLASS IN ('".implode($model->class_select["selectdata"],"','")."') ";
        if ($model->reduc != "99" || $model->reductionmoney != ""){
            $query .= "     AND t1.SCHREGNO IN (SELECT SCHREGNO ";
            $query .= "                         FROM REDUCTION_DAT ";
            $query .= "                         WHERE YEAR = '".CTRL_YEAR."' ";
            if ($model->reduc != "99"){
                $query .= "                             AND REDUC_RARE_CASE_CD = '".$model->reduc."' ";
            }
            if ($model->reductionmoney != ""){
                $query .= "                             AND VALUE(REDUCTIONMONEY_1,0) + VALUE(REDUCTIONMONEY_2,0) = ".$model->reductionmoney." ";
            }
            $query .= "                        ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.HR_CLASS, ";
        $query .= "     t1.ATTENDNO ";

        return $query;
    }

    //国
    function getCsvQueryKuni($model) {

        $query  = "";
        $query .= " WITH COUNTRY_PLAN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(CASE WHEN PLAN_CANCEL_FLG = '1' THEN NULL ";
        $query .= "                  ELSE PLAN_MONEY END) AS PLAN_MONEY, ";
        $query .= "         SUM(CASE WHEN PLAN_CANCEL_FLG = '1' THEN 0 ";
        $query .= "                  WHEN PLAN_MONTH IN('04','05','06') THEN 1 ";
        $query .= "                  ELSE 0 END) AS PLAN_MONTH_CNT1, ";
        $query .= "         SUM(CASE WHEN PLAN_CANCEL_FLG = '1' THEN 0 ";
        $query .= "                  WHEN PLAN_MONTH IN('04','05','06') THEN 0 ";
        $query .= "                  ELSE 1 END) AS PLAN_MONTH_CNT2 ";
        $query .= "     FROM ";
        $query .= "         REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR='".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.HR_CLASS, ";
        $query .= "     t1.GRADE || t1.HR_CLASS AS CHANGEPAGE, ";
        $query .= "     t2.HR_NAME, ";
        $query .= "     t1.ATTENDNO, ";
        $query .= "     t9.PASSNO, ";
        $query .= "     t5.NAME, ";
        $query .= "     t7.GUARANTOR_NAME, ";
        $query .= "     t3.REDUC_INCOME_1, ";
        $query .= "     t3.REDUC_INCOME_2, ";
        $query .= "     t4.PLAN_MONEY, ";
        $query .= "     t3.REDUCTION_ADD_MONEY_1 * VALUE(t4.PLAN_MONTH_CNT1,0) as ADD_PLAN_MONEY1, ";
        $query .= "     t3.REDUCTION_ADD_MONEY_2 * VALUE(t4.PLAN_MONTH_CNT2,0) as ADD_PLAN_MONEY2, ";
        $query .= "     (VALUE(t3.REDUCTION_ADD_MONEY_1,0) * VALUE(t4.PLAN_MONTH_CNT1,0)) + (VALUE(t3.REDUCTION_ADD_MONEY_2,0) * VALUE(t4.PLAN_MONTH_CNT2,0)) AS ADD_PLAN_MONEY_TOTAL, ";
        $query .= "     (VALUE(t3.REDUCTION_ADD_MONEY_1,0) * VALUE(t4.PLAN_MONTH_CNT1,0)) + (VALUE(t3.REDUCTION_ADD_MONEY_2,0) * VALUE(t4.PLAN_MONTH_CNT2,0)) + VALUE(t4.PLAN_MONEY,0) AS PLAN_MONEY_TOTAL, ";
        $query .= "     t8.PREF, ";
        $query .= "     t3.REDUC_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t2 ON t2.GRADE || t2.HR_CLASS = t1.GRADE || t1.HR_CLASS ";
        $query .= "         AND t2.YEAR = t1.YEAR ";
        $query .= "         AND t2.SEMESTER = t1.SEMESTER ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_DAT t3 ON t3.YEAR = t1.YEAR AND t3.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN COUNTRY_PLAN t4 ON t4.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t5 ON t5.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT t7 ON t7.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ZIP.NEW_ZIPCD, ";
        $query .= "             MAX(ZIP.PREF) AS PREF ";
        $query .= "         FROM ";
        $query .= "             ZIPCD_MST ZIP ";
        $query .= "         GROUP BY ZIP.NEW_ZIPCD ";
        $query .= "     ) t8 ON t7.GUARANTOR_ZIPCD = t8.NEW_ZIPCD ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT t9 ON t9.SCHREGNO = t1.SCHREGNO ";
        $query .= "          AND t9.DATA_DIV = '1' ";
        $query .= "          AND t9.DATA_DIV_SUB = '1' ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND t1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND t1.GRADE || t1.HR_CLASS IN ('".implode($model->class_select["selectdata"],"','")."') ";
        $query .= " ORDER BY ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.HR_CLASS, ";
        $query .= "     t1.ATTENDNO ";

        return $query;
    }
}
?>
