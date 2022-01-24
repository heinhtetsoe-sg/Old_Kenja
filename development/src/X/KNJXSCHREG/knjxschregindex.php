<?php

require_once('for_php7.php');

require_once('knjxschregModel.inc');
require_once('knjxschregQuery.inc');

class knjxschregController extends Controller {
    var $ModelClassName = "knjxschregModel";
    var $ProgramID      = "knjxschreg";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjxschregForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxschregCtl = new knjxschregController;
?>
