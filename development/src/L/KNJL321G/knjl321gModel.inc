<?php

require_once('for_php7.php');

class knjl321gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl321gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "PASS_SCORE"   =>  VARS::post("PASS_SCORE"),       //点以上
                             "SORT"         =>  VARS::post("SORT"),             //ソート
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
