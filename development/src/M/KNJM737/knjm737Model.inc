<?php

require_once('for_php7.php');
class knjm737Model extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $schregno;
    var $exp_scd;
    var $exp_mcd;
    var $exp_lcd;
    var $exp_mname;
    var $money_due;
    var $s_exist_flg;
    var $collect_grp_cd;
    var $paid_flg;
    var $pay_div;

    function knjm737Model()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->collect_grp_cd);
            unset($this->exp_scd);
            unset($this->exp_mcd);
            unset($this->exp_lcd);
            unset($this->exp_mname);
            unset($this->money_due);
            unset($this->s_exist_flg);
            unset($this->paid_flg);
            unset($this->pay_div);
        }

        if(strlen(VARS::get("SCHREGNO"))){
            $this->schregno = VARS::get("SCHREGNO");
            unset($this->collect_grp_cd);
            unset($this->exp_scd);
            unset($this->exp_mcd);
            unset($this->exp_lcd);
            unset($this->exp_mname);
            unset($this->money_due);
            unset($this->s_exist_flg);
            unset($this->paid_flg);
            unset($this->pay_div);
            $this->search_div = VARS::get("SEARCH_DIV");
        }

        if (VARS::get("init") != "") {
            unset($this->schregno);
            unset($this->collect_grp_cd);
            unset($this->exp_scd);
            unset($this->exp_mcd);
            unset($this->exp_lcd);
            unset($this->exp_mname); 
        }

        if (strlen(VARS::get("COLLECT_S_CD"))) {
            $this->collect_grp_cd   = VARS::get("COLLECT_GRP_CD");
            $this->exp_scd          = VARS::get("COLLECT_S_CD");
            $this->exp_mcd          = VARS::get("COLLECT_M_CD");
            $this->pay_div          = VARS::get("PAY_DIV");
            $this->s_exist_flg      = VARS::get("COLLECT_S_EXIST_FLG");

            //入金データが編集可能か判断
            if (VARS::get("PAY_DIV") == "1") {
                $this->paid_flg = true;
            } else {
                unset($this->paid_flg);
            }

            //振替停止が編集可能か判断
            $tdate = str_replace("-", "", CTRL_DATE);

        }

        if (strlen(VARS::get("COLLECT_L_CD"))) {
            $this->exp_lcd = VARS::get("COLLECT_L_CD");
        }

        if (strlen(VARS::get("COLLECT_S_NAME"))) {
            $this->exp_sname = VARS::get("COLLECT_S_NAME");
        }

        if (strlen(VARS::get("MONEY_DUE"))) {
            $this->money_due = VARS::get("MONEY_DUE");
        }

        $this->cmd = VARS::request("cmd");
        $this->year = $this->search_div == "1" ? (CTRL_YEAR + 1) : CTRL_YEAR;
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("TANKA"            => VARS::post("TANKA"),             //単価
                             "COLLECT_CNT"      => VARS::post("COLLECT_CNT"),       //数量
                             "MONEY_DUE"        => VARS::post("MONEY_DUE"),         //予定額
                             "PAY_DIV"          => VARS::post("PAY_DIV"),           //入金種別
                             "PAY_DATE"         => VARS::post("PAY_DATE"),          //納入日
                             "CHANGE_DATE_F"    => VARS::post("CHANGE_DATE_F"),     //期間F
                             "CHANGE_DATE_T"    => VARS::post("CHANGE_DATE_T"),     //期間T
                             "CHANGE_CD"        => VARS::post("CHANGE_CD"),         //徴収変更
                             "CHANGE_REMARK"    => VARS::post("CHANGE_REMARK"),     //徴収備考
                             "SINSEI_DATE"      => VARS::post("SINSEI_DATE"),       //申請日
                             "GENMEN_MONEY"     => VARS::post("GENMEN_MONEY"),      //減免額
                             "GENMEN_CNT"       => VARS::post("GENMEN_CNT"),        //減免科目数
                             "PAID_SEQ"         => VARS::post("PAID_SEQ"),          //入金SEQ
                             "PAID_MONEY"       => VARS::post("PAID_MONEY"),        //入金額
                             "PAID_MONEY_DATE"  => VARS::post("PAID_MONEY_DATE"),   //入金日
                             "PAID_MONEY_DIV"   => VARS::post("PAID_MONEY_DIV"),    //入金区分
                             "REPAY_SEQ"        => VARS::post("REPAY_SEQ"),         //返金SEQ
                             "REPAY_MONEY"      => VARS::post("REPAY_MONEY"),       //返金額
                             "REPAY_MONEY_DATE" => VARS::post("REPAY_MONEY_DATE"),  //返金日
                             "REPAY_MONEY_DIV"  => VARS::post("REPAY_MONEY_DIV"),   //返金区分
                             );    
    }

    function validateInput()
    {
        if (!strlen($this->schregno)) {
            $this->warning = "左の生徒一覧から生徒を選択してください";
            return false;
        }
        if (!strlen($this->exp_lcd)) {
            $this->setWarning("MSG304");
            return false;
        }
        if (!strlen($this->exp_mcd)) {
            $this->setWarning("MSG304");
            return false;
        }
        if (!strlen($this->exp_scd)) {
            $this->setWarning("MSG304");
            return false;
        }

        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();

            if ($this->cmd == "dueUpdate") {
                $query = knjm737Query::insertDue($db, $this);
                $db->query($query);
            }
            if ($this->cmd == "paidUpdate") {
                $query = knjm737Query::insertPaid($db, $this);
                $db->query($query);
            }
            if ($this->cmd == "repayUpdate") {
                $query = knjm737Query::insertRepay($db, $this);
                $db->query($query);
            }

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();

            if ($this->cmd == "dueDel") {
                $query = knjm737Query::deleteDue($db, $this);
                $db->query($query);
            }
            if ($this->cmd == "paidDel") {
                $query = knjm737Query::deletePaid($db, $this);
                $db->query($query);
            }
            if ($this->cmd == "repayDel") {
                $query = knjm737Query::deleteRepay($db, $this);
                $db->query($query);
            }

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
