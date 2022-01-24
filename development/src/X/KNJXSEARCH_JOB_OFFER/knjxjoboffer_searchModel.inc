<?php

require_once('for_php7.php');

class knjxjoboffer_searchModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjxjoboffer_searchModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");

        $this->field = array("COMPANY_NAME"     => VARS::post("COMPANY_NAME"),
                             "COMPANY_NAMEKANA" => VARS::post("COMPANY_NAMEKANA"),
                             "COMPANY_ADDR1"    => VARS::post("COMPANY_ADDR1")
                             );
    }
}
?>
