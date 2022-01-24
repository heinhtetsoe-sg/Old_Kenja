<?php
require_once('knjl402iModel.inc');
require_once('knjl402iQuery.inc');

class knjl402iController extends Controller {
    var $ModelClassName = "knjl402iModel";
    var $ProgramID      = "KNJL402I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl402i":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl402iModel();
                    $this->callView("knjl402iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl402iCtl = new knjl402iController;
?>
