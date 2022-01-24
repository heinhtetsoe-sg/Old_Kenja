<?php
require_once('knjl417hModel.inc');
require_once('knjl417hQuery.inc');

class knjl417hController extends Controller {
    var $ModelClassName = "knjl417hModel";
    var $ProgramID      = "KNJL417H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeApp":
                case "read":
                case "reset":
                case "back":
                case "next":
                case "now":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl417hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
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
$knjl417hCtl = new knjl417hController;
?>
