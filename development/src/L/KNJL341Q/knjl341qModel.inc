<?php

require_once('for_php7.php');

class knjl341qModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl341qModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV")     //入試制度
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>