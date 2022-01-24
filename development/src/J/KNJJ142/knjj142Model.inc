<?php

require_once('for_php7.php');
class knjj142Model extends Model {
    var $field = array();
    var $cmd;

    function knjj142Model() {
    }

    function init() {
        $this->field = array("SEMESTER"         => VARS::post("SEMESTER"),
                             "COMMITTEE_FLG"    => VARS::post("COMMITTEE_FLG")
                            );

        $this->cmd   = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //変数をクリアします
    function clean() {
    }
}
?>
