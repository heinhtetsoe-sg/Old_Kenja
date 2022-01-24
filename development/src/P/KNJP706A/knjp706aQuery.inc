<?php

require_once('for_php7.php');

class knjp706aQuery extends Query {

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
        } else if ($model->search["PAID_UMU"] == "2") {
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

        $where = knjp706aQuery::getStudentSearch($model);
        $and = $where == "" ? "" : "AND ";
        $query  = " WITH M_DUE AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     SUM(T1.MONEY_DUE) AS MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setYear."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD ";
        $query .= " ), S_DUE AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     SUM(T1.MONEY_DUE) AS MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setYear."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD ";
        $query .= " ), M_PAY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     SUM(T1.PAID_MONEY) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_PAID_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setYear."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD ";
        $query .= " ), M_PAY_ALL AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(T1.PAID_MONEY) AS PAID_ALL ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_PAID_M_DAT T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ), S_PAY_ALL AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(T1.PAID_MONEY) AS PAID_ALL ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_PAID_S_DAT T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ), S_PAY AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     SUM(T1.PAID_MONEY) AS PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_PAID_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setYear."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD ";
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
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     GRP_M.COLLECT_GRP_NAME, ";
        $query .= "     VALUE(M_PAY_ALL.PAID_ALL, 0) + VALUE(S_PAY_ALL.PAID_ALL, 0) AS PAID_ALL, ";
        $query .= "     VALUE(T1.MONEY_DUE, 0) + VALUE(S_DUE.MONEY_DUE, 0) AS MONEY_DUE, ";
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
        $query .= "     INNER JOIN M_DUE T1 ON T1.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "     LEFT JOIN COLLECT_GRP_MST GRP_M ON GRP_M.YEAR = '".$setYear."' ";
        $query .= "          AND T1.COLLECT_GRP_CD = GRP_M.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN M_PAY ON T1.SCHREGNO = M_PAY.SCHREGNO ";
        $query .= "          AND T1.COLLECT_GRP_CD = M_PAY.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN S_DUE ON T1.SCHREGNO = S_DUE.SCHREGNO ";
        $query .= "          AND T1.COLLECT_GRP_CD = S_DUE.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN S_PAY ON T1.SCHREGNO = S_PAY.SCHREGNO ";
        $query .= "          AND T1.COLLECT_GRP_CD = S_PAY.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN M_PAY_ALL ON MAIN.SCHREGNO = M_PAY_ALL.SCHREGNO ";
        $query .= "     LEFT JOIN S_PAY_ALL ON MAIN.SCHREGNO = S_PAY_ALL.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST SCH_B ON MAIN.SCHREGNO = SCH_B.SCHREGNO ";

        $query .= " WHERE ".$where;

        $query .= " ORDER BY ";
        if ($model->search["HR_CLASS_HYOUJI_FLG"] === '1') {
            $query .= "   SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO, ";
        } else {
            $query .= "   MAIN.SCHREGNO, ";
        }
        $query .= "     T1.COLLECT_GRP_CD ";

        return $query;
    }

    function getMDueDat($model, $setYear, $grp_cd, $schno) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_M_DAT T1 ";
        $query .= "     INNER JOIN COLLECT_M_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
        $query .= "           AND T1.COLLECT_M_CD = L1.COLLECT_M_CD ";
        $query .= "           AND L1.COLLECT_S_EXIST_FLG = '2' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$setYear}' ";
        $query .= "     AND T1.SCHREGNO = '{$schno}' ";
        $query .= "     AND T1.COLLECT_GRP_CD = '{$grp_cd}' ";
 
        return $query;
   }

    function getSDueDat($model, $setYear, $grp_cd, $schno) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_S_DAT T1 ";
        $query .= "     INNER JOIN COLLECT_S_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
        $query .= "           AND T1.COLLECT_M_CD = L1.COLLECT_M_CD ";
        $query .= "           AND T1.COLLECT_S_CD = L1.COLLECT_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$setYear}' ";
        $query .= "     AND T1.SCHREGNO = '{$schno}' ";
        $query .= "     AND T1.COLLECT_GRP_CD = '{$grp_cd}' ";
 
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
            list($grp_cd, $schno) = preg_split("/:/",$val);
            $query = knjp706aQuery::getMDueDat($model, $setYear, $grp_cd, $schno);
            $result = $db->query($query);
            $setSeq = 1;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["YEAR"][TEXT]            = $row["YEAR"];
                $data["SCHREGNO"][TEXT]        = $row["SCHREGNO"];
                $data["PAID_SEQ"][NUMBER]      = $setSeq;
                $data["COLLECT_GRP_CD"][TEXT]  = $row["COLLECT_GRP_CD"];
                $data["COLLECT_L_CD"][TEXT]    = $row["COLLECT_L_CD"];
                $data["COLLECT_M_CD"][TEXT]    = $row["COLLECT_M_CD"];
                $data["PAID_INPUT_FLG"][TEXT]  = "2";
                $data["PAID_MONEY_DATE"][DATE] = $model->field["PAID_MONEY_DATE"];
                $data["PAID_MONEY"][NUMBER]    = $row["MONEY_DUE"];
                $data["PAID_MONEY_DIV"][TEXT]  = "03";
                $data["REGISTERCD"][TEXT]      = STAFFCD;
                $data["UPDATED"][NUMBER]       = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_MONEY_PAID_M_DAT", $where);
                $db->query($query);
                $setSeq++;
            }

            $query = knjp706aQuery::getSDueDat($model, $setYear, $grp_cd, $schno);
            $result = $db->query($query);
            $setSeq = 1;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["YEAR"][TEXT]            = $row["YEAR"];
                $data["SCHREGNO"][TEXT]        = $row["SCHREGNO"];
                $data["PAID_SEQ"][NUMBER]      = $setSeq;
                $data["COLLECT_GRP_CD"][TEXT]  = $row["COLLECT_GRP_CD"];
                $data["COLLECT_L_CD"][TEXT]    = $row["COLLECT_L_CD"];
                $data["COLLECT_M_CD"][TEXT]    = $row["COLLECT_M_CD"];
                $data["COLLECT_S_CD"][TEXT]    = $row["COLLECT_S_CD"];
                $data["PAID_INPUT_FLG"][TEXT]  = "2";
                $data["PAID_MONEY_DATE"][DATE] = $model->field["PAID_MONEY_DATE"];
                $data["PAID_MONEY"][NUMBER]    = $row["MONEY_DUE"];
                $data["PAID_MONEY_DIV"][TEXT]  = "03";
                $data["REGISTERCD"][TEXT]      = STAFFCD;
                $data["UPDATED"][NUMBER]       = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_MONEY_PAID_S_DAT", $where);
                $db->query($query);
                $setSeq++;
            }
        }

        //辞退者データ作成
        $un_ent_data = ($model->field["UN_ENT"]) ? $model->field["UN_ENT"]: array();
        foreach ($un_ent_data as $val) {
            list($schno) = preg_split("/:/",$val);
            $query = " DELETE FROM COLLECT_MONEY_DUE_M_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM FRESHMAN_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM COLLECT_MONEY_DUE_PRINT_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM COLLECT_MONEY_DUE_S_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM COLLECT_MONEY_PAID_M_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM COLLECT_MONEY_PAID_S_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM COLLECT_MONEY_REPAY_M_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM COLLECT_MONEY_REPAY_S_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM CHAIR_STD_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM SCHREG_BASE_YEAR_DETAIL_MST WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM SCHREG_SEND_ADDRESS_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM SCHREG_TEXTBOOK_FREE_APPLY_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM SCHREG_TEXTBOOK_FREE_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM SCHREG_TEXTBOOK_SUBCLASS_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
            $query = " DELETE FROM SCH_COMP_DETAIL_DAT WHERE SCHREGNO = '".$schno."' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
