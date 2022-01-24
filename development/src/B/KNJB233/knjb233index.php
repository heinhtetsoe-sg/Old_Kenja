<?php

require_once('for_php7.php');

require_once('knjb233Model.inc');
require_once('knjb233Query.inc');

class knjb233Controller extends Controller {
    var $ModelClassName = "knjb233Model";
    var $ProgramID      = "KNJB233";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb233":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb233Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb233Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb233Ctl = new knjb233Controller;
?>
