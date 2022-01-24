<?php
require_once('knjl401iModel.inc');
require_once('knjl401iQuery.inc');

class knjl401iController extends Controller {
    var $ModelClassName = "knjl401iModel";
    var $ProgramID      = "KNJL401I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl401iForm1");
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
$knjl401iCtl = new knjl401iController;
?>
