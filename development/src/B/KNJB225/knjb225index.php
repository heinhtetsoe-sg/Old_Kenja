<?php

require_once('for_php7.php');

require_once('knjb225Model.inc');
require_once('knjb225Query.inc');

class knjb225Controller extends Controller {
    var $ModelClassName = "knjb225Model";
    var $ProgramID      = "KNJB225";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb225":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb225Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb225Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb225Ctl = new knjb225Controller;
?>
