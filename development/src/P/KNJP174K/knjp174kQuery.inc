<?php

require_once('for_php7.php');

class knjp174kquery extends Query {

    //学年
    function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE_CD ";

        return $query;
    }

    //相殺月
    function getMonth()
    {
        $query  = " WITH MONTH_T(LABEL, VALUE, SORTNO) AS ( ";
        $query .= " VALUES('04月', '04', '01') ";
        $query .= " UNION ";           
        $query .= " VALUES('05月', '05', '02') ";
        $query .= " UNION ";           
        $query .= " VALUES('06月', '06', '03') ";
        $query .= " UNION ";           
        $query .= " VALUES('07月', '07', '04') ";
        $query .= " UNION ";           
        $query .= " VALUES('08月', '08', '05') ";
        $query .= " UNION ";           
        $query .= " VALUES('09月', '09', '06') ";
        $query .= " UNION ";           
        $query .= " VALUES('10月', '10', '07') ";
        $query .= " UNION ";           
        $query .= " VALUES('11月', '11', '08') ";
        $query .= " UNION ";           
        $query .= " VALUES('12月', '12', '09') ";
        $query .= " UNION ";           
        $query .= " VALUES('01月', '01', '10') ";
        $query .= " UNION ";           
        $query .= " VALUES('02月', '02', '11') ";
        $query .= " UNION ";           
        $query .= " VALUES('03月', '03', '12') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " from ";
        $query .= "     MONTH_T ";
        $query .= " ORDER BY ";
        $query .= "     SORTNO ";

        return $query;
    }

    //現年月
    function getdefMonth()
    {
        $query = " VALUES(CAST(MONTH(SYSDATE()) AS CHAR(2))) ";

        return $query;
    }

    //データ更新
    function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $sYearMonth  = $model->paidMonthS < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $sYearMonth .= $model->paidMonthS;
        $eYearMonth  = $model->paidMonthE < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $eYearMonth .= $model->paidMonthE;

        if ($model->reducDiv == "1") {
            //基本金額クリア
            $query = knjp174kQuery::getClearMoney($model, $eYearMonth, "");
            $db->query($query);

            //基本金額セット
            $query = knjp174kQuery::getUpdateMoney($model, $sYearMonth, $eYearMonth, "");
            $db->query($query);
        } else {
            //加算金額クリア
            $query = knjp174kQuery::getClearMoney($model, $eYearMonth, "ADD_");
            $db->query($query);

            //加算金額セット
            $query = knjp174kQuery::getUpdateMoney($model, $sYearMonth, $eYearMonth, "ADD_");
            $db->query($query);
        }

        //相殺フラグクリア(転退学、休学)
        $query = knjp174kQuery::getDeleteOffsetFlgGrd($model);
        $db->query($query);

        //相殺フラグクリア(支援金＋補助金＋減免＝０)
        $query = knjp174kQuery::getDeleteOffsetFlgReduction($model);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getClearMoney($model, $eYearMonth, $div) {
        $query  = " UPDATE ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.REGISTERCD = '".STAFFCD."', ";
        $query .= "     T1.UPDATED = sysdate(), ";
        $query .= "     T1.{$div}PAID_MONEY = NULL ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND E1.GRADE = '{$model->grade}' ";
        $query .= "     ) ";
        $query .= "     AND T1.PLAN_YEAR || T1.PLAN_MONTH > '{$eYearMonth}' ";
        $query .= "     AND VALUE(T1.{$div}PLAN_LOCK_FLG, '0') = '0' ";

        return $query;
    }

    function getUpdateMoney($model, $sYearMonth, $eYearMonth, $div) {
        $query  = " UPDATE ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.REGISTERCD = '".STAFFCD."', ";
        $query .= "     T1.UPDATED = sysdate(), ";
        $query .= "     T1.{$div}PAID_MONEY = T1.{$div}PLAN_MONEY ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND E1.GRADE = '{$model->grade}' ";
        $query .= "     ) ";
        $query .= "     AND T1.PLAN_YEAR || T1.PLAN_MONTH BETWEEN '{$sYearMonth}' AND '{$eYearMonth}' ";
        $query .= "     AND VALUE(T1.{$div}PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(T1.{$div}PLAN_LOCK_FLG, '0') = '0' ";

        return $query;
    }

    function getDeleteOffsetFlgGrd($model) {
        $setDate = str_replace("/", "-", $model->grdDate);
        $query  = " UPDATE ";
        $query .= "     REDUCTION_COUNTRY_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.REGISTERCD = '".STAFFCD."', ";
        $query .= "     T1.UPDATED = sysdate(), ";
        $query .= "     T1.OFFSET_FLG = NULL ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1, ";
        $query .= "             (SELECT ";
        $query .= "                  BASE.SCHREGNO ";
        $query .= "              FROM ";
        $query .= "                  SCHREG_BASE_MST BASE ";
        $query .= "              WHERE ";
        $query .= "                  (BASE.ENT_DIV IN ('4') ";
        $query .= "                   AND BASE.ENT_DATE BETWEEN '".CTRL_YEAR."-04-01' AND '".(CTRL_YEAR + 1)."-03-31') ";
        $query .= "                  OR ";
        $query .= "                  (BASE.GRD_DIV IN ('2', '3') ";
        $query .= "                   AND BASE.GRD_DATE <= '{$setDate}')) BASE_GRD, ";
        $query .= "             (SELECT ";
        $query .= "                  TRANSFER.SCHREGNO ";
        $query .= "                  FROM ";
        $query .= "                      SCHREG_TRANSFER_DAT TRANSFER ";
        $query .= "                  WHERE ";
        $query .= "                      TRANSFER.TRANSFERCD IN ('2') ";
        $query .= "                      AND '{$setDate}' BETWEEN TRANSFER.TRANSFER_SDATE AND TRANSFER.TRANSFER_EDATE) SCH_TRANS ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND (E1.SCHREGNO = BASE_GRD.SCHREGNO OR E1.SCHREGNO = SCH_TRANS.SCHREGNO) ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND E1.GRADE = '{$model->grade}' ";
        $query .= "     ) ";

        return $query;
    }

    function getDeleteOffsetFlgReduction($model) {
        $query  = " UPDATE ";
        $query .= "     REDUCTION_COUNTRY_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.REGISTERCD = '".STAFFCD."', ";
        $query .= "     T1.UPDATED = sysdate(), ";
        $query .= "     T1.OFFSET_FLG = NULL ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT E1 ";
        $query .= "             LEFT JOIN REDUCTION_DAT REDUCTION ON E1.YEAR = REDUCTION.YEAR ";
        $query .= "                  AND E1.SCHREGNO = REDUCTION.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND E1.GRADE = '{$model->grade}' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND VALUE(REDUCTION.REDUCTIONMONEY_1, 0) + VALUE(REDUCTION.REDUCTIONMONEY_2, 0) = 0 ";
        $query .= "             AND VALUE(T1.REDUCTIONMONEY_1, 0) + VALUE(T1.REDUCTION_ADD_MONEY_1, 0) + VALUE(T1.REDUCTIONMONEY_2, 0) + VALUE(T1.REDUCTION_ADD_MONEY_2, 0) = 0 ";
        $query .= "             AND T1.SCHREGNO NOT IN (SELECT I1.SCHREGNO FROM SCHREG_GRANT_DAT I1 WHERE I1.YEAR = '".CTRL_YEAR."') ";
        $query .= "     ) ";

        return $query;
    }

}
?>
