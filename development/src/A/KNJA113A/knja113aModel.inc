<?php

require_once('for_php7.php');

class knja113aModel extends Model {
    var $cmd;
    var $control;        //コントロールマスタ

    function knja113aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array(
            "STD_DIV"     =>  VARS::post("STD_DIV"),        //新入生・在学生
            "SCHOOL_KIND" =>  VARS::post("SCHOOL_KIND"),    //校種
            "SCHOLARSHIP" =>  VARS::post("SCHOLARSHIP"),    //交付種別
        );

        $this->cmd = VARS::request("cmd");

        $this->year = ($this->field["STD_DIV"] == 1) ? (CTRL_YEAR + 1) : CTRL_YEAR;

        //プロパティファイル
        $this->getPropertiesAll();
    }

}
?>
