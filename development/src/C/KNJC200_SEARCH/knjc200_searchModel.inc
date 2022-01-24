<?php

require_once('for_php7.php');

class knjc200_searchModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjc200_searchModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");

        $this->field = array("GRADE_HR_CLASS"   => VARS::post("GRADE_HR_CLASS"),
                             "NAME"             => VARS::post("NAME"),
                             "NAME_KANA"        => VARS::post("NAME_KANA")
                             );
        $this->getPropertiesAll();
    }
}
?>
