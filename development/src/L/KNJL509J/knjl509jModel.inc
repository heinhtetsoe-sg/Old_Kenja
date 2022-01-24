<?php

require_once('for_php7.php');

class knjl509jModel extends model {
    var $field = array();
    var $cmd;
    var $examyear;

    function knjl509jModel() {}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試種別
                             "TARGET_DIV"       =>  VARS::post("TARGET_DIV"),       //対象者
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票種類
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE")        //発行日
                              );

    }
}
?>
