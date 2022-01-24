<?php

require_once('for_php7.php');


class knjp734Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //Z010取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //認定番号対象の生徒有無
    function checkPassNo($model, $passNo, $beginYearmonth) {
        $ren = substr($passNo, -4);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_AUTHORIZE_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND ((PASSNO IS NULL AND RENBAN = '".$ren."') ";
        $query .= "           OR ";
        $query .= "          (PASSNO = '".$passNo."')) ";

        return $query;
    }

    //更新（ＣＳＶデータより読込）
    function updateQueryCsv($model, $data_arr) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        foreach ($data_arr as $key => $val) {
            $ren = substr($val["PASSNO"], -4);
            list($year, $month, $day) = preg_split("/\//", $val["BEGIN_YEARMONTH"]);

/*
            $getData = $db->getRow(knjp734Query::getReductionAuthorizeDat($model, $val["PASSNO"], $val["BEGIN_YEARMONTH"]), DB_FETCHMODE_ASSOC);
            //データセット（保護者住所データ）
            $issuedate = date("Y-m-d", strtotime($val["ISSUEDATE"]));
            $expiredate = date("Y-m-d", strtotime($val["EXPIREDATE"]));
*/
            $setSql = array();
            $setSql["PASSNO"][TEXT]                   = $val["PASSNO"];
            $setSql["STATUS"][TEXT]                   = sprintf("%02d", $val["STATUS"]);
            $setSql["BEGIN_YEARMONTH"][TEXT]          = $year.$month;
            $setSql["REGISTERCD"][TEXT]               = STAFFCD;
            $setSql["UPDATED"][NUMBER]                = "SYSDATE()";

            $where  = " WHERE RENBAN      = '".$ren."' ";
            $where .= "   AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "   AND SCHOOL_KIND = '".$model->schoolKind."' ";
            $where .= "   AND PASSNO      IS NULL ";

            $query = Query::updateSQL($setSql, "REDUCTION_AUTHORIZE_DAT", $where);
            $db->query($query);

/*
            $zanKikan = $db->getOne(knjp734Query::getZanKikan($model, $getData["SCHREGNO"]));
            $zanKikan = $zanKikan ? $zanKikan : 0;
            $planKikan = $db->getOne(knjp734Query::getPlanKikan($model, $getData["SCHREGNO"]));
            $planKikan = $planKikan ? $planKikan : 0;
            $getData["ZANKIKAN"] = $zanKikan > $planKikan ? $zanKikan - $planKikan : 0;
*/
//            knjp734Query::setPlanDat($db, $model, $val, $getData);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;

    }

    //認定番号対象の生徒有無
    function getReductionAuthorizeDat($model, $passNo, $beginYearmonth) {
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
    function getZanKikan($model, $schregno) {
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
    function getPlanKikan($model, $schregno) {
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
                $planCnt = $db->getOne(knjp734Query::getPlanCnt($getData["SCHREGNO"], $zenkiVal));
                if ($planCnt == 0) {
                    knjp734Query::insertPlan($db, $model, $zenkiVal, $setMoney, $getData);
                } else {
                    knjp734Query::updatePlan($db, $model, $zenkiVal, $setMoney, $getData);
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
                $planCnt = $db->getOne(knjp734Query::getPlanCnt($getData["SCHREGNO"], $koukiVal));
                if ($planCnt == 0) {
                    knjp734Query::insertPlan($db, $model, $koukiVal, $setMoney, $getData);
                } else {
                    knjp734Query::updatePlan($db, $model, $koukiVal, $setMoney, $getData);
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

    /********************/
    /**  エラー処理用  **/
    /********************/
    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //ＣＳＶ出力
    function selectCsvData($model) {
        $query  = " SELECT ";
        $query .= "     L_AUTH.PASSNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     L_AUTH.STATUS, ";
        $query .= "     case when CHECK.INTENTION_YES_FLG = '1' then '' ";
        $query .= "          when CHECK.INTENTION_NO_FLG  = '1' then '1' ";
        $query .= "     end AS INTENTION_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             L1.SCHREGNO, ";
        $query .= "             VALUE(L1.ZIPCD, '') || VALUE(L1.ADDR1, '') || VALUE(L1.ADDR2, '') AS ADDR ";
        $query .= "         FROM ";
        $query .= "             SCHREG_ADDRESS_DAT L1, ";
        $query .= "             (SELECT ";
        $query .= "                  E1.SCHREGNO, ";
        $query .= "                  MAX(E1.ISSUEDATE) AS ISSUEDATE ";
        $query .= "              FROM ";
        $query .= "                  SCHREG_ADDRESS_DAT E1 ";
        $query .= "              WHERE ";
        $query .= "                  E1.SCHREGNO = '".$schregno."' ";
        $query .= "              GROUP BY ";
        $query .= "                  E1.SCHREGNO ";
        $query .= "             ) L2 ";
        $query .= "         WHERE ";
        $query .= "             L1.SCHREGNO = '".$schregno."' ";
        $query .= "             AND L1.SCHREGNO = L2.SCHREGNO ";
        $query .= "             AND L1.ISSUEDATE = L2.ISSUEDATE ";
        $query .= "     ) ADDR ON BASE.SCHREGNO = ADDR.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                   AND REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                   AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     LEFT JOIN REDUCTION_AUTHORIZE_DAT L_AUTH ON L_AUTH.SCHREGNO    = BASE.SCHREGNO ";
        $query .= "                                             AND L_AUTH.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                                             AND L_AUTH.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_APPLICANT_CHECK_DAT CHECK ON CHECK.SCHOOLCD    = L_AUTH.SCHOOLCD ";
        $query .= "                                                          AND CHECK.SCHOOL_KIND = L_AUTH.SCHOOL_KIND ";
        $query .= "                                                          AND CHECK.YEAR        = REGD.YEAR ";
        $query .= "                                                          AND CHECK.MONTH       = '{$model->month}' ";
        $query .= "                                                          AND CHECK.SCHREGNO    = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     L_AUTH.PASSNO IS NOT NULL ";

        return $query;
    }

}
?>
