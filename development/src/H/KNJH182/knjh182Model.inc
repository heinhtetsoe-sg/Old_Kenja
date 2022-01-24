<?php

require_once('for_php7.php');

class knjh182Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjh182Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    function init() {

        $this->field = array("DBNAME"   =>  VARS::post("DBNAME"));  //DB変更に備えて

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
    }

}
?>
