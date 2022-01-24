<?php
require_once('knjl439iModel.inc');
require_once('knjl439iQuery.inc');

class knjl439iController extends Controller {
    var $ModelClassName = "knjl439iModel";
    var $ProgramID      = "KNJL439I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl439iForm1");
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
$knjl439iCtl = new knjl439iController;
?>
