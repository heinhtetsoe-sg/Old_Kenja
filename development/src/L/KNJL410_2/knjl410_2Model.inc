<?php

require_once('for_php7.php');

class knjl410_2Model extends Model {
    var $auth;
    var $recruit_no;
    var $touroku_date;
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjl410_2Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->auth);
            unset($this->recruit_no);
            unset($this->touroku_date);
        }

        $this->field = array("TOUROKU_DATE"         =>  VARS::post("TOUROKU_DATE"),
                             "CONSULT_CD"           =>  VARS::post("CONSULT_CD"),
                             "METHOD_CD"            =>  VARS::post("METHOD_CD"),
                             "STAFFCD"              =>  VARS::post("STAFFCD"),
                             "CONTENTS"             =>  VARS::post("CONTENTS"),
                             "REMARK"               =>  VARS::post("REMARK")
                            );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SEND_PRGRID")){
            $this->getPrgId     = VARS::get("SEND_PRGRID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->recruit_no   = VARS::get("SEND_RECRUIT_NO");
        }
        //権限
        $this->auth = ($this->getPrgId) ? $this->sendAuth : AUTHORITY;

        if (VARS::get("TOUROKU_DATE")) {
            $this->touroku_date = VARS::get("TOUROKU_DATE");
        }

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        if ($this->recruit_no == "") {
            $this->setWarning("MSG309");
            return false;
        }

        if ($this->field["TOUROKU_DATE"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 登録日付 ）");
            return false;
        }

        if ($this->cmd == "update") {
            if ($this->touroku_date != $this->field["TOUROKU_DATE"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        if ($this->cmd == "insert") {
            //存在チェック
            $chk = knjl410_2Query::getRow_check($this);
            if ($chk > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        }

        //文字数チェック
        if ($this->validate_row_cnt($this->field["CONTENTS"], (40 * 2)) > 7) {
            $this->setWarning("MSG915","(相談内容は7行までです)");
            return false;
        }

        return true;
    }

    //入力項目チェック
    function validateInput2() {
        if ($this->recruit_no == "") {
            $this->setWarning("MSG309");
            return false;
        }

        //文字数チェック
        if ($this->validate_row_cnt($this->field["REMARK"], (20 * 2)) > 1) {
            $this->setWarning("MSG915","(来校者要約は40バイトまでです)");
            return false;
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        if ($this->cmd == "update2") {
            if (!$this->validateInput2()) {
                return;
            }
            //更新2
            knjl410_2Query::getUpdateQuery2($this, $this->field);
            $this->setMessage("MSG201");
            return;
        }

        if (!$this->validateInput()) {
            return;
        }

        //更新
        knjl410_2Query::getUpdateQuery($this, $this->field);
        $this->setMessage("MSG201");

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {

        if ($this->recruit_no == "") {
            $this->setWarning("MSG309");
            return;
        }
        if ($this->field["TOUROKU_DATE"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 登録日付 ）");
            return;
        }

        if ($this->touroku_date != $this->field["TOUROKU_DATE"]) {
            $this->setWarning("MSG308");
            return;
        }

        //削除
        knjl410_2Query::getDeleteQuery($this);

        $this->field = array();
        unset($this->touroku_date);

        $this->setMessage("MSG201");

        return;
    }
}
?>
