<?php

require_once('for_php7.php');

require_once('knjb241Model.inc');
require_once('knjb241Query.inc');

class knjb241Controller extends Controller {
    var $ModelClassName = "knjb241Model";
    var $ProgramID      = "KNJB241";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb241":                             //メニュー画面もしくはSUBMITした場合
                case "change":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb241Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb241Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb241Ctl = new knjb241Controller;
?>
