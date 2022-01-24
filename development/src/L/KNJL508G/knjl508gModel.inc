<?php

require_once('for_php7.php');

class knjl508gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl508gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),        //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),             //入試区分
                             "PASS_DIV"         =>  VARS::post("PASS_DIV"),            //合格通知書(対象者指定)
                             "PASS_EXAMNO"      =>  VARS::post("PASS_EXAMNO"),         //受験番号指定FROM
                             "PASS_EXAMNO_TO"   =>  VARS::post("PASS_EXAMNO_TO"),      //受験番号指定TO
                             "DBNAME"           =>  VARS::post("DBNAME"),              //DB変更に備えて
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
