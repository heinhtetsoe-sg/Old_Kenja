<?php

require_once('for_php7.php');

class knjp186kQuery extends Query {

    function getHist() {
        $query  = " SELECT  ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.EXE_TIME, ";
        $query .= "     L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     REDUCTION_AUTO_CREATE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.REGISTERCD = L1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXE_TIME DESC ";

        return $query;
    }

    function getStudent($lastYear) {
        $query  = " SELECT  ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T2.GRD_DIV, ";
        $query .= "     CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '0' ";
        $query .= "     END AS LAST_RARE_TYPE, ";
        $query .= "     REDUCDY.SCHREGNO AS REDUCD_SCHREGNO, ";
        $query .= "     REDUCD.REDUC_INCOME_2 AS LAST_REDUCD_REDUC_INCOME_2, ";
        $query .= "     L1.REDUC_RARE_CASE_CD_2 AS LAST_RARE_CASE, ";
        $query .= "     L1.REDUCTIONMONEY_2 AS LAST_MONEY, ";
        $query .= "     L1.REDUC_DEC_FLG_2 AS LAST_REDUC_DEC_FLG, ";
        $query .= "     L1.REDUC_INCOME_2 AS LAST_INCOME, ";
        $query .= "     L1.REDUCTION_ADD_MONEY_2 AS LAST_ADD_MONEY, ";
        $query .= "     L2.PASSNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN REDUCTION_DAT REDUCD ON REDUCD.YEAR = '{$lastYear}' ";
        $query .= "          AND T1.SCHREGNO = REDUCD.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_DAT REDUCDY ON REDUCDY.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.SCHREGNO = REDUCDY.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_DAT L1 ON L1.YEAR = '{$lastYear}' ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "          AND VALUE(L1.REDUC_RARE_CASE_CD_2, '') != 'T' ";
        $query .= "     LEFT JOIN NAME_MST RARE2 ON RARE2.NAMECD1 = 'G216' ";
        $query .= "          AND RARE2.NAMECD2 = L1.REDUC_RARE_CASE_CD_2, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L2 ON T2.SCHREGNO = L2.SCHREGNO ";
        $query .= "          AND L2.DATA_DIV = '1' ";
        $query .= "          AND L2.DATA_DIV_SUB = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";

        return $query;
    }
    //データ更新
    function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $lastYear = CTRL_YEAR - 1;
        $query = knjp186kQuery::getStudent($lastYear);
        $result = $db->query($query);
        $students = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $students[] = $row;
        }
        $result->free();

        foreach ($students as $key => $studentInfo) {
            $paidMonth = $db->getOne(knjp186kQuery::getPaidMonthCnt($lastYear, $studentInfo["SCHREGNO"]));
            $paidMonth = $paidMonth ? $paidMonth : 0;
            $zanMonth = $db->getOne(knjp186kQuery::getZanMonth($studentInfo["SCHREGNO"]));
            $zanMonth = $zanMonth ? $zanMonth : 0;
            $studentInfo["ZANKIKAN"] = $zanMonth > $paidMonth ? $zanMonth - $paidMonth : 0;
            $maxYmCancelFlg = $db->getRow(knjp186kQuery::getMaxYmCancelFlg($studentInfo["SCHREGNO"], $lastYear), DB_FETCHMODE_ASSOC);
            $studentInfo["PLAN_CANCEL_FLG"] = $maxYmCancelFlg["PLAN_CANCEL_FLG"];
            $studentInfo["ADD_PLAN_CANCEL_FLG"] = $maxYmCancelFlg["ADD_PLAN_CANCEL_FLG"];
            $query = knjp186kQuery::updateZanMonth($zanMonth, $paidMonth, $studentInfo["SCHREGNO"]);
            $db->query($query);
            //ステータス更新
            knjp186kQuery::updateKasan($db, $studentInfo["SCHREGNO"]);
            if ($studentInfo["GRD_DIV"] != "2" && $studentInfo["GRD_DIV"] != "3") {
                $counryDat = $db->getOne(knjp186kQuery::getCounryDat($studentInfo["SCHREGNO"]));
                $query = knjp186kQuery::updateCounryDat($db, $counryDat, $studentInfo);
                $db->query($query);

                $query = knjp186kQuery::updateReducDat($studentInfo);
                $db->query($query);

            }
        }

        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["EXE_TIME"][NUMBER]   = "SYSDATE()";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";
        $query = Query::insertSQL($data, "REDUCTION_AUTO_CREATE_HIST_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getPaidMonthCnt($lastYear, $schregNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$lastYear}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";
        $query .= "     AND PAID_MONEY IS NOT NULL ";

        return $query;
    }

    function getZanMonth($schregNo) {
        $query  = " SELECT ";
        $query .= "     REMAIN_SUP_LIMIT_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_AUTHORIZE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregNo}' ";
        $query .= "     AND DATA_DIV = '1' ";
        $query .= "     AND DATA_DIV_SUB = '1' ";

        return $query;
    }

    function getMaxYmCancelFlg($schregNo, $lastYear) {
        $query  = " WITH MAX_YM AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(PLAN_YEAR || PLAN_MONTH) AS MAX_YM ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$lastYear}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     PLAN_CANCEL_FLG, ";
        $query .= "     ADD_PLAN_CANCEL_FLG ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$lastYear}' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             MAX_YM E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND T1.PLAN_YEAR || T1.PLAN_MONTH = E1.MAX_YM ";
        $query .= "     ) ";

        return $query;
    }

    function updateZanMonth($zanMonth, $paidMonth, $schregNo) {
        $zanMonth = $zanMonth ? $zanMonth : 0;
        $paidMonth = $paidMonth ? $paidMonth : 0;
        $setMonth = $zanMonth > $paidMonth ? $zanMonth - $paidMonth : 0;

        $data = array();
        $data["REMAIN_SUP_LIMIT_MONTH"][TEXT]   = $setMonth;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO = '{$schregNo}' ";
        $where .= "     AND DATA_DIV = '1' ";
        $where .= "     AND DATA_DIV_SUB = '1' ";

        $query = Query::updateSQL($data, "REDUCTION_AUTHORIZE_DAT", $where);

        return $query;
    }

    function updateKasan($db, $schregNo) {

        $query = "DELETE FROM REDUCTION_AUTHORIZE_DAT WHERE SCHREGNO = '{$schregNo}' AND DATA_DIV = '2' AND DATA_DIV_SUB = '1'";
        $db->query($query);

        $data = array();
        $data["DATA_DIV_SUB"][TEXT] = "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO = '{$schregNo}' ";
        $where .= "     AND DATA_DIV = '2' ";
        $where .= "     AND DATA_DIV_SUB = '2' ";

        $query = Query::updateSQL($data, "REDUCTION_AUTHORIZE_DAT", $where);

        $db->query($query);
    }

    function getCounryDat($schregNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    function updateReducDat($studentInfo) {

        $data = array();
        if ($studentInfo["REDUCD_SCHREGNO"]) {
            $data["REDUC_INCOME_1"][NUMBER]         = $studentInfo["LAST_REDUCD_REDUC_INCOME_2"];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][NUMBER]                = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' ";
            $where .= "     AND SCHREGNO = '{$studentInfo["SCHREGNO"]}' ";

            $query = Query::updateSQL($data, "REDUCTION_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                     = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]                 = $studentInfo["SCHREGNO"];
            $data["REDUC_INCOME_1"][NUMBER]         = $studentInfo["LAST_REDUCD_REDUC_INCOME_2"];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][NUMBER]                = "SYSDATE()";

            $query = Query::insertSQL($data, "REDUCTION_DAT");
        }

        return $query;
    }

    function updateCounryDat($db, $counryDat, $studentInfo) {

        $setDecMoney = $studentInfo["LAST_MONEY"] ? $studentInfo["LAST_MONEY"] : NULL;
        $setDecFlg = $studentInfo["LAST_REDUC_DEC_FLG"] ? $studentInfo["LAST_REDUC_DEC_FLG"] : NULL;

        $setMoney = strlen($studentInfo["LAST_ADD_MONEY"]) > 0 ? $studentInfo["LAST_ADD_MONEY"] : NULL;
        $setIncome = strlen($studentInfo["LAST_INCOME"]) > 0 ? $studentInfo["LAST_INCOME"] : NULL;

        $query = knjp186kQuery::getPlanCnt($studentInfo["SCHREGNO"]);
        $planCnt = $db->getOne($query);
        $data = array();
        if ($counryDat > 0) {
            $data["REDUC_RARE_CASE_CD_1"][TEXT]     = $studentInfo["LAST_RARE_CASE"];
            $data["REDUCTIONMONEY_1"][NUMBER]       = $setDecMoney;
            $data["REDUC_DEC_FLG_1"][TEXT]          = $setDecFlg;

            $data["REDUCTION_ADD_MONEY_1"][NUMBER]  = $setMoney;
            $data["REDUC_INCOME_1"][NUMBER]         = $setIncome;
            if ($planCnt == 0) {
                $data["REDUC_ADD_FLG_1"][TEXT]          = NULL;
            }
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][NUMBER]                = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' ";
            $where .= "     AND SCHREGNO = '{$studentInfo["SCHREGNO"]}' ";

            $query = Query::updateSQL($data, "REDUCTION_COUNTRY_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                     = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]                 = $studentInfo["SCHREGNO"];
            $data["GRADE"][TEXT]                    = $studentInfo["GRADE"];
            $data["REDUC_RARE_CASE_CD_1"][TEXT]     = $studentInfo["LAST_RARE_CASE"];
            $data["REDUCTIONMONEY_1"][NUMBER]       = $setDecMoney;
            $data["REDUC_DEC_FLG_1"][TEXT]          = $setDecFlg;
            $data["REDUCTION_ADD_MONEY_1"][NUMBER]  = $setMoney;
            $data["REDUC_INCOME_1"][NUMBER]         = $setIncome;
            if ($planCnt == 0) {
                $data["REDUC_ADD_FLG_1"][TEXT]          = NULL;
            }
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][NUMBER]                = "SYSDATE()";

            $query = Query::insertSQL($data, "REDUCTION_COUNTRY_DAT");
        }

        return $query;
    }

    function getPlanCnt($schregno) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

}
?>