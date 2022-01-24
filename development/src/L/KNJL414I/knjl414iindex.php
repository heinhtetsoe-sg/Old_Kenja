<?php
require_once('knjl414iModel.inc');
require_once('knjl414iQuery.inc');

class knjl414iController extends Controller {
    var $ModelClassName = "knjl414iModel";
    var $ProgramID      = "KNJL414I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl414i":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl414iModel();
                    $this->callView("knjl414iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl414iCtl = new knjl414iController;
?>
