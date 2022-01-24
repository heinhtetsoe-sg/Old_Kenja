<?php

require_once('for_php7.php');

require_once('knjl320uModel.inc');
require_once('knjl320uQuery.inc');

class knjl320uController extends Controller {
    var $ModelClassName = "knjl320uModel";
    var $ProgramID      = "KNJL320U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl320u":
                    $this->callView("knjl320uForm1");
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
$knjl320uCtl = new knjl320uController;
?>
