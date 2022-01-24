<?php
require_once('knjl390iModel.inc');
require_once('knjl390iQuery.inc');

class knjl390iController extends Controller {
    var $ModelClassName = "knjl390iModel";
    var $ProgramID      = "KNJL390I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl390iForm1");
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
$knjl390iCtl = new knjl390iController;
?>
