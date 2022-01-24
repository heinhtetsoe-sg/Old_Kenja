<?php

require_once('for_php7.php');

class knjp120kQuery2 extends Query {

    //リストの取得
    function getList(&$model)
    {

        $query  = "";
        $query .= "  SELECT ";
        $query .= "      T1.schregno,";
        $query .= "      T1.transfer_sdate,";
        $query .= "      T1.transfer_edate,";
        $query .= "      T1.transfercd,";
        $query .= "      MEISYOU_GET(T1.transfercd,'A004',1) AS transfername,";
        $query .= "      T1.transferreason,";
        $query .= "      T1.transferplace,";
        $query .= "      T1.transferaddr,";
        $query .= "      T1.abroad_classdays,";
        $query .= "      T1.abroad_credits";
        $query .= "  FROM";
        $query .= "      schreg_transfer_dat T1";
        $query .= "  WHERE";
        $query .= "      T1.schregno = '".$model->schregno."'";
		//NO001-->
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      T1.SCHREGNO,";
        $query .= "      T1.GRD_DATE AS transfer_sdate,";
        $query .= "      CAST(NULL AS DATE) AS transfer_edate,";
        $query .= "      T1.GRD_DIV AS transfercd,";
        $query .= "      MEISYOU_GET(T1.GRD_DIV,'A003',1) AS transfername, ";
        $query .= "      T1.GRD_REASON AS transferreason,";
        $query .= "      T1.GRD_SCHOOL AS transferplace,";
        $query .= "      T1.GRD_ADDR AS transferaddr,";
        $query .= "      CAST(NULL AS SMALLINT) AS abroad_classdays,";
        $query .= "      CAST(NULL AS SMALLINT) AS abroad_credits";
        $query .= "  FROM";
        $query .= "      SCHREG_BASE_MST T1";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "      AND (T1.GRD_DIV IS NOT NULL OR T1.GRD_DIV != '') ";
        $query .= "      AND T1.GRD_DIV > '1' ";
		//NO001<--
		//NO005-->
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      T1.SCHREGNO,";
        $query .= "      T1.ENT_DATE AS transfer_sdate,";
        $query .= "      CAST(NULL AS DATE) AS transfer_edate,";
        $query .= "      T1.ENT_DIV AS transfercd,";
        $query .= "      MEISYOU_GET(T1.ENT_DIV,'A002',1) AS transfername, ";
        $query .= "      T1.ENT_REASON AS transferreason,";
        $query .= "      T1.ENT_SCHOOL AS transferplace,";
        $query .= "      T1.ENT_ADDR AS transferaddr,";
        $query .= "      CAST(NULL AS SMALLINT) AS abroad_classdays,";
        $query .= "      CAST(NULL AS SMALLINT) AS abroad_credits";
        $query .= "  FROM";
        $query .= "      SCHREG_BASE_MST T1";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "      AND (T1.ENT_DIV IS NOT NULL OR T1.ENT_DIV != '') ";
        $query .= "      AND T1.ENT_DIV > '3' ";
		//NO005<--
        $query .= "  ORDER BY";
        $query .= "      transfer_sdate,";
        $query .= "      transfercd";

        return $query;
    }
    //生徒交付データよりデータを取得 NO004
    function getlist2($schregno)
    {
        $query  = " SELECT ";
        $query .= "     year, ";
        $query .= "     schregno, ";
        $query .= "     grantcd, ";
        $query .= "     meisyou_get(grantcd,'G212',1) grantname, ";
        $query .= "     grantsdate, ";
        $query .= "     grantedate, ";
        $query .= "     grant_money, ";
        $query .= "     remark ";
        $query .= " FROM ";
        $query .= "     schreg_grant_dat ";
        $query .= " WHERE ";
#        $query .= "     year     = '".CTRL_YEAR."' AND ";
        $query .= "     schregno = '".$schregno."' ";
        $query .= " ORDER BY grantcd ";
        return $query;
    }
    //リストの取得
    function getList3(&$model)
    {
        $query  = "SELECT ";
        $query .= "     T2.schregno, ";
        $query .= "     T1.applicationcd, ";
        $query .= "     T1.applicationname, ";
        $query .= "     T2.applied_date, ";
        $query .= "     T2.appli_money_due, ";
        $query .= "     MEISYOU_GET(T2.appli_paid_div, 'G205', 1) AS appli_paid_div, ";
        $query .= "     T2.appli_paid_money, ";
        $query .= "     T2.appli_paid_date ";
        $query .= " FROM ";
        $query .= "     application_mst T1, ";
        $query .= "     application_dat T2 ";
        $query .= " WHERE ";
        $query .= "     T1.year = T2.year AND ";
        $query .= "     T1.applicationcd = T2.applicationcd AND ";
        $query .= "     T1.year = '".$model->year."' AND ";
        $query .= "     T2.schregno = '".$model->schregno."'";
        $query .= " ORDER BY ";
        $query .= "     T1.applicationcd ";

        return $query;
    }
    //１レコード取得
    function getRow(&$model)
    {
        $query = " SELECT ";
        $query .= "     T1.BANKCD, ";
        $query .= "     T2.BANKNAME, ";
        $query .= "     T2.BANKNAME_KANA, ";
        $query .= "     T1.BRANCHCD, ";
        $query .= "     T2.BRANCHNAME, ";
        $query .= "     T2.BRANCHNAME_KANA, ";
        $query .= "     T1.DEPOSIT_ITEM, ";
        $query .= "     MEISYOU_GET(T1.DEPOSIT_ITEM,'G203',1) AS DEPOSIT_ITEM2, ";
        $query .= "     T1.ACCOUNTNO, ";
        $query .= "     T1.ACCOUNTNAME, ";
        $query .= "     T1.RELATIONSHIP, ";
        $query .= "     MEISYOU_GET(T1.RELATIONSHIP,'H201',1) AS RELATIONSHIP2 ";
        $query .= " FROM ";
        $query .= "     REGISTBANK_DAT T1 ";
        $query .= "     LEFT JOIN BANK_MST T2 ON T1.BANKCD = T2.BANKCD ";
        $query .= "          AND T1.BRANCHCD = T2.BRANCHCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }
    function getRow2(&$model)
    {
        /* 2005/10/26 修正 */

        $query = " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.NAME_SHOW, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.GUARD_NAME, ";
        $query .= "   T2.PREF, ";
        $query .= "   T3.PREFECTURESCD, ";
        $query .= "   T3.REDUC_INCOME_1, ";
        $query .= "   T3.REDUCTIONMONEY_1, ";
        $query .= "   T3.REDUCTIONMONEY_2, ";
        $query .= "   T3.REDUC_RARE_CASE_CD, ";
        $query .= "   T3.REDUC_DEC_FLG_1, ";
        $query .= "   T3.PREF AS PREF2, ";
        $query .= "   T3.REDUC_REMARK, ";
        $query .= "   T4.GRANTCD, ";
        $query .= "   T5.NAMESPARE1 as RARE_CASE_NAME, ";
        $query .= "   T6.COUNTTRANSFER ";
        $query .= " FROM ";
        $query .= "   (SELECT ";
        $query .= "     ST2.YEAR, ";
        $query .= "     ST2.SEMESTER, ";
        $query .= "     ST1.SCHREGNO, ";
        $query .= "     ST1.NAME_SHOW, ";
        $query .= "     ST2.GRADE, ";
        $query .= "     ST2.HR_CLASS, ";
        $query .= "     ST2.ATTENDNO, ";
        $query .= "     ST3.HR_NAMEABBV ";
        $query .= "   FROM ";
        $query .= "     SCHREG_BASE_MST ST1, ";
        $query .= "     SCHREG_REGD_DAT ST2 LEFT OUTER JOIN SCHREG_REGD_HDAT ST3  ";
        $query .= "       ON ST2.YEAR = ST3.YEAR AND  ";
        $query .= "       ST2.SEMESTER = ST3.SEMESTER AND  ";
        $query .= "       ST2.GRADE = ST3.GRADE AND  ";
        $query .= "       ST2.HR_CLASS = ST3.HR_CLASS ";
        $query .= "   WHERE ";
        $query .= "     ST1.SCHREGNO = ST2.SCHREGNO ";
        $query .= "   ) T1 LEFT OUTER JOIN (SELECT DISTINCT ";
        $query .= "                           ST1.SCHREGNO, ";
#        $query .= "                           ST1.GUARD_NAME, ";           #2005/11/28 保証人・学資負担者へ変更
        $query .= "                           ST1.GUARANTOR_NAME AS GUARD_NAME, ";
        $query .= "                           SUBSTR(ST2.CITYCD,1,2) AS PREFECTURESCD, ";
        $query .= "                           MEISYOU_GET(SUBSTR(ST2.CITYCD,1,2),'G202',1) AS PREF ";
        $query .= "                         FROM ";
        $query .= "                           GUARDIAN_DAT ST1 LEFT OUTER JOIN ZIPCD_MST ST2  ";
#        $query .= "                           ON ST1.GUARD_ZIPCD = ST2.NEW_ZIPCD ";        #2005/11/28 保証人・学資負担者へ変更
        $query .= "                           ON ST1.GUARANTOR_ZIPCD = ST2.NEW_ZIPCD ";
        $query .= "                         ) T2 ON T1.SCHREGNO = T2.SCHREGNO  ";

        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "             ST1.YEAR, ";
        $query .= "             ST1.SCHREGNO, ";
        $query .= "             ST1.PREFECTURESCD, ";
        $query .= "             MEISYOU_GET(ST1.PREFECTURESCD, 'G202', 1) AS PREF, ";
        $query .= "             ST1.REDUC_INCOME_1, ";
        $query .= "             ST1.REDUCTIONMONEY_1, ";
        $query .= "             ST1.REDUCTIONMONEY_2, ";
        $query .= "             ST1.REDUC_DEC_FLG_1, ";
        $query .= "             ST1.REDUC_RARE_CASE_CD, ";
        $query .= "             ST1.REDUC_REMARK ";
        $query .= "           FROM ";
        $query .= "             REDUCTION_DAT ST1 ";
        $query .= "           ) T3 ON T1.YEAR = T3.YEAR AND T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "       LEFT OUTER JOIN (SELECT MAX(GRANTCD) AS GRANTCD, ";
        $query .= "             YEAR, ";
        $query .= "             SCHREGNO  ";
        $query .= "           FROM  ";
        $query .= "             SCHREG_GRANT_DAT ST1  ";
        $query .= "           GROUP BY YEAR,SCHREGNO) T4  ";
        $query .= "             ON T1.YEAR = T4.YEAR AND  ";
        $query .= "             T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "       LEFT OUTER JOIN NAME_MST T5  ";
        $query .= "             ON T3.REDUC_RARE_CASE_CD = T5.NAMECD2 AND ";
        $query .= "             T5.NAMECD1 = 'G211' ";
        $query .= " LEFT OUTER JOIN ( ";
        $query .= " SELECT COUNT(*) AS COUNTTRANSFER, SCHREGNO FROM SCHREG_TRANSFER_DAT GROUP BY SCHREGNO ";
        $query .= " ) T6 ON T1.SCHREGNO = T6.SCHREGNO ";


        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "   T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "   T1.schregno = '".$model->schregno."' ";

        return $query;
    }
    //中分類リストの取得(小分類がある中分類の金額は小分類の合計金額)
    function getList4($schregno)
    {
        $query .= "SELECT T1.bank_trans_sdate ";
        $query .= "      ,T1.expense_m_cd ";
        $query .= "      ,T1.expense_l_cd ";
        $query .= "      ,T1.expense_m_name ";
        $query .= "      ,CASE WHEN T1.expense_s_exist_flg = '1' THEN T3.money_due ELSE T2.money_due END AS money_due ";
        $query .= "      ,MEISYOU_GET(T2.reduction_reason, 'G204', 1) AS reduction_reason ";
        $query .= "  FROM expense_m_mst T1 ";
        $query .= "      ,money_due_m_dat T2 ";
        $query .= "       LEFT OUTER JOIN ";
        $query .= "       (SELECT expense_m_cd, SUM(money_due) AS money_due ";
        $query .= "          FROM money_due_s_dat ";
        $query .= "         WHERE year         = '".CTRL_YEAR."' ";
        $query .= "           AND schregno     = '".$schregno."' ";
        $query .= "         GROUP BY expense_m_cd) T3 ";
        $query .= "      ON T2.expense_m_cd = T3.expense_m_cd ";
        $query .= " WHERE T1.year         = T2.year ";
        $query .= "   AND T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "   AND T1.year         = '".CTRL_YEAR."' ";
        $query .= "   AND T2.schregno     = '".$schregno."' ";
        return $query;
    }

    //小分類リストの取得
    function getList5($schregno)
    {
        $query .= "SELECT T3.expense_l_cd ";
        $query .= "      ,T3.expense_m_cd ";
        $query .= "      ,T2.expense_s_cd ";
        $query .= "      ,T2.expense_s_name ";
        $query .= "      ,T1.money_due ";
        $query .= "      ,T3.expense_m_name ";
        $query .= "  FROM money_due_s_dat T1 ";
        $query .= "      ,v_expense_s_mst T2 ";
        $query .= "      ,expense_m_mst T3 ";
        $query .= " WHERE T1.year         = T2.year ";
        $query .= "   AND T1.year         = T3.year ";
        $query .= "   AND T1.expense_s_cd = T2.expense_s_cd ";
        $query .= "   AND T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "   AND T2.expense_m_cd = T3.expense_m_cd ";
        $query .= "   AND T1.year         = '".CTRL_YEAR."'";
        $query .= "   AND T1.schregno     = '".$schregno."'";
        return $query;
    }
    //入金予定データ
    function getList6($schregno)
    {
        $query .= "SELECT T2.inst_cd";
        $query .= "      ,T3.expense_m_cd ";
        $query .= "      ,T3.expense_m_name ";
        $query .= "      ,T3.expense_s_exist_flg ";
        $query .= "      ,T2.money_due ";
        $query .= "      ,SUM(T1.paid_money)      AS paid_money ";
        $query .= "      ,SUM(T1.repay_money)     AS repay_money ";
        $query .= "      ,MAX(T1.paid_money_date) AS paid_money_date ";
        $query .= "  FROM money_due_m_dat T2 ";
        $query .= "       INNER JOIN expense_m_mst T3 ";
        $query .= "               ON T2.year         = T3.year ";
        $query .= "              AND T2.expense_m_cd = T3.expense_m_cd ";
        $query .= "       LEFT OUTER JOIN ";
        $query .= "       installment_dat T1 ";
        $query .= "    ON T1.year         = T2.year ";
        $query .= "   AND T1.schregno     = T2.schregno ";
        $query .= "   AND T1.inst_cd      = T2.inst_cd ";
        $query .= " WHERE T2.schregno     = '".$schregno."'";
        $query .= "   AND T2.year         = '".CTRL_YEAR."'";
        $query .= "   AND COALESCE(T2.inst_cd, '') <> ''";  //INST_CDがNULLまたは空じゃないもの
        $query .= " GROUP BY T2.inst_cd ";
        $query .= "         ,T3.expense_m_cd,T3.expense_m_name ";
        $query .= "         ,T2.money_due,T3.expense_s_exist_flg ";
        //echo $query;
        return $query;
    }
    //入金必要金額の取得
    function getMoneyDue($schregno, $m_cd)
    {
        $query  = " SELECT ";
        $query .= "     sum(money_due) ";
        $query .= "   FROM ";
        $query .= "     money_due_s_dat ";
        $query .= "  WHERE ";
        $query .= "     year = '".CTRL_YEAR."' AND ";
        $query .= "     schregno = '".$schregno."' AND ";
        $query .= "     expense_m_cd = '".$m_cd."'";

        return $query;
    }
}
?>
