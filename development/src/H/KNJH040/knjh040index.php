<?php

require_once('for_php7.php');

require_once('knjh040Model.inc');
require_once('knjh040Query.inc');

class knjh040Controller extends Controller {
    var $ModelClassName = "knjh040Model";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh040":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh040Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh040Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh040Ctl = new knjh040Controller;
var_dump($_REQUEST);
?>
