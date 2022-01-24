<?php

require_once('for_php7.php');


//定数定義
class knjl254cModel extends Model {
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ

    //メンバ変数の初期化
    function knjl254cModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->field = array("PRE_TESTDIV"    => VARS::post("PRE_TESTDIV")
                             );
    }
}
?>
