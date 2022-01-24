<?php
require_once('knjl369iModel.inc');
require_once('knjl369iQuery.inc');

class knjl369iController extends Controller {
    var $ModelClassName = "knjl369iModel";
    var $ProgramID      = "KNJL369I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl369i":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl369iModel();
                    $this->callView("knjl369iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl369iCtl = new knjl369iController;
?>
