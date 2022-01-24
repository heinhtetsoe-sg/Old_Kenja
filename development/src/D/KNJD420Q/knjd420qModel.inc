<?php

require_once('for_php7.php');

class knjd420qModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade = "";
    var $exp_year = "";
    var $exp_semester = "";
    var $testTable;         //テストコンボ使用テーブル
    var $field  = array();
    var $field2 = array();
    var $field2seq = array();
    var $field3 = array();
    var $field4 = array();
    var $control;           //コントロールマスタ
    var $subclasscd;
    var $unitcd;
    var $retprg;
    var $getPrgId;
    var $auth;
    var $sendAuth;

    function knjd420qModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->subclasscd);
            unset($this->chaircd);
            unset($this->unitcd);
            unset($this->auth);
            unset($this->sendAuth);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->maxRemarkCnt = '8';

        $this->semester         = VARS::request("SEMESTER");
        $this->staffname        = VARS::request("STAFFNAME");
        $this->subclasscd       = VARS::request("SUBCLASSCD");
        $this->chaircd          = VARS::request("CHAIRCD");
        $this->unitcd           = VARS::request("UNITCD");
        $this->unitcd00         = '00';

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }

        //生徒毎の入力値
        $this->schField = array();
        $this->checkedCnt = 0;
        if (is_array($this->schInfo)) {
            foreach ($this->schInfo as $schregno => $val) {
                //チェックボックス
                $checkedFlg = (VARS::post("CHKBOX_".$schregno)) ? "1" : "";
                $this->schField[$schregno]["CHKBOX"] = $checkedFlg;
                $this->checkedCnt += ($checkedFlg) ? 1 : 0;
                //REMARK
                foreach ($this->itemNameArr as $itemNo => $item_remark) {
                    $this->schField[$schregno]["REMARK"][$itemNo] = VARS::post("REMARK_".$schregno."_".$itemNo);
                }
                //帳票パターン
                $this->schField[$schregno]["GUIDANCE_PATTERN"] = $val["GUIDANCE_PATTERN"];
            }
        }
    }

    //入力項目チェック
    function validateInput($cmd) {
        //必須チェック
        if ($this->cmd == "delete" && $this->checkedCnt == 0) {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->subclasscd == "") {
            $this->setWarning("MSG301", "科目コード");
            return false;
        }
        if ($this->chaircd == "") {
            $this->setWarning("MSG301", "講座コード");
            return false;
        }

        //文字数チェック
        if (strlen($this->staffname) > 75 ) {
            $this->setWarning("MSG914","(作成者名は75バイトまでです)");
            return false;
        }
        if ($cmd == "update") {
            foreach ($this->schInfo as $schregno => $val) {
                foreach ($this->itemNameArr as $itemNo => $item_remark) {
                    if ($this->validate_row_cnt($this->schField[$schregno]["REMARK"][$itemNo], 10 * 2) > 25) {
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
        knjd420qQuery::getInsertQuery($this);
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        if (!$this->validateInput("delete")) {
            return;
        }
        knjd420qQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
