<?php

require_once('for_php7.php');

class knjl322eModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $outputtype;

    function knjl322eModel(){}

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->outputtype  = VARS::post("OUTPUT_TYPE");
    }
}
?>
