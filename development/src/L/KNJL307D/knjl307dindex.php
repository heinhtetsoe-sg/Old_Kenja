<?php

require_once('for_php7.php');

require_once('knjl307dModel.inc');
require_once('knjl307dQuery.inc');

class knjl307dController extends Controller {
    var $ModelClassName = "knjl307dModel";
    var $ProgramID      = "KNJL307D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl307d":
                    $this->callView("knjl307dForm1");
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
$knjl307dCtl = new knjl307dController;
?>
