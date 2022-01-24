<?php

require_once('for_php7.php');

class knjl332fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl332fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TESTDIV0"         =>  VARS::post("TESTDIV0"),         //入試回数
                             "PRINT"            =>  VARS::post("PRINT")             //入金額・入金日印刷
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>