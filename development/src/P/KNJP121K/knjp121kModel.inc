<?php

require_once('for_php7.php');

class knjp121kModel extends Model {
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

    function knjp121kModel()
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
            unset($this->hrclass);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
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

        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->div  = VARS::request("radiodiv");

        $this->field = array("MONEY_DUE"                => VARS::post("MONEY_DUE"),                 //入金必要額
                             "PAID_MONEY"               => VARS::post("PAID_MONEY"),                //入金額
                             "PAID_MONEY_DATE"          => VARS::post("PAID_MONEY_DATE"),           //入金日
                             "PAID_MONEY_DIV"           => VARS::post("PAID_MONEY_DIV"),            //入金区分
                             "REPAY_MONEY"              => VARS::post("REPAY_MONEY"),               //返金額
                             "REPAY_DATE"               => VARS::post("REPAY_DATE"),                //返金日
                             "REPAY_DEV"                => VARS::post("REPAY_DEV"),                 //返金区分
                             "REMARK"                   => VARS::post("REMARK"),                    //備考
                             "TMP_PAID_MONEY"           => VARS::post("TMP_PAID_MONEY"),
                             "TMP_REPAY_MONEY"          => VARS::post("TMP_REPAY_MONEY"),    
                             "TOTALCD"                  => VARS::post("TOTALCD"),
                             "EXPENSE_L_CD"             => substr(VARS::post("TOTALCD"),0,2),
                             "EXPENSE_M_CD"             => substr(VARS::post("TOTALCD"),2,2),
                             "CHECKED1"                 => VARS::post("CHECKED1"),
                             "CHECKED2"                 => VARS::post("CHECKED2"),
                             "CHECKED3"                 => VARS::post("CHECKED3")
                             );

        if (VARS::post("HRCLASS") != "") {
            $this->hrclass = VARS::post("HRCLASS");
        }
        if (VARS::post("SEX") != "") {
            $this->sex = VARS::post("SEX");
        }

        $this->selectdata = VARS::post("selectdata");
    }

    //更新チェック
    function validateInput() {
        if ($this->cmd != "all_update") {
            if (!strlen($this->schregno)) {
                $this->warning = "左の生徒一覧から生徒を選択してください";
                return false;
            }
            if (!strlen($this->exp_mcd)) {
                $this->setWarning("MSG304");
                return false;
            }
        } else {
            if (!strlen($this->field["EXPENSE_M_CD"])) {
                $this->setWarning("MSG304");
                return false;
            }
        }

        //入金額と返金額の大小チェック
        if ((int)$this->field["MONEY_DUE"] < (int)$this->field["TMP_REPAY_MONEY"]) {
            $this->setWarning("MSG901", "必要額、返金額");
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

        //小分類を有する費目(生活行事)は、返金区分のみの入力を認める
        if ($this->cmd == "all_update") {
            $this->s_exist_flg = $this->s_exist_flg_ikkatsu;
        }
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
        if (strlen($this->field["REMARK"]) > 60 ) {
            $this->setWarning("MSG901", "( 備考 ) 60バイトまで");
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();

            $row = $db->getOne(knjp121kQuery::ExistDueData($this));
            if (!strlen($row)) {
                $query = knjp121kQuery::getInsDueQuery($this);
                $db->query($query);
            } else {
                $query = knjp121kQuery::getUpdDueQuery($this);
                $db->query($query);
            }

            $row = $db->getOne(knjp121kQuery::ExistPayData($this));
            if (!strlen($row)) {
                $query = knjp121kQuery::getInsPayQuery($this);
                $db->query($query);
            } else {
                $query = knjp121kQuery::getUpdPayQuery($this);
                $db->query($query);
                $query = knjp121kQuery::getDelPayQuery($this);
                $db->query($query);
            }

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    //一括更新
    function &getAllUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = explode(",", $this->selectdata);
        foreach ($data as $schregno) {
            if (!$this->validateInput()) {
                return;
            }
        }

        $backUpDiv = $this->div;
        foreach ($data as $schregno) {
            //生徒毎に入金データの入金区分（CSV,窓口）を取得する。
            $this->div = $backUpDiv;
            $paidDiv = $db->getOne(knjp121kQuery::getPaidDiv($this, $schregno));
            $this->div = $paidDiv ? $paidDiv : $this->div;

            //入金予定データチェック
            $exist = $db->getOne(knjp121kQuery::ExistDueData($this, $schregno));
            if (!strlen($exist)) {
                $this->setWarning("MSG203", "入金予定データに存在しません。\\n\\n学籍番号：".$schregno);
                Query::dbCheckIn($db);
                return;
            }
            //分納データチェック
            $exist = $db->getOne(knjp121kQuery::MoneyInstData($this, $schregno));
            if (strlen($exist)) {
                $this->setWarning("MSG203", "分納データが存在します。\\n\\n学籍番号：".$schregno);
                Query::dbCheckIn($db);
                return;
            }

            $query = knjp121kQuery::getUpdDueQuery($this, $schregno);
            $db->query($query);

            $row = $db->getOne(knjp121kQuery::ExistPayData($this, $schregno, $this->div));
            if (!strlen($row)) {
                $query = knjp121kQuery::getInsPayQuery($this, $schregno);
                $db->query($query);
            } else {
                $query = knjp121kQuery::getUpdPayQuery($this, $schregno);
                $db->query($query);
                $query = knjp121kQuery::getDelPayQuery($this, $schregno);
                $db->query($query);
            }
        }

        $this->div = $backUpDiv;
        $db->commit();
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }
}
?>
