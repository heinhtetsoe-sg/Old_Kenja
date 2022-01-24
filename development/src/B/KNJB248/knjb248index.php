<?php

require_once('for_php7.php');

require_once('knjb248Model.inc');
require_once('knjb248Query.inc');

class knjb248Controller extends Controller {
    var $ModelClassName = "knjb248Model";
    var $ProgramID      = "KNJB248";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb248":                             //メニュー画面もしくはSUBMITした場合
                case "change":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb248Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb248Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb248Ctl = new knjb248Controller;
?>
