<?php

require_once('for_php7.php');

class knjl312pModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl312pModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TESTSUBCLASSCD"   =>  VARS::post("TESTSUBCLASSCD")    //受験科目
                             );

        $this->cmd = VARS::request("cmd");
    }
}
?>
