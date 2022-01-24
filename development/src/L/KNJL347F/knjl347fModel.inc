<?php

require_once('for_php7.php');

class knjl347fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl347fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票種類
                             "OUTPUT1DIV"       =>  VARS::post("OUTPUT1DIV")        //帳票種類
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
