<?php

require_once('for_php7.php');

require_once('knjm440dModel.inc');
require_once('knjm440dQuery.inc');

class knjm440dController extends Controller {
    var $ModelClassName = "knjm440dModel";
    var $ProgramID      = "KNJM440D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjm440dForm1");
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
$knjm440dCtl = new knjm440dController;
?>
