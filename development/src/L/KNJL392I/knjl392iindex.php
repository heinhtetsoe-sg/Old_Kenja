<?php
require_once('knjl392iModel.inc');
require_once('knjl392iQuery.inc');

class knjl392iController extends Controller {
    var $ModelClassName = "knjl392iModel";
    var $ProgramID      = "KNJL392I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl392i":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl392iModel();
                    $this->callView("knjl392iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl392iCtl = new knjl392iController;
?>
