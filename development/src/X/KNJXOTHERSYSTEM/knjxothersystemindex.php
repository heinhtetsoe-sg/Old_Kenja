<?php

require_once('for_php7.php');

require_once('knjxothersystemModel.inc');
require_once('knjxothersystemQuery.inc');

class knjxothersystemController extends Controller {
    var $ModelClassName = "knjxothersystemModel";
    var $ProgramID      = "KNJXOTHERSYSTEM";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "detail":
                    $this->callView("knjxothersystemForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxothersystemCtl = new knjxothersystemController;
?>
