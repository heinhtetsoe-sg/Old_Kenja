<?php

require_once('for_php7.php');

require_once('knjc030kModel.inc');
require_once('knjc030kQuery.inc');

class knjc030kController extends Controller {
    var $ModelClassName = "knjc030kModel";
    var $ProgramID        = "KNJC030K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjc030kForm1");
                   break 2;

                case "clear":
                    $sessionInstance->setCmd("main");
                    break 1;

                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;

                case "error":
                    $this->callView("error");
                    break 2;

                case "read":
                    $sessionInstance->keyClean("");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "read_before":
                    $sessionInstance->key_Move_Model("before");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "read_next":
                    $sessionInstance->key_Move_Model("next");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "":
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
$knjc030kCtl = new knjc030kController;
//var_dump($_REQUEST);
?>
