<?php

require_once('for_php7.php');


class knjm210m_2Model extends Model {

    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $schregno;      //学籍番号(検索画面からのパラメータ)
    var $chaircd;       //講座コード(個人情報画面（全体）からのパラメータ)
    var $subclasscd;        //講座コード(個人情報画面（全体）からのパラメータ)

    function knjm210m_2Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {  
        if (VARS::get("SCHNO"))     $this->schregno     = VARS::get("SCHNO");
        if (VARS::get("CHAIR"))     $this->chaircd      = VARS::get("CHAIR");
        if (VARS::get("SUBCLASS"))  $this->subclasscd   = VARS::get("SUBCLASS");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }
}
?>
