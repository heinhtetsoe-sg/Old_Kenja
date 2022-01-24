<?php
require_once('knjl434iModel.inc');
require_once('knjl434iQuery.inc');

class knjl434iController extends Controller {
    var $ModelClassName = "knjl434iModel";
    var $ProgramID      = "KNJL434I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl434i":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl434iModel();
                    $this->callView("knjl434iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl434iCtl = new knjl434iController;
?>
