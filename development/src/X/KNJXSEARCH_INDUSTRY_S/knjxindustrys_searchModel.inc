<?php

require_once('for_php7.php');

class knjxindustrys_searchModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjxindustrys_searchModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");

        $this->field = array("INDUSTRY_LCD"     => VARS::post("INDUSTRY_LCD"),
                             "INDUSTRY_MCD"     => VARS::post("INDUSTRY_MCD"),
                             "INDUSTRY_SNAME"   => VARS::post("INDUSTRY_SNAME")
                             );
    }
}
?>
