<?php

require_once('for_php7.php');


class knjl360Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;

    function knjl360Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {
        $this->cmd         = VARS::request("cmd");
    }
}
?>
