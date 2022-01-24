<?php

require_once('for_php7.php');

require_once('knjb247Model.inc');
require_once('knjb247Query.inc');

class knjb247Controller extends Controller {
    var $ModelClassName = "knjb247Model";
    var $ProgramID      = "KNJB247";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb247":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb247Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb247Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb247Ctl = new knjb247Controller;
?>
