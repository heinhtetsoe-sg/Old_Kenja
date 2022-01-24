<?php

require_once('for_php7.php');

class knjmp735Query extends Query {

    function getStudentSearch($model)
    {
        $and = "";
        $where = "";

        if ($model->search["SEARCH_DIV"] == "1") {
            $where .= "   ".$and."MAIN.ENTERYEAR = '".(CTRL_YEAR + 1)."' ";
            $and = "AND ";
        } else {
            $where .= "   ".$and."VALUE(MAIN.GRD_DIV, '0') NOT IN ('1', '2', '3', '9') ";
            $and = "AND ";
        }

        //入学年度
        if ($model->search["ENT_YEAR"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."MAIN.ENTERYEAR = '".$model->search["ENT_YEAR"]."' ";
                $and = "AND ";
            } else {
                $where .= "   ".$and."FISCALYEAR(MAIN.ENT_DATE) = '".$model->search["ENT_YEAR"]."' ";
                $and = "AND ";
            }
        }

        //入学区分
        if ($model->search["A028"]) {
            $where .= "   ".$and."substr(MAIN.EXAMNO, 2) BETWEEN A028.NAMESPARE1 AND A028.NAMESPARE2 ";
            $and = "AND ";
        }

        //入金有無
        if ($model->search["PAID_UMU"] == "1") {
            $where .= "     ".$and."VALUE(M_PAY.PAID_MONEY, 0) + VALUE(S_PAY.PAID_MONEY, 0) > 0 ";
            $and = "AND ";
        } else {
            $where .= "     ".$and."VALUE(M_PAY.PAID_MONEY, 0) + VALUE(S_PAY.PAID_MONEY, 0) = 0 ";
            $and = "AND ";
        }

        //卒業予定年度
        if ($model->search["GRD_YEAR"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."(CAST(MAIN.ENTERYEAR AS SMALLINT) + 3) = ".$model->search["GRD_YEAR"]." ";
                $and = "AND ";
            } else {
                $where .= "   ".$and."FISCALYEAR(MAIN.GRD_DATE) = '".$model->search["GRD_YEAR"]."' ";
                $and = "AND ";
            }
        }

        //学年
        if ($model->search["GRADE"]) {
            $where .= "   ".$and."SCH_R.GRADE = '".$model->search["GRADE"]."' ";
            $and = "AND ";
        }

        //年組
        if ($model->search["HR_CLASS"]) {
            $where .= "   ".$and."SCH_R.GRADE || SCH_R.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
            $and = "AND ";
        }

        //学籍番号
        if ($model->search["SCHREGNO"]) {
            $where .= "   ".$and."MAIN.SCHREGNO = '".$model->search["SCHREGNO"]."' ";
            $and = "AND ";
        }

        //氏名
        if ($model->search["NAME"]) {
            $where .= "   ".$and."MAIN.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' ";
            $and = "AND ";
        }

        //氏名かな
        if ($model->search["NAME_KANA"]) {
            $where .= "   ".$and."MAIN.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' ";
            $and = "AND ";
        }

        return $where;
    }

    //生徒情報取得
    function getStudentInfoData($model)
    {
        if ($model->search["SEARCH_DIV"] == "1") {
            $setYear = (CTRL_YEAR + 1);
        } else {
            $setYear = CTRL_YEAR;
        }


        $tableName = "V_SCHREG_BASE_MST";
        if ($model->search["SEARCH_DIV"] == "1") {
            $tableName = "FRESHMAN_DAT";
        }

        $where = knjmp735Query::getStudentSearch($model);
        $and = $where == "" ? "" : "AND ";
        $query  = " WITH PRINT_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLAIM_NO, ";
        $query .= "     MAX(T1.CLAIM_EDA) AS CLAIM_EDA, ";
        $query .= "     MAX(T1.SLIP_NO) AS SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PRINT_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setYear."' ";
        $query .= "     AND VALUE(T1.CLAIM_NONE_FLG, '0') = '0' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLAIM_NO ";
        $query .= " ), M_DUE AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SLIP_NO, ";
        $query .= "     SUM(T1.MONEY_DUE * T1.COLLECT_CNT) AS MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_DUE_M_DAT T1, ";
        $query .= "     PRINT_T ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setYear."' ";
        $query .= "     AND PRINT_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND PRINT_T.SLIP_NO = T1.SLIP_NO ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SLIP_NO ";
        $query .= " ), S_DUE AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SLIP_NO, ";
        $query .= "     SUM(T1.MONEY_DUE * T1.COLLECT_CNT) AS MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_DUE_S_DAT T1, ";
        $query .= "     PRINT_T ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setYear."' ";
        $query .= "     AND PRINT_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND PRINT_T.SLIP_NO = T1.SLIP_NO ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SLIP_NO ";
        $query .= " ), M_PAY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SLIP_NO, ";
        $query .= "     SUM(T1.PAID_MONEY) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_PAID_M_DAT T1, ";
        $query .= "     PRINT_T ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setYear."' ";
        $query .= "     AND PRINT_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND PRINT_T.SLIP_NO = T1.SLIP_NO ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SLIP_NO ";
        $query .= " ), S_PAY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SLIP_NO, ";
        $query .= "     SUM(T1.PAID_MONEY) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_PAID_S_DAT T1, ";
        $query .= "     PRINT_T ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setYear."' ";
        $query .= "     AND PRINT_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND PRINT_T.SLIP_NO = T1.SLIP_NO ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SLIP_NO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO AS GRADE_HR_CLASS_ATTENDNO, ";
        $query .= "     SCH_B.SCHREGNO AS GAKUSEKI_ARI, ";
        $query .= "     SCH_R.GRADE, ";
        $query .= "     SCH_R.HR_CLASS, ";
        $query .= "     SCH_R.ATTENDNO, ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.NAME, ";
        $query .= "     MAIN.SEX, ";
        $query .= "     MAIN.EXAMNO, ";
        $query .= "     JUKEN.NAME1 AS JUKEN_DIV, ";
        $query .= "     SCH_RH.HR_NAME || ' - ' || SCH_R.ATTENDNO AS HR_ATTEND, ";
        $query .= "     SCH_RH.HR_NAME || ' ' || SCH_R.ATTENDNO || '番' AS HR_ATTEND_NAME, ";
        $query .= "     PRINT_T.CLAIM_NO, ";
        $query .= "     PRINT_T.CLAIM_EDA, ";
        $query .= "     PRINT_T.SLIP_NO, ";
        $query .= "     VALUE(M_DUE.MONEY_DUE, 0) + VALUE(S_DUE.MONEY_DUE, 0) AS MONEY_DUE, ";
        $query .= "     VALUE(M_PAY.PAID_MONEY, 0) + VALUE(S_PAY.PAID_MONEY, 0) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "   {$tableName} MAIN ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT SCH_R ON MAIN.SCHREGNO = SCH_R.SCHREGNO ";
        $query .= "        AND SCH_R.YEAR = '".$setYear."' ";
        $query .= "        AND SCH_R.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON SCH_R.YEAR = SCH_RH.YEAR ";
        $query .= "        AND SCH_R.SEMESTER = SCH_RH.SEMESTER ";
        $query .= "        AND SCH_R.GRADE = SCH_RH.GRADE ";
        $query .= "        AND SCH_R.HR_CLASS = SCH_RH.HR_CLASS ";
        $query .= "     LEFT JOIN NAME_MST AS A028 ON A028.NAMECD1 = 'A028' ";
        $query .= "          AND A028.NAMECD2 = '{$model->search["A028"]}' ";
        $query .= "     LEFT JOIN NAME_MST AS JUKEN ON JUKEN.NAMECD1 = 'A028' ";
        $query .= "          AND substr(MAIN.EXAMNO, 2) BETWEEN JUKEN.NAMESPARE1 AND JUKEN.NAMESPARE2 ";
        $query .= "     INNER JOIN PRINT_T ON PRINT_T.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "     LEFT JOIN M_DUE ON M_DUE.SCHREGNO = PRINT_T.SCHREGNO ";
        $query .= "          AND M_DUE.SLIP_NO = PRINT_T.SLIP_NO ";
        $query .= "     LEFT JOIN M_PAY ON M_DUE.SCHREGNO = M_PAY.SCHREGNO ";
        $query .= "          AND M_DUE.SLIP_NO = M_PAY.SLIP_NO ";
        $query .= "     LEFT JOIN S_DUE ON M_DUE.SCHREGNO = S_DUE.SCHREGNO ";
        $query .= "          AND M_DUE.SLIP_NO = S_DUE.SLIP_NO ";
        $query .= "     LEFT JOIN S_PAY ON M_DUE.SCHREGNO = S_PAY.SCHREGNO ";
        $query .= "          AND M_DUE.SLIP_NO = S_PAY.SLIP_NO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST SCH_B ON MAIN.SCHREGNO = SCH_B.SCHREGNO ";

        $query .= " WHERE ".$where;

        $query .= " ORDER BY ";
        if ($model->search["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "   SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO, ";
        } else {
            $query .= "   MAIN.SCHREGNO, ";
        }
        $query .= "   PRINT_T.SLIP_NO ";

        return $query;
    }

    function getMDueDat($model, $setYear, $slipNo, $schno) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_DUE_M_DAT T1 ";
        $query .= "     INNER JOIN COLLECT_M_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
        $query .= "           AND T1.COLLECT_M_CD = L1.COLLECT_M_CD ";
        $query .= "           AND L1.COLLECT_S_EXIST_FLG = '2' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$setYear}' ";
        $query .= "     AND T1.SCHREGNO = '{$schno}' ";
        $query .= "     AND T1.SLIP_NO = '{$slipNo}' ";
 
        return $query;
   }

    function getSDueDat($model, $setYear, $slipNo, $schno) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_MONEY_DUE_S_DAT T1 ";
        $query .= "     INNER JOIN COLLECT_S_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
        $query .= "           AND T1.COLLECT_M_CD = L1.COLLECT_M_CD ";
        $query .= "           AND T1.COLLECT_S_CD = L1.COLLECT_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$setYear}' ";
        $query .= "     AND T1.SCHREGNO = '{$schno}' ";
        $query .= "     AND T1.SLIP_NO = '{$slipNo}' ";
 
        return $query;
   }

    /**
     * 更新
     */
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->search["SEARCH_DIV"] == "1") {
            $setYear = (CTRL_YEAR + 1);
        } else {
            $setYear = CTRL_YEAR;
        }

        //入金作成
        $paid_data = ($model->field["PAID_FLG"]) ? $model->field["PAID_FLG"]: array();
        foreach ($paid_data as $val) {
            list($slipNo, $schno) = preg_split("/:/",$val);
            $query = knjmp735Query::getMDueDat($model, $setYear, $slipNo, $schno);
            $result = $db->query($query);
            $setSeq = 1;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["YEAR"][TEXT]             = $row["YEAR"];
                $data["SLIP_NO"][TEXT]          = $row["SLIP_NO"];
                $data["SEQ"][TEXT]              = $row["SEQ"];
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["COLLECT_L_CD"][TEXT]     = $row["COLLECT_L_CD"];
                $data["COLLECT_M_CD"][TEXT]     = $row["COLLECT_M_CD"];
                $data["PAID_MONEY_DATE"][TEXT]  = CTRL_DATE;
                $data["PAID_MONEY"][NUMBER]     = $row["MONEY_DUE"] * $row["COLLECT_CNT"];
                $data["PAID_MONEY_DIV"][TEXT]   = "1";
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_SLIP_MONEY_PAID_M_DAT", $where);
                $db->query($query);
                $setSeq++;
            }

            $query = knjmp735Query::getSDueDat($model, $setYear, $slipNo, $schno);
            $result = $db->query($query);
            $setSeq = 1;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["YEAR"][TEXT]             = $row["YEAR"];
                $data["SLIP_NO"][TEXT]          = $row["SLIP_NO"];
                $data["MSEQ"][TEXT]             = $row["MSEQ"];
                $data["SSEQ"][TEXT]             = $row["SSEQ"];
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["COLLECT_L_CD"][TEXT]     = $row["COLLECT_L_CD"];
                $data["COLLECT_M_CD"][TEXT]     = $row["COLLECT_M_CD"];
                $data["COLLECT_S_CD"][TEXT]     = $row["COLLECT_S_CD"];
                $data["PAID_MONEY_DATE"][TEXT]  = CTRL_DATE;
                $data["PAID_MONEY"][NUMBER]     = $row["MONEY_DUE"] * $row["COLLECT_CNT"];
                $data["PAID_MONEY_DIV"][TEXT]   = "1";
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_SLIP_MONEY_PAID_S_DAT", $where);
                $db->query($query);
                $setSeq++;
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
