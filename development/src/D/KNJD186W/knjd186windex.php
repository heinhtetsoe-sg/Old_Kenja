<?php

require_once('for_php7.php');

require_once('knjd186wModel.inc');
require_once('knjd186wQuery.inc');

class knjd186wController extends Controller {
    var $ModelClassName = "knjd186wModel";
    var $ProgramID      = "KNJD186W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd186w":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd186wModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd186wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186wCtl = new knjd186wController;
?>
