<?php

require_once('for_php7.php');

class knjd680Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjd680Model() {}

    function init() {

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        
        $this->field["SEMESTER"] = VARS::post("SEMESTER"); //学期
        $this->field["GRADE"]    = VARS::post("GRADE");    //学年
        $this->cmd               = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["GRADE_HR_CLASS"]); $i++) {
            $title = "TITLE-".$i; 
            $this->fields["TITLE"][]    = VARS::post($title);
        }
        //サイズ初期値
        $this->getPro["TITLE"]["moji"] = 20;
        $this->getPro["TITLE"]["gyou"] = 1;
    }

    //入力項目チェック
    function validateInput() {
        for ($i = 0; $i < get_count($this->data["GRADE_HR_CLASS"]); $i++) {
            if ($this->validate_row_cnt($this->fields["TITLE"][$i], ($this->getPro["TITLE"]["moji"] * 2)) > $this->getPro["TITLE"]["gyou"]) {
                $this->setWarning("MSG915","( タイトルは{$this->getPro["TITLE"]["gyou"]}行までです )");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjd680Query::update($this);
            $this->setMessage("MSG201");
        }
    }

}
?>
