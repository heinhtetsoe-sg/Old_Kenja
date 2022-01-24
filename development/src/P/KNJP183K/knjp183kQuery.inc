<?php

require_once('for_php7.php');


class knjp183kQuery extends Query {

    //認定番号対象の生徒有無
    function checkPassNo($model, $passNo, $beginYearmonth)
    {
        list($nen, $ken, $school, $ren) = preg_split("/-/", $passNo);
        $dataDivSub = "1";
        if ($model->field["DATA_DIV"] == "2") {
            list($year, $month, $day) = preg_split("/\//", $beginYearmonth);
            $dataDivSub = (int) $month == 4 || (int) $month == 5 || (int) $month == 6 ? "1" : "2";
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_AUTHORIZE_DAT ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV = '".$model->field["DATA_DIV"]."' ";
        $query .= "     AND DATA_DIV_SUB = '".$dataDivSub."' ";
        if ($model->field["DATA_DIV"] == "1") {
            $query .= "     AND ((PASSNO IS NULL AND RENBAN = '".$ren."') ";
            $query .= "           OR ";
            $query .= "          (PASSNO = '".$passNo."')) ";
        } else {
            $query .= "     AND PASSNO = '".$passNo."' ";
        }
        return $query;
    }

    //更新（ＣＳＶデータより読込）
    function updateQueryCsv($model, $data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        foreach ($data_arr as $key => $val) {

            $getData = $db->getRow(knjp183kQuery::getReductionAuthorizeDat($model, $val["PASSNO"], $val["BEGIN_YEARMONTH"]), DB_FETCHMODE_ASSOC);
            //データセット（保護者住所データ）
            $issuedate = date("Y-m-d", strtotime($val["ISSUEDATE"]));
            $expiredate = date("Y-m-d", strtotime($val["EXPIREDATE"]));
            $setSql = array();
            $setSql["PASSNO"][TEXT]                   = $val["PASSNO"];
            $setSql["STATUS"][TEXT]                   = sprintf("%02d", $val["STATUS"]);
            if ($model->field["DATA_DIV"] == "1") {
                $setSql["RECEIVE_MONEY"][NUMBER]          = $val["RECEIVE_MONEY"];
                $setSql["REMAIN_SUP_LIMIT_MONTH"][TEXT]   = $val["REMAIN_SUP_LIMIT_MONTH"];
            } else {
                $setSql["RECEIVE_MONEY"][NUMBER]          = $val["ADD_MONEY"];
            }
            $setSql["REGISTERCD"][TEXT]               = STAFFCD;
            $setSql["UPDATED"][NUMBER]                = "SYSDATE()";

            $where  = " WHERE SCHREGNO = '".$getData["SCHREGNO"]."' ";
            $where .= "       AND DATA_DIV = '".$getData["DATA_DIV"]."' ";
            $where .= "       AND DATA_DIV_SUB = '".$getData["DATA_DIV_SUB"]."' ";

            $query = Query::updateSQL($setSql, "REDUCTION_AUTHORIZE_DAT", $where);
            $db->query($query);

            $zanKikan = $db->getOne(knjp183kQuery::getZanKikan($model, $getData["SCHREGNO"]));
            $zanKikan = $zanKikan ? $zanKikan : 0;
            $planKikan = $db->getOne(knjp183kQuery::getPlanKikan($model, $getData["SCHREGNO"]));
            $planKikan = $planKikan ? $planKikan : 0;
            $getData["ZANKIKAN"] = $zanKikan > $planKikan ? $zanKikan - $planKikan : 0;

            knjp183kQuery::setPlanDat($db, $model, $val, $getData);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;

    }

    //認定番号対象の生徒有無
    function getReductionAuthorizeDat($model, $passNo, $beginYearmonth)
    {
        list($nen, $ken, $school, $ren) = preg_split("/-/", $passNo);
        $dataDivSub = "1";
        if ($model->field["DATA_DIV"] == "2") {
            list($year, $month, $day) = preg_split("/\//", $beginYearmonth);
            $dataDivSub = (int) $month == 4 || (int) $month == 5 || (int) $month == 6 ? "1" : "2";
        }

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REDUCTION_AUTHORIZE_DAT ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV = '".$model->field["DATA_DIV"]."' ";
        $query .= "     AND DATA_DIV_SUB = '".$dataDivSub."' ";
        if ($model->field["DATA_DIV"] == "1") {
            $query .= "     AND ((PASSNO IS NULL AND RENBAN = '".$ren."') ";
            $query .= "           OR ";
            $query .= "          (PASSNO = '".$passNo."')) ";
        } else {
            $query .= "     AND PASSNO = '".$passNo."' ";
        }

        return $query;
    }

    //生徒残期間
    function getZanKikan($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     REMAIN_SUP_LIMIT_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_AUTHORIZE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= "     AND DATA_DIV = '1' ";
        $query .= "     AND DATA_DIV_SUB = '1' ";

        return $query;
    }

    //生徒残期間
    function getPlanKikan($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR ."' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND ADD_PLAN_MONEY IS NOT NULL ";

        return $query;
    }

    function setPlanDat($db, $model, $val, $getData) {

        /*************************/
        /* REDUCTION_COUNTRY_DAT */
        /*************************/
        $cuntryCnt = $db->getOne("SELECT COUNT(*) FROM REDUCTION_COUNTRY_DAT WHERE YEAR = '".CTRL_YEAR ."' AND SCHREGNO = '{$getData["SCHREGNO"]}'");
        $setGrade = $db->getOne("SELECT GRADE FROM SCHREG_REGD_DAT WHERE SCHREGNO = '{$getData["SCHREGNO"]}' AND YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."'");
        $data = array();
        if ($model->field["DATA_DIV"] == "1") {
            $setMoney = $val["RECEIVE_MONEY"];
            $data["REDUCTIONMONEY_1"][NUMBER] = $setMoney;
            $data["REDUC_DEC_FLG_1"][TEXT]    = "1";
            $month = "04";
        } else {
            $setMoney = $val["ADD_MONEY"];
            $beginYearmonth = $val["BEGIN_YEARMONTH"];
            list($year, $month, $day) = preg_split("/\//", $beginYearmonth);
            $dataDivSub = (int) $month == 4 || (int) $month == 5 || (int) $month == 6 ? "1" : "2";

            $data["REDUCTION_ADD_MONEY_".$dataDivSub][NUMBER]  = $setMoney;
            $data["REDUC_ADD_FLG_".$dataDivSub][TEXT]          = "1";
        }

        $data["GRADE"][TEXT]                    = $setGrade;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR        = '".CTRL_YEAR ."' AND ";
        $where .= "     SCHREGNO    = '{$getData["SCHREGNO"]}' ";

        if ($cuntryCnt == 0) {
            $data["YEAR"][TEXT]     = CTRL_YEAR;
            $data["SCHREGNO"][TEXT] = $getData["SCHREGNO"];
            $query = Query::insertSQL($data, "REDUCTION_COUNTRY_DAT");
        } else {
            $query = Query::updateSQL($data, "REDUCTION_COUNTRY_DAT", $where);
        }
        $db->query($query);

        /******************************/
        /* REDUCTION_COUNTRY_PLAN_DAT */
        /******************************/
        $zanKikan = $getData["ZANKIKAN"] ? $getData["ZANKIKAN"] : 0;
        if ($model->field["DATA_DIV"] == "1") {
            $arrayMonth = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");
        } else if ($dataDivSub == "1") {
            $arrayMonth = array("04", "05", "06");
        } else {
            $arrayMonth = array("07", "08", "09", "10", "11", "12", "01", "02", "03");
        }
        $setMonth = array();
        $monthFlg = false;
        foreach ($arrayMonth as $key => $val) {
            if ((int) $val == (int) $month) {
                $monthFlg = true;
            }
            if ($monthFlg) {
                $setMonth[] = $val;
            }
        }
        $setCnt = 1;
        if ($model->field["DATA_DIV"] == "1" || $dataDivSub == "1") {
            foreach ($setMonth as $key => $zenkiVal) {
                if ($setCnt > $zanKikan) {
                    break;
                }
                //計画データの有無
                $planCnt = $db->getOne(knjp183kQuery::getPlanCnt($getData["SCHREGNO"], $zenkiVal));
                if ($planCnt == 0) {
                    knjp183kQuery::insertPlan($db, $model, $zenkiVal, $setMoney, $getData);
                } else {
                    knjp183kQuery::updatePlan($db, $model, $zenkiVal, $setMoney, $getData);
                }
                $setCnt++;
            }
        }
        if ($model->field["DATA_DIV"] == "1" || $dataDivSub == "2") {
            foreach ($setMonth as $key => $koukiVal) {
                if ($setCnt > $zanKikan) {
                    break;
                }
                //計画データの有無
                $planCnt = $db->getOne(knjp183kQuery::getPlanCnt($getData["SCHREGNO"], $koukiVal));
                if ($planCnt == 0) {
                    knjp183kQuery::insertPlan($db, $model, $koukiVal, $setMoney, $getData);
                } else {
                    knjp183kQuery::updatePlan($db, $model, $koukiVal, $setMoney, $getData);
                }
                $setCnt++;
            }
        }

    }

    function getPlanCnt($schregno, $month) {
        $year = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND PLAN_YEAR = '".$year."' ";
        $query .= "     AND PLAN_MONTH = '".$month."' ";

        return $query;
    }

    function insertPlan($db, $model, $month, $setMoney, $getData) {
        $data = array();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]         = $getData["SCHREGNO"];
        $data["PLAN_YEAR"][TEXT]        = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $data["PLAN_MONTH"][TEXT]       = $month;
        if ($model->field["DATA_DIV"] == "1") {
            $data["PLAN_MONEY"][NUMBER]     = $setMoney;
        } else {
            $data["ADD_PLAN_MONEY"][NUMBER] = $setMoney;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_COUNTRY_PLAN_DAT");
        $db->query($query);
    }

    function updatePlan($db, $model, $month, $setMoney, $getData) {
        $data = array();

        if ($model->field["DATA_DIV"] == "1") {
            $data["PLAN_MONEY"][NUMBER]     = $setMoney;
        } else {
            $data["ADD_PLAN_MONEY"][NUMBER] = $setMoney;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $where  = " WHERE ";
        $where .= "     YEAR            = '".CTRL_YEAR."' ";
        $where .= "     AND SCHREGNO    = '".$getData["SCHREGNO"]."' ";
        $where .= "     AND PLAN_YEAR   = '".$setYear."' ";
        $where .= "     AND PLAN_MONTH  = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);

        $db->query($query);
    }

    //エラーデータの削除
    function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "w_csvmsg_dat");

        $result = $db->query($query);

    }

    //CVSエラー作成用のQUERY
    function selectCsvErrQuery()
    {
        $query  = "select * from w_csvmsg_dat";

        return $query;
    }

}
?>
