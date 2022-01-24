<?php

require_once('for_php7.php');

class knjl509gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl509gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("PRINT_DIV"        =>  VARS::post("PRINT_DIV"),           //印刷範囲
                             "PASS_DIV"         =>  VARS::post("PASS_DIV"),            //合格通知書(対象者指定)
                             "PASS_EXAMNO"      =>  VARS::post("PASS_EXAMNO"),         //受験番号
                             "DBNAME"           =>  VARS::post("DBNAME")               //DB変更に備えて
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
