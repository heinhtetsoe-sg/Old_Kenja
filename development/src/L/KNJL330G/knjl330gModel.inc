<?php

require_once('for_php7.php');

class knjl330gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;
    var $special_reason_div;

    function knjl330gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "FORM_TYPE"    =>  VARS::post("FORM_TYPE"),        //帳票種類
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
        $this->cmd = VARS::request("cmd");
    }
}
?>
