<?php

require_once('for_php7.php');
class knjp824query extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/

    //出力期間
    function getFromToMonth($model) {
    
        $query  = " WITH MONTH(LABEL, VALUE) AS ( ";
        $sep = "";
        for ($i = 1; $i <= 12; $i++) {
            $monthLabel = sprintf("%02d", $i);
            $yearVal    = ($i < 4) ? CTRL_YEAR + 1 : CTRL_YEAR;
            $monthVal   = $yearVal ."-". sprintf("%02d", $i);
            $query .= " {$sep} ";
            $query .= " VALUES( '".$monthLabel."', '".$monthVal."') ";
            $sep = "UNION";
        }
        $query .= " ) ";
        $query .= "  ";
        $query .= " SELECT LABEL, VALUE FROM MONTH ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //ヘッダー情報取得
    function getCollectSchoolBankMst() {
        $query  = " SELECT ";
        $query .= "     T1.ACCOUNTNAME_KANA, ";
        $query .= "     T1.ACCOUNT_CD, ";
        $query .= "     T1.BANK_CD, ";
        $query .= "     T1.BANKNAME_KANA, ";
        $query .= "     T1.BRANCHCD, ";
        $query .= "     T1.BRANCHNAME_KANA, ";
        $query .= "     case ";
        $query .= "         when T1.DEPOSIT_TYPE = '1' then '普通' ";
        $query .= "         else '当座' ";
        $query .= "     end as DEPOSIT_TYPE, ";
        $query .= "     T1.ACCOUNTNO, ";
        $query .= "     T2.BANKNAME, ";
        $query .= "     T2.BRANCHNAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST T1 ";
        $query .= "     LEFT JOIN BANK_MST T2 ON T2.BANKCD   = T1.BANK_CD ";
        $query .= "                          AND T2.BRANCHCD = T1.BRANCHCD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND T1.SCHOOL_KIND = '99' ";   //共通
        $query .= "     AND T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND T1.FORMAT_DIV  = '1' ";    // 1:引落 2:返金
        $query .= "     AND T1.SEQ         = '001' ";  // 固定

        return $query;
    }

    //UPDATE
    function &getUpdatePaidPlanQuery($db, &$model, $updDiv = '') {
        $query = knjp824query::getPaidPlan($model, $updDiv);

        $result = $db->query($query);
        $model->csvData = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $customerHead = substr($row["CUSTOMER_NUMBER_HEAD"], 0, 10);
            $row["CUSTOMER_NUMBER"] = str_pad($customerHead.$row["CUSTOMER_SCHREG"], 20, 0, STR_PAD_RIGHT);

            if (is_array($model->csvData[$row["SCHREGNO"]])) {
                $model->csvData[$row["SCHREGNO"]]["DECISION_MONEY"] += $row["DECISION_MONEY"];
            } else {
                $model->csvData[$row["SCHREGNO"]] = $row;
            }
        }
        $result->free();
    }

    //振込日
    function getPaidPlan($model) {

        $query  = " WITH RPLAN_BASE AS ( ";
        $query .= " 	SELECT ";
        $query .= " 	  SCHOOLCD, ";
        $query .= " 	  SCHOOL_KIND, ";
        $query .= " 	  YEAR, ";
        $query .= " 	  SLIP_NO, ";
        $query .= " 	  PLAN_YEAR, ";
        $query .= " 	  PLAN_MONTH, ";
        $query .= " 	  SCHREGNO, ";
        $query .= " 	  VALUE(DECISION_MONEY, 0) + VALUE(ADD_DECISION_MONEY, 0) AS DECISION_MONEY ";
        $query .= " 	FROM ";
        $query .= " 	  REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " 	UNION ";
        $query .= " 	SELECT ";
        $query .= " 	  SCHOOLCD, ";
        $query .= " 	  SCHOOL_KIND, ";
        $query .= " 	  YEAR, ";
        $query .= " 	  SLIP_NO, ";
        $query .= " 	  PLAN_YEAR, ";
        $query .= " 	  PLAN_MONTH, ";
        $query .= " 	  SCHREGNO, ";
        $query .= " 	  VALUE(DECISION_MONEY, 0) AS DECISION_MONEY ";
        $query .= " 	FROM ";
        $query .= " 	  REDUCTION_PLAN_DAT ";
        $query .= " 	UNION ";
        $query .= " 	SELECT ";
        $query .= " 	  SCHOOLCD, ";
        $query .= " 	  SCHOOL_KIND, ";
        $query .= " 	  YEAR, ";
        $query .= " 	  SLIP_NO, ";
        $query .= " 	  PLAN_YEAR, ";
        $query .= " 	  PLAN_MONTH, ";
        $query .= " 	  SCHREGNO, ";
        $query .= " 	  VALUE(BURDEN_CHARGE, 0) AS DECISION_MONEY ";
        $query .= " 	FROM ";
        $query .= " 	  REDUCTION_BURDEN_CHARGE_PLAN_DAT ";
        $query .= " ), RPLAN_DATA AS ( ";
        $query .= " 	SELECT ";
        $query .= " 	  SCHOOLCD, ";
        $query .= " 	  SCHOOL_KIND, ";
        $query .= " 	  YEAR, ";
        $query .= " 	  SLIP_NO, ";
        $query .= " 	  PLAN_YEAR, ";
        $query .= " 	  PLAN_MONTH, ";
        $query .= " 	  SCHREGNO, ";
        $query .= " 	  DECISION_MONEY ";
        $query .= " 	FROM ";
        $query .= " 	  RPLAN_BASE ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "         AND DATE(PLAN_YEAR || '-' || PLAN_MONTH || '-' || '01') ";
        $query .= "             BETWEEN DATE('".$model->field["FROM_MONTH"]."' || '-' || '01') AND LAST_DAY('".$model->field["TO_MONTH"]."' || '-' || '01') ";
        $query .= "         AND DECISION_MONEY > 0 ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     RPLAN.*, ";
        $query .= "     VALUE(CUSTOMER.CUSTOMER_NUMBER, '0000000000') AS CUSTOMER_NUMBER_HEAD, ";
        $query .= "     CASE WHEN RPLAN.SCHOOL_KIND = 'J' THEN MAPPING.SCHREGNO_J ";
        $query .= "          WHEN RPLAN.SCHOOL_KIND = 'H' THEN MAPPING.SCHREGNO_H ";
        $query .= "          ELSE '' ";
        $query .= "     END AS CUSTOMER_SCHREG, ";
        $query .= "     RBANK.BANKCD, ";
        $query .= "     BANK.BANKNAME, ";
        $query .= "     BANK.BANKNAME_KANA, ";
        $query .= "     RBANK.BRANCHCD, ";
        $query .= "     BANK.BRANCHNAME, ";
        $query .= "     BANK.BRANCHNAME_KANA, ";
        $query .= "     CASE ";
        $query .= "         WHEN RBANK.DEPOSIT_ITEM = '1' THEN '普通' ";
        $query .= "         ELSE '当座' ";
        $query .= "     END AS DEPOSIT_TYPE, ";
        $query .= "     RBANK.ACCOUNTNO, ";
        $query .= "     RBANK.ACCOUNTNAME, ";
        $query .= "     RBANK.RELATIONSHIP, ";
        $query .= "     RBANK.PAID_INFO_CD ";
        $query .= " FROM ";
        $query .="      RPLAN_DATA RPLAN ";
        $query .="      LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT MAPPING ";
        $query .="        ON RPLAN.SCHREGNO = MAPPING.SCHREGNO ";
        $query .= "     LEFT JOIN REGISTBANK_DAT RBANK ON RPLAN.SCHOOLCD = RBANK.SCHOOLCD ";
        $query .= "          AND RPLAN.SCHREGNO = RBANK.SCHREGNO ";
        $query .= "          AND RBANK.SEQ = '1' ";
        $query .= "     LEFT JOIN BANK_MST BANK ON RBANK.BANKCD = BANK.BANKCD ";
        $query .= "          AND RBANK.BRANCHCD = BANK.BRANCHCD ";
        $query .= "     LEFT JOIN COLLECT_SCHREG_CUSTOMER_DAT CUSTOMER ON RPLAN.SCHREGNO = CUSTOMER.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     RPLAN.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND RPLAN.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     RPLAN.SCHOOLCD, ";
        $query .= "     RPLAN.SCHOOL_KIND, ";
        $query .= "     RPLAN.YEAR, ";
        $query .= "     RPLAN.SLIP_NO, ";
        $query .= "     RPLAN.PLAN_YEAR, ";
        $query .= "     RPLAN.PLAN_MONTH ";

        return $query;
    }



}
?>
