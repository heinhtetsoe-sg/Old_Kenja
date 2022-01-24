<?php

require_once('for_php7.php');

require_once('knjl340wModel.inc');
require_once('knjl340wQuery.inc');

class knjl340wController extends Controller {
    var $ModelClassName = "knjl340wModel";
    var $ProgramID      = "KNJL340W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl340w":
                    $sessionInstance->knjl340wModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl340wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl340wCtl = new knjl340wController;
?>
