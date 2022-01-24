<?php

require_once('for_php7.php');

require_once('knjb0032Model.inc');
require_once('knjb0032Query.inc');

class knjb0032Controller extends Controller {
    var $ModelClassName = "knjb0032Model";
    var $ProgramID      = "KNJB0032";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0032":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb0032Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb0032Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0032Ctl = new knjb0032Controller;
?>
