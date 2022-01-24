<?php
require_once('knjl389iModel.inc');
require_once('knjl389iQuery.inc');

class knjl389iController extends Controller {
    var $ModelClassName = "knjl389iModel";
    var $ProgramID      = "KNJL389I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl389i":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl389iModel();
                    $this->callView("knjl389iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl389iCtl = new knjl389iController;
?>
