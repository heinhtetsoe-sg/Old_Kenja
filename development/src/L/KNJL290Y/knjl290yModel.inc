<?php

require_once('for_php7.php');


class knjl290yModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $judgement;         //判定区分
    var $procedurediv;      //手続区分
    var $proceduredate;     //入金日
    var $pay_money;         //入金額
    var $testdiv2;          //入試区分
    var $judgediv;          //合否区分
    var $entdiv;            //入学区分
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $cflg;                      //変更フラグ（同じ受験番号を編集中チェック）

    function knjl290yModel() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->procedurediv);
            unset($this->proceduredate);
            unset($this->pay_money);
            unset($this->judgement);
            unset($this->testdiv2);
            unset($this->judgediv);
            unset($this->checkexam);
            unset($this->cflg);
            unset($this->entdiv);
        }

        $this->cmd              = VARS::request("cmd");
        $this->year             = CTRL_YEAR + 1;

        if (strlen(VARS::post("EXAMNO"))) {
            if ($this->examno != VARS::post("EXAMNO")) {
                $this->cflg = "false";
                unset($this->judgement);
            } elseif ($this->cmd == "change" || $this->cmd == "change_testdiv2") { //編集中フラグ
                $this->cflg = "true";
            } else {
                $this->cflg = "false";
            }
            $this->examno  = VARS::post("EXAMNO");
            $this->examno = sprintf("%05d",$this->examno);
        } else {
            unset($this->judgement);
        }

        $this->procedurediv     = VARS::post("PROCEDUREDIV");
        $this->proceduredate    = VARS::post("PROCEDUREDATE");
        $this->pay_money        = str_replace(",", "", VARS::post("PAY_MONEY"));
        $this->testdiv2         = VARS::post("TESTDIV2");
        $this->judgediv         = VARS::post("JUDGEDIV");
        $this->entdiv           = VARS::post("ENTDIV");

        //フィールド
        $this->field = array("JUDGEMENT"            => VARS::post("JUDGEMENT"),                  //合否判定
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
                             "PROCEDUREDATE"        => VARS::post("PROCEDUREDATE"),              //入金日
                             "PAY_MONEY"            => str_replace(",", "", VARS::post("PAY_MONEY")),                  //入金額
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入学区分
                             "TESTDIV2"             => VARS::post("TESTDIV2"),                   //入試区分
                             "JUDGEDIV"             => VARS::post("JUDGEDIV")                    //合否区分
                            );
    }


    //入力項目チェック
    function validateInput() {
        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->field["TESTDIV2"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }        
        //存在チェック
        $row = knjl290yQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //入金チェック
        if (strlen($this->field["PROCEDUREDATE"]) || strlen($this->field["PAY_MONEY"])) {
            if (!strlen($this->field["PROCEDUREDATE"]) || !strlen($this->field["PAY_MONEY"])) {
                $this->setWarning("MSG901", "( 入金日・入金額 )\\n入金日・入金額を入力する場合は、両方とも入力して下さい。");
                return false;
            }
            if (!strlen($this->field["PROCEDUREDIV"])) {
                $this->setWarning("MSG901", "( 手続区分 )\\n入金日・入金額を入力する場合は、手続区分を選択して下さい。");
                return false;
            }
        }
        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受験番号は5バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl290yQuery::getUpdateQuery($this);
            $this->testdiv2 = $this->field["TESTDIV2"];
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        } else {
            $this->cmd = "main";
        }
        return;
    } 
}
?>