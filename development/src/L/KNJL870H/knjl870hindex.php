<?php
require_once('knjl870hModel.inc');
require_once('knjl870hQuery.inc');

class knjl870hController extends Controller {
    var $ModelClassName = "knjl870hModel";
    var $ProgramID      = "KNJL870H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl870hForm1");
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
$knjl870hCtl = new knjl870hController;
?>
