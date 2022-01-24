<?php

require_once('for_php7.php');

class knjl323fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl323fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票種類
                             "JUDGE"            =>  VARS::post("JUDGE")             //各種帳票
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
