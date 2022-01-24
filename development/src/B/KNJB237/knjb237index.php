<?php

require_once('for_php7.php');

require_once('knjb237Model.inc');
require_once('knjb237Query.inc');

class knjb237Controller extends Controller {
    var $ModelClassName = "knjb237Model";
    var $ProgramID      = "KNJB237";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb237":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb237Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb237Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb237Ctl = new knjb237Controller;
?>
