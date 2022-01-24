<?php

require_once('for_php7.php');

class knjl512gModel extends model {
    var $field      = array();
    var $cmd;
    var $applicantdiv;
    var $year;

    function knjl512gModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
        }

        $this->year = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        $this->field = array("OUTDATE"          =>  VARS::post("OUTDATE"),      //発行日
                             "PERMIT_DATE"      =>  VARS::post("PERMIT_DATE")   //許可年月日
                            );

        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }

        $this->selectdata      = VARS::post("selectdata");

        $this->file = VARS::file("FILE");
    }

    function &getMainModel() {}

}
?>
