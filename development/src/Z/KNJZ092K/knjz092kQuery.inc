<?php

require_once('for_php7.php');

class knjz092kQuery extends Query {
    //データ取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.PREFECTURESCD, ";
        $query .= "     T2.NAME1, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.RANK_DIV, ";
        $query .= "     CASE WHEN (T1.YEAR < '2016') OR (T1.YEAR = '2016' AND T1.GRADE > '01') OR (T1.YEAR = '2017' AND T1.GRADE = '03') ";
        $query .= "          THEN G213.NAME1 ";
        $query .= "          ELSE G218.NAME1 ";
        $query .= "     END AS RANK_NAME, ";
        $query .= "     T1.REDUCTIONMONEY_1, ";
        $query .= "     T1.REDUCTIONMONEY_2, ";
        $query .= "     T1.MAX_MONEY, ";
        $query .= "     T1.MIN_MONEY_1, ";
        $query .= "     T1.MIN_MONEY_2, ";
        $query .= "     T1.MIN_MONEY, ";
        $query .= "     T1.PARENTS_MONEY_1, ";
        $query .= "     T1.PARENTS_MONEY_2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MAX_MONEY_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST G213 ON G213.NAMECD1 = 'G213' ";
        $query .= "          AND G213.NAMECD2 = T1.RANK_DIV ";
        $query .= "     LEFT JOIN NAME_MST G218 ON G218.NAMECD1 = 'G218' ";
        $query .= "          AND G218.NAMECD2 = T1.RANK_DIV, ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.PREFECTURESCD = T2.NAMECD2 ";
        $query .= "     AND T2.NAMECD1 = 'G202' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PREFECTURESCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.RANK_DIV ";

        return $query;
    }

    //都道府県コード取得
    function getName($year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2,NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'G202' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学年取得
    function getGrade($year) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ランク取得
    function getRank($model, $nameCd1) {
        $query = " SELECT ";
        $query .= "   NAMECD2 AS VALUE, ";
        $query .= "   NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = '{$nameCd1}' ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";
        return $query;
    }

    //１レコード取得
    function getRow($model, $flg) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MAX_MONEY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";

        if ($flg == 1) {
            $query .= "    AND PREFECTURESCD  = '".$model->prefecturescd."' ";
            $query .= "    AND GRADE  = '".$model->grade."' ";
            $query .= "    AND RANK_DIV  = '".$model->rankDiv."' ";
        } elseif ($flg == 2) {
            $query .= "    AND PREFECTURESCD = '".$model->field["PREFECTURESCD"]."' ";
            $query .= "    AND GRADE = '".$model->field["GRADE"]."' ";
            $query .= "    AND RANK_DIV = '".$model->field["RANK_DIV"]."' ";
        }

        return $query;
    }

    //データが存在するかチェック
    function CheckData($year) {

        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) AS CNT";
        $query .= " FROM ";
        $query .= "     REDUCTION_MAX_MONEY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //前年度のデータをコピー
    function &getCopyQuery($year) {

        $query  = " INSERT INTO REDUCTION_MAX_MONEY_DAT ";
        $query .= "                ( ";
        $query .= "                 YEAR, ";
        $query .= "                 PREFECTURESCD, ";
        $query .= "                 GRADE, ";
        $query .= "                 RANK_DIV, ";
        $query .= "                 REDUCTIONMONEY_1, ";
        $query .= "                 REDUCTIONMONEY_2, ";
        $query .= "                 MAX_MONEY, ";
        $query .= "                 MIN_MONEY_1, ";
        $query .= "                 MIN_MONEY_2, ";
        $query .= "                 MIN_MONEY, ";
        $query .= "                 PARENTS_MONEY_1, ";
        $query .= "                 PARENTS_MONEY_2, ";
        $query .= "                 REGISTERCD, ";
        $query .= "                 UPDATED ";
        $query .= "                ) ";
        $query .= "    (SELECT ";
        $query .= "         '".$year."', ";
        $query .= "         PREFECTURESCD, ";
        $query .= "         GRADE, ";
        $query .= "         RANK_DIV, ";
        $query .= "         REDUCTIONMONEY_1, ";
        $query .= "         REDUCTIONMONEY_2, ";
        $query .= "         MAX_MONEY, ";
        $query .= "         MIN_MONEY_1, ";
        $query .= "         MIN_MONEY_2, ";
        $query .= "         MIN_MONEY, ";
        $query .= "         PARENTS_MONEY_1, ";
        $query .= "         PARENTS_MONEY_2, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         REDUCTION_MAX_MONEY_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".($year - 1)."'";
        $query .= "     ) ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields) {

        $data["YEAR"][TEXT]                 = $fields["YEAR"];
        $data["PREFECTURESCD"][TEXT]        = $fields["PREFECTURESCD"];
        $data["GRADE"][TEXT]                = $fields["GRADE"];
        $data["RANK_DIV"][TEXT]             = $fields["RANK_DIV"];
        $data["REDUCTIONMONEY_1"][NUMBER]   = is_null($fields["REDUCTIONMONEY_1"])? 0 : $fields["REDUCTIONMONEY_1"];
        $data["REDUCTIONMONEY_2"][NUMBER]   = is_null($fields["REDUCTIONMONEY_2"])? 0 : $fields["REDUCTIONMONEY_2"];
        $data["MAX_MONEY"][NUMBER]          = $fields["MAX_MONEY"];
        $data["MIN_MONEY_1"][NUMBER]        = is_null($fields["MIN_MONEY_1"])? 0 : $fields["MIN_MONEY_1"];
        $data["MIN_MONEY_2"][NUMBER]        = is_null($fields["MIN_MONEY_2"])? 0 : $fields["MIN_MONEY_2"];
        $data["MIN_MONEY"][NUMBER]          = $fields["MIN_MONEY"];
        $data["PARENTS_MONEY_1"][NUMBER]    = is_null($fields["PARENTS_MONEY_1"])? 0 : $fields["PARENTS_MONEY_1"];
        $data["PARENTS_MONEY_2"][NUMBER]    = is_null($fields["PARENTS_MONEY_2"])? 0 : $fields["PARENTS_MONEY_2"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_MAX_MONEY_DAT");
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($fields) {

        $data["REDUCTIONMONEY_1"][NUMBER]   = $fields["REDUCTIONMONEY_1"];
        $data["REDUCTIONMONEY_2"][NUMBER]   = $fields["REDUCTIONMONEY_2"];
        $data["MAX_MONEY"][NUMBER]          = $fields["MAX_MONEY"];
        $data["MIN_MONEY_1"][NUMBER]        = $fields["MIN_MONEY_1"];
        $data["MIN_MONEY_2"][NUMBER]        = $fields["MIN_MONEY_2"];
        $data["MIN_MONEY"][NUMBER]          = $fields["MIN_MONEY"];
        $data["PARENTS_MONEY_1"][NUMBER]    = $fields["PARENTS_MONEY_1"];
        $data["PARENTS_MONEY_2"][NUMBER]    = $fields["PARENTS_MONEY_2"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE YEAR          = '".$fields["YEAR"]."' ";
        $where .= "   AND PREFECTURESCD = '".$fields["PREFECTURESCD"]."' ";
        $where .= "   AND GRADE = '".$fields["GRADE"]."' ";
        $where .= "   AND RANK_DIV = '".$fields["RANK_DIV"]."' ";

        $query = Query::updateSQL($data, "REDUCTION_MAX_MONEY_DAT", $where);
        return $query;
    }

    //DELETE
    function &getDeleteQuery($fields) {

        $query  = " DELETE FROM REDUCTION_MAX_MONEY_DAT ";
        $query .= " WHERE YEAR           = '".$fields["YEAR"]."' ";
        $query .= "   AND PREFECTURESCD  = '".$fields["PREFECTURESCD"]."' ";
        $query .= "   AND GRADE  = '".$fields["GRADE"]."' ";
        $query .= "   AND RANK_DIV  = '".$fields["RANK_DIV"]."' ";

        return $query;
    }

}
?>
