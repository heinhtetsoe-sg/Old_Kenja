<?php
require_once('knjl431iModel.inc');
require_once('knjl431iQuery.inc');

class knjl431iController extends Controller {
    var $ModelClassName = "knjl431iModel";
    var $ProgramID      = "KNJL431I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl431iForm1");
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
$knjl431iCtl = new knjl431iController;
?>
