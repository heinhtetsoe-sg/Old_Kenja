<?php

require_once('for_php7.php');


class knjp371Query extends Query {

    //担当
    function GetStaff($model){
        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND JOBCD = '3000' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象年月
    function getYearMonth()
    {
        $year = CTRL_YEAR;
        $nextYear = CTRL_YEAR + 1;

        $query  = " SELECT DISTINCT ";
        $query .= "     PAID_YEARMONTH AS VALUE, ";
        $query .= "     SUBSTR(PAID_YEARMONTH, 1, 4) || '年' || SUBSTR(PAID_YEARMONTH, 5) || '月' AS LABEL ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND PAID_YEARMONTH IS NOT NULL ";
        $query .= "     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ADD_PAID_YEARMONTH AS VALUE, ";
        $query .= "     SUBSTR(ADD_PAID_YEARMONTH, 1, 4) || '年' || SUBSTR(ADD_PAID_YEARMONTH, 5) || '月' AS LABEL ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ADD_PAID_YEARMONTH IS NOT NULL ";
        $query .= "     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //現年月
    function getSysYearMonth()
    {
        $query = " VALUES(CAST(YEAR(SYSDATE()) AS CHAR(4)) || CAST(MONTH(SYSDATE()) AS CHAR(2))) ";

        return $query;
    }

    //クラスデータ取得
    function getclass($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     GRADE || HR_CLASS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE,HR_CLASS,HR_NAME ";

        return $query;
    }

    //生徒データ取得
    function getsch($model)
    {
        $query  = " WITH REDUCTION_DAT_SUM AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' ";
        $query .= "              THEN t1.REDUCTIONMONEY_1 ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "         + ";
        $query .= "         CASE WHEN REDUC_DEC_FLG_2 = '1' ";
        $query .= "              THEN t1.REDUCTIONMONEY_2 ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS REDUCTIONMONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT t1 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND (REDUC_DEC_FLG_1 = '1' ";
        $query .= "          OR ";
        $query .= "          REDUC_DEC_FLG_2 = '1') ";
        $query .= "     AND VALUE(OFFSET_FLG, '0') = '1' ";
        $query .= "     AND VALUE(LOCK_FLG, '0') = '0' ";
        $query .= " GROUP BY ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), REDUCTION_COUNTRY_PLAN_DAT_SUM AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(CASE WHEN T1.PAID_YEARMONTH = '{$model->field["YEAR_MONTH"]}' ";
        $query .= "                   AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "                   AND VALUE(T1.PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "              THEN T1.PLAN_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "         + ";
        $query .= "         CASE WHEN T1.ADD_PAID_YEARMONTH = '{$model->field["YEAR_MONTH"]}' ";
        $query .= "                   AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "                   AND VALUE(T1.ADD_PLAN_LOCK_FLG, '0') = '0' ";
        $query .= "              THEN T1.ADD_PLAN_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS PAID_PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND (T1.PAID_YEARMONTH = '{$model->field["YEAR_MONTH"]}' ";
        $query .= "          OR ";
        $query .= "          T1.ADD_PAID_YEARMONTH = '{$model->field["YEAR_MONTH"]}') ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ), MONEY_SUM AS ( ";
        $query .= " SELECT  ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     VALUE(T1.SCHREGNO, T2.SCHREGNO) AS SCHREGNO, ";
        $query .= "     VALUE(T1.REDUCTIONMONEY, 0) + VALUE(T2.PAID_PLAN_MONEY, 0) - VALUE(T3.TOTAL_ADJUSTMENT_MONEY,0) AS REDUCTIONMONEY ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT_SUM T1 ";
        $query .= "     FULL OUTER JOIN REDUCTION_COUNTRY_PLAN_DAT_SUM T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND T1.YEAR = T2.YEAR ";
        $query .= "     LEFT JOIN REDUCTION_ADJUSTMENT_DAT T3 ON T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T3.SCHREGNO = T1.SCHREGNO ";

        if ("03" == substr($model->field["YEAR_MONTH"], 5)) {
            $query .= " ), MONEY_DUE_T AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SUM(T1.MONEY_DUE) AS MONEY_DUE ";
            $query .= " FROM ";
            $query .= "     MONEY_DUE_M_DAT T1 ";
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND T1.EXPENSE_M_CD IN ('11', '12', '13') ";
            $query .= " GROUP BY ";
            $query .= "     T1.SCHREGNO ";
            $query .= " ), REDUC_T AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SUM(CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ";
            $query .= "              THEN VALUE(T1.REDUCTIONMONEY_1, 0) ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "         + ";
            $query .= "         CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ";
            $query .= "              THEN VALUE(T1.REDUCTIONMONEY_2, 0) ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "     ) AS REDUCTIONMONEY ";
            $query .= " FROM ";
            $query .= "     REDUCTION_DAT T1 ";
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND (T1.REDUC_DEC_FLG_1 = '1' ";
            $query .= "         OR ";
            $query .= "         T1.REDUC_DEC_FLG_2 = '1') ";
            $query .= " GROUP BY ";
            $query .= "     T1.SCHREGNO ";
            $query .= " ), REDUC_COUNTRY_T AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "              THEN VALUE(T1.PLAN_MONEY, 0) ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "         + ";
            $query .= "         CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "     ) AS REDUCTION_C_MONEY, ";
            $query .= "     SUM(CASE WHEN T1.PAID_YEARMONTH <= '".CTRL_YEAR."12' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "              THEN VALUE(T1.PLAN_MONEY, 0) ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "         + ";
            $query .= "         CASE WHEN T1.ADD_PAID_YEARMONTH <= '".CTRL_YEAR."12' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ";
            $query .= "              ELSE 0 ";
            $query .= "         END ";
            $query .= "     ) AS REDUCTION_C12_MONEY ";
            $query .= " FROM ";
            $query .= "     REDUCTION_COUNTRY_PLAN_DAT T1 ";
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "         OR ";
            $query .= "         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ";
            $query .= " GROUP BY ";
            $query .= "     T1.SCHREGNO ";
        }
        $query .= " ), SCHREG_DAT AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MONEY_SUM T1 ";
        if ("03" == substr($model->field["YEAR_MONTH"], 5)) {
            $query .= " UNION ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     REDUCTION_BURDEN_CHARGE_DAT T1 ";
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             MONEY_SUM E1 ";
            $query .= "         WHERE ";
            $query .= "            T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "    ) ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     t2.SCHREGNO,t2.ATTENDNO,t2.MAJORCD,t2.GRADE,t2.HR_CLASS,t3.NAME_SHOW,t4.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t2 LEFT JOIN SCHREG_BASE_MST t3 ON t2.SCHREGNO = t3.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT t4 ";
        $query .= " WHERE ";
        $query .= "     t2.YEAR = '".CTRL_YEAR."' AND t2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     t2.GRADE||t2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     t4.YEAR = t2.YEAR AND t4.SEMESTER = t2.SEMESTER AND ";
        $query .= "     t4.GRADE||t4.HR_CLASS = t2.GRADE||t2.HR_CLASS AND ";
        $query .= "     t2.SCHREGNO IN ( SELECT t5.SCHREGNO ";
        $query .= "                      FROM SCHREG_DAT t5) ";
        $query .= " ORDER BY ";
        $query .= "     t2.ATTENDNO ";

        return $query;
    }

    //DELETE(更新処理)
    function getDeleteQuery2()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SCHOOL_EXPENSES_SYS_INI WHERE PROGRAMID = 'KNJP371' AND DIV = '".CTRL_YEAR."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
    
    //UPDATE(更新処理)
    function getUpdateQuery2(&$model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["PROGRAMID"][TEXT]  = "KNJP371";
        $data["DIV"][TEXT]        = CTRL_YEAR;
        $data["DATE1"][TEXT]      = str_replace("/","-",$model->date);
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHOOL_EXPENSES_SYS_INI");

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //三井住友
    function getVar1() {
        $query  = " SELECT ";
        $query .= "     T1.VAR1 ";
        //$query .= "    ,T1.VAR2 ";
        //$query .= "    ,L1.BANKNAME ";
        //$query .= "    ,L1.BRANCHNAME ";
        $query .= " FROM ";
        $query .= "     SCHOOL_EXPENSES_SYS_INI T1 ";
        $query .= "     LEFT JOIN BANK_MST L1 ON L1.BANKCD = T1.VAR1 AND L1.BRANCHCD = T1.VAR2 ";
        $query .= " WHERE ";
        $query .= "     T1.PROGRAMID = 'BANK' AND ";
        $query .= "     T1.DIV = '0001' ";

        return $query;
    }

    /** クラス別軽減データを抽出 */
    function meisaiSql($model, $isMinou, $var1) {
        //変数
        $mcd11 = "11";
        $mcd12 = "12";
        $mcd13 = "13";
        $year = CTRL_YEAR;
        $semester = CTRL_SEMESTER;
        $month3Flg = (substr($model->field["YEAR_MONTH"], 4) == "03") ? true : false;
        $taisyouMcd = (substr($model->field["YEAR_MONTH"], 4) == "09") ? $mcd12 : $mcd13;
        $selectleft = explode(",", $model->selectleft); //印刷対象外
        $unprint = "";
        if (get_count($selectleft) == 0) {
            $unprint = "NOT IN ('')";
        } else {
            $unprint .= "NOT IN (";
            $com = "";
            foreach ($selectleft as $key => $val) {
                $schno = explode(":", $val);
                $unprint .= $com . "'" . $schno[0] . "'";
                $com = ",";
            }
            $unprint .= ")";
        }

        //SQL
        $query  = "  WITH REDUCTION_DAT_SUM AS ( ";
        $query .= "  SELECT ";
        $query .= "      t1.YEAR, ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' ";
        $query .= "               THEN t1.REDUCTIONMONEY_1 ";
        $query .= "               ELSE 0 ";
        $query .= "          END ";
        $query .= "          + ";
        $query .= "          CASE WHEN REDUC_DEC_FLG_2 = '1' ";
        $query .= "               THEN t1.REDUCTIONMONEY_2 ";
        $query .= "               ELSE 0 ";
        $query .= "          END ";
        $query .= "      ) AS REDUCTIONMONEY ";
        $query .= "  FROM ";
        $query .= "      REDUCTION_DAT t1 ";
        $query .= "  WHERE ";
        $query .= "     t1.YEAR = '{$year}' ";
        $query .= "     AND t1.SCHREGNO {$unprint} ";
        $query .= "     AND (REDUC_DEC_FLG_1 = '1' ";
        $query .= "          OR ";
        $query .= "          REDUC_DEC_FLG_2 = '1') ";
        $query .= "     AND  VALUE(OFFSET_FLG, '0') = '1' AND VALUE(LOCK_FLG, '0') <> '1' ";
        $query .= "  GROUP BY ";
        $query .= "      t1.YEAR, ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ), REDUCTION_COUNTRY_PLAN_DAT_SUM AS ( ";
        $query .= "  SELECT  ";
        $query .= "      T1.YEAR, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      SUM((CASE WHEN VALUE(T1.PAID_YEARMONTH, '0') = '{$model->field["YEAR_MONTH"]}' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' AND VALUE(T1.PLAN_LOCK_FLG, '0') = '0' THEN T1.PLAN_MONEY ";
        $query .= "          ELSE 0 END) ";
        $query .= "      + (CASE WHEN VALUE(T1.ADD_PAID_YEARMONTH, '0') = '{$model->field["YEAR_MONTH"]}' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' AND VALUE(T1.ADD_PLAN_LOCK_FLG, '0') = '0' THEN T1.ADD_PLAN_MONEY ";
        $query .= "          ELSE 0 END)) AS PAID_PLAN_MONEY ";
        $query .= "  FROM ";
        $query .= "      REDUCTION_COUNTRY_PLAN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '{$year}' ";
        $query .= "      AND T1.SCHREGNO {$unprint} ";
        $query .= "      AND (VALUE(T1.PAID_YEARMONTH, '0') = '{$model->field["YEAR_MONTH"]}' ";
        $query .= "           OR VALUE(T1.ADD_PAID_YEARMONTH, '0') = '{$model->field["YEAR_MONTH"]}') ";
        $query .= "  GROUP BY ";
        $query .= "      T1.YEAR, ";
        $query .= "      T1.SCHREGNO ";
        $query .= "  ), MONEY_SUM AS ( ";
        $query .= "  SELECT  ";
        $query .= "      '{$year}' AS YEAR, ";
        $query .= "      VALUE(T1.SCHREGNO, T2.SCHREGNO) AS SCHREGNO, ";
        $query .= "      VALUE(T1.REDUCTIONMONEY, 0) + VALUE(T2.PAID_PLAN_MONEY, 0) - VALUE(T3.TOTAL_ADJUSTMENT_MONEY,0) AS REDUCTIONMONEY ";
        $query .= "  FROM ";
        $query .= "      REDUCTION_DAT_SUM T1 ";
        $query .= "      FULL OUTER JOIN REDUCTION_COUNTRY_PLAN_DAT_SUM T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "          AND T1.YEAR = T2.YEAR ";
        $query .= "      LEFT JOIN REDUCTION_ADJUSTMENT_DAT T3 ON T3.YEAR = '{$year}' ";
        $query .= "          AND T3.SCHREGNO = T1.SCHREGNO ";

        if ($month3Flg) {
            $query .= "  ), MONEY_DUE_T AS ( ";
            $query .= "  SELECT ";
            $query .= "      T1.SCHREGNO, ";
            $query .= "      SUM(T1.MONEY_DUE) AS MONEY_DUE ";
            $query .= "  FROM ";
            $query .= "      MONEY_DUE_M_DAT T1 ";
            $query .= "  WHERE ";
            $query .= "     T1.YEAR = '{$year}' ";
            $query .= "     AND T1.EXPENSE_M_CD IN ('{$mcd11}', '{$mcd12}', '{$mcd13}') ";
            $query .= "  GROUP BY ";
            $query .= "      T1.SCHREGNO ";
            $query .= "  ), REDUC_T AS ( ";
            $query .= "  SELECT ";
            $query .= "      T1.SCHREGNO, ";
            $query .= "      SUM(CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ";
            $query .= "               THEN VALUE(T1.REDUCTIONMONEY_1, 0) ";
            $query .= "               ELSE 0 ";
            $query .= "          END ";
            $query .= "          + ";
            $query .= "          CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ";
            $query .= "               THEN VALUE(T1.REDUCTIONMONEY_2, 0) ";
            $query .= "               ELSE 0 ";
            $query .= "          END ";
            $query .= "      ) AS REDUCTIONMONEY ";
            $query .= "  FROM ";
            $query .= "      REDUCTION_DAT T1 ";
            $query .= "  WHERE ";
            $query .= "     T1.YEAR = '{$year}' ";
            $query .= "     AND (T1.REDUC_DEC_FLG_1 = '1' ";
            $query .= "          OR ";
            $query .= "          T1.REDUC_DEC_FLG_2 = '1') ";
            $query .= "  GROUP BY ";
            $query .= "      T1.SCHREGNO ";
            $query .= "  ), REDUC_COUNTRY_T AS ( ";
            $query .= "  SELECT ";
            $query .= "      T1.SCHREGNO, ";
            $query .= "      SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "               THEN VALUE(T1.PLAN_MONEY, 0) ";
            $query .= "               ELSE 0 ";
            $query .= "          END ";
            $query .= "          + ";
            $query .= "          CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "               THEN VALUE(T1.ADD_PLAN_MONEY, 0) ";
            $query .= "               ELSE 0 ";
            $query .= "          END ";
            $query .= "      ) AS REDUCTION_C_MONEY, ";
            $query .= "      SUM(CASE WHEN T1.PAID_YEARMONTH <= '{$year}12' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "               THEN VALUE(T1.PLAN_MONEY, 0) ";
            $query .= "               ELSE 0 ";
            $query .= "          END ";
            $query .= "          + ";
            $query .= "          CASE WHEN T1.ADD_PAID_YEARMONTH <= '{$year}12' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "               THEN VALUE(T1.ADD_PLAN_MONEY, 0) ";
            $query .= "               ELSE 0 ";
            $query .= "          END ";
            $query .= "      ) AS REDUCTION_C12_MONEY ";
            $query .= "  FROM ";
            $query .= "      REDUCTION_COUNTRY_PLAN_DAT T1 ";
            $query .= "  WHERE ";
            $query .= "     T1.YEAR = '{$year}' ";
            $query .= "     AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ";
            $query .= "          OR ";
            $query .= "          VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ";
            $query .= "  GROUP BY ";
            $query .= "      T1.SCHREGNO ";
            $query .= "  ), ADJUST AS ( ";
            $query .= "  SELECT ";
            $query .= "      T1.SCHREGNO, ";
            $query .= "      VALUE(T1.MONEY_DUE, 0) AS TOTAL_LESSON_MONEY, ";
            $query .= "      VALUE(L1.REDUCTIONMONEY, 0) AS REDUCTIONMONEY, ";
            $query .= "      VALUE(L2.REDUCTION_C_MONEY, 0) AS REDUCTION_COUNTRY_MONEY, ";
            $query .= "      VALUE(L2.REDUCTION_C12_MONEY, 0) AS REDUCTION_C12_MONEY ";
            $query .= "  FROM ";
            $query .= "      MONEY_DUE_T T1 ";
            $query .= "      LEFT JOIN REDUC_T L1 ON T1.SCHREGNO = L1.SCHREGNO ";
            $query .= "      LEFT JOIN REDUC_COUNTRY_T L2 ON T1.SCHREGNO = L2.SCHREGNO ";
            $query .= "  WHERE ";
            $query .= "      VALUE(L1.REDUCTIONMONEY, 0) + VALUE(L2.REDUCTION_C_MONEY, 0) < VALUE(T1.MONEY_DUE, 0) ";

            $query .= "  ), OVER_MONEY AS ( ";
            $query .= "  SELECT ";
            $query .= "      T1.SCHREGNO, ";
            $query .= "      VALUE(T1.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY ";
            $query .= "  FROM ";
            $query .= "      REDUCTION_BURDEN_CHARGE_DAT T1 ";
            $query .= "  WHERE ";
            $query .= "      T1.YEAR = '{$year}' ";
            $query .= "      AND EXISTS( ";
            $query .= "           SELECT ";
            $query .= "               'x' ";
            $query .= "           FROM ";
            $query .= "               MONEY_SUM E1 ";
            $query .= "           WHERE ";
            $query .= "              T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "      ) ";
        }
        $query .= "  ), SCHREG_DAT AS ( ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.SCHREGNO ";
        $query .= "  FROM ";
        $query .= "      MONEY_SUM T1 ";
        if ($month3Flg) {
            $query .= "  UNION ";
            $query .= "  SELECT DISTINCT ";
            $query .= "      T1.SCHREGNO ";
            $query .= "  FROM ";
            $query .= "      OVER_MONEY T1 ";
        }

        $query .= "  ), MINOU_SCH AS ( ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.SCHREGNO ";
        $query .= "  FROM ";
        $query .= "      MONEY_DUE_M_DAT T1 ";
        $query .= "      LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "           AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '{$year}' ";
        $query .= "      AND T1.EXPENSE_M_CD = '{$mcd11}' ";
        $query .= "      AND (L1.SCHREGNO IS NULL ";
        $query .= "           OR ";
        $query .= "           VALUE(L1.PAID_MONEY, 0) <= 0) ";
        $query .= "  UNION   ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.SCHREGNO ";
        $query .= "  FROM ";
        $query .= "      MONEY_DUE_M_DAT T1 ";
        $query .= "      LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "           AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '{$year}' ";
        $query .= "      AND T1.EXPENSE_M_CD = '{$mcd12}' ";
        $query .= "      AND (L1.SCHREGNO IS NULL ";
        $query .= "           OR ";
        $query .= "           VALUE(L1.PAID_MONEY, 0) <= 0) ";
        if ($month3Flg) {
            $query .= "  UNION   ";
            $query .= "  SELECT DISTINCT ";
            $query .= "      T1.SCHREGNO ";
            $query .= "  FROM ";
            $query .= "      ADJUST T1 ";
            $query .= "      LEFT JOIN (SELECT ";
            $query .= "                     LL1.SCHREGNO, ";
            $query .= "                     SUM(VALUE(LL1.PAID_MONEY, 0)) AS PAID_MONEY ";
            $query .= "                 FROM ";
            $query .= "                     MONEY_PAID_M_DAT LL1 ";
            $query .= "                 WHERE ";
            $query .= "                     LL1.YEAR = '{$year}' ";
            $query .= "                     AND LL1.EXPENSE_M_CD IN ('{$mcd11}', '{$mcd12}', '{$mcd13}') ";
            $query .= "                 GROUP BY ";
            $query .= "                     LL1.SCHREGNO ";
            $query .= "                 ) L1 ON T1.SCHREGNO = L1.SCHREGNO ";
            $query .= "  WHERE ";
            $query .= "      T1.TOTAL_LESSON_MONEY > L1.PAID_MONEY + T1.REDUCTIONMONEY + T1.REDUCTION_C12_MONEY ";
        }
        $query .= " ), MONEYTBL AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREG_DAT.SCHREGNO, t0.NAME, t2.GRADE, t2.HR_CLASS, t2.ATTENDNO, ";
        $query .= "     t3.BANKCD, ";
        $query .= "     t4.BANKNAME_KANA, ";
        $query .= "     t3.BRANCHCD, ";
        $query .= "     t4.BRANCHNAME_KANA, ";
        $query .= "     t3.DEPOSIT_ITEM, ";
        $query .= "     t5.ABBV1, ";
        $query .= "     t3.ACCOUNTNO, ";
        $query .= "     t3.ACCOUNTNAME, ";

        $query .= "     t1.REDUCTIONMONEY, ";
        if (!$month3Flg) {
            $query .= "     CASE WHEN t6.MONEY_DUE >= t1.REDUCTIONMONEY THEN '1' ELSE '0' END FRMID, ";
            $query .= "     CASE WHEN t6.MONEY_DUE >= t1.REDUCTIONMONEY ";
            $query .= "          THEN t6.MONEY_DUE - t1.REDUCTIONMONEY ";
            $query .= "          ELSE t1.REDUCTIONMONEY - t6.MONEY_DUE END MONEY ";
        } else {
            $query .= "     CASE WHEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END >= t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0) THEN '1' ELSE '0' END FRMID, ";
            $query .= "     CASE WHEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END >= t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0) ";
            $query .= "          THEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END - (t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0)) ";
            $query .= "          ELSE (t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0)) - CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END END MONEY ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_DAT ";
        $query .= "     LEFT JOIN MONEY_SUM t1 ON t1.SCHREGNO = SCHREG_DAT.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t0 ON t0.SCHREGNO = SCHREG_DAT.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCHREG_DAT.SCHREGNO ";
        $query .= "     AND t2.YEAR = '{$year}' ";
        $query .= "     AND t2.SEMESTER = '{$semester}' ";
        $query .= "     LEFT JOIN REGISTBANK_DAT t3 ON t3.SCHREGNO = SCHREG_DAT.SCHREGNO ";
        $query .= "     LEFT JOIN BANK_MST t4 ON t4.BANKCD = t3.BANKCD ";
        $query .= "     AND t4.BRANCHCD = t3.BRANCHCD ";
        $query .= "     LEFT JOIN NAME_MST t5 ON t5.NAMECD1 = 'G203' ";
        $query .= "     AND t5.NAMECD2 = t3.DEPOSIT_ITEM ";
        $query .= "     LEFT JOIN MONEY_DUE_M_DAT t6 ON t6.YEAR = '{$year}' ";
        $query .= "     AND t6.SCHREGNO = SCHREG_DAT.SCHREGNO ";
        $query .= "     AND t6.EXPENSE_M_CD = '{$taisyouMcd}' ";
        if ($month3Flg) {
            $query .= "     LEFT JOIN OVER_MONEY ON OVER_MONEY.SCHREGNO = SCHREG_DAT.SCHREGNO ";
            $query .= "     LEFT JOIN REDUCTION_DAT_SUM RS ON RS.SCHREGNO = SCHREG_DAT.SCHREGNO ";
        }
        $query .= "     LEFT JOIN MINOU_SCH ON MINOU_SCH.SCHREGNO = SCHREG_DAT.SCHREGNO ";
        $query .= " WHERE ";
        if ($isMinou) {
            $query .= "     MINOU_SCH.SCHREGNO IS NOT NULL ";
        } else {
            $query .= "     MINOU_SCH.SCHREGNO IS NULL ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '1' AS BANKNO, ";
        $query .= "     SCHREGNO, NAME, GRADE, HR_CLASS, ATTENDNO, ";
        $query .= "     BANKCD, ";
        $query .= "     BANKNAME_KANA, ";
        $query .= "     BRANCHCD, ";
        $query .= "     BRANCHNAME_KANA, ";
        $query .= "     DEPOSIT_ITEM, ";
        $query .= "     ABBV1, ";
        $query .= "     ACCOUNTNO, ";
        $query .= "     ACCOUNTNAME, ";
        $query .= "     FRMID, ";
        $query .= "     MONEY ";
        $query .= " FROM ";
        $query .= "     MONEYTBL ";
        $query .= " WHERE ";
        $query .= "     FRMID = '0' ";
        if (!$isMinou) {
            $query .= "     AND BANKCD = '{$var1}' ";
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     '2' AS BANKNO, ";
            $query .= "     SCHREGNO, NAME, GRADE, HR_CLASS, ATTENDNO, ";
            $query .= "     BANKCD, ";
            $query .= "     BANKNAME_KANA, ";
            $query .= "     BRANCHCD, ";
            $query .= "     BRANCHNAME_KANA, ";
            $query .= "     DEPOSIT_ITEM, ";
            $query .= "     ABBV1, ";
            $query .= "     ACCOUNTNO, ";
            $query .= "     ACCOUNTNAME, ";
            $query .= "     FRMID, ";
            $query .= "     MONEY ";
            $query .= " FROM ";
            $query .= "     MONEYTBL ";
            $query .= " WHERE ";
            $query .= "     FRMID = '0' ";
            $query .= "     AND (BANKCD < '{$var1}' OR BANKCD > '{$var1}') ";
        }
        $query .= " ORDER BY ";
        $query .= "     BANKNO, GRADE, HR_CLASS, ATTENDNO ";
//echo "isMinou = " .$isMinou ."<BR>";
//echo "query = " .$query ."<BR>";

        return $query;
    }

}
?>
