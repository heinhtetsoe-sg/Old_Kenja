<?php

require_once('for_php7.php');

class knjp904_saimokuQuery extends Query {
    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->getYear}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1   = '{$model->getSchoolKind}' ";

        return $query;
    }

    //預り金科目取得
    function getLevyLMst($model) {
        $query  =" SELECT ";
        $query .="   LEVY_L_NAME ";
        $query .=" FROM ";
        $query .="   LEVY_L_MST ";
        $query .=" WHERE ";
        $query .="       SCHOOLCD    = '{$model->getSchoolCd}' ";
        $query .="   AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .="   AND YEAR        = '{$model->getYear}' ";
        $query .="   AND LEVY_L_CD   = '{$model->getOutgoLcd}' ";

        return $query;
    }

    //預り金項目取得
    function getLevyMMst($model) {
        $query  =" SELECT ";
        $query .="   LEVY_M_NAME ";
        $query .=" FROM ";
        $query .="   LEVY_M_MST ";
        $query .=" WHERE ";
        $query .="       SCHOOLCD    = '{$model->getSchoolCd}' ";
        $query .="   AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .="   AND YEAR        = '{$model->getYear}' ";
        $query .="   AND LEVY_L_CD   = '{$model->getOutgoLcd}' ";
        $query .="   AND LEVY_M_CD   = '{$model->getOutgoMcd}' ";

        return $query;
    }

    //去年の情報を取得
    function getLastData($model) {
        $query  = " WITH JISEKI_DATA AS ( ";
        $query .= "       SELECT ";
        $query .= "         BMEI.SCHOOLCD, ";
        $query .= "         BMEI.SCHOOL_KIND, ";
        $query .= "         BMEI.YEAR, ";
        $query .= "         BMEI.BUDGET_L_CD, ";
        $query .= "         BMEI.BUDGET_M_CD, ";
        $query .= "         BMEI.BUDGET_S_CD, ";
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
        $query .= "         BMEI.BUDGET_M_CD, ";
        $query .= "         BMEI.BUDGET_S_CD ";
        $query .= " ), YOBIHI (LEVY_S_CD, LEVY_S_NAME) AS ( ";
        $query .= "      VALUES('AAA', '予備費') ";
        $query .= " ), S_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         LEVY_L_CD, ";
        $query .= "         LEVY_M_CD, ";
        $query .= "         LEVY_S_CD, ";
        $query .= "         LEVY_S_NAME ";
        $query .= "     FROM ";
        $query .= "         LEVY_S_MST ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '{$model->getSchoolCd}' ";
        $query .= "         AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= "         AND YEAR        = '{$model->lastYear}' ";
        $query .= "         AND LEVY_L_CD   = '{$model->getOutgoLcd}' ";
        $query .= "         AND LEVY_M_CD   = '{$model->getOutgoMcd}' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         '{$model->getSchoolCd}' as SCHOOLCD, ";
        $query .= "         '{$model->getSchoolKind}' as SCHOOL_KIND, ";
        $query .= "         '{$model->lastYear}' as YEAR, ";
        $query .= "         '{$model->getOutgoLcd}' as LEVY_L_CD, ";
        $query .= "         '{$model->getOutgoMcd}' as LEVY_M_CD, ";
        $query .= "         LEVY_S_CD, ";
        $query .= "         LEVY_S_NAME ";
        $query .= "     FROM ";
        $query .= "         YOBIHI  ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LEVY_S_CD, ";
        $query .= "     BGET.BUDGET_MONEY as LASTYEAR_BUDGET, ";
        $query .= "     JISK.SCH_PRICE as LASTYEAR_SCH_PRICE ";
        $query .= " FROM ";
        $query .= "     S_MST SMST ";
        $query .= "     LEFT JOIN LEVY_BUDGET_DAT BGET ON BGET.SCHOOLCD    = SMST.SCHOOLCD ";
        $query .= "                                   AND BGET.SCHOOL_KIND = SMST.SCHOOL_KIND ";
        $query .= "                                   AND BGET.YEAR        = SMST.YEAR ";
        $query .= "                                   AND BGET.BUDGET_L_CD = SMST.LEVY_L_CD ";
        $query .= "                                   AND BGET.BUDGET_M_CD = SMST.LEVY_M_CD ";
        $query .= "                                   AND BGET.BUDGET_S_CD = SMST.LEVY_S_CD ";
        $query .= "     LEFT JOIN JISEKI_DATA JISK ON JISK.SCHOOLCD    = SMST.SCHOOLCD ";
        $query .= "                               AND JISK.SCHOOL_KIND = SMST.SCHOOL_KIND ";
        $query .= "                               AND JISK.YEAR        = SMST.YEAR ";
        $query .= "                               AND JISK.BUDGET_L_CD = SMST.LEVY_L_CD ";
        $query .= "                               AND JISK.BUDGET_M_CD = SMST.LEVY_M_CD ";
        $query .= "                               AND JISK.BUDGET_S_CD = SMST.LEVY_S_CD ";

        return $query;
    }

    //リスト一覧を取得
    function getList($model) {
        $query  = " WITH YOBIHI (LEVY_S_CD, LEVY_S_NAME) AS ( ";
        $query .= "      VALUES('AAA', '予備費') ";
        $query .= " ), S_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         YEAR, ";
        $query .= "         LEVY_L_CD, ";
        $query .= "         LEVY_M_CD, ";
        $query .= "         LEVY_S_CD, ";
        $query .= "         LEVY_S_NAME ";
        $query .= "     FROM ";
        $query .= "         LEVY_S_MST ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '{$model->getSchoolCd}' ";
        $query .= "         AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= "         AND YEAR        = '{$model->getYear}' ";
        $query .= "         AND LEVY_L_CD   = '{$model->getOutgoLcd}' ";
        $query .= "         AND LEVY_M_CD   = '{$model->getOutgoMcd}' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         '{$model->getSchoolCd}' as SCHOOLCD, ";
        $query .= "         '{$model->getSchoolKind}' as SCHOOL_KIND, ";
        $query .= "         '{$model->getYear}' as YEAR, ";
        $query .= "         '{$model->getOutgoLcd}' as LEVY_L_CD, ";
        $query .= "         '{$model->getOutgoMcd}' as LEVY_M_CD, ";
        $query .= "         LEVY_S_CD, ";
        $query .= "         LEVY_S_NAME ";
        $query .= "     FROM ";
        $query .= "         YOBIHI  ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LEVY_S_CD, ";
        $query .= "     LEVY_S_NAME, ";
        $query .= "     BGET.BUDGET_MONEY ";
        $query .= " FROM ";
        $query .= "     S_MST SMST ";
        $query .= "     LEFT JOIN LEVY_BUDGET_DAT BGET ON BGET.SCHOOLCD    = SMST.SCHOOLCD ";
        $query .= "                                   AND BGET.SCHOOL_KIND = SMST.SCHOOL_KIND ";
        $query .= "                                   AND BGET.YEAR        = SMST.YEAR ";
        $query .= "                                   AND BGET.BUDGET_L_CD = SMST.LEVY_L_CD ";
        $query .= "                                   AND BGET.BUDGET_M_CD = SMST.LEVY_M_CD ";
        $query .= "                                   AND BGET.BUDGET_S_CD = SMST.LEVY_S_CD ";
        $query .= " ORDER BY ";
        $query .= "     SMST.LEVY_S_CD ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updateArr as $sCd) {
            $budgetMoney = $model->field["BUDGET_MONEY:".$sCd];

            if ($budgetMoney != 0 && $budgetMoney != '') {
                //LEVY_BUDGET_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]         = $model->getSchoolCd;
                $data["SCHOOL_KIND"][TEXT]      = $model->getSchoolKind;
                $data["YEAR"][TEXT]             = $model->getYear;
                $data["BUDGET_L_CD"][TEXT]      = $model->getOutgoLcd;
                $data["BUDGET_M_CD"][TEXT]      = $model->getOutgoMcd;
                $data["BUDGET_S_CD"][TEXT]      = $sCd;
                $data["BUDGET_MONEY"][NUMBER]   = $budgetMoney;

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "LEVY_BUDGET_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //LEVY_BUDGET_DAT
        $query  = " DELETE FROM LEVY_BUDGET_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '{$model->getSchoolCd}' ";
        $query .= " AND SCHOOL_KIND = '{$model->getSchoolKind}' ";
        $query .= " AND YEAR        = '{$model->getYear}' ";
        $query .= " AND BUDGET_L_CD = '{$model->getOutgoLcd}' ";
        $query .= " AND BUDGET_M_CD = '{$model->getOutgoMcd}' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
