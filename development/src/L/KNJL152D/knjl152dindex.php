<?php

require_once('for_php7.php');

require_once('knjl152dModel.inc');
require_once('knjl152dQuery.inc');

class knjl152dController extends Controller {
    var $ModelClassName = "knjl152dModel";
    var $ProgramID      = "KNJL152D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl152d":
                case "changeTest":
                    $this->callView("knjl152dForm1");
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
$knjl152dCtl = new knjl152dController;
?>
