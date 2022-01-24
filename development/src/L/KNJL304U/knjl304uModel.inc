<?php

require_once('for_php7.php');

class knjl304uModel extends Model {
    var $cmd;
    var $examyear;
    var $selected_data;
    var $field;

    function knjl304uModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->selected_data = VARS::post("SELECTED_DATA");
        $this->exam_type     = "1";//固定値

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "EXAMHALLCD"       =>  VARS::post("EXAMHALLCD")        //会場
                              );

    }
}
?>
