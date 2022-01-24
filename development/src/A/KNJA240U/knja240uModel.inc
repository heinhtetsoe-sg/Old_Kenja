<?php

require_once('for_php7.php');

class knja240uModel extends Model {
    var $field = array();
    var $cmd;

    function knja240uModel() {}

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("DATE"     =>  VARS::post("DATE"),         //処理日
                             "DBNAME"   =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
