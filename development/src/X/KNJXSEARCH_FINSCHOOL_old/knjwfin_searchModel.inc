<?php

require_once('for_php7.php');

class knjwfin_searchModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $setField;
    var $getParameter;

    function knjwfin_searchModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");
        if ($this->cmd == "") {
            $this->setField = "";
            $this->getParameter = "";
        }
        if ($this->cmd == "searchMain") {
            $this->setField = array("fscdname"      => VARS::get("fscdname"),
                                    "fsname"        => VARS::get("fsname"),
                                    "fsChikuName"   => VARS::get("fsChikuName"),
                                    "fsRitsuNameId" => VARS::get("fsRitsuNameId"),
                                    "fsaddr"        => VARS::get("fsaddr"),
                                    "school_div"    => VARS::get("school_div"),
                                    "schoolCd"      => VARS::get("schoolCd"),
                                    "setschooltype" => VARS::get("setschooltype")
                                    );
            $this->getParameter = '1';
        }

        $this->field = array("FINSCHOOL_TYPE" => VARS::post("FINSCHOOL_TYPE"),
                             "FINSCHOOL_NAME" => VARS::post("FINSCHOOL_NAME"),
                             "FINSCHOOL_KANA" => VARS::post("FINSCHOOL_KANA")
                             );
    }
}
?>
