<?php

require_once('for_php7.php');

//定数定義

class knjm281mModel extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;

    //メンバ変数の初期化
    function knjm281mModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

}
?>
