<?php

require_once('for_php7.php');

class knjl329pModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl329pModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "MENSETU"          =>  VARS::post("MENSETU"),          //面接checkbox
                             "ORDERDIV"         =>  VARS::post("ORDERDIV")          //出力順
                             );

        $this->cmd = VARS::request("cmd");
    }
}
?>
