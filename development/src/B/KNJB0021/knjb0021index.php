<?php

require_once('for_php7.php');

require_once('knjb0021Model.inc');
require_once('knjb0021Query.inc');

class knjb0021Controller extends Controller {
    var $ModelClassName = "knjb0021Model";
    var $ProgramID      = "KNJB0021";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0021":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb0021Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb0021Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0021Ctl = new knjb0021Controller;
?>
