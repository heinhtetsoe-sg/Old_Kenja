<?php

require_once('for_php7.php');

class knjl503gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl503gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "EXAMCOUSE"    =>  VARS::post("EXAMCOUSE"),        //合格コース
                             "SHDIV"        =>  VARS::post("SHDIV"),            //専併区分
                             "JUDGE"        =>  VARS::post("JUDGE"),            //合否区分
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
