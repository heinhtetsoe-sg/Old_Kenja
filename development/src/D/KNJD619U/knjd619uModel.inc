<?php

require_once('for_php7.php');

class knjd619uModel extends Model {
    var $field = array();
    var $cmd;

    function knjd619uModel() {}

    function init() {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field["DBNAME"] = VARS::post("DBNAME");      //DB変更に備えて

        $this->cmd = VARS::request("cmd");

    }
}
?>
