<?php

require_once('for_php7.php');

class knjl507jModel extends Model {
    var $field = array();
    var $cmd;
    var $examyear;

    function knjl507jModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試種別
                             "OUTPUT"           =>  VARS::post("OUTPUT")            //帳票種類
                              );

    }
}
?>
