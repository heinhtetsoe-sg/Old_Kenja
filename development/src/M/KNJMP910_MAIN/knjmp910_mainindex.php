<?php

require_once('for_php7.php');

require_once('knjmp910_mainModel.inc');
require_once('knjmp910_mainQuery.inc');

class knjmp910_mainController extends Controller {
    var $ModelClassName = "knjmp910_mainModel";
    var $ProgramID      = "KNJMP910_MAIN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "torikomi":
                case "edit":
                case "main":
                case "cancel":
                    $this->callView("knjmp910_mainForm1");
                    break 2;
                case "delete_update":
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
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
$knjmp910_mainCtl = new knjmp910_mainController;
//var_dump($_REQUEST);
?>
