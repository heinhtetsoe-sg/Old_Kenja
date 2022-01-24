<?php

require_once('for_php7.php');

class knje368Model extends Model {
    var $cmd;
    var $control;

    function knje368Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

    }
}
?>
