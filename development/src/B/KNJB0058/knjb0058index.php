<?php

require_once('for_php7.php');

require_once('knjb0058Model.inc');
require_once('knjb0058Query.inc');

class knjb0058Controller extends Controller {
    var $ModelClassName = "knjb0058Model";
    var $ProgramID      = "KNJB0058";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "subMain":
                case "clear";
                    $sessionInstance->knjb0058Model();
                    $this->callView("knjb0058Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subMain");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0058Ctl = new knjb0058Controller;
?>
