<?php

require_once('for_php7.php');

require_once('knjh113cModel.inc');
require_once('knjh113cQuery.inc');

class knjh113cController extends Controller {
    var $ModelClassName = "knjh113cModel";
    var $ProgramID      = "KNJH113C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjh113cForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $this->callView("knjh113cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh113cCtl = new knjh113cController;
?>
