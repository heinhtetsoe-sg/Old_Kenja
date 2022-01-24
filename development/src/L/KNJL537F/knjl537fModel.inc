<?php

require_once('for_php7.php');

class knjl537fModel extends Model {
    var $cmd;
    var $year_add;

    function knjl537fModel() {}

    function init() {
        $this->entexamyear = CTRL_YEAR + 1;

        $this->cmd      = VARS::request("cmd");

        $this->field = array(
            "DEV_CD"     => VARS::post("DEV_CD"),    //コード
            "DEV_MARK"   => VARS::post("DEV_MARK"),  //記号
            "DEV_LOW"    => VARS::post("DEV_LOW"),   //from
            "DEV_HIGH"   => VARS::post("DEV_HIGH"),  //to
        );
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "insert") {
            //必須チェック
            if ($this->field["DEV_CD"] == "") {
                $this->setWarning("MSG301", "(コード)");
                return;
            }
            if ($this->field["DEV_MARK"] == "") {
                $this->setWarning("MSG301", "(記号)");
                return;
            }
            if ($this->field["DEV_LOW"] == "") {
                $this->setWarning("MSG301", "(From)");
                return;
            }
            if ($this->field["DEV_HIGH"] == "") {
                $this->setWarning("MSG301", "(To)");
                return;
            }

            //文字サイズチェック
            if (strlen($this->field["DEV_CD"]) > 3) {
                $this->setWarning("MSG915","(コードは3バイトまでです)");
                return;
            }
            if (strlen($this->field["DEV_MARK"]) > 6) {
                $this->setWarning("MSG915","(記号は6バイトまでです)");
                return;
            }

            //数値チェック
            if (!is_numeric($this->field["DEV_LOW"])) {
                $this->setWarning("MSG909","(From)");
                return;
            }
            if (!is_numeric($this->field["DEV_HIGH"])) {
                $this->setWarning("MSG909","(To)");
                return;
            }

            //From、Toのチェック
            //上下限チェック
            if ($this->field["DEV_LOW"] < 0 || 999.9 < $this->field["DEV_LOW"]) {
                $this->setWarning("MSG913","(Fromは0.0から999.9までの範囲で入力してください)");
                return;
            }
            if ($this->field["DEV_HIGH"] < 0 || 999.9 < $this->field["DEV_HIGH"]) {
                $this->setWarning("MSG913","(Toは0.0から999.9までの範囲で入力してください)");
                return;
            }
            //大小チェック
            if ($this->field["DEV_HIGH"] <= $this->field["DEV_LOW"]) {
                $this->setWarning("MSG901","(From, Toの大小が不正です)");
                return;
            }
            //小数チェック
            if (!preg_match('/^(\d{1,3})(\.\d)?$/', ltrim($this->field["DEV_LOW"], "0")) && (ltrim($this->field["DEV_LOW"], "0") != "")) {
                $this->setWarning("MSG901","(Fromは小数第一まで)");
                return;
            }
            if (!preg_match('/^(\d{1,3})(\.\d)?$/', ltrim($this->field["DEV_HIGH"], "0")) && (ltrim($this->field["DEV_HIGH"], "0") != "")) {
                $this->setWarning("MSG901","(Toは小数第一まで)");
                return;
            }
        }

        return true;
    }

    //登録
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjl537fQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
