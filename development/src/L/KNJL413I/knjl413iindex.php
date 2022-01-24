<?php
require_once('knjl413iModel.inc');
require_once('knjl413iQuery.inc');

class knjl413iController extends Controller {
    var $ModelClassName = "knjl413iModel";
    var $ProgramID      = "KNJL413I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl413i":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl413iModel();
                    $this->callView("knjl413iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl413iCtl = new knjl413iController;
?>
