<?php

require_once('for_php7.php');

class knjc172Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjc172Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("GRADE_HR_CLASS"     =>  VARS::post("GRADE_HR_CLASS"),     //年組
                            );

        $this->cmd         = VARS::request("cmd");

        //更新対象生徒
        $this->selected = explode(",", VARS::post("CATEGORY_SELECTED_DATA"));
    }

    function clean() {}

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjc172Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
