<?php

require_once('for_php7.php');

require_once('knjl343uModel.inc');
require_once('knjl343uQuery.inc');

class knjl343uController extends Controller {
    var $ModelClassName = "knjl343uModel";
    var $ProgramID      = "KNJL343U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343u":
                    $this->callView("knjl343uForm1");
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
$knjl343uCtl = new knjl343uController;
?>
