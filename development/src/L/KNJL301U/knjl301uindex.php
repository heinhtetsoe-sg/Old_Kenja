<?php

require_once('for_php7.php');

require_once('knjl301uModel.inc');
require_once('knjl301uQuery.inc');

class knjl301uController extends Controller {
    var $ModelClassName = "knjl301uModel";
    var $ProgramID      = "KNJL301U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301u":
                    $this->callView("knjl301uForm1");
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
$knjl301uCtl = new knjl301uController;
?>
