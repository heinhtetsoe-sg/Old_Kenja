<?php
require_once('knjl438iModel.inc');
require_once('knjl438iQuery.inc');

class knjl438iController extends Controller {
    var $ModelClassName = "knjl438iModel";
    var $ProgramID      = "KNJL438I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl438iForm1");
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
$knjl438iCtl = new knjl438iController;
?>
