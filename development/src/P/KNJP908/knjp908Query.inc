<?php

require_once('for_php7.php');

class knjp908Query extends Query {

    //校種取得
    function getSchoolKind($grade) {
        $db = Query::dbCheckOut();

        $query = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '".$grade."' ";

        $schoolKind = $db->getOne($query);
        Query::dbCheckIn($db);

        return $schoolKind;
    }

    function getStudentName($schregno) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     NAME_SHOW, ";
        $query .= "     NAME_KANA ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        return $query;
    }

    //収入
    function getIncomeData($model) {
        $setYear = $model->month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $month = $setYear."-".$model->month."-01";
        $lastDate = date('Y-m-t', strtotime(date($month)));

        $query  = " SELECT ";
        $query .= "     INCS.INCOME_L_CD AS LCD, ";
        $query .= "     LEVYL.LEVY_L_NAME, ";
        $query .= "     INCS.INCOME_M_CD, ";
        $query .= "     LEVYM.LEVY_M_NAME AS INCOME_M_NAME, ";
        $query .= "     SUM(VALUE(INCS.INCOME_MONEY, 0)) AS INCOME_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT INCD ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCS ON INCD.SCHOOLCD = INCS.SCHOOLCD ";
        $query .= "           AND INCD.SCHOOL_KIND= INCS.SCHOOL_KIND ";
        $query .= "           AND INCD.YEAR = INCS.YEAR ";
        $query .= "           AND INCD.INCOME_L_CD = INCS.INCOME_L_CD ";
        $query .= "           AND INCD.INCOME_M_CD = INCS.INCOME_M_CD ";
        $query .= "           AND INCD.REQUEST_NO = INCS.REQUEST_NO ";
        $query .= "           AND INCS.SCHREGNO = '{$model->schregno}' ";
        $query .= "     LEFT JOIN LEVY_M_MST LEVYM ON INCD.SCHOOLCD = LEVYM.SCHOOLCD ";
        $query .= "          AND INCD.SCHOOL_KIND= LEVYM.SCHOOL_KIND ";
        $query .= "          AND INCD.YEAR = LEVYM.YEAR ";
        $query .= "          AND INCD.INCOME_L_CD = LEVYM.LEVY_L_CD ";
        $query .= "          AND INCD.INCOME_M_CD = LEVYM.LEVY_M_CD ";
        $query .= "     LEFT JOIN LEVY_L_MST LEVYL ON INCD.SCHOOLCD = LEVYL.SCHOOLCD ";
        $query .= "          AND INCD.SCHOOL_KIND= LEVYL.SCHOOL_KIND ";
        $query .= "          AND INCD.YEAR = LEVYL.YEAR ";
        $query .= "          AND INCD.INCOME_L_CD = LEVYL.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     INCD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(INCD.INCOME_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(INCD.INCOME_CANCEL, '0') = '0' ";
        $query .= "     AND INCD.SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND INCD.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND INCD.INCOME_DATE <= '{$lastDate}' ";
        $query .= "     AND INCD.INCOME_DATE >= '".CTRL_YEAR."-04-01' ";
        $query .= " GROUP BY ";
        $query .= "     INCS.INCOME_L_CD, ";
        $query .= "     LEVYL.LEVY_L_NAME, ";
        $query .= "     INCS.INCOME_M_CD, ";
        $query .= "     LEVYM.LEVY_M_NAME ";
        $query .= " ORDER BY ";
        $query .= "     INCS.INCOME_L_CD, ";
        $query .= "     INCS.INCOME_M_CD ";

        return $query;
    }

    //支出
    function getOutGoData($model) {
        $setYear = $model->month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $month = $setYear."-".$model->month."-01";
        $lastDate = date('Y-m-t', strtotime(date($month)));

        $query  = " SELECT ";
        $query .= "     OUTS.OUTGO_L_CD AS LCD, ";
        $query .= "     LEVYL.LEVY_L_NAME, ";
        $query .= "     OUTS.OUTGO_M_CD AS MCD, ";
        $query .= "     OUTS.OUTGO_S_CD AS SCD, ";
        $query .= "     LEVYS.LEVY_S_NAME AS OUTGO_S_NAME, ";
        $query .= "     CASE WHEN LEVYS.BENEFIT = '1' THEN 'レ' ELSE '' END AS BENEFIT, ";
        $query .= "     SUM(VALUE(OUTS.OUTGO_MONEY, 0)) AS OUTGO_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT OUTD ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ON OUTD.SCHOOLCD = OUTS.SCHOOLCD ";
        $query .= "           AND OUTD.SCHOOL_KIND= OUTS.SCHOOL_KIND ";
        $query .= "           AND OUTD.YEAR = OUTS.YEAR ";
        $query .= "           AND OUTD.OUTGO_L_CD = OUTS.OUTGO_L_CD ";
        $query .= "           AND OUTD.OUTGO_M_CD = OUTS.OUTGO_M_CD ";
        $query .= "           AND OUTD.REQUEST_NO = OUTS.REQUEST_NO ";
        $query .= "           AND OUTS.SCHREGNO = '{$model->schregno}' ";
        $query .= "     LEFT JOIN LEVY_S_MST LEVYS ON OUTS.SCHOOLCD = LEVYS .SCHOOLCD ";
        $query .= "          AND OUTS.SCHOOL_KIND= LEVYS .SCHOOL_KIND ";
        $query .= "          AND OUTS.YEAR = LEVYS .YEAR ";
        $query .= "          AND OUTS.OUTGO_L_CD = LEVYS .LEVY_L_CD ";
        $query .= "          AND OUTS.OUTGO_M_CD = LEVYS .LEVY_M_CD ";
        $query .= "          AND OUTS.OUTGO_S_CD = LEVYS .LEVY_S_CD ";
        $query .= "     LEFT JOIN LEVY_L_MST LEVYL ON OUTD.SCHOOLCD = LEVYL.SCHOOLCD ";
        $query .= "          AND OUTD.SCHOOL_KIND= LEVYL.SCHOOL_KIND ";
        $query .= "          AND OUTD.YEAR = LEVYL.YEAR ";
        $query .= "          AND OUTD.OUTGO_L_CD = LEVYL.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "     OUTD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(OUTD.OUTGO_APPROVAL, '0') = '1' ";
        $query .= "     AND VALUE(OUTD.OUTGO_CANCEL, '0') = '0' ";
        $query .= "     AND OUTD.OUTGO_L_CD != '99' AND OUTD.OUTGO_M_CD != '99' ";
        $query .= "     AND OUTD.SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND OUTD.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND OUTD.OUTGO_DATE <= '{$lastDate}' ";
        $query .= "     AND OUTD.OUTGO_DATE >= '".CTRL_YEAR."-04-01' ";
        $query .= " GROUP BY ";
        $query .= "     OUTS.OUTGO_L_CD, ";
        $query .= "     LEVYL.LEVY_L_NAME, ";
        $query .= "     OUTS.OUTGO_M_CD, ";
        $query .= "     OUTS.OUTGO_S_CD, ";
        $query .= "     LEVYS.LEVY_S_NAME, ";
        $query .= "     LEVYS.BENEFIT ";

        return $query;
    }
}
?>
