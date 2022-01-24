<?php

require_once('for_php7.php');

class knjl327aModel extends Model {
    var $cmd;
    var $examyear;
    var $selected_data;
    var $examType;
    var $field;

    function knjl327aModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->examType = 1;    //固定

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //受験校種
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //試験名
                             "FORM"             =>  VARS::post("FORM"),             //帳票
                              );

    }

}
?>
