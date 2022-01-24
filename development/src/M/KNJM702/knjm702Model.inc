<?php

require_once('for_php7.php');

class knjm702Model extends Model {
    var $cmd;
    var $control;        //コントロールマスタ

    function knjm702Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

    }
}
?>
