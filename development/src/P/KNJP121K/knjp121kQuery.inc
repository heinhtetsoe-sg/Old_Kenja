<?php

require_once('for_php7.php');

class knjp121kQuery extends Query {

    //生徒の名前の取得
    function getStudentName($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     NAME_SHOW, ";
        $query .= "     NAME_KANA ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO ='".$schregno."' ";
        return $query;
    }

    //名称マスタより取得
    function getNamecd($year, $cd, $lavelNameFlg = false)
    {
        $query  = " SELECT ";
        if ($lavelNameFlg) {
            $query .= "     NAME1 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        }
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";

        return $query;
    }

    //リストの取得
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXPENSE_L_CD, ";
        $query .= "     T1.EXPENSE_M_CD, ";
        $query .= "     T4.EXPENSE_M_NAME, ";
        $query .= "     T4.BANK_TRANS_SDATE, ";
        $query .= "     T4.EXPENSE_S_EXIST_FLG, ";
        $query .= "     T2.MONEY_DUE, ";
        $query .= "     T3.PAID_MONEY, ";
        $query .= "     T3.PAID_MONEY_DATE, ";
        $query .= "     T3.PAID_MONEY_DIV, ";
        $query .= "     T3.REPAY_DEV, ";
        $query .= "     T3.REPAY_MONEY, ";
        $query .= "     T3.PAID_INPUT_FLG, ";
        $query .= "     T2.BANK_TRANS_STOP_RESON, ";
        $query .= "     T2.INST_CD, ";
        $query .= "     T3.REMARK ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         WT1.YEAR, ";
        $query .= "         WT1.SCHREGNO, ";
        $query .= "         WT2.EXPENSE_L_CD, ";
        $query .= "         WT1.EXPENSE_M_CD ";
        $query .= "     FROM ";
        $query .= "         MONEY_DUE_M_DAT WT1, ";
        $query .= "         EXPENSE_M_MST WT2 ";
        $query .= "     WHERE ";
        $query .= "         WT1.YEAR = WT2.YEAR ";
        $query .= "         AND WT1.EXPENSE_M_CD = WT2.EXPENSE_M_CD ";
        $query .= "         AND WT1.YEAR = '".$model->year."' ";
        $query .= "         AND WT1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     UNION    ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         EXPENSE_L_CD, ";
        $query .= "         EXPENSE_M_CD ";
        $query .= "     FROM ";
        $query .= "         MONEY_PAID_M_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$model->year."' ";
        $query .= "         AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     ) T1 ";
        $query .= "     LEFT OUTER JOIN MONEY_DUE_M_DAT T2 ON T1.YEAR = T2.YEAR ";
        $query .= "          AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "          AND T1.EXPENSE_M_CD = T2.EXPENSE_M_CD ";
        $query .= "     LEFT OUTER JOIN MONEY_PAID_M_DAT T3 ON T1.YEAR = T3.YEAR ";
        $query .= "          AND T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "          AND T1.EXPENSE_L_CD = T3.EXPENSE_L_CD ";
        $query .= "          AND T1.EXPENSE_M_CD = T3.EXPENSE_M_CD ";
        $query .= "     LEFT OUTER JOIN EXPENSE_M_MST T4 ON T1.YEAR = T4.YEAR ";
        $query .= "          AND T1.EXPENSE_M_CD = T4.EXPENSE_M_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXPENSE_M_CD ";

        return $query;
    }

    //入金必要金額の取得
    function getMoneyDueS($schregno, $m_cd)
    {
        $query  = " SELECT ";
        $query .= "     SUM(MONEY_DUE) ";
        $query .= "   FROM ";
        $query .= "     MONEY_DUE_S_DAT ";
        $query .= "  WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     EXPENSE_M_CD = '".$m_cd."'";

        return $query;
    }

    //名称取得
    function getName($year, $cd, $cd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        $query .= "     AND NAMECD2 = '".$cd2."' ";
        return $query;
    }

    //１レコード取得
    function getRow($model)
    {

        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     WT1.YEAR, ";
        $query .= "     WT1.SCHREGNO, ";
        $query .= "     WT2.EXPENSE_L_CD, ";
        $query .= "     WT1.EXPENSE_M_CD ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT WT1, ";
        $query .= "     EXPENSE_M_MST WT2 ";
        $query .= " WHERE ";
        $query .= "     WT1.YEAR = WT2.YEAR ";
        $query .= "     AND WT1.EXPENSE_M_CD = WT2.EXPENSE_M_CD ";
        $query .= "     AND WT1.YEAR = '".$model->year."' ";
        $query .= "     AND WT1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND WT2.EXPENSE_L_CD     = '".$model->exp_lcd."' ";
        $query .= "     AND WT1.EXPENSE_M_CD     = '".$model->exp_mcd."' ";
        $query .= " GROUP BY ";
        $query .= "     WT1.YEAR, ";
        $query .= "     WT1.SCHREGNO, ";
        $query .= "     WT2.EXPENSE_L_CD, ";
        $query .= "     WT1.EXPENSE_M_CD ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     EXPENSE_L_CD, ";
        $query .= "     EXPENSE_M_CD ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND EXPENSE_L_CD     = '".$model->exp_lcd."' ";
        $query .= "     AND EXPENSE_M_CD     = '".$model->exp_mcd."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     EXPENSE_L_CD, ";
        $query .= "     EXPENSE_M_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.PAID_MONEY, ";
        $query .= "     T1.PAID_MONEY_DATE, ";
        $query .= "     T1.PAID_MONEY_DIV, ";
        $query .= "     T1.REPAY_MONEY, ";
        $query .= "     T1.REPAY_DEV, ";
        $query .= "     T1.REPAY_DATE, ";
        $query .= "     T1.REMARK, ";
        $query .= "     T2.MONEY_DUE, ";
        $query .= "     T2.BANK_TRANS_STOP_RESON ";
        $query .= " FROM ";
        $query .= "     MAIN_T M1 ";
        $query .= "     LEFT JOIN MONEY_PAID_M_DAT T1 ON M1.YEAR = T1.YEAR ";
        $query .= "          AND M1.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND M1.EXPENSE_L_CD = T1.EXPENSE_L_CD ";
        $query .= "          AND M1.EXPENSE_M_CD = T1.EXPENSE_M_CD ";
        $query .= "          AND T1.PAID_INPUT_FLG   = '".$model->paid_input_flg."'";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT T2 ON M1.YEAR = T2.YEAR ";
        $query .= "          AND M1.SCHREGNO = T2.SCHREGNO ";
        $query .= "          AND M1.EXPENSE_M_CD = T2.EXPENSE_M_CD ";

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

    //データ存在チェック
    function ExistDueData($model, $schreNo="")
    {
        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' ";
        if ($model->cmd == "all_update") {
            $query .= "     AND SCHREGNO        = '".$schreNo."' ";
            $query .= "     AND EXPENSE_M_CD    = '".$model->field["EXPENSE_M_CD"]."'";
        } else {
            $query .= "     AND SCHREGNO        = '".$model->schregno."' ";
            $query .= "     AND EXPENSE_M_CD    = '".$model->exp_mcd."'";
        }

        return $query;
    }

    //追加
    function &getInsDueQuery($model, $schreNo="")
    {
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        if ($model->cmd == "all_update") {
            $data["SCHREGNO"][TEXT]         = $schreNo;
            $data["EXPENSE_M_CD"][TEXT]     = $model->field["EXPENSE_M_CD"];
            if (strlen($model->field["CHECKED1"])) {
                $data["MONEY_DUE"][NUMBER]      = $model->field["MONEY_DUE"];
            }
        } else {
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["EXPENSE_M_CD"][TEXT]     = $model->exp_mcd;
            $data["MONEY_DUE"][NUMBER]      = $model->field["MONEY_DUE"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "MONEY_DUE_M_DAT");
        return $query;
    }

    //更新
    function &getUpdDueQuery($model, $schreNo="")
    {
        $data = array();
        if ($model->cmd == "all_update") {
            if (strlen($model->field["CHECKED1"])) {
                $data["MONEY_DUE"][NUMBER]      = $model->field["MONEY_DUE"];
            }
        } else {
            $data["MONEY_DUE"][NUMBER]      = $model->field["MONEY_DUE"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE YEAR           = '".$model->year."'";
        if ($model->cmd == "all_update") {
            $where .= "   AND SCHREGNO       = '".$schreNo."'";
            $where .= "   AND EXPENSE_M_CD   = '".$model->field["EXPENSE_M_CD"]."'";
        } else {
            $where .= "   AND SCHREGNO       = '".$model->schregno."'";
            $where .= "   AND EXPENSE_M_CD   = '".$model->exp_mcd."'";
        }

        $query = Query::updateSQL($data, "MONEY_DUE_M_DAT", $where);

        return $query;
    }

    //データ存在チェック
    function ExistPayData($model, $schreNo="", $inputFlg="")
    {
        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' ";
        if ($model->cmd == "all_update") {
            $query .= "     AND SCHREGNO        = '".$schreNo."' ";
            $query .= "     AND EXPENSE_L_CD    = '".$model->field["EXPENSE_L_CD"]."'";
            $query .= "     AND EXPENSE_M_CD    = '".$model->field["EXPENSE_M_CD"]."'";
            $query .= "     AND PAID_INPUT_FLG  = '".$inputFlg."'";
        } else {
            $query .= "     AND SCHREGNO        = '".$model->schregno."' ";
            $query .= "     AND EXPENSE_L_CD    = '".$model->exp_lcd."'";
            $query .= "     AND EXPENSE_M_CD    = '".$model->exp_mcd."'";
            $query .= "     AND PAID_INPUT_FLG  = '".$model->paid_input_flg."'";
        }

        return $query;
    }

    //追加
    function &getInsPayQuery($model, $schreNo="")
    {
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        if ($model->cmd == "all_update") {
            $data["SCHREGNO"][TEXT]         = $schreNo;
            $data["EXPENSE_L_CD"][TEXT]     = $model->field["EXPENSE_L_CD"];
            $data["EXPENSE_M_CD"][TEXT]     = $model->field["EXPENSE_M_CD"];
            $data["PAID_INPUT_FLG"][TEXT]   = $model->div;
            if (strlen($model->field["CHECKED1"])) {
                $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
                $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
                $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
            }
            if (strlen($model->field["CHECKED2"])) {
                $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
                $data["REPAY_DEV"][TEXT]        = $model->field["REPAY_DEV"];
                $data["REPAY_DATE"][TEXT]       = str_replace("/", "-", $model->field["REPAY_DATE"]);
            }
            if (strlen($model->field["CHECKED3"])) {
                $data["REMARK"][TEXT]           = $model->field["REMARK"];
            }
        } else {
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["EXPENSE_L_CD"][TEXT]     = $model->exp_lcd;
            $data["EXPENSE_M_CD"][TEXT]     = $model->exp_mcd;
            $data["PAID_INPUT_FLG"][TEXT]   = (strlen($model->paid_input_flg)) ? $model->paid_input_flg : '2';
            $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
            $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
            $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
            $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
            $data["REPAY_DEV"][TEXT]        = $model->field["REPAY_DEV"];
            $data["REPAY_DATE"][TEXT]       = str_replace("/", "-", $model->field["REPAY_DATE"]);
            $data["REMARK"][TEXT]           = $model->field["REMARK"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "money_paid_m_dat");
        return $query;
    }

    //更新
    function &getUpdPayQuery($model, $schreNo="")
    {
        $data = array();

        if ($model->cmd == "all_update") {
            if ($model->s_exist_flg != "1") {
                if (strlen($model->field["CHECKED1"])) {
                    $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
                    $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
                    $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
                }
                if (strlen($model->field["CHECKED2"])) {
                    $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
                    $data["REPAY_DATE"][TEXT]       = str_replace("/", "-", $model->field["REPAY_DATE"]);
                    $data["REPAY_DEV"][TEXT]        = $model->field["REPAY_DEV"];
                }
            }
            if (strlen($model->field["CHECKED3"])) {
                $data["REMARK"][TEXT]           = $model->field["REMARK"];
            }
        } else {
            if ($model->s_exist_flg != "1") {
                $data["PAID_MONEY"][NUMBER]     = $model->field["PAID_MONEY"];
                $data["PAID_MONEY_DATE"][TEXT]  = str_replace("/", "-", $model->field["PAID_MONEY_DATE"]);
                $data["PAID_MONEY_DIV"][TEXT]   = $model->field["PAID_MONEY_DIV"];
                $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
                $data["REPAY_DATE"][TEXT]       = str_replace("/", "-", $model->field["REPAY_DATE"]);
                $data["REPAY_DEV"][TEXT]        = $model->field["REPAY_DEV"];
            }
            $data["REMARK"][TEXT]           = $model->field["REMARK"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE year           = '".$model->year."'";
        if ($model->cmd == "all_update") {
            $where .= "   AND schregno       = '".$schreNo."'";
            $where .= "   AND expense_l_cd   = '".$model->field["EXPENSE_L_CD"]."'";
            $where .= "   AND expense_m_cd   = '".$model->field["EXPENSE_M_CD"]."'";
            $where .= "   AND paid_input_flg = '".$model->div."'";
        } else {
            $where .= "   AND schregno       = '".$model->schregno."'";
            $where .= "   AND expense_l_cd   = '".$model->exp_lcd."'";
            $where .= "   AND expense_m_cd   = '".$model->exp_mcd."'";
            $where .= "   AND paid_input_flg = '".$model->paid_input_flg."'";
        }

        $query = Query::updateSQL($data, "money_paid_m_dat", $where);

        return $query;
    }

    //削除  データ(入金データ、返金データ)無しの場合、レコード削除
    function getDelPayQuery($model, $schreNo="")
    {
        $query  = " DELETE FROM  money_paid_m_dat ";
        $query .= "  WHERE year             = '".$model->year."' ";
        if ($model->cmd == "all_update") {
            $query .= "    AND schregno         = '".$schreNo."' ";
            $query .= "    AND expense_l_cd     = '".$model->field["EXPENSE_L_CD"]."'";
            $query .= "    AND expense_m_cd     = '".$model->field["EXPENSE_M_CD"]."'";
            $query .= "    AND paid_input_flg   = '".$model->div."'";
        } else {
            $query .= "    AND schregno         = '".$model->schregno."' ";
            $query .= "    AND expense_l_cd     = '".$model->exp_lcd."'";
            $query .= "    AND expense_m_cd     = '".$model->exp_mcd."'";
            $query .= "    AND paid_input_flg   = '".$model->paid_input_flg."'";
        }
        $query .= "    AND paid_money_date  IS NULL";
        $query .= "    AND paid_money       IS NULL";
        $query .= "    AND paid_money_div   IS NULL";
        $query .= "    AND repay_date       IS NULL";
        $query .= "    AND repay_money      IS NULL";
        $query .= "    AND repay_dev        IS NULL";

        return $query;
    }

    /****************/
    /* 一括更新画面 */
    /****************/
    //学年毎のクラスの取得
    function GetHrclass() {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一括更新画面の生徒一覧
    function GetStudent($hrClass, $sex) {
        $arr = explode("-", $hrClass);

        $query  = " SELECT ";
        $query .= "     T1.NAME_SHOW,  ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T3.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     RIGHT OUTER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T2.YEAR     = T3.YEAR ";
        $query .= "                                   AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "                                   AND T2.GRADE    = T3.GRADE ";
        $query .= "                                   AND T2.HR_CLASS = T3.HR_CLASS ";
        if ($sex != 99){
            $query .= " WHERE ";
            $query .= "         T1.SEX  = '".$sex."' ";
            $query .= "     AND T2.YEAR = '".CTRL_YEAR."'";
        }else {                  
            $query .= " WHERE ";
            $query .= "     T2.YEAR     = '".CTRL_YEAR."' ";
        }
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "     AND T2.GRADE    = '".$arr[0]."'";
        $query .= "     AND T2.HR_CLASS = '".$arr[1]."'";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO  ";

        return $query;
    }

    //対象費目の取得
    function getMcd($year, $lmCd="") {
        $query  = " SELECT ";
        if ($lmCd != "") {
            $query .= "     EXPENSE_S_EXIST_FLG ";
        } else {
            $query .= "     EXPENSE_L_CD || EXPENSE_M_CD AS TOTALCD, ";
            $query .= "     EXPENSE_M_NAME, ";
            $query .= "     EXPENSE_M_MONEY, ";
            $query .= "     EXPENSE_S_EXIST_FLG, ";
            $query .= "     BANK_TRANS_SDATE ";
        }
        $query .= " FROM ";
        $query .= "     EXPENSE_M_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($lmCd != "") {
            $query .= "     AND EXPENSE_L_CD || EXPENSE_M_CD = '{$lmCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     EXPENSE_M_CD ";

        return $query;
    }

    //データ存在チェック(入金済み中分類データ)
    function getPaidDiv($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     PAID_INPUT_FLG ";    
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT ";    
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->year."' ";    
        $query .= "     AND SCHREGNO     = '".$schregno."' ";
        $query .= "     AND EXPENSE_L_CD = '".$model->field["EXPENSE_L_CD"]."' ";
        $query .= "     AND EXPENSE_M_CD = '".$model->field["EXPENSE_M_CD"]."' ";

        return $query;
    }

    //分納データチェック
    function MoneyInstData($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT ";   
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->year."' ";   
        $query .= "     AND SCHREGNO     = '".$schregno."' ";
        $query .= "     AND EXPENSE_M_CD = '".$model->field["EXPENSE_M_CD"]."' ";
        $query .= "     AND INST_CD IS NOT NULL ";
        
        return $query;
    }
}
?>
