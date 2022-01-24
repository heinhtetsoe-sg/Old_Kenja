<?php

require_once('for_php7.php');

require_once('knjb240Model.inc');
require_once('knjb240Query.inc');

class knjb240Controller extends Controller {
    var $ModelClassName = "knjb240Model";
    var $ProgramID      = "KNJB240";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb240":                             //メニュー画面もしくはSUBMITした場合
                case "change":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb240Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb240Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb240Ctl = new knjb240Controller;
?>
