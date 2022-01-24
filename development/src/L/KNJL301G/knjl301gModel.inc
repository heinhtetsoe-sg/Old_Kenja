<?php

require_once('for_php7.php');

class knjl301gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl301gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "KAIPAGE"      =>  VARS::post("KAIPAGE"),          //改ページ
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
