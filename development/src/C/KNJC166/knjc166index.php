<?php

require_once('for_php7.php');

require_once('knjc166Model.inc');
require_once('knjc166Query.inc');

class knjc166Controller extends Controller {
    var $ModelClassName = "knjc166Model";
    var $ProgramID      = "KNJC166";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc166":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc166Model();      //コントロールマスタの呼び出し
                    $this->callView("knjc166Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc166Ctl = new knjc166Controller;
?>
