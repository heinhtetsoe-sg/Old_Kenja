<?php

require_once('for_php7.php');

require_once('knjl322dModel.inc');
require_once('knjl322dQuery.inc');

class knjl322dController extends Controller {
    var $ModelClassName = "knjl322dModel";
    var $ProgramID      = "KNJL322D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322d":
                case "changeTest":
                    $this->callView("knjl322dForm1");
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
$knjl322dCtl = new knjl322dController;
?>
