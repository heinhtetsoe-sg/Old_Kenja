<?php

require_once('for_php7.php');

class knjp372kquery extends Query {

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
    function getYearMonth($model, $div)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     {$div}PAID_YEARMONTH AS VALUE, ";
        $query .= "     substr({$div}PAID_YEARMONTH, 1, 4) || '年' || substr({$div}PAID_YEARMONTH, 5) || '月' AS LABEL ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND {$div}PAID_YEARMONTH IS NOT NULL ";
        $query .= "     AND VALUE({$div}PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE({$div}PLAN_LOCK_FLG, '0') = '0' ";
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
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //データ更新
    function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $div = $model->reducDiv == "1" ? "" : "ADD_";

        $query  = " UPDATE ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.REGISTERCD = '".STAFFCD."', ";
        $query .= "     T1.UPDATED = sysdate(), ";
        $query .= "     T1.{$div}PLAN_LOCK_FLG = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND {$div}PAID_YEARMONTH = '{$model->paidYearMonth}' ";
        $query .= "     AND VALUE(T1.{$div}PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND T1.{$div}PAID_MONEY is not null ";
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

        $db->query($query);

        if ($model->lock) {
            //相殺フラグセット
            $query = knjp372kQuery::getUpdateOffsetFlg($model);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getUpdateOffsetFlg($model) {
        $query  = " UPDATE ";
        $query .= "     REDUCTION_DAT T1 ";
        $query .= " SET ";
        $query .= "     T1.REGISTERCD = '".STAFFCD."', ";
        $query .= "     T1.UPDATED = sysdate(), ";
        $query .= "     T1.LOCK_FLG = '1' ";
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
        $query .= "     AND T1.OFFSET_FLG = '1' ";
        $query .= "     AND (VALUE(T1.REDUC_DEC_FLG_1, '0') = '1' ";
        $query .= "          OR ";
        $query .= "          VALUE(T1.REDUC_DEC_FLG_2, '0') = '1') ";

        return $query;
    }

}
?>
