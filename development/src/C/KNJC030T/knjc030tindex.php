<?php

require_once('for_php7.php');

require_once('knjc030tModel.inc');
require_once('knjc030tQuery.inc');

class knjc030tController extends Controller {
    var $ModelClassName = "knjc030tModel";
    var $ProgramID        = "KNJC030T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjc030tForm1");
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
$knjc030tCtl = new knjc030tController;
//var_dump($_REQUEST);
?>
