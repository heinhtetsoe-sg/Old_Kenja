<?php

require_once('for_php7.php');

require_once('knjl303uModel.inc');
require_once('knjl303uQuery.inc');

class knjl303uController extends Controller {
    var $ModelClassName = "knjl303uModel";
    var $ProgramID      = "KNJL303U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl303u":
                    $this->callView("knjl303uForm1");
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
$knjl303uCtl = new knjl303uController;
?>
