<?php

require_once('for_php7.php');

require_once('knjc020Model.inc');
require_once('knjc020Query.inc');

class knjc020Controller extends Controller {
    var $ModelClassName = "knjc020Model";
    var $ProgramID        = "KNJC020";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjc020Form1");
                    break 2;

                case "clear":
                    $sessionInstance->setCmd("main");
                    break 1;

                case "confirm":
                case "update":
                case "update2":
                    if (trim($sessionInstance->cmd) == "update2") {
                        $sessionInstance->setCmd("update");
                    }
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;

                case "error":
                    $this->callView("error");
                    break 2;

                case "read":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->keyClean("");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "read_before":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->key_Move_Model("before");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "read_next":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->key_Move_Model("next");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjc020Ctl = new knjc020Controller;
//var_dump($_REQUEST);
?>
