<?php

require_once('for_php7.php');

require_once('knjg106Model.inc');
require_once('knjg106Query.inc');

class knjg106Controller extends Controller {
    var $ModelClassName = "knjg106Model";
    var $ProgramID      = "KNJG106";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg106":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg106Model();       //コントロールマスタの呼び出し
                    $this->callView("knjg106Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg106Ctl = new knjg106Controller;
?>
