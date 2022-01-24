<?php
require_once('knjl412iModel.inc');
require_once('knjl412iQuery.inc');

class knjl412iController extends Controller {
    var $ModelClassName = "knjl412iModel";
    var $ProgramID      = "KNJL412I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl412iForm1");
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
$knjl412iCtl = new knjl412iController;
?>
