<?php
require_once('knjl430iModel.inc');
require_once('knjl430iQuery.inc');

class knjl430iController extends Controller {
    var $ModelClassName = "knjl430iModel";
    var $ProgramID      = "KNJL430I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl430iForm1");
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
$knjl430iCtl = new knjl430iController;
?>
