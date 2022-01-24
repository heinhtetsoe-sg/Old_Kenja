<?php

require_once('for_php7.php');
require_once('knjl031eModel.inc');
require_once('knjl031eQuery.inc');

class knjl031eController extends Controller {
    var $ModelClassName = "knjl031eModel";
    var $ProgramID      = "KNJL031E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl031eForm1");
                    break 2;
                case "update":
                case "replace":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl031eCtl = new knjl031eController;
?>
