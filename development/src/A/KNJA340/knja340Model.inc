<?php

require_once('for_php7.php');

class knja340Model extends Model {
    var $field = array();
    var $cmd;

    function knja340Model() {}

    function init() {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();

    }
}
?>
