<?php

require_once('for_php7.php');

class knjl304qModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl304qModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV")           //入試区分
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
