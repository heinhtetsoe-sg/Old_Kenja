<?php

require_once('for_php7.php');
class knjp120kModel extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $schregno;
    var $exp_mcd;
    var $exp_lcd;
    var $exp_mname;
    var $money_due;
    var $s_exist_flg;
    var $paid_flg;
    var $paid_input_flg;
    var $inst_cd;
    var $bank_date;

    function knjp120kModel()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->exp_mcd);
            unset($this->exp_lcd);
            unset($this->exp_mname);
            unset($this->money_due);
            unset($this->s_exist_flg);
            unset($this->inst_cd);
            unset($this->paid_flg);
            unset($this->paid_input_flg);
            unset($this->bank_date);
        }

        if(strlen(VARS::get("SCHREGNO"))){
            $this->schregno = VARS::get("SCHREGNO");
            unset($this->exp_mcd);
            unset($this->exp_lcd);
            unset($this->exp_mname);
            unset($this->money_due);
            unset($this->s_exist_flg);
            unset($this->inst_cd);
            unset($this->paid_flg);
            unset($this->paid_input_flg);
            unset($this->bank_date);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if(VARS::get("init") != ""){
            unset($this->schregno);
            unset($this->exp_mcd);
            unset($this->exp_lcd);
            unset($this->exp_mname); 
        }

        if(strlen(VARS::get("EXPENSE_M_CD"))){
            $this->exp_mcd          = VARS::get("EXPENSE_M_CD");
            $this->paid_input_flg   = VARS::get("PAID_INPUT_FLG");
            $this->s_exist_flg      = VARS::get("EXPENSE_S_EXIST_FLG");
            $this->inst_cd          = VARS::get("INST_CD");

            //入金データが編集可能か判断
            if (VARS::get("PAID_INPUT_FLG") == "1") {
                $this->paid_flg = true;
            } else {
                unset($this->paid_flg);
            }

            //振替停止が編集可能か判断
#            $tdate = date("Ymd");  2005/12/12
            $tdate = str_replace("-", "", CTRL_DATE);
            $sdate = str_replace("-", "", VARS::get("BANK_TRANS_SDATE"));

            if ($tdate > $sdate) {
                $this->bank_date = true;
            } else {
                unset($this->bank_date);
            }
        }

        if(strlen(VARS::get("EXPENSE_L_CD"))){
            $this->exp_lcd = VARS::get("EXPENSE_L_CD");
        }

        if(strlen(VARS::get("EXPENSE_M_NAME"))){
            $this->exp_mname = VARS::get("EXPENSE_M_NAME");
        }

        if(strlen(VARS::get("MONEY_DUE"))){
            $this->money_due = VARS::get("MONEY_DUE");
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        $this->field = array("PAID_MONEY"               => VARS::post("PAID_MONEY"),                //入金額
                             "PAID_MONEY_DATE"          => VARS::post("PAID_MONEY_DATE"),           //入金日
                             "PAID_MONEY_DIV"           => VARS::post("PAID_MONEY_DIV"),            //入金区分
                             "REPAY_MONEY"              => VARS::post("REPAY_MONEY"),               //返金額
                             "REPAY_DATE"               => VARS::post("REPAY_DATE"),                //返金日
                             "REPAY_DEV"                => VARS::post("REPAY_DEV"),                 //返金区分
                             "BANK_TRANS_STOP_RESON"    => VARS::post("BANK_TRANS_STOP_RESON"),     //振替停止区分
                             "TMP_PAID_MONEY"           => VARS::post("TMP_PAID_MONEY"),
                             "TMP_REPAY_MONEY"          => VARS::post("TMP_REPAY_MONEY"));    
    }

    function validateInput()
    {
        if (!strlen($this->schregno)) {
            $this->warning = "左の生徒一覧から生徒を選択してください";
            return false;
        }
        if (!strlen($this->exp_mcd)) {
            $this->setWarning("MSG304");
            return false;
        }

        //入金額と返金額の大小チェック
        if ((int)$this->field["TMP_PAID_MONEY"] < (int)$this->field["TMP_REPAY_MONEY"]) {
            $this->setWarning("MSG901", "入金額、返金額");
            return;
        }
        //入金データ入力チェック
        if (strlen($this->field["PAID_MONEY"]) || strlen($this->field["PAID_MONEY_DATE"]) || strlen($this->field["PAID_MONEY_DIV"])) {
            if (!strlen($this->field["PAID_MONEY"]) || !strlen($this->field["PAID_MONEY_DATE"]) || !strlen($this->field["PAID_MONEY_DIV"])) {
                if (!strlen($this->field["PAID_MONEY"])) {
                    $msg = "入金額";
                }
                if (!strlen($this->field["PAID_MONEY_DATE"])) {
                    $msg = (strlen($msg)) ? $msg."、入金日" : "入金日";
                }
                if (!strlen($this->field["PAID_MONEY_DIV"])) {
                    $msg = (strlen($msg)) ? $msg."、入金区分" : "入金区分";
                }
                $this->setWarning("MSG301", $msg);
                return;
            }
        }

        //小分類を有する費目(生活行事)は、返金区分のみの入力を認める    2005/12/13
        if ( $this->s_exist_flg == "1" && !strlen($this->field["REPAY_MONEY"]) && !strlen($this->field["REPAY_DATE"]) && strlen($this->field["REPAY_DEV"])) {

            if ((int)$this->field["TMP_PAID_MONEY"] == 0 ) {
                $this->setWarning("MSG901", "入金額、返金額");
                return;
            }

            return true;
        }

        //返金金データ入力チェック
        if (strlen($this->field["REPAY_MONEY"]) || strlen($this->field["REPAY_DATE"]) || strlen($this->field["REPAY_DEV"])) {
            if (!strlen($this->field["REPAY_MONEY"]) || !strlen($this->field["REPAY_DATE"]) || !strlen($this->field["REPAY_DEV"])) {
                if (!strlen($this->field["REPAY_MONEY"])) {
                    $msg = "返金額";
                }
                if (!strlen($this->field["REPAY_DATE"])) {
                    $msg = (strlen($msg)) ? $msg."、返金日" : "返金日";
                }
                if (!strlen($this->field["REPAY_DEV"])) {
                    $msg = (strlen($msg)) ? $msg."、返金区分" : "返金区分";
                }
                $this->setWarning("MSG301", $msg);
                return;
            }
        }
        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();

            $row = $db->getOne(knjp120kQuery::ExistData($this));
            if (!strlen($row)) {
                $query = knjp120kQuery::getInsertQuery($this);
                $db->query($query);
            } else {
#                if ($this->s_exist_flg != "1") {   #2005/12/13 小分類費目有りの場合でも更新対象とする。
                    $query = knjp120kQuery::getUpdateQuery($this);
                    $db->query($query);
                    $query = knjp120kQuery::getDeleteQuery($this);
                    $db->query($query);
#                }
            }

            if (!strlen($this->bank_date)) {
                $db->query(knjp120kQuery::getUpdateQuery2($this));
            }

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
