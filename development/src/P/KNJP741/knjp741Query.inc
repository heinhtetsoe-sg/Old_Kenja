<?php

require_once('for_php7.php');

class knjp741Query extends Query {

    //返金データ
    function selectQuery($model, $selectDiv = "")
    {
        $query  = " SELECT ";
        $query .= "     REPAY_D.SCHOOLCD, ";
        $query .= "     REPAY_D.SCHOOL_KIND, ";
        $query .= "     REPAY_D.YEAR, ";
        $query .= "     REPAY_D.REPAY_SLIP_NO, ";
        $query .= "     REPAY_D.SCHREGNO, ";
        $query .= "     REPAY_D.REPAY_DATE, ";
        $query .= "     REPAY_D.REPAY_DIV, ";
        $query .= "     REPAY_D.REPAY_MONEY, ";
        $query .= "     REPAY_D.REPAID_DATE, ";
        $query .= "     REPAY_D.CANCEL_FLG ";
        $query .= " FROM ";
        $query .= "     COLLECT_REPAY_SLIP_DAT REPAY_D ";
        $query .= " WHERE ";
        $query .= "     REPAY_D.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND REPAY_D.SCHREGNO = '{$model->schregno}' ";
        if ($selectDiv == "bottom") {
            $query .= "     AND REPAY_D.SCHOOL_KIND   = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "     AND REPAY_D.YEAR          = '{$model->field["YEAR"]}' ";
            $query .= "     AND REPAY_D.REPAY_SLIP_NO = '{$model->field["REPAY_SLIP_NO"]}' ";
        } else {
            $query .= "     AND VALUE(REPAY_D.CANCEL_FLG, '0') <> '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REPAY_D.REPAY_DATE DESC, ";
        $query .= "     REPAY_D.REPAY_SLIP_NO DESC ";

        return $query;
    }

    //対応者を取得
    function getStdInfo($schregno)
    {
        $query  = " SELECT ";
        $query .= "   REGDH.HR_NAME, ";
        $query .= "   REGD.ATTENDNO, ";
        $query .= "   BASE.SCHREGNO, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.GRD_DATE, ";
        $query .= "   GDAT.SCHOOL_KIND, ";
        $query .= "   A003.NAME1 ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE_MST BASE ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "        AND BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "        AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "        AND REGD.GRADE = REGDH.GRADE ";
        $query .= "        AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
        $query .= "        AND REGD.GRADE = GDAT.GRADE ";
        $query .= "   LEFT JOIN NAME_MST A003 ON A003.NAMECD1 = 'A003' ";
        $query .= "        AND A003.NAMECD2 = GRD_DIV";
        $query .= " WHERE ";
        $query .= "   BASE.SCHREGNO = '" .$schregno."' ";
        return $query;
    }

    // INSERT
    function insertData($db, &$model)
    {
        $model->field["REPAY_SLIP_NO"] = $model->field["YEAR"].$model->schregno."001";
        $result = $db->query(knjp741Query::getMaxSlipNo($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SLIP_NO"]) {
                $model->field["REPAY_SLIP_NO"] = substr($row["SLIP_NO"], 0, 12).sprintf("%03d", (substr($row["SLIP_NO"], 12) + 1));
            }
        }
        $result->free();

        $data["SCHOOLCD"][TEXT]         = $model->field["SCHOOLCD"];
        $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
        $data["YEAR"][TEXT]             = $model->field["YEAR"];
        $data["REPAY_SLIP_NO"][TEXT]    = $model->field["REPAY_SLIP_NO"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["REPAY_DATE"][DATE]       = $model->field["REPAY_DATE"];
        $data["REPAY_DIV"][TEXT]        = $model->field["REPAY_DIV"];
        $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
        $data["CANCEL_FLG"][TEXT]       = $model->field["CANCEL_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_REPAY_SLIP_DAT");
        $db->query($query);
        return;
    }

    //MAX伝票番号
    function getMaxSlipNo($model) {
        $query  = " SELECT ";
        $query .= "     MAX(REPAY_SLIP_NO) AS SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_REPAY_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '{$model->field["SCHOOLCD"]}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "     AND YEAR        = '{$model->field["YEAR"]}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";

        return $query;
    }

    //UPDATE
    function updateData($db, $model)
    {
        $data["REPAY_DATE"][DATE]       = $model->field["REPAY_DATE"];
        $data["REPAY_DIV"][TEXT]        = $model->field["REPAY_DIV"];
        $data["REPAY_MONEY"][NUMBER]    = $model->field["REPAY_MONEY"];
        $data["CANCEL_FLG"][TEXT]       = $model->field["CANCEL_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE SCHOOLCD      = '{$model->field["SCHOOLCD"]}' ";
        $where .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
        $where .= " AND YEAR            = '{$model->field["YEAR"]}' ";
        $where .= " AND REPAY_SLIP_NO   = '{$model->field["REPAY_SLIP_NO"]}' ";

        $query = Query::updateSQL($data, "COLLECT_REPAY_SLIP_DAT", $where);
        $db->query($query);
        return;
    }

    // DELETE
    function deleteData($db, $model)
    {

        $query = " DELETE FROM ";
        $query .= "   COLLECT_REPAY_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD      = '{$model->field["SCHOOLCD"]}' ";
        $query .= "     AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "     AND YEAR            = '{$model->field["YEAR"]}' ";
        $query .= "     AND REPAY_SLIP_NO   = '{$model->field["REPAY_SLIP_NO"]}' ";

        $db->query($query);
        return;
    }

}
?>
