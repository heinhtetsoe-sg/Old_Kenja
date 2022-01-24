<?php

require_once('for_php7.php');

class knjl307eModel extends Model {
    var $cmd;
    var $examyear;
    var $selected_data;
    var $field;

    function knjl307eModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV")           //入試区分
                              );

    }
}
?>
