<?php

require_once('for_php7.php');

class knjl325aModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl325aModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),   //受験校種
                             "TESTDIV"          =>  VARS::post("TESTDIV"),        //入試区分
                             "PASSCOURSE"       =>  VARS::post("PASSCOURSE")      //合格コース
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
