<?php
require_once('knjl436iModel.inc');
require_once('knjl436iQuery.inc');

class knjl436iController extends Controller {
    var $ModelClassName = "knjl436iModel";
    var $ProgramID      = "KNJL436I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl436iForm1");
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
$knjl436iCtl = new knjl436iController;
?>
