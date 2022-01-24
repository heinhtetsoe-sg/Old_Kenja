<?php

require_once('for_php7.php');

class knjl044qModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl044qModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "SORT_DIV"         =>  VARS::post("SORT_DIV")          //出力順
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
