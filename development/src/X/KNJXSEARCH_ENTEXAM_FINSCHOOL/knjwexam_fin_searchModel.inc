<?php

require_once('for_php7.php');

class knjwexam_fin_searchModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $setField;
    var $getParameter;

    function knjwexam_fin_searchModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->entexamyear  = VARS::request("entexamyear");

        if ($this->cmd == "") {
            $this->setField = array();
            $this->getParameter = "";
        }
        if ($this->cmd == "searchMain") {
            $this->setField = array("fscdname"      => VARS::get("fscdname"),
                                    "fsname"        => VARS::get("fsname"),
                                    "fsChikuName"   => VARS::get("fsChikuName"),
                                    "fsRitsuNameId" => VARS::get("fsRitsuNameId"),
                                    "fsaddr"        => VARS::get("fsaddr"),
                                    "fszip"         => VARS::get("fszip"),
                                    "fsaddr1"       => VARS::get("fsaddr1"),
                                    "fsaddr2"       => VARS::get("fsaddr2"),
                                    "l015"          => VARS::get("l015"),
                                    "school_div"    => VARS::get("school_div"),
                                    "schoolCd"      => VARS::get("schoolCd"),
                                    "setschooltype" => VARS::get("setschooltype"),
                                    "setSchoolKind" => VARS::get("setSchoolKind"),
                                    "tell"          => VARS::get("tell")
                                    );
            $this->getParameter = '1';
        }
        if ($this->cmd == "searchMain2") {
            $this->setField = array("fscdname"      => VARS::get("fscdname"),
                                    "fsname"        => VARS::get("fsname"),
                                    "fsChikuName"   => VARS::get("fsChikuName"),
                                    "fsRitsuNameId" => VARS::get("fsRitsuNameId"),
                                    "fsaddr"        => VARS::get("fsaddr"),
                                    "fszip"         => VARS::get("fszip"),
                                    "fsaddr1"       => VARS::get("fsaddr1"),
                                    "fsaddr2"       => VARS::get("fsaddr2"),
                                    "l015"          => VARS::get("l015"),
                                    "school_div"    => VARS::get("school_div"),
                                    "schoolCd"      => VARS::get("schoolCd"),
                                    "setschooltype" => VARS::get("setschooltype"),
                                    "setSchoolKind" => VARS::get("setSchoolKind"),
                                    "tell"          => VARS::get("tell")
                                    );
            $this->getParameter = '2';
        }

        $this->field = array("FINSCHOOL_TYPE" => VARS::post("FINSCHOOL_TYPE"),
                             "FINSCHOOL_NAME" => VARS::post("FINSCHOOL_NAME"),
                             "FINSCHOOL_KANA" => VARS::post("FINSCHOOL_KANA"),
                             "PREF_CD" => VARS::post("PREF_CD"),
                             "FINSCHOOL_DISTCD" => VARS::post("FINSCHOOL_DISTCD"),
                             "FINSCHOOL_DIV" => VARS::post("FINSCHOOL_DIV")
                             
                             );
    }
}
?>
