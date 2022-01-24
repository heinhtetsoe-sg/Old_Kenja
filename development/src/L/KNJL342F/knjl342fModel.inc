<?php

require_once('for_php7.php');

class knjl342fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl342fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV")
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
