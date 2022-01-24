<?php

require_once('for_php7.php');
class knjd153rModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjd153rModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->getPro["REMARK1"]["moji"] = 15;
        $this->getPro["REMARK1"]["gyou"] = 13;

        $this->getPro["REMARK2"]["moji"] = 9;
        $this->getPro["REMARK2"]["gyou"] = 5;
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["REMARK1"][$i], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
                $this->setWarning("MSG915","(担任所見は{$this->getPro["REMARK1"]["gyou"]}行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->fields["REMARK2"][$i], ($this->getPro["REMARK2"]["moji"] * 2)) > $this->getPro["REMARK2"]["gyou"]) {
                $this->setWarning("MSG915","(その他は{$this->getPro["REMARK2"]["gyou"]}行までです)");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $this->fields["REMARK1"][] = VARS::post("REMARK1"."-".$i);
            $this->fields["REMARK2"][] = VARS::post("REMARK2"."-".$i);
        }

        if ($this->validateInput()) {
            knjd153rQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
