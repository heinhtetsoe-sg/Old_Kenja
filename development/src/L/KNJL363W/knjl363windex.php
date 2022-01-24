<?php

require_once('for_php7.php');

require_once('knjl363wModel.inc');
require_once('knjl363wQuery.inc');

class knjl363wController extends Controller {
    var $ModelClassName = "knjl363wModel";
    var $ProgramID      = "KNJL360W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl363wForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl363wForm1");
                    break 2;
                //報告取り下げ
                case "cancel":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCancelModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl363wCtl = new knjl363wController;
?>
