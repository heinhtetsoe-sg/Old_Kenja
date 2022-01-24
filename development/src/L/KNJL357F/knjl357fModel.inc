<?php

require_once('for_php7.php');

class knjl357fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl357fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"          =>  VARS::post("TESTDIV"),
                             "PRINTDIV"         =>  VARS::post("PRINTDIV"),
                             "RECEPTNO"         =>  VARS::post("RECEPTNO"),
                             "PASSDIV"          =>  VARS::post("PASSDIV")
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
