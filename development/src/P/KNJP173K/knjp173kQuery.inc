<?php

require_once('for_php7.php');

class knjp173kQuery extends Query {

    //支援金予定額
    function getSelectCountry($model, $div)
    {
        $query  = " SELECT ";
        $query .= "     PLAN_MONTH AS SET_MONTH, ";
        $query .= "     PLAN_CANCEL_FLG, ";
        $query .= "     ADD_PLAN_CANCEL_FLG, ";
        $query .= "     PLAN_LOCK_FLG, ";
        $query .= "     ADD_PLAN_LOCK_FLG, ";
        if ($div == "DUE") {
            $query .= "     PLAN_MONEY AS SET_MONEY, ";
            $query .= "     PAID_YEARMONTH AS PAID_YEARMONTH ";
        } else if ($div == "DUE_PAY") {
            $query .= "     PAID_MONEY AS SET_MONEY, ";
            $query .= "     PAID_YEARMONTH AS PAID_YEARMONTH ";
        } else if ($div == "ADD") {
            $query .= "     ADD_PLAN_MONEY AS SET_MONEY, ";
            $query .= "     ADD_PAID_YEARMONTH AS PAID_YEARMONTH ";
        } else {
            $query .= "     ADD_PAID_MONEY AS SET_MONEY, ";
            $query .= "     ADD_PAID_YEARMONTH AS PAID_YEARMONTH ";
        }
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($schregno)
    {
        $query  = " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.NAME, ";
        $query .= "   L2.HR_NAMEABBV, ";
        $query .= "   L1.ATTENDNO, ";
        $query .= "   L3.PASSNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND L1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ON L1.YEAR = L2.YEAR ";
        $query .= "       AND L1.SEMESTER = L2.SEMESTER ";
        $query .= "       AND L1.GRADE = L2.GRADE ";
        $query .= "       AND L1.HR_CLASS = L2.HR_CLASS ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L3 ON L1.SCHREGNO = L3.SCHREGNO ";
        $query .= "          AND L3.DATA_DIV = '1' ";
        $query .= "          AND L3.DATA_DIV_SUB = '1' ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($schregno)
    {
        $db = Query::dbCheckOut();
        $query  = "SELECT * FROM schreg_train_dat ";
        $query .= " WHERE year    = '".CTRL_YEAR."'";
        $query .= " AND  schregno = '".$schregno. "'";
        
        $record = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $record[$row["SEMESTER"]] = array("caution"    => $row["CAUTION_GUIDANCE"],
                                              "admonition" => $row["ADMONITION_GUIDANCE"]);    
        }
        Query::dbCheckIn($db);
        return $record;
    }

    function getUpdatePlanQuery($db, $model, $fields) {

        $data["PLAN_CANCEL_FLG"][TEXT]     = $fields["PLAN_CANCEL_FLG"];
        $data["ADD_PLAN_CANCEL_FLG"][TEXT] = $fields["ADD_PLAN_CANCEL_FLG"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $where  = " WHERE YEAR = '".CTRL_YEAR."' ";
        $where .= "   AND SCHREGNO = '".$model->schregno."' ";
        $where .= "   AND PLAN_YEAR = '".$fields["PLAN_YEAR"]."' "; 
        $where .= "   AND PLAN_MONTH = '".$fields["PLAN_MONTH"]."' "; 

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);
        return $query;
    }

    function getUpdateQuery($db, $model) {
        $query  = " SELECT ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS ZENKI_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".CTRL_YEAR."07' AND ADD_PLAN_MONEY IS NOT NULL AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS KOUKI_CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        $dataCnt = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($dataCnt["ZENKI_CNT"] > 0) {
            $data["REDUC_ADD_FLG_1"][TEXT] = "1";
        } else {
            $data["REDUC_ADD_FLG_1"][TEXT] = NULL;
        }

        if ($dataCnt["KOUKI_CNT"] > 0) {
            $data["REDUC_ADD_FLG_2"][TEXT] = "1";
        } else {
            $data["REDUC_ADD_FLG_2"][TEXT] = NULL;
        }

        $data["REDUC_DEC_FLG_1"][TEXT]    = $model->decFlg == "2" ? NULL : "1";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE YEAR = '".CTRL_YEAR."' ";
        $where .= "   AND SCHREGNO = '".$model->schregno."' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_DAT", $where);
        return $query;
    }
}
?>
