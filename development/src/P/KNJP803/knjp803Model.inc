<?php

require_once('for_php7.php');

class knjp803Model extends Model {
    var $cmd;
    var $control;        //コントロールマスタ

    function knjp803Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("CTRL_YEAR" =>  CTRL_YEAR,                       //年度
                             "FORM"      =>  VARS::post("FORM")               //フォーム
                             );

        $this->cmd        = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //更新前チェック
    function validateInput() {
        return true;
    }
}
?>
