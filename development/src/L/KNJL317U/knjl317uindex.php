<?php

require_once('for_php7.php');

require_once('knjl317uModel.inc');
require_once('knjl317uQuery.inc');

class knjl317uController extends Controller {
    var $ModelClassName = "knjl317uModel";
    var $ProgramID      = "KNJL317U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl317u":
                    $this->callView("knjl317uForm1");
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
$knjl317uCtl = new knjl317uController;
?>
