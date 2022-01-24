<?php

require_once('for_php7.php');

class knjp175kquery extends Query {

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

    //相殺年月
    function getYearMonth()
    {
        $year = CTRL_YEAR;
        $nextYear = CTRL_YEAR + 1;
        $query  = " WITH YEAR_MONTH_T(LABEL, VALUE) AS ( ";
        $query .= " VALUES('{$year}年12月', '{$year}12') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " from ";
        $query .= "     YEAR_MONTH_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //相殺月
    function getMonth()
    {
        $query  = " WITH MONTH_T(LABEL, VALUE, SORTNO) AS ( ";
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

    //データ更新
    function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $sYearMonth  = $model->paidMonthS < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $sYearMonth .= $model->paidMonthS;
        $eYearMonth  = $model->paidMonthE < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $eYearMonth .= $model->paidMonthE;

        //相殺年月クリア
        $query  = " UPDATE ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.REGISTERCD = '".STAFFCD."', ";
        $query .= "     T1.UPDATED = sysdate(), ";
        if ($model->reducDiv == "1") {
            $query .= "     T1.PAID_YEARMONTH = NULL ";
        } else {
            $query .= "     T1.ADD_PAID_YEARMONTH = NULL ";
        }
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
        if ($model->reducDiv == "1") {
            $query .= "     AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "     AND VALUE(T1.PLAN_LOCK_FLG, '0') = '0' ";
        } else {
            $query .= "     AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "     AND VALUE(T1.ADD_PLAN_LOCK_FLG, '0') = '0' ";
        }

        $db->query($query);

        $query  = " UPDATE ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.REGISTERCD = '".STAFFCD."', ";
        $query .= "     T1.UPDATED = sysdate(), ";
        if ($model->reducDiv == "1") {
            $query .= "     T1.PAID_YEARMONTH = '{$model->paidYearMonth}' ";
        } else {
            $query .= "     T1.ADD_PAID_YEARMONTH = '{$model->paidYearMonth}' ";
        }
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
        if ($model->reducDiv == "1") {
            $query .= "     AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "     AND T1.PAID_MONEY is not null ";
            $query .= "     AND VALUE(T1.PLAN_LOCK_FLG, '0') = '0' ";
        } else {
            $query .= "     AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "     AND T1.ADD_PAID_MONEY is not null ";
            $query .= "     AND VALUE(T1.ADD_PLAN_LOCK_FLG, '0') = '0' ";
        }
        $query .= "     AND EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 REDUCTION_COUNTRY_DAT E1 ";
        $query .= "             WHERE ";
        $query .= "                 E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                 AND E1.OFFSET_FLG = '1' ";
        $query .= "         ) ";

        $db->query($query);

        if ($model->offset) {
            //相殺フラグクリア
            $query = knjp175kQuery::getDeleteOffsetFlg($model);
            $db->query($query);

            //相殺フラグセット
            $query = knjp175kQuery::getUpdateOffsetFlg($model);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getDeleteOffsetFlg($model) {
        $query  = " UPDATE ";
        $query .= "     REDUCTION_DAT T1 ";
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
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND E1.GRADE = '{$model->grade}' ";
        $query .= "     ) ";
        $query .= "     AND VALUE(T1.LOCK_FLG, '0') = '0' ";

        return $query;
    }

    function getUpdateOffsetFlg($model) {
        $query  = " UPDATE ";
        $query .= "     REDUCTION_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.REGISTERCD = '".STAFFCD."', ";
        $query .= "     T1.UPDATED = sysdate(), ";
        $query .= "     T1.OFFSET_FLG = '1' ";
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
        $query .= "     AND VALUE(T1.LOCK_FLG, '0') = '0' ";
        $query .= "     AND (VALUE(T1.REDUC_DEC_FLG_1, '0') = '1' ";
        $query .= "          OR ";
        $query .= "          VALUE(T1.REDUC_DEC_FLG_2, '0') = '1') ";

        return $query;
    }

}
?>
