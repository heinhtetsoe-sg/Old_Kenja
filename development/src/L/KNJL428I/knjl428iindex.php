<?php
require_once('knjl428iModel.inc');
require_once('knjl428iQuery.inc');

class knjl428iController extends Controller {
    var $ModelClassName = "knjl428iModel";
    var $ProgramID      = "KNJL428I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl428iForm1");
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
$knjl428iCtl = new knjl428iController;
?>
