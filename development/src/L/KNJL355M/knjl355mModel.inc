<?php

require_once('for_php7.php');

class knjl355mModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjc171aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }

    function init() {
        $this->field = array("DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                             );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>