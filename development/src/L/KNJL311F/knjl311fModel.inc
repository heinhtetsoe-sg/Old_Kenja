<?php

require_once('for_php7.php');

class knjl311fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl311fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TESTDIV0"         =>  VARS::post("TESTDIV0"),         //入試回数
                             "EXAMCOURSE"       =>  VARS::post("EXAMCOURSE")        //志望区分
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
