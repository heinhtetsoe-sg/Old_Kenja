<?php

require_once('for_php7.php');

require_once('knjg048Model.inc');
require_once('knjg048Query.inc');

class knjg048Controller extends Controller {
    var $ModelClassName = "knjg048Model";
    var $ProgramID      = "KNJG048";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjg048Form1");
                    break 2;
                case "update":
                case "delete":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjg048Ctl = new knjg048Controller;
//var_dump($_REQUEST);
?>
