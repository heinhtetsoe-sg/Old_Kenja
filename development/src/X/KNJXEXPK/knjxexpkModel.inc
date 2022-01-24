<?php

require_once('for_php7.php');

class knjxexpkModel extends Model {
    var $cmd;
    var $programid;         //プログラムＩＤ
    var $target;            //フレーム
    var $usr_auth;
    var $grade;
    var $check;
    var $search = array();
    var $mode;  //在籍生
    
    function knjxexpkModel()
    {
    }
    function init()
    {
        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->search);
            unset($this->search2);
            unset($this->search3);
            unset($this->search4);
            unset($this->search5);
            unset($this->search6);
            unset($this->grade);
            unset($this->scrollleft);
            unset($this->scrollltop);
        }
        if(VARS::get("TARGET")){
            $this->target[$this->programid] = VARS::get("TARGET");
        }
        if(VARS::get("MODEL")){
            $this->modelName = VARS::get("MODEL");
        }
        if(VARS::get("PATH")){
            $this->path[$this->programid] = VARS::get("PATH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");
        if (VARS::post("GRADE")) {
            $this->grade    = VARS::post("GRADE");
        }
        if (is_numeric(VARS::post("ROW"))) {
            $this->row = VARS::post("ROW");
        }
        if (VARS::post("DISP")) {
            $this->disp     = VARS::post("DISP");
        }
        if (is_numeric(VARS::post("SCROLLLEFT"))) {
            $this->scrollleft     = VARS::post("SCROLLLEFT");
        }
        if (is_numeric(VARS::post("SCROLLTOP"))) {
            $this->scrolltop     = VARS::post("SCROLLTOP");
        }
        //チェックボックス表示
        if(VARS::get("CHECK") == "ON"){
            $this->check[$this->programid] = true;
        }
        if ($this->cmd == "search"){
            //検索用
            $this->search = array("GRADE"       => VARS::post("GRADE2"),
                                  "GRD_YEAR"    => VARS::post("GRD_YEAR"),
                                  "HR_CLASS"    => VARS::post("HR_CLASS"),
                                  "NAME"        => VARS::post("NAME"),
                                  "NAME_SHOW"   => VARS::post("NAME_SHOW"),
                                  "NAME_KANA"   => VARS::post("NAME_KANA"),
                                  "NAME_ENG"    => VARS::post("NAME_ENG"),
                                  "SEX"         => VARS::post("SEX"),
                                  "COURSECODE"  => VARS::post("COURSECODE")
                               );
        }else if ($this->cmd == "search2"){
            $this->grade = "00-000";
            //検索用
            $this->search2 = array("DIV1"       => VARS::post("DIV1"),
                                   "DIV2"       => VARS::post("DIV2"),
                                   "DIV3"       => VARS::post("DIV3"),
                                   "BASEDATE1"  => VARS::post("BASEDATE1"),
                                   "BASEDATE2"  => VARS::post("BASEDATE2"),
                                   "BASEDATE3"  => VARS::post("BASEDATE3")
                                    );
        }else if ($this->cmd == "search3"){
            $this->grade = "00-000";
            //検索用
            $this->search3 = array("GRANTCD"    => VARS::post("GRANTCD"),
                                   "YEAR"       => VARS::post("YEAR"),
                                   "BASEDATE"   => VARS::post("BASEDATE"),
                                );
        }else if ($this->cmd == "search4"){
            $this->grade = "00-000";
            //検索用
            $this->search4 = array("BANKCD"         => VARS::post("BANKCD"),
                                   "BRANCHCD"       => VARS::post("BRANCHCD"),
                                   "DEPOSIT_ITEM"   => VARS::post("DEPOSIT_ITEM")
                                );
        }else if ($this->cmd == "search5"){
            $this->grade = "00-000";
            //検索用
            $this->search5 = array("EXPENSE_L_CD"             => VARS::post("EXPENSE_L_CD"),
                                   "EXPENSE_M_CD"             => VARS::post("EXPENSE_M_CD"),
                                   "MONEY_DUE"                => VARS::post("MONEY_DUE"),
                                   "REDUCTION_REASON"         => VARS::post("REDUCTION_REASON"),
                                   "INST_CD"                  => VARS::post("INST_CD"),
                                   "BANK_TRANS_STOP_REASON"   => VARS::post("BANK_TRANS_STOP_REASON"),
                                   "PAID_INPUT_FLG"           => VARS::post("PAID_INPUT_FLG"),
                                   "PAID_MONEY_DATE"          => VARS::post("PAID_MONEY_DATE"),
                                   "PAID_MONEY"               => VARS::post("PAID_MONEY"),
                                   "PAID_MONEY_DIV"           => VARS::post("PAID_MONEY_DIV"),
                                   "REPAY_DATE"               => VARS::post("REPAY_DATE"),
                                   "REPAY_MONEY"              => VARS::post("REPAY_MONEY"),
                                   "REPAY_DEV"                => VARS::post("REPAY_DEV")
                                );
        }else if ($this->cmd == "search6"){
            $this->grade = "00-000";
            //検索用
            $this->search6 = array("EXPENSE_L_CD"       => VARS::post("EXPENSE_L_CD"),
                                   "EXPENSE_M_CD"       => VARS::post("EXPENSE_M_CD"),
                                   "EXPENSE_S_CD"       => VARS::post("EXPENSE_S_CD"),
                                   "MONEY_DUE"          => VARS::post("MONEY_DUE"),
                                   "PAID_INPUT_FLG"     => VARS::post("PAID_INPUT_FLG"),
                                   "PAID_MONEY_DATE"    => VARS::post("PAID_MONEY_DATE"),
                                   "PAID_MONEY"         => VARS::post("PAID_MONEY"),
                                   "PAID_MONEY_DIV"     => VARS::post("PAID_MONEY_DIV"),
                                   "REPAY_DATE"         => VARS::post("REPAY_DATE"),
                                   "REPAY_MONEY"        => VARS::post("REPAY_MONEY"),
                                   "REPAY_DEV"          => VARS::post("REPAY_DEV")
                                );
        }else if ($this->cmd == "list"){
            unset($this->search);
            unset($this->search2);
            unset($this->search3);
            unset($this->search4);
            unset($this->search5);
            unset($this->search6);
        }
        //校納金拡張検索
        if ($this->cmd == "show"){
            $this->showno = VARS::request("showno");
        }
        if (VARS::get("cmb") == "EXPENSE_L_CD"){
            unset($this->Expense["M"]);
            unset($this->Expense["S"]);
        }else if (VARS::get("cmb") == "EXPENSE_M_CD"){
            $this->Expense["M"] = VARS::request("EXPENSE_M_CD");
            unset($this->Expense["S"]);
        }else{
            $this->Expense["M"] = VARS::request("EXPENSE_M_CD");
            $this->Expense["S"] = VARS::request("EXPENSE_S_CD");
        }
        $this->Expense["L"] = VARS::request("EXPENSE_L_CD");
    }
}
?>
