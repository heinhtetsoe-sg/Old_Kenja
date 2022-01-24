<?php

require_once('for_php7.php');

require_once('knjl301dModel.inc');
require_once('knjl301dQuery.inc');

class knjl301dController extends Controller {
    var $ModelClassName = "knjl301dModel";
    var $ProgramID      = "KNJL301D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301d":
                    $this->callView("knjl301dForm1");
                    break 2;
                case "csv":
                    $sessionInstance->downloadCsvFile();
                    $sessionInstance->setCmd("");
                    break 2;
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
$knjl301dCtl = new knjl301dController;
?>
