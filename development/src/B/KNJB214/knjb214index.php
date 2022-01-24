<?php

require_once('for_php7.php');

require_once('knjb214Model.inc');
require_once('knjb214Query.inc');

class knjb214Controller extends Controller {
    var $ModelClassName = "knjb214Model";
    var $ProgramID      = "KNJB214";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb214":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb214Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb214Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb214Ctl = new knjb214Controller;
?>
