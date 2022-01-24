<?php
require_once('knjl394iModel.inc');
require_once('knjl394iQuery.inc');

class knjl394iController extends Controller {
    var $ModelClassName = "knjl394iModel";
    var $ProgramID      = "KNJL394I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl394iForm1");
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
$knjl394iCtl = new knjl394iController;
?>
