<?php

require_once('for_php7.php');

class knjl346fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl346fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV")      //入試制度
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
