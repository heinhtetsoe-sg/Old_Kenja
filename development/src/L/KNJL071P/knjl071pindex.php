<?php

require_once('for_php7.php');

require_once('knjl071pModel.inc');
require_once('knjl071pQuery.inc');

class knjl071pController extends Controller {
    var $ModelClassName = "knjl071pModel";
    var $ProgramID      = "KNJL071P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "app":
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl071pForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl071pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl071pCtl = new knjl071pController;
?>
