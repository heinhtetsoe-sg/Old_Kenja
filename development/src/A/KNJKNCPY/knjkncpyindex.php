<?php

require_once('for_php7.php');

require_once('knjkncpyModel.inc');
require_once('knjkncpyQuery.inc');

class knjkncpyController extends Controller {
    var $ModelClassName = "knjkncpyModel";
    var $ProgramID      = "KNJKNCPY";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjkncpyForm1");
                    break 2;
                case "shomei":
                    $sessionInstance->getShomeiModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjkncpyModel();
                    $this->callView("knjkncpyForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjkncpyCtl = new knjkncpyController;
?>
