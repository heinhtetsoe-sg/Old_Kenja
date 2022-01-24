<?php

require_once('for_php7.php');

class knjwpri_searchModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $setField;

    function knjwpri_searchModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");
        if ($this->cmd == "") {
            $this->setField = array();
            $this->getParameter = "";
            $this->submitFlg = "";
        }

        $this->submitFlg = VARS::request("submitFlg");

        if ($this->cmd == "searchMain") {
            $this->setField = array("PRISCHOOL_CD"          => VARS::get("pricd"),
                                    "PRISCHOOL_NAME"        => VARS::get("priname"),
                                    "PRISCHOOL_CLASS_CD"    => VARS::get("priclasscd"),
                                    "PRISCHOOL_CLASS_NAME"  => VARS::get("priclassname")
                                    );
            $this->getParameter = '1';
        }

        $this->field = array("PRISCHOOL_NAME"       => VARS::post("PRISCHOOL_NAME"),
                             "PRISCHOOL_CLASS_NAME" => VARS::post("PRISCHOOL_CLASS_NAME")
                             );
    }
}
?>
