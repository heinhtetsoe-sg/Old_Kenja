<?php

require_once('for_php7.php');

require_once('knjl301eModel.inc');
require_once('knjl301eQuery.inc');

class knjl301eController extends Controller {
    var $ModelClassName = "knjl301eModel";
    var $ProgramID      = "KNJL301E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301e":
                    $this->callView("knjl301eForm1");
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
$knjl301eCtl = new knjl301eController;
?>
