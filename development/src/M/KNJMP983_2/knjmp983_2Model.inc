<?php

require_once('for_php7.php');

class knjmp983_2Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $trader_cd = "";

    function knjmp983_2Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("TRADER_CD"          => VARS::post("TRADER_CD"),
                             "TRADER_NAME"        => VARS::post("TRADER_NAME"),
                             "ZIPCD"              => VARS::post("ZIPCD"),
                             "ADDR1"              => VARS::post("ADDR1"),
                             "ADDR2"              => VARS::post("ADDR2"),
                             "BANKCD"             => VARS::post("BANKCD"),
                             "BRANCHCD"           => VARS::post("BRANCHCD"),
                             "BANK_DEPOSIT_ITEM"  => VARS::post("BANK_DEPOSIT_ITEM"),
                             "BANK_ACCOUNTNO"     => VARS::post("BANK_ACCOUNTNO"),
                             "ACCOUNTNAME"        => VARS::post("ACCOUNTNAME"),
                             "ACCOUNTNAME_KANA"   => VARS::post("ACCOUNTNAME_KANA"),
                             "PAY_DIV"            => VARS::post("PAY_DIV")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("mode") == "1"){
            unset($this->trader_cd);
        }
        $temp = VARS::get("TRADER_CD");
        if (isset($temp)){
            $this->trader_cd = VARS::get("TRADER_CD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (!strlen($this->field["TRADER_CD"]) && $this->cmd == "add") {
            $this->setWarning("MSG301", "(業者コード)");
            return false;
        }
        if (!strlen($this->trader_cd) && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->trader_cd != $this->field["TRADER_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //コードのゼロ埋め
        if (strlen($this->field["TRADER_CD"]) < 8) {
            $this->field["TRADER_CD"] = sprintf("%08d", $this->field["TRADER_CD"]);
        }

        //１レコード取得
        //学校DB
        $row = knjmp983_2Query::getRow($this->field["TRADER_CD"]);

        //データの存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(業者コード)");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["TRADER_NAME"]) == 0) {
            $this->setWarning("MSG301","(業者名称)");
            return false;
        } else if (strlen($this->field["TRADER_NAME"]) > 120 ) {
            $this->setWarning("MSG915", "(業者名称) 120バイトまで");
            return false;
        }
        if (strlen($this->field["TRADER_CD"]) > 8) {
            $this->setWarning("MSG901", "( 業者コード ) 8バイトまで");
            return false;
        } else if (strlen($this->field["ZIPCD"]) > 8) {
            $this->setWarning("MSG901", "( 郵便番号 ) 8バイトまで");
            return false;
        } else if (strlen($this->field["ADDR1"]) > 150) {
            $this->setWarning("MSG901", "( 住所1 ) 150バイトまで");
            return false;
        } else if (strlen($this->field["ADDR2"]) > 150) {
            $this->setWarning("MSG901", "( 住所2 ) 150バイトまで");
            return false;
        } else if (strlen($this->field["BANK_ACCOUNTNO"]) > 7) {
            $this->setWarning("MSG901", "( 口座番号 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["ACCOUNTNAME"]) > 120) {
            $this->setWarning("MSG901", "( 口座名義 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["ACCOUNTNAME_KANA"]) > 120) {
            $this->setWarning("MSG901", "( 口座名義カナ ) 120バイトまで");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->trader_cd = $this->field["TRADER_CD"];
            $this->setMessage("MSG201");
        }
    }

    //更新
    function &update() {
        knjmp983_2Query::getUpdateQuery($this, $this->cmd);
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if (!strlen($this->trader_cd)) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->trader_cd != $this->field["TRADER_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $row = knjmp983_2Query::getRow($this->trader_cd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;    
        }
        if (knjmp983_2Query::IsExisting($this->trader_cd)) {
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }

        //削除
        knjmp983_2Query::getDeleteQuery($this);
        $this->trader_cd = "";
        $this->setMessage("MSG201");
    }
}
?>
