<?php

require_once('for_php7.php');

class knjl674aModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;

    function knjl674aModel(){}

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = CTRL_YEAR + 1;
        $this->applicantdiv = "1";  //固定値
        $this->exam_type    = "1";  //固定値
        $this->hallcd       =  VARS::post("HALL_CD");
    }
}
?>
