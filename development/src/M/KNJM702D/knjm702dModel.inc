<?php

require_once('for_php7.php');

class knjm702dModel extends Model {
    var $cmd;
    var $control;        //コントロールマスタ

    function knjm702dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

    }
}
?>
