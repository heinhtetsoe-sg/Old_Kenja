<?php

require_once('for_php7.php');

class knjxcol_searchModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $target_number;

    function knjxcol_searchModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");

        $this->field = array("SCHOOL_NAME"  => VARS::post("SCHOOL_NAME"),
                             "SCHOOL_GROUP" => VARS::post("SCHOOL_GROUP"),
                             "PUBPRIV_KIND" => VARS::post("PUBPRIV_KIND"),
                             "SCHOOL_LIST"  => VARS::post("SCHOOL_LIST"),
                             "FACULTY"      => VARS::post("FACULTY"),
                             "DEPARTMENT"   => VARS::post("DEPARTMENT"),
                             "SORT"         => VARS::post("SORT"),
                             "AREA_NAME"    => VARS::post("AREA_NAME")
                             );

        if (!$this->cmd) {
            unset($this->target_number);
        }

        if (VARS::get("target_number")) {
            $this->target_number = VARS::get("target_number");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
