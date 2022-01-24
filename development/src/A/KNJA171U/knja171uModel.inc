<?php

require_once('for_php7.php');

class knja171uModel extends Model {
    var $field = array();
    var $cmd;

    function knja171uModel() {}

    function init() {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("GRADE"    =>  VARS::post("GRADE"),
                             "FORM"     =>  VARS::post("FORM"),
                             "DATE"     =>  VARS::post("DATE"),
                             "DBNAME"   =>  VARS::post("DBNAME")
                            );

        $this->cmd = VARS::request("cmd");
    }

}
?>
