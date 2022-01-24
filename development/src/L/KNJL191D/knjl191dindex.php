<?php

require_once('for_php7.php');

require_once('knjl191dModel.inc');
require_once('knjl191dQuery.inc');

class knjl191dController extends Controller {
    var $ModelClassName = "knjl191dModel";
    var $ProgramID      = "KNJL191D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl191d":
                case "changeTest":
                case "changeRadio":
                    $this->callView("knjl191dForm1");
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
$knjl191dCtl = new knjl191dController;
?>
