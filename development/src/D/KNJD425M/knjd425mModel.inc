<?php

require_once('for_php7.php');

class knjd425mModel extends Model {

    var $cmd;
    var $field  = array();
    var $control;           //コントロールマスタ
    var $itemNameArr = array();
    var $schInfo = array();
    var $textLimit = array();

    function knjd425mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
        }
        $this->cmd = VARS::post("cmd");

        $this->field = array(
            "SCHOOL_KIND" => VARS::post("SCHOOL_KIND"),
            "SEMESTER"    => VARS::post("SEMESTER"),
            "CHAIRCD"     => VARS::post("CHAIRCD"),
            "SUBCLASSCD"  => VARS::post("SUBCLASSCD"),
        );

        $this->schField = array();
        foreach ($this->schInfo as $schregno => $val) {
            $this->schField[$schregno]["NAME_{$schregno}"] = VARS::post("NAME_{$schregno}");
            foreach ($this->itemNameArr as $itemNo => $item_remark) {
                $this->schField[$schregno]["REMARK_{$itemNo}"] = VARS::post("REMARK_{$schregno}_{$itemNo}");
            }
        }

        //各textareaの最大文字数
        $this->textLimit = array(
            "1"     =>     array("moji" => 12, "gyou" => 25),
            "2"     =>     array("moji" => 12, "gyou" => 25),
            "3"     =>     array("moji" => 15, "gyou" => 25),
        );
    }

    //入力項目チェック
    function validateInput($cmd) {
        //必須チェック
        if ($this->field["SUBCLASSCD"] == "") {
            $this->setWarning("MSG301", "科目コード");
            return false;
        }
        if ($this->field["CHAIRCD"] == "") {
            $this->setWarning("MSG301", "講座コード");
            return false;
        }

        //文字数チェック
        if ($cmd == "update") {
            foreach ($this->schField as $schregno => $val) {
                foreach ($this->itemNameArr as $itemNo => $item_remark) {
                    if ($this->validate_row_cnt($val["REMARK_{$itemNo}"], $this->textLimit[$itemNo]["moji"] * 2) > $this->textLimit[$itemNo]["gyou"]) {
                        $this->setWarning("MSG915","(".$item_remark."は25行までです)");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        if (!$this->validateInput("update")) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }
    //アップデート時の内部メソッド
    function &update() {
        knjd425mQuery::getInsertQuery($this);
        return;
    }
}
?>
