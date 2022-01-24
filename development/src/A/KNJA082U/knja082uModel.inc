<?php

require_once('for_php7.php');

class knja082uModel extends Model {
    var $field = array();
    var $cmd;

    function knja082uModel() {}

    function init() {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SORT"     =>  VARS::post("SORT"),
                             "DBNAME"   =>  VARS::post("DBNAME")
                            );

        $this->cmd = VARS::request("cmd");
    }

}
?>
