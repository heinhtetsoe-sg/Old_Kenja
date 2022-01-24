<?php

require_once('for_php7.php');

class knjp748query extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/
    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJP748' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年組一覧
    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS AS VALUE, ";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = HDAT.YEAR ";
        $query .= "                                    AND GDAT.GRADE = HDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND HDAT.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /****************/
    /** ＣＳＶ出力 **/
    /****************/
    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     CUST.CUSTOMER_NUMBER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN COLLECT_SCHREG_CUSTOMER_DAT CUST ON CUST.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                                    AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "                                    AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["GRADE_HR_CLASS"] != '') {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //SCHREG_BASE_MST
    function checkBaseMstSql($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    /****************/
    /** ＣＳＶ取込 **/
    /****************/
    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            if ($data_arr[$i]["CUSTOMER_NUMBER"] != '') {
                $dataCnt = $db->getOne(knjp748Query::getCountCustomerDat($data_arr[$i]["SCHREGNO"]));

                //顧客番号が重複していないかチェック
                $retArr = array();
                $query = knjp748Query::getCountCustomerDat2($data_arr[$i]["CUSTOMER_NUMBER"]);
                $custNoChk = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($custNoChk["SCHREGNO"] == '') {
                } else {
                    if ($custNoChk["SCHREGNO"] != $data_arr[$i]["SCHREGNO"]) {
                        $retArr["SCHREGNO"] = $data_arr[$i]["SCHREGNO"];
                        return $retArr;
                    }
                }

                $data = array();
                $data["CUSTOMER_NUMBER"][TEXT]      = $data_arr[$i]["CUSTOMER_NUMBER"];

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                if ($dataCnt < 1) {
                    $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];

                    $query = Query::insertSQL($data, "COLLECT_SCHREG_CUSTOMER_DAT");
                } else {
                    $where  = " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                    $query = Query::updateSQL($data, "COLLECT_SCHREG_CUSTOMER_DAT", $where);
                }
                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //COLLECT_SCHREG_CUSTOMER_DAT
    function getCountCustomerDat($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHREG_CUSTOMER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //COLLECT_SCHREG_CUSTOMER_DAT
    function getCountCustomerDat2($custmoerNo) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHREG_CUSTOMER_DAT ";
        $query .= " WHERE ";
        $query .= "     CUSTOMER_NUMBER = '{$custmoerNo}' ";

        return $query;
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
}
?>
