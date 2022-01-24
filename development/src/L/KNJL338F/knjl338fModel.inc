<?php

require_once('for_php7.php');

class knjl338fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl338fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),
                             "PASSDIV"      =>  VARS::post("PASSDIV"),
                             "RECEPTNO"     =>  VARS::post("RECEPTNO"),
                             "RECEPTNO2S"   =>  VARS::post("RECEPTNO2S"),
                             "RECEPTNO2E"   =>  VARS::post("RECEPTNO2E"),
                             "RECEPTNO4S"   =>  VARS::post("RECEPTNO4S"),
                             "RECEPTNO4E"   =>  VARS::post("RECEPTNO4E"),
                             "GYOU"         =>  VARS::post("GYOU"),
                             "PRINTADDRESS" =>  VARS::post("PRINTADDRESS"),
                             "RETU"         =>  VARS::post("RETU")
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
