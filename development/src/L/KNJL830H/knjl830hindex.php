<?php
require_once('knjl830hModel.inc');
require_once('knjl830hQuery.inc');

class knjl830hController extends Controller {
    var $ModelClassName = "knjl830hModel";
    var $ProgramID      = "KNJL830H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl830hForm1");
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
$knjl830hCtl = new knjl830hController;
?>
