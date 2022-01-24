<?php
require_once('knjl109iModel.inc');
require_once('knjl109iQuery.inc');

class knjl109iController extends Controller {
    var $ModelClassName = "knjl109iModel";
    var $ProgramID      = "KNJL109I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "huban":
                    $this->callView("knjl109iForm1");
                    break 2;
                case "update":
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
$knjl109iCtl = new knjl109iController;
?>
