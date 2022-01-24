<?php

require_once('for_php7.php');

class knjh121Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata = array();

    function knjh121Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SEITO_BETU"   => VARS::post("SEITO_BETU"),
                             "DBNAME"       => VARS::post("DBNAME")
                             );
        $this->cmd = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>
