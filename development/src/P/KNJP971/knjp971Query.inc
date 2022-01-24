<?php

require_once('for_php7.php');

class knjp971Query extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade() {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象の生徒取得
    function getSchregData($model) {
        $query .= " WITH MINOU_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(CASE WHEN VALUE(L1.PAID_MONEY, 0) > 0 ";
        $query .= "              THEN VALUE(L1.PAID_MONEY, 0) ";
        $query .= "              ELSE VALUE(L2.PAID_MONEY, 0) ";
        $query .= "         END ";
        $query .= "     ) AS PAID_MONEY, ";
        $query .= "     SUM(CASE WHEN VALUE(T1.MONEY_DUE, 0) > 0 ";
        $query .= "              THEN VALUE(T1.MONEY_DUE, 0) ";
        $query .= "              ELSE VALUE(L3.MONEY_DUE, 0) ";
        $query .= "         END ";
        $query .= "     ) AS MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_M_DAT T1 ";
        $query .= "     LEFT JOIN COLLECT_MONEY_PAID_M_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L1.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "          AND L1.COLLECT_M_CD = T1.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_MONEY_DUE_S_DAT L3 ON L3.YEAR = T1.YEAR ";
        $query .= "          AND L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L3.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "          AND L3.COLLECT_M_CD = T1.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_MONEY_PAID_S_DAT L2 ON L2.YEAR = L3.YEAR ";
        $query .= "          AND L2.SCHREGNO = L3.SCHREGNO ";
        $query .= "          AND L2.COLLECT_L_CD = L3.COLLECT_L_CD ";
        $query .= "          AND L2.COLLECT_M_CD = L3.COLLECT_M_CD ";
        $query .= "          AND L2.COLLECT_S_CD = L3.COLLECT_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.PAY_DATE < DATE('".str_replace("/","-",$model->field["LIMIT_DATE"])."') ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " HAVING ";
        $query .= "     SUM(CASE WHEN VALUE(L1.PAID_MONEY, 0) > 0 ";
        $query .= "              THEN VALUE(L1.PAID_MONEY, 0) ";
        $query .= "              ELSE VALUE(L2.PAID_MONEY, 0) ";
        $query .= "         END ";
        $query .= "     ) <  ";
        $query .= "     SUM(CASE WHEN VALUE(T1.MONEY_DUE, 0) > 0 ";
        $query .= "              THEN VALUE(T1.MONEY_DUE, 0) ";
        $query .= "              ELSE VALUE(L3.MONEY_DUE, 0) ";
        $query .= "         END ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME AS SCH_NAME, ";
        $query .= "     T1.MONEY_DUE, ";
        $query .= "     VALUE(T1.MONEY_DUE, 0) - VALUE(T1.PAID_MONEY, 0) AS MINOU_GK ";
        $query .= " FROM ";
        $query .= "     MINOU_T T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND REGD.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "           AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "           AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGDH.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //未納の科目と項目を取得
    function getCollectData($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        //対象の科目、項目
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     SUM(CASE WHEN VALUE(L1.PAID_MONEY, 0) > 0 ";
        $query .= "              THEN VALUE(L1.PAID_MONEY, 0) ";
        $query .= "              ELSE VALUE(L2.PAID_MONEY, 0) ";
        $query .= "         END ";
        $query .= "     ) AS PAID_MONEY, ";
        $query .= "     SUM(CASE WHEN VALUE(T1.MONEY_DUE, 0) > 0 ";
        $query .= "              THEN VALUE(T1.MONEY_DUE, 0) ";
        $query .= "              ELSE VALUE(L3.MONEY_DUE, 0) ";
        $query .= "         END ";
        $query .= "     ) AS MONEY_DUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONEY_DUE_M_DAT T1 ";
        $query .= "     LEFT JOIN COLLECT_MONEY_PAID_M_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L1.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "          AND L1.COLLECT_M_CD = T1.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_MONEY_DUE_S_DAT L3 ON L3.YEAR = T1.YEAR ";
        $query .= "          AND L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L3.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "          AND L3.COLLECT_M_CD = T1.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_MONEY_PAID_S_DAT L2 ON L2.YEAR = L3.YEAR ";
        $query .= "          AND L2.SCHREGNO = L3.SCHREGNO ";
        $query .= "          AND L2.COLLECT_L_CD = L3.COLLECT_L_CD ";
        $query .= "          AND L2.COLLECT_M_CD = L3.COLLECT_M_CD ";
        $query .= "          AND L2.COLLECT_S_CD = L3.COLLECT_S_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.PAY_DATE < DATE('".str_replace("/","-",$model->field["LIMIT_DATE"])."') ";
        $query .= "     AND T1.SCHREGNO = '{$schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COLLECT_GRP_CD, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD ";
        $query .= " HAVING ";
        $query .= "     SUM(CASE WHEN VALUE(L1.PAID_MONEY, 0) > 0 ";
        $query .= "              THEN VALUE(L1.PAID_MONEY, 0) ";
        $query .= "              ELSE VALUE(L2.PAID_MONEY, 0) ";
        $query .= "         END ";
        $query .= "     ) <  ";
        $query .= "     SUM(CASE WHEN VALUE(T1.MONEY_DUE, 0) > 0 ";
        $query .= "              THEN VALUE(T1.MONEY_DUE, 0) ";
        $query .= "              ELSE VALUE(L3.MONEY_DUE, 0) ";
        $query .= "         END ";
        $query .= "     ) ";

        return $query;
    }
    
    //UPDATE(更新処理)
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        //リストより選択した学生データ
        $schregdata_array = explode(',', $model->selectleft);
        foreach ($schregdata_array as $schregno) { 
            //対象の未納の科目と項目を取得
            $query = knjp971Query::getCollectData($model, $schregno);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["PAY_DATE"][DATE]   = str_replace("/","-",$model->field["TOKUSOKU_DATE"]);
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][NUMBER]  = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     YEAR = '".CTRL_YEAR."' ";
                $where .= " AND SCHREGNO = '".$row["SCHREGNO"]."' ";
                $where .= " AND COLLECT_GRP_CD = '".$row["COLLECT_GRP_CD"]."' ";
                $where .= " AND COLLECT_L_CD = '".$row["COLLECT_L_CD"]."' ";
                $where .= " AND COLLECT_M_CD = '".$row["COLLECT_M_CD"]."' ";

                $query = Query::updateSQL($data, "COLLECT_MONEY_DUE_M_DAT", $where);
                $db->query($query);
            }
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
    
    
}
?>
