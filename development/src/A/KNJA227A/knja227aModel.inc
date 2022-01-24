<?php

require_once('for_php7.php');

class knja227aModel extends Model {
    var $field = array();
    var $cmd;

    function knja227aModel() {}

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        $this->field = array(
                             "DBNAME"   =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
