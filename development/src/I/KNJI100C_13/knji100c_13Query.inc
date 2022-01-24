<?php

require_once('for_php7.php');

class knji100c_13Query extends Query {

    //CSVヘッダ
    function selectDueTitleQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.EXPENSE_L_CD, ";
        $query .= "     T1.EXPENSE_M_CD, ";
        $query .= "     T4.EXPENSE_M_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         WT1.YEAR, ";
        $query .= "         WT2.EXPENSE_L_CD, ";
        $query .= "         WT1.EXPENSE_M_CD ";
        $query .= "      FROM ";
        $query .= "         MONEY_DUE_M_DAT WT1, ";
        $query .= "         EXPENSE_M_MST WT2 ";
        $query .= "      WHERE ";
        $query .= "         WT1.YEAR = WT2.YEAR ";
        $query .= "         AND WT1.EXPENSE_M_CD = WT2.EXPENSE_M_CD ";
        $query .= "         AND WT1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= "      UNION ";
        $query .= "      SELECT ";
        $query .= "         YEAR, ";
        $query .= "         EXPENSE_L_CD, ";
        $query .= "         EXPENSE_M_CD ";
        $query .= "      FROM ";
        $query .= "         MONEY_PAID_M_DAT ";
        $query .= "      WHERE ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= "     ) T1 ";
        $query .= " LEFT OUTER JOIN EXPENSE_M_MST T4 ON T1.YEAR = T4.YEAR ";
        $query .= "      AND T1.EXPENSE_M_CD = T4.EXPENSE_M_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.EXPENSE_L_CD, ";
        $query .= "     T1.EXPENSE_M_CD ";

        return $query;
    }

    //CSVヘッダ
    function selectReducTitleQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //CSVヘッダ
    function selectApplicationTitleQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.APPLICATIONCD ";
        $query .= " FROM ";
        $query .= "     APPLICATION_DAT T1, ";
        $query .= "     APPLICATION_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.APPLICATIONCD = T2.APPLICATIONCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.APPLICATIONCD ";

        return $query;
    }

    //費目CSV
    function getDueCsvQuery($model, $year, $schregno, $lcd, $mcd, $headFlg)
    {
        $query  = " SELECT ";
        if ($headFlg) {
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO, ";
            if ($model->output == OUT_CODE_NAME) {       //コード＋名称
                $query .= "  BASE.SEX, ";
                $query .= "  Z002.NAME1,";
            } else if ($model->output == OUT_CODE_ONLY) { //コードのみ
                $query .= "  BASE.SEX, ";
            } else if ($model->output == OUT_NAME_ONLY) { //名称のみ
                $query .= "  Z002.NAME1,";
            }
        }
        $query .= "     T5.ACCOUNTNO, ";
        $query .= "     T1.EXPENSE_L_CD || T1.EXPENSE_M_CD AS LMCD, ";
        $query .= "     T4.EXPENSE_M_NAME, ";
        $query .= "     T4.EXPENSE_S_EXIST_FLG, ";
        $query .= "     T2.MONEY_DUE, ";
        $query .= "     T3.PAID_MONEY, ";
        $query .= "     T3.PAID_MONEY_DATE, ";
        $query .= "     T3.PAID_MONEY_DIV, ";
        $query .= "     T3.REPAY_DEV, ";
        $query .= "     T3.REPAY_MONEY, ";
        $query .= "     T3.REPAY_DATE, ";
        $query .= "     T3.PAID_INPUT_FLG, ";
        $query .= "     T2.INST_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN (SELECT wt1.year, wt1.schregno, wt2.expense_l_cd, wt1.expense_m_cd ";
        $query .= "        FROM money_due_m_dat wt1, ";
        $query .= "             expense_m_mst wt2 ";
        $query .= "       WHERE wt1.year            = wt2.year ";
        $query .= "         AND wt1.expense_m_cd    = wt2.expense_m_cd ";
        $query .= "         AND wt1.year            = '".$year."' ";
        $query .= "         AND wt1.schregno        = '".$schregno."' ";
        $query .= "         AND wt2.expense_l_cd    = '".$lcd."' ";
        $query .= "         AND wt1.expense_m_cd    = '".$mcd."' ";
        $query .= "       UNION ";
        $query .= "      SELECT year,schregno,expense_l_cd,expense_m_cd ";
        $query .= "        FROM money_paid_m_dat ";
        $query .= "       WHERE year        = '".$year."' ";
        $query .= "         AND schregno    = '".$schregNo."' ";
        $query .= "     ) T1 ON REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT OUTER JOIN money_due_m_dat T2 ON T1.year = T2.year ";
        $query .= "          AND T1.schregno = T2.schregno ";
        $query .= "          AND T1.expense_m_cd = T2.expense_m_cd ";
        $query .= "     LEFT OUTER JOIN money_paid_m_dat T3 ON T1.year = T3.year ";
        $query .= "          AND T1.schregno     = T3.schregno ";
        $query .= "          AND T1.expense_l_cd = T3.expense_l_cd ";
        $query .= "          AND T1.expense_m_cd = T3.expense_m_cd ";
        $query .= "     LEFT OUTER JOIN expense_m_mst T4 ON T1.year = T4.year ";
        $query .= "          AND T1.expense_m_cd = T4.expense_m_cd ";
        $query .= "     LEFT JOIN REGISTBANK_DAT T5 ON REGD.SCHREGNO = T5.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.expense_m_cd ";

        return $query;
    }

    //入金必要金額の取得
    function getMoneyDue($schregno, $m_cd)
    {
        $query  = " SELECT ";
        $query .= "     SUM(MONEY_DUE) AS MONEY_DUE ";
        $query .= "   FROM ";
        $query .= "     money_due_s_dat ";
        $query .= "  WHERE ";
        $query .= "     year = '".CTRL_YEAR."' AND ";
        $query .= "     schregno = '".$schregno."' AND ";
        $query .= "     expense_m_cd = '".$m_cd."'";

        return $query;
    }

    //小分類返金区分の取得
    function getRepayDiv($schregno, $m_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     MIN(T1.REPAY_MONEY_DIV) AS MIN_DIV, ";
        $query .= "     MAX(T1.REPAY_MONEY_DIV) AS MAX_DIV ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND T1.EXPENSE_M_CD = '".$m_cd."' ";
        $query .= "     AND T1.REPAY_MONEY_DIV IS NOT NULL ";

        return $query;
    }

    //名称マスタより取得
    function getNamecd($year,$cd)
    {
        return " SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = '".$cd."' ";
    }

    //軽減CSV
    function getReducCsvQuery($model, $year, $schregno) {
        $query  = " SELECT ";
        $query .= "     VALUE(REDUCTIONMONEY_1, 0) + VALUE(REDUCTIONMONEY_2, 0) AS REDUCTIONMONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //支援金CSV
    function getReducCountryCsvQuery($model, $year, $schregno) {
        $query  = " SELECT ";
        $query .= "     CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "          THEN VALUE(T1.REDUCTIONMONEY_1, 0) ";
        $query .= "          ELSE VALUE(T1.REDUCTIONMONEY_1, 0) * 3 ";
        $query .= "     END ";
        $query .= "     + ";
        $query .= "     CASE WHEN RARE.NAMESPARE1 like '1:%' ";
        $query .= "          THEN VALUE(T1.REDUCTION_ADD_MONEY_1, 0) ";
        $query .= "          ELSE VALUE(T1.REDUCTION_ADD_MONEY_1, 0) * 3 ";
        $query .= "     END ";
        $query .= "     + ";
        $query .= "     CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "          THEN VALUE(T1.REDUCTIONMONEY_2, 0) ";
        $query .= "          ELSE VALUE(T1.REDUCTIONMONEY_2, 0) * 9 ";
        $query .= "     END ";
        $query .= "     + ";
        $query .= "     CASE WHEN RARE2.NAMESPARE1 like '1:%' ";
        $query .= "          THEN VALUE(T1.REDUCTION_ADD_MONEY_2, 0) ";
        $query .= "          ELSE VALUE(T1.REDUCTION_ADD_MONEY_2, 0) * 9 ";
        $query .= "     END AS REDUCTIONMONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST RARE ON RARE.NAMECD1 = 'G216' ";
        $query .= "          AND RARE.NAMECD2 = T1.REDUC_RARE_CASE_CD_1 ";
        $query .= "     LEFT JOIN NAME_MST RARE2 ON RARE2.NAMECD1 = 'G216' ";
        $query .= "          AND RARE2.NAMECD2 = T1.REDUC_RARE_CASE_CD_2 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //申し込みCSV
    function getApplicationCsvQuery($model, $year, $schregno, $appCd)
    {
        $query  = "SELECT ";
        $query .= "     T2.APPLICATIONNAME, ";
        $query .= "     T1.APPLI_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     application_dat T1, ";
        $query .= "     application_mst T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.APPLICATIONCD = '".$appCd."' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.APPLICATIONCD = T2.APPLICATIONCD ";

        return $query;
    }

    //学年
    function getGrade() {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT ";
            $query .= "    GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "    AND GRADE = '". $model->grade ."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT ";
            $query .= "    GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "    AND GRADE = '". $model->grade ."' ";
            $query .= "    AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model)
    {
        $date = str_replace("/","-",$model->date);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
                           //卒業(1)・転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    (S1.GRD_DIV IN('1','2','3') AND S1.GRD_DATE < ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト右
    function getStudent_right($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }


    //リストtoリスト左
    function getStudent_left($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //サブシステム名取得
    function getSubSystem() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
}
?>