<?php

require_once('for_php7.php');

require_once('knjb0033Model.inc');
require_once('knjb0033Query.inc');

class knjb0033Controller extends Controller {
    var $ModelClassName = "knjb0033Model";
    var $ProgramID      = "KNJB0033";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0033":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb0033Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb0033Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0033Ctl = new knjb0033Controller;
?>
