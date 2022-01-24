<?php

require_once('for_php7.php');

require_once('knjm440wModel.inc');
require_once('knjm440wQuery.inc');

class knjm440wController extends Controller {
    var $ModelClassName = "knjm440wModel";
    var $ProgramID      = "KNJM440W";

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
                    $this->callView("knjm440wForm1");
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
$knjm440wCtl = new knjm440wController;
?>
