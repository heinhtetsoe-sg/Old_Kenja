<?php

require_once('for_php7.php');

class knjx_marathon_selectQuery extends Query {

    //対象年度取得
    function getRegdYear($model) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T2.SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR AND T1.GRADE = T2.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM MAIN WHERE YEAR = '".$model->exp_year."') ";

        return $query;
    }

    //マラソン大会マスタ情報取得
    function getMarathonEventMst($model, $year) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM MARATHON_EVENT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR IN ('".implode($year, "','")."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEQ ";

        return $query;
    }

    //マラソン大会情報取得(生徒情報)
    function getMarathonEventDat($model, $year) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= "   , N1.NAME1 ATTEND_NAME ";
        $query .= " FROM MARATHON_EVENT_RANK_DAT T1 ";
        $query .= " LEFT JOIN NAME_MST N1 ";
        $query .= "     ON N1.NAMECD1 = 'J010' ";
        $query .= "     AND N1.NAMECD2 = T1.ATTEND_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR IN ('".implode($year, "','")."') ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEQ ";

        return $query;
    }

    //学籍基礎情報(性別)
    function getSchregBase($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   , T1.SEX ";
        $query .= " FROM SCHREG_BASE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

}
?>
