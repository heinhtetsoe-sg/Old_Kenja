<?php

require_once('for_php7.php');

class knja226Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knja226Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),    //クラス方式
                             "DBNAME"           => VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
