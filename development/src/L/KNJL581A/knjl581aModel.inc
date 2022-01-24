<?php

require_once('for_php7.php');

class knjl581aModel extends model {
    var $cmd;
    var $applicantdiv;
    var $passCourse;
    var $field;
    var $ObjYear;

    function knjl581aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->passCourse);
            unset($this->examno);
            unset($this->sort);
            unset($this->field);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->field = array("OUTPUT"    => VARS::post("OUTPUT"),
                             "SET_DATE"  => VARS::post("SET_DATE"),
                             "TEST_DATE" => VARS::post("TEST_DATE"),
                             "FS_CD"     => VARS::post("FS_CD"),
                             "TESTDIV"   => VARS::post("TESTDIV")
                            );
    }

    function &getMainModel() {}

}
?>
