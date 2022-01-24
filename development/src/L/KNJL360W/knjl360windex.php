<?php

require_once('for_php7.php');

require_once('knjl360wModel.inc');
require_once('knjl360wQuery.inc');

class knjl360wController extends Controller {
    var $ModelClassName = "knjl360wModel";
    var $ProgramID      = "KNJL360W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl360w":
                    $sessionInstance->knjl360wModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl360wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl360wCtl = new knjl360wController;
?>
