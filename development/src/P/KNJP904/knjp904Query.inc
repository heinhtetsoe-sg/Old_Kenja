<?php

require_once('for_php7.php');
class knjp904Query extends Query {
    // マスタ存在確認
    function checkLevyMst($model, $table) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //預り金科目取得
    function getLevyLMst($model) {
        $query  =" SELECT ";
        $query .="   LEVY_L_CD as VALUE, ";
        $query .="   LEVY_L_CD || ':' || LEVY_L_NAME as LABEL ";
        $query .=" FROM ";
        $query .="   LEVY_L_MST ";
        $query .=" WHERE ";
        $query .="       SCHOOLCD    = '{$model->schoolCd}' ";
        $query .="   AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .="   AND YEAR        = '{$model->year}' ";
        $query .="   AND LEVY_L_CD  not in ('98', '99') ";

        return $query;
    }

    //去年の情報を取得
    function getLastData($model) {
        $query .= " WITH BUDGET_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         BUDGET_L_CD, ";
        $query .= "         BUDGET_M_CD, ";
        $query .= "         sum(BUDGET_MONEY) as BUDGET_MONEY ";
        $query .= "     FROM ";
        $query .= "         LEVY_BUDGET_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         BUDGET_L_CD, ";
        $query .= "         BUDGET_M_CD ";
        $query .= " ), JISEKI_DATA AS ( ";
        $query .= "       SELECT ";
        $query .= "         BMEI.SCHOOLCD, ";
        $query .= "         BMEI.SCHOOL_KIND, ";
        $query .= "         BMEI.YEAR, ";
        $query .= "         BMEI.BUDGET_L_CD, ";
        $query .= "         BMEI.BUDGET_M_CD, ";
        $query .= "         sum(OMEI.SCH_PRICE) as SCH_PRICE ";
        $query .= "     FROM ";
        $query .= "         LEVY_BUDGET_MEISAI_DAT BMEI ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT OMEI ON OMEI.SCHOOLCD    = BMEI.SCHOOLCD ";
        $query .= "                                                 AND OMEI.SCHOOL_KIND = BMEI.SCHOOL_KIND ";
        $query .= "                                                 AND OMEI.YEAR        = BMEI.YEAR ";
        $query .= "                                                 AND OMEI.REQUEST_NO  = BMEI.REQUEST_NO ";
        $query .= "                                                 AND OMEI.LINE_NO     = BMEI.LINE_NO ";
        $query .= "                                                 AND OMEI.OUTGO_L_CD  = BMEI.OUTGO_L_CD ";
        $query .= "                                                 AND OMEI.OUTGO_M_CD  = BMEI.OUTGO_M_CD ";
        $query .= "                                                 AND OMEI.OUTGO_S_CD  = BMEI.OUTGO_S_CD ";
        $query .= "     GROUP BY ";
        $query .= "         BMEI.SCHOOLCD, ";
        $query .= "         BMEI.SCHOOL_KIND, ";
        $query .= "         BMEI.YEAR, ";
        $query .= "         BMEI.BUDGET_L_CD, ";
        $query .= "         BMEI.BUDGET_M_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MMST.LEVY_L_CD, ";
        $query .= "     MMST.LEVY_M_CD, ";
        $query .= "     BGET.BUDGET_MONEY as LASTYEAR_BUDGET, ";
        $query .= "     JISK.SCH_PRICE as LASTYEAR_SCH_PRICE ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST MMST ";
        $query .= "     LEFT JOIN BUDGET_DAT BGET ON BGET.SCHOOLCD    = MMST.SCHOOLCD ";
        $query .= "                              AND BGET.SCHOOL_KIND = MMST.SCHOOL_KIND ";
        $query .= "                              AND BGET.YEAR        = MMST.YEAR ";
        $query .= "                              AND BGET.BUDGET_L_CD = MMST.LEVY_L_CD ";
        $query .= "                              AND BGET.BUDGET_M_CD = MMST.LEVY_M_CD ";
        $query .= "     LEFT JOIN JISEKI_DATA JISK ON JISK.SCHOOLCD    = MMST.SCHOOLCD ";
        $query .= "                               AND JISK.SCHOOL_KIND = MMST.SCHOOL_KIND ";
        $query .= "                               AND JISK.YEAR        = MMST.YEAR ";
        $query .= "                               AND JISK.BUDGET_L_CD = MMST.LEVY_L_CD ";
        $query .= "                               AND JISK.BUDGET_M_CD = MMST.LEVY_M_CD ";
        $query .= " WHERE ";
        $query .= "         MMST.SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "     AND MMST.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "     AND MMST.YEAR        = '{$model->lastYear}' ";
        $query .= "     AND MMST.LEVY_L_CD   = '{$model->field["LEVY_L_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     MMST.LEVY_M_CD ";

        return $query;
    }

    //リスト一覧を取得
    function getList($model) {
        $query  = " WITH BUDGET_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         BUDGET_L_CD, ";
        $query .= "         BUDGET_M_CD, ";
        $query .= "         sum(BUDGET_MONEY) as BUDGET_MONEY ";
        $query .= "     FROM ";
        $query .= "         LEVY_BUDGET_DAT ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "         AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "         AND YEAR        = '{$model->year}' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         BUDGET_L_CD, ";
        $query .= "         BUDGET_M_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MMST.SCHOOLCD, ";
        $query .= "     MMST.SCHOOL_KIND, ";
        $query .= "     MMST.YEAR, ";
        $query .= "     MMST.LEVY_L_CD as OUTGO_L_CD, ";
        $query .= "     MMST.LEVY_M_CD as OUTGO_M_CD, ";
        $query .= "     MMST.LEVY_L_CD || LEVY_M_CD || ':' || MMST.LEVY_M_NAME as LEVY_M_NAME, ";
        $query .= "     BGET.BUDGET_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST MMST ";
        $query .= "     LEFT JOIN BUDGET_DAT BGET ON BGET.SCHOOLCD    = MMST.SCHOOLCD ";
        $query .= "                              AND BGET.SCHOOL_KIND = MMST.SCHOOL_KIND ";
        $query .= "                              AND BGET.YEAR        = MMST.YEAR ";
        $query .= "                              AND BGET.BUDGET_L_CD = MMST.LEVY_L_CD ";
        $query .= "                              AND BGET.BUDGET_M_CD = MMST.LEVY_M_CD ";
        $query .= " WHERE ";
        $query .= "         MMST.SCHOOLCD         = '{$model->schoolCd}' ";
        $query .= "     AND MMST.SCHOOL_KIND      = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "     AND MMST.YEAR             = '{$model->year}' ";
        $query .= "     AND MMST.LEVY_L_CD        = '{$model->field["LEVY_L_CD"]}' ";
        $query .= "     AND MMST.LEVY_IN_OUT_DIV  = '2' ";
        $query .= " ORDER BY ";
        $query .= "     MMST.LEVY_M_CD ";

        return $query;
    }

}
?>
